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
    private static final int ANCHO_PAGINA = 842;
    private static final int ALTO_PAGINA = 595;
    private static final int MARGEN = 32;
    private static final int ALTO_FILA = 17;
    private static final int FILAS_POR_PAGINA = 21;
    private static final int[] ANCHOS_COLUMNAS = {44, 64, 92, 58, 122, 100, 44, 88, 90, 76};
    private static final String NAVY = "0.09 0.22 0.32";
    private static final String TEAL = "0.08 0.51 0.43";
    private static final String TEXT = "0.20 0.31 0.39";
    private static final String MUTED = "0.48 0.56 0.61";
    private static final String LIGHT = "0.95 0.97 0.98";
    private static final String GRID = "0.87 0.91 0.93";
    private static final String GREEN = "0.10 0.45 0.35";
    private static final String RED = "0.67 0.20 0.25";

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
        List<ReportesService.Fila> filas = resultado.filas();
        int paginas = Math.max(1, (int) Math.ceil(filas.size() / (double) FILAS_POR_PAGINA));
        int numObjetos = 4 + paginas * 2;
        List<byte[]> objetos = new ArrayList<>();
        objetos.add(bytes("<< /Type /Catalog /Pages 2 0 R >>"));
        StringBuilder kids = new StringBuilder("<< /Type /Pages /Kids [");
        for (int i = 0; i < paginas; i++) kids.append(5 + i * 2).append(" 0 R ");
        kids.append("] /Count ").append(paginas).append(" >>");
        objetos.add(bytes(kids.toString()));
        objetos.add(bytes("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>"));
        objetos.add(bytes("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>"));

        for (int pagina = 0; pagina < paginas; pagina++) {
            int inicio = pagina * FILAS_POR_PAGINA;
            int fin = Math.min(inicio + FILAS_POR_PAGINA, filas.size());
            List<ReportesService.Fila> filasPagina = filas.subList(inicio, fin);
            int pageId = 5 + pagina * 2;
            int contentId = pageId + 1;
            objetos.add(bytes("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + ANCHO_PAGINA + " " + ALTO_PAGINA
                    + "] /Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents " + contentId + " 0 R >>"));
            byte[] contenido = dibujarPagina(resultado, filasPagina, pagina + 1, paginas);
            ByteArrayOutputStream obj = new ByteArrayOutputStream();
            write(obj, "<< /Length " + contenido.length + " >>\nstream\n");
            obj.writeBytes(contenido);
            write(obj, "\nendstream");
            objetos.add(obj.toByteArray());
        }

        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        write(pdf, "%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n");
        long[] offsets = new long[numObjetos + 1];
        for (int i = 0; i < objetos.size(); i++) {
            offsets[i + 1] = pdf.size();
            write(pdf, (i + 1) + " 0 obj\n");
            pdf.writeBytes(objetos.get(i));
            write(pdf, "\nendobj\n");
        }
        long xref = pdf.size();
        write(pdf, "xref\n0 " + (numObjetos + 1) + "\n0000000000 65535 f \n");
        for (int i = 1; i <= numObjetos; i++) write(pdf, String.format("%010d 00000 n \n", offsets[i]));
        write(pdf, "trailer\n<< /Size " + (numObjetos + 1) + " /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF");
        return pdf.toByteArray();
    }

    private byte[] dibujarPagina(ReportesService.Resultado resultado, List<ReportesService.Fila> filas,
                                 int numeroPagina, int totalPaginas) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        rect(out, 0, 509, ANCHO_PAGINA, 86, NAVY);
        rect(out, 0, 509, 8, 86, TEAL);
        texto(out, MARGEN, 558, "REPORTE DE VENTAS", 19, "F2", "1 1 1");
        texto(out, MARGEN, 536, "Período: " + resultado.desde() + " a " + resultado.hasta(), 10, "F1", "0.84 0.90 0.93");
        texto(out, 690, 560, "WEB INVENTARY", 9, "F2", "0.64 0.86 0.80");

        int cardY = 444;
        int cardWidth = 250;
        tarjeta(out, MARGEN, cardY, cardWidth, "LÍNEAS ENCONTRADAS", String.valueOf(resultado.filas().size()));
        tarjeta(out, MARGEN + 263, cardY, cardWidth, "TOTAL CONFIRMADO", String.valueOf(resultado.totalConfirmado()));
        tarjeta(out, MARGEN + 526, cardY, cardWidth, "LÍNEAS ANULADAS", String.valueOf(resultado.lineasAnuladas()));

        int tableTop = 432;
        rect(out, MARGEN, tableTop - 29, ANCHO_PAGINA - MARGEN * 2, 29, NAVY);
        int x = MARGEN;
        String[] cabecerasCortas = {"VENTA", "FECHA", "CLIENTE", "CÓDIGO", "PRODUCTO", "VENDEDOR", "CANT.", "PRECIO UNIT.", "TOTAL", "ESTADO"};
        for (int i = 0; i < cabecerasCortas.length; i++) {
            textoAjustado(out, x + 5, tableTop - 18, cabecerasCortas[i], ANCHOS_COLUMNAS[i] - 10, 7, "F2", "1 1 1");
            x += ANCHOS_COLUMNAS[i];
        }

        if (filas.isEmpty()) {
            rect(out, MARGEN, tableTop - 29 - ALTO_FILA, ANCHO_PAGINA - MARGEN * 2, ALTO_FILA, LIGHT);
            texto(out, MARGEN + 12, tableTop - 29 - 13, "No hay ventas que coincidan con los filtros.", 9, "F1", MUTED);
        } else {
            for (int r = 0; r < filas.size(); r++) {
                ReportesService.Fila fila = filas.get(r);
                int rowTop = tableTop - 29 - r * ALTO_FILA;
                int rowBottom = rowTop - ALTO_FILA;
                if (r % 2 == 1) rect(out, MARGEN, rowBottom, ANCHO_PAGINA - MARGEN * 2, ALTO_FILA, LIGHT);
                linea(out, MARGEN, rowBottom, ANCHO_PAGINA - MARGEN, rowBottom, GRID);
                String[] valores = {String.valueOf(fila.ventaId()), String.valueOf(fila.fecha()), fila.cliente(),
                        fila.codigoProducto(), fila.producto(), fila.vendedor(), String.valueOf(fila.cantidad()),
                        String.valueOf(fila.precioUnitario()), String.valueOf(fila.total()), fila.estado()};
                x = MARGEN;
                for (int c = 0; c < valores.length; c++) {
                    String color = "ESTADO".equals(cabecerasCortas[c])
                            ? ("ANULADA".equals(fila.estado()) ? RED : GREEN) : TEXT;
                    textoAjustado(out, x + 5, rowBottom + 5, valores[c], ANCHOS_COLUMNAS[c] - 10, 7, "F1", color);
                    x += ANCHOS_COLUMNAS[c];
                }
            }
        }
        linea(out, MARGEN, 40, ANCHO_PAGINA - MARGEN, 40, GRID);
        texto(out, MARGEN, 24, "Reporte generado con los filtros seleccionados", 8, "F1", MUTED);
        texto(out, ANCHO_PAGINA - 105, 24, "Página " + numeroPagina + " de " + totalPaginas, 8, "F1", MUTED);
        return out.toByteArray();
    }

    private void tarjeta(ByteArrayOutputStream out, int x, int y, int ancho, String etiqueta, String valor) {
        rect(out, x, y, ancho, 50, LIGHT);
        rect(out, x, y, 3, 50, TEAL);
        textoAjustado(out, x + 13, y + 32, etiqueta, ancho - 24, 7, "F2", MUTED);
        texto(out, x + 13, y + 11, valor, 15, "F2", NAVY);
    }

    private void rect(ByteArrayOutputStream out, int x, int y, int ancho, int alto, String color) {
        write(out, color + " rg\n" + x + " " + y + " " + ancho + " " + alto + " re f\n");
    }

    private void linea(ByteArrayOutputStream out, int x1, int y1, int x2, int y2, String color) {
        write(out, color + " RG\n" + x1 + " " + y1 + " m " + x2 + " " + y2 + " l S\n");
    }

    private void textoAjustado(ByteArrayOutputStream out, int x, int y, String valor, int ancho,
                               int tamano, String fuente, String color) {
        int maxCaracteres = Math.max(1, (int) (ancho / (tamano * 0.52)));
        texto(out, x, y, corto(valor, maxCaracteres), tamano, fuente, color);
    }

    private void texto(ByteArrayOutputStream out, int x, int y, String valor, int tamano,
                       String fuente, String color) {
        write(out, color + " rg\nBT /" + fuente + " " + tamano + " Tf 1 0 0 1 " + x + " " + y
                + " Tm (" + pdfEscape(valor == null ? "" : valor) + ") Tj ET\n");
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
        return valor.length() <= maximo ? valor : valor.substring(0, Math.max(0, maximo - 3)) + "...";
    }
    private static byte[] bytes(String value) { return value.getBytes(Charset.forName("windows-1252")); }
    private static void write(ByteArrayOutputStream stream, String value) { stream.writeBytes(bytes(value)); }
    private static void agregar(ZipOutputStream zip, String nombre, String contenido) throws IOException {
        zip.putNextEntry(new ZipEntry(nombre)); zip.write(contenido.getBytes(StandardCharsets.UTF_8)); zip.closeEntry();
    }
}
