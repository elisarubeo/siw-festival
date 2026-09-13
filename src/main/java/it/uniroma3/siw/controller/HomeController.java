package it.uniroma3.siw.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import it.uniroma3.siw.model.Screening;
import it.uniroma3.siw.model.ScreeningStatus;
import it.uniroma3.siw.service.FestivalService;
import it.uniroma3.siw.service.MovieService;
import it.uniroma3.siw.service.ScreeningService;

@Controller
public class HomeController {

    private static final int NEXT_SCREENINGS = 5;

    private FestivalService festivalService;
    private MovieService movieService;
    private ScreeningService screeningService;

    public HomeController(FestivalService festivalService,
                          MovieService movieService,
                          ScreeningService screeningService) {
        this.festivalService = festivalService;
        this.movieService = movieService;
        this.screeningService = screeningService;
    }

    @GetMapping("/")
    public String getHome(Model model) {
        LocalDate today = LocalDate.now();

        model.addAttribute("festivals", this.festivalService.findCurrentAndUpcoming(today));

        List<Screening> upcoming = this.screeningService.findFutureScreenings(today).stream()
                .filter(screening -> screening.getStatus() != ScreeningStatus.CANCELLED)
                .toList();
        model.addAttribute("nextScreenings",
                upcoming.stream().limit(NEXT_SCREENINGS).toList());

        model.addAttribute("screeningCount", upcoming.size());
        model.addAttribute("movieCount", this.movieService.count());

        model.addAttribute("today", today);
        return "homepage";
    }
}
