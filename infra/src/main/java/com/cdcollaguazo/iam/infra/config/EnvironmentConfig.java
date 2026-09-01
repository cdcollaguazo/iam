package com.cdcollaguazo.iam.infra.config;

public class EnvironmentConfig {

    // Credentials
    public static final String KEYCLOAK_DB_USER = System.getenv("KEYCLOAK_DB_USER");
    public static final String KEYCLOAK_ADMIN_USER = System.getenv("KEYCLOAK_ADMIN_USER");
    public static final String KEYCLOAK_ADMIN_PASSWORD = System.getenv("KEYCLOAK_ADMIN_PASSWORD");

    // Hosted Zone & URLs
    public static final String HOSTED_ZONE_ID = System.getenv("HOSTED_ZONE_ID");
    public static final String MAIN_HOST = System.getenv("MAIN_HOST");
    public static final String KEYCLOAK_HOST = System.getenv("KEYCLOAK_HOST");
    public static final String BSN_USERS_API_URL = System.getenv("BSN_USERS_API_URL");

    // Docker Image
    public static final String REPOSITORY_NAME = System.getenv("REPOSITORY_NAME");
    public static final String TAG_VERSION = System.getenv("TAG_VERSION");

}
