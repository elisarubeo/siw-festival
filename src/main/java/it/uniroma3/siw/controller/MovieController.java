package it.uniroma3.siw.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import it.uniroma3.siw.exception.ResourceNotFoundException;
import it.uniroma3.siw.model.Movie;
import it.uniroma3.siw.service.MovieService;

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
}
