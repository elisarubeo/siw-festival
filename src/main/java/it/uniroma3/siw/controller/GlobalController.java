package it.uniroma3.siw.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/*Classe che permette che lo username sia sempre disponibile ai template, 
associa un attributo al modello per tutte le richieste.

basePackages la limita ai controller Thymeleaf, ed e' indispensabile: senza,
girerebbe anche sulle richieste REST, dove il principal e' la stringa messa
dal JwtAuthenticationFilter e non un UserDetails — il cast qui sotto
solleverebbe ClassCastException su ogni chiamata autenticata a /api.
I controller REST non hanno comunque alcun model da popolare. */
@ControllerAdvice(basePackages = "it.uniroma3.siw.controller")
public class GlobalController {
  
  @ModelAttribute("userDetails")
  public UserDetails getUser() {
    UserDetails user = null;
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!(authentication instanceof AnonymousAuthenticationToken)) {
      user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
    return user;
  }
}
