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
    public String create(@Valid @ModelAttribute("director") Director director,
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

    @GetMapping("/directors/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Director director = this.directorService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nessun regista con id " + id));
        model.addAttribute("director", director);
        return "directors/form";
    }

    @PostMapping("/directors/{id}")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute("director") Director director,
                         BindingResult bindingResult) {

        /* Nel controllo di duplicato si esclude il regista stesso, altrimenti
           risalvarlo senza modifiche verrebbe segnalato come duplicato. */
        if (this.directorService.existsByNameAndSurnameExcluding(
                director.getName(), director.getSurname(), id)) {
            bindingResult.rejectValue("name", "director.duplicate",
                    "Esiste già un altro regista con questo nome");
        }

        if (bindingResult.hasErrors()) {
            director.setId(id);
            return "directors/form";
        }

        this.directorService.update(id, director);
        return "redirect:/directors/" + id;
    }

    /* L'eliminazione e' una POST, non una GET: cambia lo stato del sistema e
       deve passare per il controllo CSRF. */
    @PostMapping("/directors/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            this.directorService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Regista eliminato.");
        } catch (EntityInUseException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/directors";
    }
}
