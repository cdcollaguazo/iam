package com.cdcollaguazo.iam.infra.config;

public record Config(
        String platformName,
        String platformUrl,
        String bootstrapImage,
        String keycloakImage,
        String keycloakDbUser,
        String keycloakAdminUser,
        String keycloakAdminPassword,
        String bsnUsersApiEndpoint
) {
}
