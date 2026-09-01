package it.uniroma3.siw.controller;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.cglib.core.Local;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.uniroma3.siw.exception.BusinessRuleException;
import it.uniroma3.siw.exception.ResourceNotFoundException;
import it.uniroma3.siw.model.Screening;
import it.uniroma3.siw.service.ScreeningService;
import it.uniroma3.siw.service.TheaterService;

@Controller
public class ScreeningController {

    private final ScreeningService screeningService;
    private final TheaterService theaterService;

    public ScreeningController(ScreeningService screeningService, TheaterService theaterService) {
        this.screeningService = screeningService;
        this.theaterService = theaterService;
    }

    @GetMapping("/screenings")
    public String list(@RequestParam(required = false)
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                       Model model) {

        LocalDate today = LocalDate.now();
        LocalDate from = (date == null || date.isBefore(today)) ? today : date;

        model.addAttribute("screenings", this.screeningService.findFutureScreenings(from));
        model.addAttribute("selectedDate", from);
        model.addAttribute("today", today);
        return "screenings/list";
    }

    @GetMapping("/screenings/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Screening screening = this.screeningService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nessuna proiezione con id " + id));
        model.addAttribute("screening", screening);
        /* Nella tendina solo i film che partecipano a questo festival:
           gli altri verrebbero comunque rifiutati dal service. */
        model.addAttribute("movies", screening.getFestival().getMovies());
        model.addAttribute("theaters", this.theaterService.findAll());
        return "screenings/form";
    }

    @PostMapping("/screenings/{id}")
    public String update(@PathVariable("id") Long id,
                         @RequestParam("movieId") Long movieId,
                         @RequestParam("theaterId") Long theaterId,
                         @RequestParam("date")
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                         @RequestParam("time")
                         @DateTimeFormat(pattern = "HH:mm") LocalTime time,
                         RedirectAttributes redirectAttributes) {

        
        try {
            Long festivalId = this.screeningService.reschedule(id, movieId, theaterId, date, time);
            redirectAttributes.addFlashAttribute("successMessage", "Proiezione aggiornata.");
            return "redirect:/festivals/" + festivalId;
        } catch (BusinessRuleException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            // in caso di conflitto si torna alla form, con i dati ancora a video
            return "redirect:/screenings/" + id + "/edit";
        }
    }

    @PostMapping("/screenings/{id}/cancel")
    public String cancel(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Long festivalId = this.screeningService.cancelScreening(id);
        redirectAttributes.addFlashAttribute("successMessage", "Proiezione annullata.");
        return "redirect:/festivals/" + festivalId;
    }

    @PostMapping("/screenings/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Long festivalId = this.screeningService.removeScreening(id);
        redirectAttributes.addFlashAttribute("successMessage", "Proiezione eliminata.");
        return "redirect:/festivals/" + festivalId;
    }
}
