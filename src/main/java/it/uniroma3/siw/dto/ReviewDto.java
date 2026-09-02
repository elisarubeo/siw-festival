package it.uniroma3.siw.dto;

import java.time.LocalDate;

import it.uniroma3.siw.model.Review;

/**
 * Una recensione come la vede il client.
 *
 * Non si restituisce l'entita' Review: Jackson seguirebbe Review -> Movie ->
 * reviews -> Review all'infinito, e serializzando fuori dalla transazione
 * troverebbe i proxy LAZY non inizializzati.
 *
 * authorId non serve a mostrare qualcosa a schermo: serve a React per
 * riconoscere le recensioni dell'utente collegato, confrontandolo con l'id che
 * riceve al login. E' cosi' che decide su quali card mostrare "Modifica" ed
 * "Elimina" — che restano pero' solo cortesia visiva: il controllo che conta
 * e' la verifica di proprieta' nel service.
 *
 * Si usa l'id e non lo username perche' User non ha alcun riferimento verso
 * Credentials: la relazione e' a senso unico (Credentials.user), quindi da una
 * Review non si puo' risalire allo username senza una query in piu' per ogni
 * recensione — cioe' reintroducendo le N+1 che il JOIN FETCH ha appena tolto.
 */
public record ReviewDto(
        Long id,
        String text,
        Integer rating,
        LocalDate reviewDate,
        Long authorId,
        String authorName,
        String authorSurname) {

    /**
     * ATTENZIONE: questo metodo naviga review.getUser(), che e' LAZY.
     * Va chiamato DENTRO un metodo @Transactional, altrimenti la sessione
     * Hibernate e' gia' chiusa e parte LazyInitializationException. E' anche
     * il motivo per cui la query della lista usa JOIN FETCH.
     */
    public static ReviewDto from(Review review) {
        return new ReviewDto(
                review.getId(),
                review.getText(),
                review.getRating(),
                review.getReviewDate(),
                review.getUser().getId(),
                review.getUser().getName(),
                review.getUser().getSurname());
    }
}
