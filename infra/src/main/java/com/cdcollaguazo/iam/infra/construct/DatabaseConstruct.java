package com.cdcollaguazo.iam.infra.construct;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.rds.*;
import software.constructs.Construct;

import java.util.List;

import static com.cdcollaguazo.iam.infra.config.EnvironmentConfig.KEYCLOAK_DB_USER;

public class DatabaseConstruct extends Construct {

    public static final String KEYCLOAK_DATABASE_NAME = "keycloak";

    private final DatabaseInstance rds;

    public DatabaseConstruct(Construct scope, String id, Vpc vpc, SecurityGroup rdsSg) {
        super(scope, id);

        // RDS
        rds = DatabaseInstance.Builder.create(this, "Rds")
                .engine(DatabaseInstanceEngine.postgres(
                        PostgresInstanceEngineProps.builder()
                                .version(PostgresEngineVersion.VER_18)
                                .build()
                ))
                .credentials(Credentials.fromGeneratedSecret(KEYCLOAK_DB_USER,
                        CredentialsBaseOptions.builder()
                                .secretName("iam-rds")
                                .build()))
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE3, InstanceSize.MICRO))
                .allocatedStorage(30)
                .multiAz(false)
                .networkType(NetworkType.IPV4)
                .vpc(vpc)
                .vpcSubnets(SubnetSelection.builder()
                        .subnetType(SubnetType.PRIVATE_WITH_EGRESS)
                        .build())
                .securityGroups(List.of(rdsSg))
                .caCertificate(CaCertificate.RDS_CA_RSA2048_G1)
                .publiclyAccessible(false)
                .port(5432)
                .databaseInsightsMode(DatabaseInsightsMode.STANDARD)
                .enablePerformanceInsights(true)
                .performanceInsightRetention(PerformanceInsightRetention.DEFAULT)
                .databaseName(KEYCLOAK_DATABASE_NAME)
                .backupRetention(Duration.days(1))
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
    }

    public DatabaseInstance getRds() {
        return rds;
    }

}
