package it.uniroma3.siw.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import it.uniroma3.siw.exception.InvalidImageException;

@Service
public class ImageStorageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageStorageService.class);

    private static final Map<String, String> ESTENSIONI = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/gif", "gif");

    private final Path directory;

    public ImageStorageService(@Value("${app.uploads.directory}") String directory) {
        this.directory = Paths.get(directory).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.directory);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "Impossibile creare la cartella delle immagini: " + this.directory, e);
        }
        logger.info("Le immagini caricate vengono salvate in {}", this.directory);
    }

    public boolean isSupported(MultipartFile file) {
        return file != null && !file.isEmpty() && ESTENSIONI.containsKey(file.getContentType());
    }

    public String store(MultipartFile file) {
        if (!isSupported(file)) {
            throw new InvalidImageException(
                    "Il file non è un'immagine valida: sono ammessi JPG, PNG, WEBP e GIF.");
        }

        String filename = UUID.randomUUID() + "." + ESTENSIONI.get(file.getContentType());

        try (InputStream contenuto = file.getInputStream()) {
            Files.copy(contenuto, risolvi(filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Salvataggio dell'immagine non riuscito", e);
        }

        return filename;
    }

    public void delete(String filename) {
        if (filename == null || filename.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(risolvi(filename));
        } catch (IOException e) {
            logger.warn("Impossibile eliminare il file {}: {}", filename, e.getMessage());
        }
    }

    public Path getDirectory() {
        return this.directory;
    }

    private Path risolvi(String filename) {
        Path percorso = this.directory.resolve(filename).normalize();
        if (!percorso.startsWith(this.directory)) {
            throw new InvalidImageException("Nome di file non valido: " + filename);
        }
        return percorso;
    }
}
