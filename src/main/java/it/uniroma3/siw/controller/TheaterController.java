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
    public String create(@Valid @ModelAttribute("theater") Theater theater,
                         BindingResult bindingResult) {

        if (this.theaterService.existsByNameAndAddress(theater.getName(), theater.getAddress())) {
            bindingResult.rejectValue("name", "theater.duplicate",
                    "Questa sala è già presente");
        }

        if (bindingResult.hasErrors()) {
            return "theaters/form";
        }

        this.theaterService.save(theater);
        return "redirect:/theaters";
    }

    @GetMapping("/theaters/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Theater theater = this.theaterService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nessuna sala con id " + id));
        model.addAttribute("theater", theater);
        return "theaters/form";
    }

    @PostMapping("/theaters/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("theater") Theater theater,
                         BindingResult bindingResult) {

        if (this.theaterService.existsByNameAndAddressExcluding(
                theater.getName(), theater.getAddress(), id)) {
            bindingResult.rejectValue("name", "theater.duplicate",
                    "Esiste già un'altra sala con questo nome e indirizzo");
        }

        if (bindingResult.hasErrors()) {
            theater.setId(id);
            return "theaters/form";
        }

        this.theaterService.update(id, theater);
        return "redirect:/theaters/" + id;
    }

    /* L'eliminazione e' una POST, non una GET: cambia lo stato del sistema e
       deve passare per il controllo CSRF. */
    @PostMapping("/theaters/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            this.theaterService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Sala eliminata.");
        } catch (EntityInUseException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/theaters";
    }
}
