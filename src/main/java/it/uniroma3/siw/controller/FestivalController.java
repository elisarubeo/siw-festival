package it.uniroma3.siw.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import it.uniroma3.siw.exception.ResourceNotFoundException;
import it.uniroma3.siw.model.Festival;
import it.uniroma3.siw.service.FestivalService;
import jakarta.validation.Valid;



@Controller
public class FestivalController {

    private FestivalService festivalService;

    public FestivalController(FestivalService festivalService) {
        this.festivalService = festivalService;
    }

    @GetMapping("/festivals/{id}")
    public String show(@PathVariable("id") Long id, Model model) {
        Festival festival = this.festivalService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nessun festival con id " + id));
        model.addAttribute("festival", festival);
        return "festivals/show";
    }

    @GetMapping("/festivals")
    public String list(Model model) {
        model.addAttribute("festivals", this.festivalService.findAll());
        return "festivals/list";
    }

    @GetMapping("/festivals/new")
    public String createForm(Model model) {
        model.addAttribute("festival", new Festival());
        return "festivals/form";
    }

    @PostMapping("/festivals")
    public String save(@Valid @ModelAttribute("festival") Festival festival,
                       BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors())
            return "festivals/form";

        this.festivalService.save(festival);
        return "redirect:/festivals";
    }
    
}
