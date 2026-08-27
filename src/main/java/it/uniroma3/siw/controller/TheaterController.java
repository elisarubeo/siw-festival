package it.uniroma3.siw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import it.uniroma3.siw.exception.ResourceNotFoundException;
import it.uniroma3.siw.model.Theater;
import it.uniroma3.siw.service.TheaterService;
import jakarta.validation.Valid;

@Controller
public class TheaterController {

    private TheaterService theaterService;

    public TheaterController(TheaterService theaterService) {
        this.theaterService = theaterService;
    }

    @GetMapping("/theaters")
    public String list(Model model) {
        model.addAttribute("theaters", this.theaterService.findAll());
        return "theaters/list";
    }

    @GetMapping("/theaters/{id}")
    public String show(@PathVariable("id") Long id, Model model) {
        Theater theater = this.theaterService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nessuna sala con id " + id));
        model.addAttribute("theater", theater);
        return "theaters/show";
    }

    @GetMapping("/theaters/new")
    public String createForm(Model model) {
        model.addAttribute("theater", new Theater());
        return "theaters/form";
    }

    @PostMapping("/theaters")
    public String save(@Valid @ModelAttribute("theater") Theater theater,
                       BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "theaters/form";
        }

        this.theaterService.save(theater);
        return "redirect:/theaters";
    }
}
