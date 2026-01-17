package com.innowise.userservice.exceptions;

public class CardNotFoundException extends RuntimeException {

  public CardNotFoundException(String message) {
    super(message);
  }

  public CardNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public CardNotFoundException(Throwable cause) {
    super(cause);
  }

  public CardNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }


}
