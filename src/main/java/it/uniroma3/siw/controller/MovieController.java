package it.uniroma3.siw.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import it.uniroma3.siw.exception.DuplicateResourceException;
import it.uniroma3.siw.exception.ResourceNotFoundException;
import it.uniroma3.siw.model.Movie;
import it.uniroma3.siw.service.MovieService;
import jakarta.validation.Valid;

@Controller
public class MovieController {

    private MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/movies/{id}")
    public String show(@PathVariable("id") Long id, Model model) {
        Movie movie = this.movieService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nessun film con id " + id));
        model.addAttribute("movie", movie);
        return "movies/show";
    }

    @GetMapping("/movies")
    public String list(Model model) {
        List<Movie> allMovies = this.movieService.findAll();
        model.addAttribute("movies", allMovies);
        return "movies/list";
    }

    @GetMapping("/movies/new")
    public String createForm(Model model) {
        model.addAttribute("movie", new Movie());
        return "movies/form";
    }

    @PostMapping("/movie")
    public String newMovie(@ModelAttribute("movie") Movie movie) {
        this.movieService.save(movie);
        return "redirect:/movies";
    }

    @PostMapping("movies")
    public String save(@Valid @ModelAttribute("movie") Movie movie, 
                                    BindingResult bindingResult, Model model) {
    if (bindingResult.hasErrors()) {
        return "movies/form";
    } 
    try {
        movieService.save(movie);
        return "redirect:/movies";
    } 
    catch (DuplicateResourceException e) {
        bindingResult.reject("movie.duplicate");
        return "movies/form";
    }
    }

}
