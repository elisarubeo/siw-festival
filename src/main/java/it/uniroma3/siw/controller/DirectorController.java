package it.uniroma3.siw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import it.uniroma3.siw.exception.ResourceNotFoundException;
import it.uniroma3.siw.model.Director;
import it.uniroma3.siw.service.DirectorService;
import jakarta.validation.Valid;

@Controller
public class DirectorController {

    private DirectorService directorService;

    public DirectorController(DirectorService directorService) {
        this.directorService = directorService;
    }

    @GetMapping("/directors")
    public String list(Model model) {
        model.addAttribute("directors", this.directorService.findAll());
        return "directors/list";
    }

    @GetMapping("/directors/{id}")
    public String show(@PathVariable("id") Long id, Model model) {
        Director director = this.directorService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nessun regista con id " + id));
        model.addAttribute("director", director);
        return "directors/show";
    }

    @GetMapping("/directors/new")
    public String createForm(Model model) {
        model.addAttribute("director", new Director());
        return "directors/form";
    }

    @PostMapping("/directors")
    public String save(@Valid @ModelAttribute("director") Director director,
                       BindingResult bindingResult) {

        if (this.directorService.existsByNameAndSurname(director.getName(), director.getSurname())) {
            bindingResult.rejectValue("name", "director.duplicate",
                    "Questo regista è già presente");
        }

        if (bindingResult.hasErrors()) {
            return "directors/form";
        }

        this.directorService.save(director);
        return "redirect:/directors";
    }
}
