package com.jdc.webinventary.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class GastoComprobanteStorageService {
    private static final int MAX_BYTES = 5 * 1024 * 1024;
    private final Path directorio;

    public GastoComprobanteStorageService(
            @Value("${app.upload.gastos-dir:uploads/gastos}") String directorio) {
        this.directorio = Paths.get(directorio).toAbsolutePath().normalize();
    }

    public String guardar(MultipartFile comprobante) {
        if (comprobante == null || comprobante.isEmpty()) return null;
        try {
            if (comprobante.getSize() > MAX_BYTES) {
                throw new IllegalArgumentException("El comprobante no puede superar 5 MB.");
            }
            byte[] bytes = comprobante.getBytes();
            String extension = extensionValida(bytes);
            String nombre = UUID.randomUUID() + extension;
            Files.createDirectories(directorio);
            Files.write(directorio.resolve(nombre), bytes);
            return nombre;
        } catch (IOException ex) {
            throw new IllegalStateException("No fue posible guardar el comprobante del gasto.", ex);
        }
    }

    public Resource cargar(String nombre) {
        try {
            Path archivo = resolver(nombre);
            Resource resource = new UrlResource(archivo.toUri());
            if (resource.exists() && resource.isReadable()) return resource;
        } catch (IOException ignored) {
            // Se informa como archivo inexistente al llamador.
        }
        throw new ComprobanteNoEncontradoException();
    }

    public void eliminar(String nombre) {
        if (nombre == null || nombre.isBlank()) return;
        try { Files.deleteIfExists(resolver(nombre)); }
        catch (IOException ignored) { }
    }

    private Path resolver(String nombre) throws IOException {
        if (nombre == null || !nombre.matches("[0-9a-fA-F-]{36}\\.(pdf|png|jpg)")) {
            throw new IOException("Nombre de comprobante no válido");
        }
        Path archivo = directorio.resolve(nombre).normalize();
        if (!archivo.startsWith(directorio)) throw new IOException("Ruta no válida");
        return archivo;
    }

    private String extensionValida(byte[] bytes) {
        if (bytes.length >= 5 && bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D'
                && bytes[3] == 'F' && bytes[4] == '-') return ".pdf";
        if (bytes.length >= 8 && (bytes[0] & 0xff) == 0x89 && bytes[1] == 'P'
                && bytes[2] == 'N' && bytes[3] == 'G' && (bytes[4] & 0xff) == 0x0d
                && (bytes[5] & 0xff) == 0x0a && (bytes[6] & 0xff) == 0x1a
                && (bytes[7] & 0xff) == 0x0a) return ".png";
        if (bytes.length >= 3 && (bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xd8
                && (bytes[2] & 0xff) == 0xff) return ".jpg";
        throw new IllegalArgumentException("El comprobante debe ser un archivo PDF, PNG o JPG válido.");
    }

    public static class ComprobanteNoEncontradoException extends RuntimeException { }
}
