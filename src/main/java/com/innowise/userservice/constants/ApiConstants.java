package com.innowise.userservice.constants;

public final class ApiConstants {
  public static final String API_V1 = "/api/v1";
  public static final String USERS = API_V1 + "/users";
  public static final String CARDS = API_V1 + "/cards";

  private ApiConstants() {}

  public static final String BY_ID = "/{id}";
  public static final String SEARCH = "/search";
  public static final String BY_ID_WITH_CARDS = "/{id}/cards";
  public static final String ACTIVE = "/active";
  public static final String ACTIVATE= "/{id}/activate";
  public static final String DEACTIVATE = "/{id}/deactivate";
}
