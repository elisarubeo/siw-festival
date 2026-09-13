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
    public String list(@RequestParam(name = "q", required = false) String q, Model model) {
        model.addAttribute("movies", this.movieService.search(q));
        model.addAttribute("q", q);
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

        controllaLocandina(poster, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("directors", this.directorService.findAll());
            return "movies/form";
        }

        Movie salvato = this.movieService.save(movie);

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

        if (haContenuto(poster)) {
            this.movieService.updatePoster(id, poster);
        }

        return "redirect:/movies/" + id;
    }

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

    private boolean haContenuto(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private void controllaLocandina(MultipartFile poster, BindingResult bindingResult) {
        if (haContenuto(poster) && !this.imageStorageService.isSupported(poster)) {
            bindingResult.reject("poster.invalid",
                    "La locandina deve essere un'immagine JPG, PNG, WEBP o GIF.");
        }
    }
}
