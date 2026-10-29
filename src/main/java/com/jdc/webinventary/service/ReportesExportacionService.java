package com.jdc.webinventary.service;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ReportesExportacionService {
    private static final String[] CABECERAS = {"Venta", "Fecha", "Cliente", "Código", "Producto", "Vendedor", "Cantidad", "Precio unitario", "Total", "Estado"};

    public byte[] excel(ReportesService.Resultado resultado) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(bytes, StandardCharsets.UTF_8)) {
                agregar(zip, "[Content_Types].xml", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/><Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/><Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/></Types>");
                agregar(zip, "_rels/.rels", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/></Relationships>");
                agregar(zip, "xl/workbook.xml", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><sheets><sheet name=\"Reporte de ventas\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>");
                agregar(zip, "xl/_rels/workbook.xml.rels", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/></Relationships>");
                StringBuilder sheet = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>");
                sheet.append("<row r=\"1\">");
                for (int col = 0; col < CABECERAS.length; col++) celdaTexto(sheet, col, 1, CABECERAS[col]);
                sheet.append("</row>");
                int row = 2;
                for (ReportesService.Fila f : resultado.filas()) {
                    sheet.append("<row r=\"").append(row).append("\">");
                    int col = 0;
                    celdaNumero(sheet, col++, row, f.ventaId()); celdaTexto(sheet, col++, row, f.fecha().toString());
                    celdaTexto(sheet, col++, row, f.cliente()); celdaTexto(sheet, col++, row, f.codigoProducto());
                    celdaTexto(sheet, col++, row, f.producto()); celdaTexto(sheet, col++, row, f.vendedor());
                    celdaNumero(sheet, col++, row, f.cantidad()); celdaNumero(sheet, col++, row, f.precioUnitario());
                    celdaNumero(sheet, col++, row, f.total()); celdaTexto(sheet, col, row, f.estado());
                    sheet.append("</row>"); row++;
                }
                sheet.append("</sheetData></worksheet>");
                agregar(zip, "xl/worksheets/sheet1.xml", sheet.toString());
            }
            return bytes.toByteArray();
        } catch (IOException ex) { throw new IllegalStateException("No fue posible generar el archivo Excel.", ex); }
    }

    public byte[] pdf(ReportesService.Resultado resultado) {
        List<String> lineas = new ArrayList<>();
        lineas.add("REPORTE DE VENTAS " + resultado.desde() + " a " + resultado.hasta());
        lineas.add("Total confirmado: " + resultado.totalConfirmado() + " | Lineas anuladas: " + resultado.lineasAnuladas());
        lineas.add("VENTA | FECHA | CLIENTE | PRODUCTO | VENDEDOR | CANT. | UNITARIO | TOTAL | ESTADO");
        for (ReportesService.Fila f : resultado.filas()) {
            lineas.add(String.format("%d | %s | %s | %s (%s) | %s | %d | %d | %d | %s",
                    f.ventaId(), f.fecha(), corto(f.cliente(), 16), corto(f.producto(), 20),
                    corto(f.codigoProducto(), 8), corto(f.vendedor(), 16), f.cantidad(),
                    f.precioUnitario(), f.total(), f.estado()));
        }
        if (resultado.filas().isEmpty()) lineas.add("No hay ventas para los filtros seleccionados.");
        return construirPdf(lineas);
    }

    private byte[] construirPdf(List<String> lineas) {
        List<List<String>> paginas = new ArrayList<>();
        for (int inicio = 0; inicio < lineas.size(); inicio += 36)
            paginas.add(lineas.subList(inicio, Math.min(inicio + 36, lineas.size())));
        int numObjetos = 3 + paginas.size() * 2;
        List<byte[]> objetos = new ArrayList<>();
        objetos.add(bytes("<< /Type /Catalog /Pages 2 0 R >>"));
        StringBuilder kids = new StringBuilder("<< /Type /Pages /Kids [");
        for (int i = 0; i < paginas.size(); i++) kids.append(4 + i * 2).append(" 0 R ");
        kids.append("] /Count ").append(paginas.size()).append(" >>");
        objetos.add(bytes(kids.toString()));
        objetos.add(bytes("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>"));
        for (int i = 0; i < paginas.size(); i++) {
            int pageId = 4 + i * 2, contentId = pageId + 1;
            objetos.add(bytes("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 842 595] /Resources << /Font << /F1 3 0 R >> >> /Contents " + contentId + " 0 R >>"));
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            write(stream, "BT /F1 8 Tf 28 568 Td 10 TL\n");
            for (String linea : paginas.get(i)) {
                write(stream, "(" + pdfEscape(linea) + ") Tj T*\n");
            }
            write(stream, "ET");
            byte[] data = stream.toByteArray();
            ByteArrayOutputStream obj = new ByteArrayOutputStream();
            write(obj, "<< /Length " + data.length + " >>\nstream\n");
            obj.writeBytes(data); write(obj, "\nendstream");
            objetos.add(obj.toByteArray());
        }
        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        write(pdf, "%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n");
        long[] offsets = new long[numObjetos + 1];
        for (int i = 0; i < objetos.size(); i++) {
            offsets[i + 1] = pdf.size(); write(pdf, (i + 1) + " 0 obj\n");
            pdf.writeBytes(objetos.get(i)); write(pdf, "\nendobj\n");
        }
        long xref = pdf.size(); write(pdf, "xref\n0 " + (numObjetos + 1) + "\n0000000000 65535 f \n");
        for (int i = 1; i <= numObjetos; i++) write(pdf, String.format("%010d 00000 n \n", offsets[i]));
        write(pdf, "trailer\n<< /Size " + (numObjetos + 1) + " /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF");
        return pdf.toByteArray();
    }

    private static void celdaTexto(StringBuilder xml, int col, int row, String value) {
        xml.append("<c r=\"").append(ref(col, row)).append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
                .append(xml(value)).append("</t></is></c>");
    }
    private static void celdaNumero(StringBuilder xml, int col, int row, Number value) {
        xml.append("<c r=\"").append(ref(col, row)).append("\"><v>").append(value).append("</v></c>");
    }
    private static String ref(int col, int row) { return String.valueOf((char) ('A' + col)) + row; }
    private static String xml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }
    private static String pdfEscape(String value) {
        return value.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)")
                .replace("\r", " ").replace("\n", " ");
    }
    private static String corto(String valor, int maximo) {
        if (valor == null) return "";
        return valor.length() <= maximo ? valor : valor.substring(0, maximo - 1) + "…";
    }
    private static byte[] bytes(String value) { return value.getBytes(Charset.forName("windows-1252")); }
    private static void write(ByteArrayOutputStream stream, String value) { stream.writeBytes(bytes(value)); }
    private static void agregar(ZipOutputStream zip, String nombre, String contenido) throws IOException {
        zip.putNextEntry(new ZipEntry(nombre)); zip.write(contenido.getBytes(StandardCharsets.UTF_8)); zip.closeEntry();
    }
}
