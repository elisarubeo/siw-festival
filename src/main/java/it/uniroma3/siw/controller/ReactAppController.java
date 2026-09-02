package it.uniroma3.siw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serve la radice dell'app React, cioe' /reviews e /reviews/.
 *
 * Il resource handler di ReactAppConfig copre tutto il resto sotto /reviews/**,
 * ma non questi due indirizzi: Spring non serve automaticamente l'index di una
 * cartella, e per una richiesta a /reviews/ il percorso della risorsa e' la
 * stringa vuota, che non corrisponde ad alcun file.
 *
 * Il mapping elenca i due percorsi ESATTI e non usa /**: cosi' non intercetta
 * /reviews/assets/index-abc.js, che deve continuare ad arrivare al resource
 * handler ed essere servito come JavaScript e non come HTML.
 *
 * "forward:" e non "redirect:": l'inoltro e' interno al server e l'indirizzo
 * nella barra del browser resta invariato.
 */
@Controller
public class ReactAppController {

    @GetMapping({"/reviews", "/reviews/"})
    public String reactAppRoot() {
        return "forward:/reviews/index.html";
    }
}
