package it.uniroma3.siw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import it.uniroma3.siw.service.FestivalService;

@Controller
public class HomeController {

    private FestivalService festivalService;

    public HomeController(FestivalService festivalService) {
        this.festivalService = festivalService;
    }

    @GetMapping("/")
    public String getHome(Model model) {
        model.addAttribute("festivals", this.festivalService.findAll());
        return "homepage";
    }
}
