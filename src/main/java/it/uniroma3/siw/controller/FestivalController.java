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
import it.uniroma3.siw.model.Festival;
import it.uniroma3.siw.service.FestivalService;
import jakarta.validation.Valid;

@Controller
public class FestivalController {

    private FestivalService festivalService;

    public FestivalController(FestivalService festivalService) {
        this.festivalService = festivalService;
    }

    @GetMapping("/festivals")
    public String list(Model model) {
        model.addAttribute("festivals", this.festivalService.findAll());
        return "festivals/list";
    }

    @GetMapping("/festivals/{id}")
    public String show(@PathVariable("id") Long id, Model model) {
        Festival festival = this.festivalService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nessun festival con id " + id));
        model.addAttribute("festival", festival);
        return "festivals/show";
    }

    @GetMapping("/festivals/new")
    public String createForm(Model model) {
        model.addAttribute("festival", new Festival());
        return "festivals/form";
    }

    @PostMapping("/festivals")
    public String create(@Valid @ModelAttribute("festival") Festival festival,
                         BindingResult bindingResult) {

        if (this.festivalService.existsByNameAndYear(festival.getName(), festival.getYear())) {
            bindingResult.rejectValue("name", "festival.duplicate",
                    "Esiste già un festival con questo nome per lo stesso anno");
        }

        if (bindingResult.hasErrors()) {
            return "festivals/form";
        }

        this.festivalService.save(festival);
        return "redirect:/festivals";
    }

    @GetMapping("/festivals/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Festival festival = this.festivalService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nessun festival con id " + id));
        model.addAttribute("festival", festival);
        return "festivals/form";
    }

    @PostMapping("/festivals/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("festival") Festival festival,
                         BindingResult bindingResult) {

        if (this.festivalService.existsByNameAndYearExcluding(
                festival.getName(), festival.getYear(), id)) {
            bindingResult.rejectValue("name", "festival.duplicate",
                    "Esiste già un altro festival con questo nome per lo stesso anno");
        }

        if (bindingResult.hasErrors()) {
            festival.setId(id);
            return "festivals/form";
        }

        this.festivalService.update(id, festival);
        return "redirect:/festivals/" + id;
    }

    @PostMapping("/festivals/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            this.festivalService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Festival eliminato.");
        } catch (EntityInUseException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/festivals";
    }
}
