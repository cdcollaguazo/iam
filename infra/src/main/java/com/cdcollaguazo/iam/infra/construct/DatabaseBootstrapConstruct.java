package com.cdcollaguazo.iam.infra.construct;

import com.cdcollaguazo.iam.infra.config.Config;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.logs.RetentionDays;

import software.amazon.awscdk.services.rds.DatabaseSecret;
import software.amazon.awscdk.services.secretsmanager.ISecret;
import software.constructs.Construct;

import java.util.Map;

public class DatabaseBootstrapConstruct extends Construct {

    private final ISecret keycloakDbSecret;

    public DatabaseBootstrapConstruct(Construct scope, String id, ISecret rdsSecret, String rdsEndpointAddress,
                                      Config config) {
        super(scope, id);

        // Secret
        keycloakDbSecret = DatabaseSecret.Builder.create(this, "KeycloakDbSecret")
                .username(config.keycloakDbUser())
                .secretName("iam-keycloak-db")
                .build();

        // Bootstrap Task Definition
        FargateTaskDefinition bootstrapTask = FargateTaskDefinition.Builder.create(this, "BootstrapTask")
                .family("iam-bootstrap")
                .cpu(1024)
                .memoryLimitMiB(3072)
                .build();

        // Bootstrap Container
        bootstrapTask.addContainer("BootstrapContainer", ContainerDefinitionOptions.builder()
                .containerName("iam-bootstrap")
                .essential(true)
                .image(ContainerImage.fromRegistry(config.bootstrapImage()))
                .environment(
                        Map.of(
                                "HOST", rdsEndpointAddress
                        )
                )
                .secrets(
                        Map.of(
                                "ROOT_DB_USER", Secret.fromSecretsManager(rdsSecret, "username"),
                                "ROOT_DB_PASSWORD", Secret.fromSecretsManager(rdsSecret, "password"),
                                "KEYCLOAK_DB_USER", Secret.fromSecretsManager(keycloakDbSecret, "username"),
                                "KEYCLOAK_DB_PASSWORD", Secret.fromSecretsManager(keycloakDbSecret, "password")
                        )
                )
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                        .streamPrefix("bootstrap")
                        .logRetention(RetentionDays.ONE_WEEK)
                        .build()))
                .build());
    }

    public ISecret getKeycloakDbSecret() {
        return keycloakDbSecret;
    }

}
