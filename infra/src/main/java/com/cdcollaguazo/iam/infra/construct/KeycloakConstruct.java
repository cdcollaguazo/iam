package com.cdcollaguazo.iam.infra.construct;

import com.cdcollaguazo.iam.infra.config.Config;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.SecretValue;
import software.amazon.awscdk.services.ec2.ISecurityGroup;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.elasticloadbalancingv2.*;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.secretsmanager.ISecret;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

public class KeycloakConstruct extends Construct {

    public KeycloakConstruct(Construct scope, String id, IVpc vpc, ICluster cluster, ISecurityGroup ecsSg,
                             IApplicationListener albListener, ISecret dbSecret, String rdsHost, String rdsPort, Config config) {
        super(scope, id);

        // Secret
        ISecret adminSecret = software.amazon.awscdk.services.secretsmanager.Secret.Builder.create(this, "AdminSecret")
                .secretName("iam-keycloak")
                .secretObjectValue(Map.of(
                        "username", SecretValue.Builder.create(config.keycloakAdminUser()).build(),
                        "password", SecretValue.Builder.create(config.keycloakAdminPassword()).build()
                ))
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();

        // ECS
        FargateTaskDefinition taskDefinition = FargateTaskDefinition.Builder.create(this, "TaskDefinition")
                .family("iam-keycloak")
                .cpu(1024)
                .memoryLimitMiB(3072)
                .build();

        String jdbcUrl = "jdbc:postgresql://" + rdsHost + ":" + rdsPort + "/keycloak";

        taskDefinition.addContainer("Container", ContainerDefinitionOptions.builder()
                .containerName("iam-keycloak")
                .essential(true)
                .image(ContainerImage.fromRegistry(config.keycloakImage()))
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
                .environment(
                        Map.of(
                                "KC_DB", "postgres",
                                "KC_DB_URL", jdbcUrl,
                                "KC_HOSTNAME", config.keycloakHost(),
                                "KC_HTTP_ENABLED", "true",
                                "KC_HTTP_RELATIVE_PATH", config.keycloakRelativePath(),
                                "KC_HEALTH_ENABLED", "true",
                                "BSN_USERS_API_URL", config.bsnUsersApiUrl()
                        )
                )
                .secrets(
                        Map.of(
                                "KC_DB_USERNAME", Secret.fromSecretsManager(dbSecret, "username"),
                                "KC_DB_PASSWORD", Secret.fromSecretsManager(dbSecret, "password"),
                                "KC_BOOTSTRAP_ADMIN_USERNAME", Secret.fromSecretsManager(adminSecret, "username"),
                                "KC_BOOTSTRAP_ADMIN_PASSWORD", Secret.fromSecretsManager(adminSecret, "password")
                        )
                )
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                        .streamPrefix("iam-keycloak")
                        .logRetention(RetentionDays.ONE_WEEK)
                        .build()))
                .command(List.of("start"))
                .build());

        FargateService ecs = FargateService.Builder.create(this, "Ecs")
                .serviceName("iam-keycloak")
                .cluster(cluster)
                .taskDefinition(taskDefinition)
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

        // ALB
        ApplicationTargetGroup targetGroup = ApplicationTargetGroup.Builder.create(this, "TargetGroup")
                .targetGroupName("iam-keycloak")
                .targetType(TargetType.IP)
                .protocol(ApplicationProtocol.HTTP)
                .port(8080)
                .ipAddressType(TargetGroupIpAddressType.IPV4)
                .vpc(vpc)
                .protocolVersion(ApplicationProtocolVersion.HTTP1)
                .healthCheck(HealthCheck.builder()
                        .protocol(software.amazon.awscdk.services.elasticloadbalancingv2.Protocol.HTTP)
                        .port("9000")
                        .path(config.keycloakRelativePath() + "/health/ready")
                        .healthyThresholdCount(3)
                        .unhealthyThresholdCount(3)
                        .timeout(Duration.seconds(55))
                        .interval(Duration.seconds(60))
                        .healthyHttpCodes("200")
                        .build())
                .build();

        ecs.attachToApplicationTargetGroup(targetGroup);

        ApplicationListenerRule.Builder.create(this, "ALBListenerRule")
                .listener(albListener)
                .priority(100)
                .conditions(List.of(
                        ListenerCondition.pathPatterns(
                                List.of("/auth", "/auth/*"))
                        )
                )
                .targetGroups(List.of(targetGroup))
                .build();
    }

}
