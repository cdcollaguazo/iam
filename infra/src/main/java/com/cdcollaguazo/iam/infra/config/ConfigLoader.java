package com.cdcollaguazo.iam.infra.config;

public class ConfigLoader {

    private ConfigLoader() {}

    public static Config loadConfig() {
        return new Config(
                required("PLATFORM_NAME"),
                required("PLATFORM_URL"),
                required("BOOTSTRAP_IMAGE"),
                required("KEYCLOAK_IMAGE"),
                required("KEYCLOAK_DB_USER"),
                required("KEYCLOAK_ADMIN_USER"),
                required("KEYCLOAK_ADMIN_PASSWORD"),
                required("BSN_USERS_API_ENDPOINT")
        );
    }

    private static String required(String name) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing environment variable: " + name);
        }

        return value;
    }

}
