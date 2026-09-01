package com.cdcollaguazo.iam.infra.construct;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.SecretValue;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.rds.DatabaseInstance;
import software.amazon.awscdk.services.secretsmanager.ISecret;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cdcollaguazo.iam.infra.config.EnvironmentConfig.*;

public class ServiceConstruct extends Construct {

    private final FargateService keycloakEcs;

    public ServiceConstruct(Construct scope, String id, Vpc vpc, SecurityGroup ecsSg, DatabaseInstance rds) {
        super(scope, id);

        // Secrets
        ISecret keycloakSecret = software.amazon.awscdk.services.secretsmanager.Secret.Builder.create(this, "KeycloakSecret")
                .secretName("iam-keycloak")
                .secretObjectValue(Map.of(
                        "username", SecretValue.Builder.create(KEYCLOAK_ADMIN_USER).build(),
                        "password", SecretValue.Builder.create(KEYCLOAK_ADMIN_PASSWORD).build()
                ))
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();

        // ECS
        Cluster cluster = Cluster.Builder.create(this, "EcsCluster")
                .clusterName("iam")
                .vpc(vpc)
                .containerInsightsV2(ContainerInsights.ENHANCED)
                .build();



        // Keycloak
        FargateTaskDefinition keycloakTaskDef = FargateTaskDefinition.Builder.create(this, "KeycloakTaskDef")
                .family("iam-keycloak")
                .cpu(1024)
                .memoryLimitMiB(3072)
                .build();

        String jdbcUrl = "jdbc:postgresql://" + rds.getDbInstanceEndpointAddress() + ":" +
                rds.getDbInstanceEndpointPort() + "/" + DatabaseConstruct.KEYCLOAK_DATABASE_NAME;

        Map<String, String> keycloakEnvironment = new HashMap<>();
        keycloakEnvironment.put("KC_DB", "postgres");
        keycloakEnvironment.put("KC_DB_URL", jdbcUrl);
        keycloakEnvironment.put("KC_HOSTNAME", KEYCLOAK_HOST);
        keycloakEnvironment.put("KC_HTTP_ENABLED", "true");
        keycloakEnvironment.put("KC_PROXY_HEADERS", "xforwarded");
        keycloakEnvironment.put("KC_HEALTH_ENABLED", "true");
        keycloakEnvironment.put("BSN_USERS_API_URL", BSN_USERS_API_URL);

        keycloakTaskDef.addContainer("KeycloakContainer", ContainerDefinitionOptions.builder()
                .containerName("iam-keycloak")
                .essential(true)
                .image(ContainerImage.fromRegistry(REPOSITORY_NAME + "/iam-keycloak:" + TAG_VERSION))
                .portMappings(List.of(
                        PortMapping.builder()
                                .containerPort(8080)
                                .hostPort(8080)
                                .protocol(software.amazon.awscdk.services.ecs.Protocol.TCP)
                                .build(),
                        PortMapping.builder()
                                .containerPort(9000)
                                .hostPort(9000)
                                .protocol(software.amazon.awscdk.services.ecs.Protocol.TCP)
                                .build()
                ))
                .environment(keycloakEnvironment)
                .secrets(
                        Map.of(
                                "KC_DB_USERNAME", Secret.fromSecretsManager(rds.getSecret(), "username"),
                                "KC_DB_PASSWORD", Secret.fromSecretsManager(rds.getSecret(), "password"),
                                "KC_BOOTSTRAP_ADMIN_USERNAME", Secret.fromSecretsManager(keycloakSecret, "username"),
                                "KC_BOOTSTRAP_ADMIN_PASSWORD", Secret.fromSecretsManager(keycloakSecret, "password")
                        )
                )
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                        .streamPrefix("iam-keycloak")
                        .logRetention(RetentionDays.ONE_WEEK)
                        .build()))
                .command(List.of("start"))
                .build());

        keycloakEcs = FargateService.Builder.create(this, "KeycloakEcs")
                .serviceName("iam-keycloak")
                .cluster(cluster)
                .taskDefinition(keycloakTaskDef)
                .desiredCount(1)
                .availabilityZoneRebalancing(AvailabilityZoneRebalancing.ENABLED)
                .healthCheckGracePeriod(Duration.seconds(0))
                .deploymentStrategy(DeploymentStrategy.ROLLING)
                .minHealthyPercent(100)
                .maxHealthyPercent(200)
                .platformVersion(FargatePlatformVersion.LATEST)
                .enableExecuteCommand(false)
                .vpcSubnets(SubnetSelection.builder()
                        .subnetType(SubnetType.PRIVATE_WITH_EGRESS)
                        .build())
                .securityGroups(List.of(ecsSg))
                .assignPublicIp(false)
                .build();
    }

    public FargateService getKeycloakEcs() {
        return keycloakEcs;
    }

}
