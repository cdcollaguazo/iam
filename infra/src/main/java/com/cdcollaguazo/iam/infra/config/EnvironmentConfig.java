package com.cdcollaguazo.iam.infra.config;

public class EnvironmentConfig {

    // Database
    public static final String KEYCLOAK_DB_USER = System.getenv("KEYCLOAK_DB_USER");

    // Hosted Zone & Domain
    public static final String HOSTED_ZONE_ID = System.getenv("HOSTED_ZONE_ID");
    public static final String MAIN_DOMAIN = System.getenv("MAIN_DOMAIN");

    // Mail
    public static final String MAIL_USER =  System.getenv("MAIL_USER");
    public static final String MAIL_PASSWORD =  System.getenv("MAIL_PASSWORD");

    // URLs
    public static final String BSN_USERS_API_URL = System.getenv("BSN_USERS_API_URL");

    // Keycloak
    public static final String KEYCLOAK_HOST = System.getenv("KEYCLOAK_HOST");
    public static final String KEYCLOAK_ADMIN_USER = System.getenv("KEYCLOAK_ADMIN_USER");
    public static final String KEYCLOAK_ADMIN_PASSWORD = System.getenv("KEYCLOAK_ADMIN_PASSWORD");

    // Docker Image
    public static final String REPOSITORY_NAME = System.getenv("REPOSITORY_NAME");
    public static final String TAG_VERSION = System.getenv("TAG_VERSION");

}
