package it.uniroma3.siw.exception;

public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException() {
      super("Elemento già presente nel sistema");
    }
  }
  