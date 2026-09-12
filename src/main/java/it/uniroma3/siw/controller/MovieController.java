package it.uniroma3.siw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.uniroma3.siw.exception.EntityInUseException;
import it.uniroma3.siw.exception.ResourceNotFoundException;
import it.uniroma3.siw.model.Movie;
import it.uniroma3.siw.service.DirectorService;
import it.uniroma3.siw.service.ImageStorageService;
import it.uniroma3.siw.service.MovieService;
import jakarta.validation.Valid;

@Controller
public class MovieController {

    private MovieService movieService;
    private DirectorService directorService;
    private ImageStorageService imageStorageService;

    public MovieController(MovieService movieService, DirectorService directorService,
                           ImageStorageService imageStorageService) {
        this.movieService = movieService;
        this.directorService = directorService;
        this.imageStorageService = imageStorageService;
    }

    @GetMapping("/movies")
    public String list(Model model) {
        model.addAttribute("movies", this.movieService.findAll());
        return "movies/list";
    }

    @GetMapping("/movies/{id}")
    public String show(@PathVariable("id") Long id, Model model) {
        Movie movie = this.movieService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nessun film con id " + id));
        model.addAttribute("movie", movie);
        return "movies/show";
    }

    @GetMapping("/movies/new")
    public String createForm(Model model) {
        model.addAttribute("movie", new Movie());
        model.addAttribute("directors", this.directorService.findAll());
        return "movies/form";
    }

    @PostMapping("/movies")
    public String create(@Valid @ModelAttribute("movie") Movie movie,
                         BindingResult bindingResult,
                         @RequestParam(name = "poster", required = false) MultipartFile poster,
                         Model model) {

        if (this.movieService.existsByTitleAndYear(movie.getTitle(), movie.getYear())) {
            bindingResult.rejectValue("title", "movie.duplicate",
                    "Esiste già un film con questo titolo e anno");
        }

        /* La locandina si controlla PRIMA di salvare il film: scoprire che il
           file non va bene dopo aver creato il film lascerebbe a metà
           l'operazione che l'utente ha chiesto. */
        controllaLocandina(poster, bindingResult);

        if (bindingResult.hasErrors()) {
            /* La select dei registi va ricaricata: il model si ricostruisce
               a ogni richiesta, e senza questa riga la form tornerebbe vuota. */
            model.addAttribute("directors", this.directorService.findAll());
            return "movies/form";
        }

        Movie salvato = this.movieService.save(movie);

        /* La locandina si carica dopo: updatePoster lavora su un film che
           esiste gia', e l'id lo assegna il salvataggio. */
        if (haContenuto(poster)) {
            this.movieService.updatePoster(salvato.getId(), poster);
        }

        return "redirect:/movies";
    }

    @GetMapping("/movies/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Movie movie = this.movieService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nessun film con id " + id));
        model.addAttribute("movie", movie);
        model.addAttribute("directors", this.directorService.findAll());
        return "movies/form";
    }

    @PostMapping("/movies/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("movie") Movie movie,
                         BindingResult bindingResult,
                         @RequestParam(name = "poster", required = false) MultipartFile poster,
                         Model model) {

        if (this.movieService.existsByTitleAndYearExcluding(movie.getTitle(), movie.getYear(), id)) {
            bindingResult.rejectValue("title", "movie.duplicate",
                    "Esiste già un altro film con questo titolo e anno");
        }

        controllaLocandina(poster, bindingResult);

        if (bindingResult.hasErrors()) {
            movie.setId(id);
            model.addAttribute("directors", this.directorService.findAll());
            return "movies/form";
        }

        this.movieService.update(id, movie);

        /* Campo lasciato vuoto: la locandina attuale resta com'e'. E' anche il
           motivo per cui posterFilename non e' un campo della form — se lo
           fosse, il binding lo azzererebbe a ogni salvataggio. */
        if (haContenuto(poster)) {
            this.movieService.updatePoster(id, poster);
        }

        return "redirect:/movies/" + id;
    }

    /** Toglie la locandina a un film, lasciando il film al suo posto. */
    @PostMapping("/movies/{id}/poster/delete")
    public String deletePoster(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        this.movieService.removePoster(id);
        redirectAttributes.addFlashAttribute("successMessage", "Locandina rimossa.");
        return "redirect:/movies/" + id;
    }

    @PostMapping("/movies/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            this.movieService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Film eliminato.");
        } catch (EntityInUseException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/movies";
    }

    /* ==================================================================
       SUPPORTO PER LA LOCANDINA
       ================================================================== */

    /** Il campo file e' facoltativo: se non e' stato scelto nulla, e' vuoto. */
    private boolean haContenuto(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    /**
     * Aggiunge un errore alla form se il file caricato non e' un'immagine
     * utilizzabile. La regola su quali formati siano ammessi sta nel service:
     * qui si decide solo come comunicarla all'utente.
     */
    private void controllaLocandina(MultipartFile poster, BindingResult bindingResult) {
        if (haContenuto(poster) && !this.imageStorageService.isSupported(poster)) {
            bindingResult.reject("poster.invalid",
                    "La locandina deve essere un'immagine JPG, PNG, WEBP o GIF.");
        }
    }
}
