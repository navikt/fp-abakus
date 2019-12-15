package no.nav.foreldrepenger.abakus.felles.diff;

/**
 * Typisk utvikling exception når deler av grafen ikke kan initialiseres (eks. hibernate LazyInitializationException)
 */
public class TraverseGraphException extends RuntimeException {

    public TraverseGraphException(String message, Throwable t) {
        super(message, t);
    }
    
    public TraverseGraphException(String message) {
        super(message);
    }
}
