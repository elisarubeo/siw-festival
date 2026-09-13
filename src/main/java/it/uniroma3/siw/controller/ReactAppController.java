package it.uniroma3.siw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReactAppController {

    @GetMapping({"/reviews", "/reviews/"})
    public String reactAppRoot() {
        return "forward:/reviews/index.html";
    }
}
