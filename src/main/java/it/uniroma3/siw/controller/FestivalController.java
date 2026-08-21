package it.uniroma3.siw.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import it.uniroma3.siw.exception.ResourceNotFoundException;
import it.uniroma3.siw.model.Festival;
import it.uniroma3.siw.service.FestivalService;

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
        List<Festival> allFestivals = this.festivalService.findAll();
        model.addAttribute("festivals", allFestivals);
        return "festivals/list";
    }
}
