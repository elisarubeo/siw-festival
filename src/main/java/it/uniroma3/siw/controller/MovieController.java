package it.uniroma3.siw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.uniroma3.siw.exception.EntityInUseException;
import it.uniroma3.siw.exception.ResourceNotFoundException;
import it.uniroma3.siw.model.Movie;
import it.uniroma3.siw.service.DirectorService;
import it.uniroma3.siw.service.MovieService;
import jakarta.validation.Valid;

@Controller
public class MovieController {

    private MovieService movieService;
    private DirectorService directorService;

    public MovieController(MovieService movieService, DirectorService directorService) {
        this.movieService = movieService;
        this.directorService = directorService;
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
                         BindingResult bindingResult, Model model) {

        if (this.movieService.existsByTitleAndYear(movie.getTitle(), movie.getYear())) {
            bindingResult.rejectValue("title", "movie.duplicate",
                    "Esiste già un film con questo titolo e anno");
        }

        if (bindingResult.hasErrors()) {
            /* La select dei registi va ricaricata: il model si ricostruisce
               a ogni richiesta, e senza questa riga la form tornerebbe vuota. */
            model.addAttribute("directors", this.directorService.findAll());
            return "movies/form";
        }

        this.movieService.save(movie);
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
                         BindingResult bindingResult, Model model) {

        if (this.movieService.existsByTitleAndYearExcluding(movie.getTitle(), movie.getYear(), id)) {
            bindingResult.rejectValue("title", "movie.duplicate",
                    "Esiste già un altro film con questo titolo e anno");
        }

        if (bindingResult.hasErrors()) {
            movie.setId(id);
            model.addAttribute("directors", this.directorService.findAll());
            return "movies/form";
        }

        this.movieService.update(id, movie);
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
}
