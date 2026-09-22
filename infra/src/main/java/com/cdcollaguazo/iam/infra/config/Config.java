package com.cdcollaguazo.iam.infra.config;

public record Config(
        String platformName,
        String bootstrapImage,
        String keycloakImage,
        String keycloakDbUser,
        String keycloakAdminUser,
        String keycloakAdminPassword,
        String keycloakHost,
        String keycloakRelativePath,
        String bsnUsersApiUrl
) {
}
