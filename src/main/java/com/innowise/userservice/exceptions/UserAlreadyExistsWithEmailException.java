package com.innowise.userservice.exceptions;

public class UserAlreadyExistsWithEmailException extends RuntimeException {

  public UserAlreadyExistsWithEmailException(String message) {
    super(message);
  }

  public UserAlreadyExistsWithEmailException(String message, Throwable cause) {
    super(message, cause);
  }

  public UserAlreadyExistsWithEmailException(Throwable cause) {
    super(cause);
  }

  public UserAlreadyExistsWithEmailException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }


}