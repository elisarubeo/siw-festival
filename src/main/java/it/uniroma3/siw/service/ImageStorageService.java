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

/**
 * Salvataggio dei file immagine su disco.
 *
 * È l'unico punto dell'applicazione che tocca il filesystem: i service di
 * dominio gli chiedono di scrivere o cancellare un file e ricevono (o passano)
 * soltanto un nome. Il database non contiene i byte delle immagini ma solo
 * quel nome, in Movie.posterFilename.
 *
 * ATTENZIONE, ed è la cosa da sapere di questa classe: scrivere un file NON è
 * un'operazione transazionale. Se la transazione che ha chiesto il salvataggio
 * viene annullata, il file resta sul disco lo stesso. Per questo chi lo usa
 * (MovieService) lega la cancellazione dei file all'esito della transazione,
 * invece di cancellare subito.
 */
@Service
public class ImageStorageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageStorageService.class);

    /**
     * Formati ammessi, con l'estensione da usare per ciascuno.
     *
     * L'estensione la decide questa mappa e non il nome del file caricato: quel
     * nome arriva dal client e non merita fiducia — può contenere un percorso
     * ("../../application.properties") oppure un'estensione che non
     * corrisponde al contenuto.
     */
    private static final Map<String, String> ESTENSIONI = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/gif", "gif");

    private final Path directory;

    public ImageStorageService(@Value("${app.uploads.directory}") String directory) {
        /* Percorso assoluto e normalizzato una volta sola, all'avvio: serve
           come riferimento per il controllo anti path traversal più sotto. */
        this.directory = Paths.get(directory).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.directory);
        } catch (IOException e) {
            /* Senza cartella non si può salvare niente: meglio che
               l'applicazione non parta, invece di scoprirlo al primo upload. */
            throw new UncheckedIOException(
                    "Impossibile creare la cartella delle immagini: " + this.directory, e);
        }
        logger.info("Le immagini caricate vengono salvate in {}", this.directory);
    }

    /**
     * Il file è un'immagine di un formato che sappiamo gestire?
     *
     * Serve al controller per segnalare l'errore nella form PRIMA di salvare
     * qualsiasi cosa: il controllo vive qui, dove sta la regola, e il
     * controller si limita a chiederlo.
     */
    public boolean isSupported(MultipartFile file) {
        return file != null && !file.isEmpty() && ESTENSIONI.containsKey(file.getContentType());
    }

    /**
     * Salva il file e restituisce il nome con cui è stato scritto: è quello
     * che va memorizzato nel database.
     *
     * Il nome è un UUID generato qui, non quello scelto dall'utente: due
     * utenti che caricano "locandina.jpg" non si sovrascrivono a vicenda, e
     * nessun nome proveniente dall'esterno finisce mai in un percorso.
     */
    public String store(MultipartFile file) {
        /* Ricontrollato anche qui e non solo nel controller: questo metodo è
           pubblico e non può fidarsi di chi lo chiama. */
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

    /**
     * Cancella un file, se esiste. Un nome null o vuoto (film senza locandina)
     * non è un errore: non c'è semplicemente nulla da fare.
     */
    public void delete(String filename) {
        if (filename == null || filename.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(risolvi(filename));
        } catch (IOException e) {
            /* Un file rimasto sul disco è spiacevole ma non compromette lo
               stato dell'applicazione: si annota nei log e si prosegue, invece
               di far fallire un'operazione che sui dati è già riuscita. */
            logger.warn("Impossibile eliminare il file {}: {}", filename, e.getMessage());
        }
    }

    /** Usata da UploadConfig per sapere da dove servire /uploads/**. */
    public Path getDirectory() {
        return this.directory;
    }

    /**
     * Costruisce il percorso di un file dentro la cartella delle immagini,
     * verificando che ci resti davvero dentro.
     *
     * È la difesa dal path traversal: un nome come "../../segreto" sarebbe
     * risolto fuori dalla cartella, e qui viene respinto. I nomi li generiamo
     * noi, ma il controllo costa nulla e vale anche per quelli riletti dal
     * database, che potrebbero essere stati scritti da una versione futura del
     * codice o modificati a mano.
     */
    private Path risolvi(String filename) {
        Path percorso = this.directory.resolve(filename).normalize();
        if (!percorso.startsWith(this.directory)) {
            throw new InvalidImageException("Nome di file non valido: " + filename);
        }
        return percorso;
    }
}
