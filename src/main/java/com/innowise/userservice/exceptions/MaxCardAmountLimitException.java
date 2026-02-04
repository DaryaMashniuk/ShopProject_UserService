package com.innowise.userservice.exceptions;

public class MaxCardAmountLimitException extends RuntimeException {

  public MaxCardAmountLimitException(String message) {
    super(message);
  }

  public MaxCardAmountLimitException(String message, Throwable cause) {
    super(message, cause);
  }

  public MaxCardAmountLimitException(Throwable cause) {
    super(cause);
  }

  public MaxCardAmountLimitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public MaxCardAmountLimitException(Long userId, int currentCount, int maxLimit) {
    super(String.format(
            "User with id %d already has %d cards. Maximum allowed is %d cards.",
            userId, currentCount, maxLimit));
  }
}

