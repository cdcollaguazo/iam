package com.cdcollaguazo.iam.infra.construct;

import software.amazon.awscdk.services.ec2.*;
import software.constructs.Construct;

import java.util.List;

public class NetworkConstruct extends Construct {

    private final Vpc vpc;
    private final SecurityGroup albSg;
    private final SecurityGroup ecsSg;
    private final SecurityGroup rdsSg;

    public NetworkConstruct( Construct scope, String id) {
        super(scope, id);

        // VPC and subnets
        vpc = Vpc.Builder
                .create(this, "Vpc")
                .vpcName("iam")
                .maxAzs(2)
                .natGateways(1)
                .subnetConfiguration(List.of(
                        SubnetConfiguration.builder()
                                .name("iam-public")
                                .subnetType(SubnetType.PUBLIC)
                                .cidrMask(24)
                                .build(),
                        SubnetConfiguration.builder()
                                .name("iam-private")
                                .subnetType(SubnetType.PRIVATE_WITH_EGRESS)
                                .cidrMask(24)
                                .build()
                ))
                .build();

        // Security Groups and inbound/outbound rules for ALB, ECS and RDS
        albSg = SecurityGroup.Builder.create(this, "AlbSg")
                .vpc(vpc)
                .securityGroupName("iam-alb")
                .allowAllOutbound(false)
                .description("Security group for ALB")
                .build();
        albSg.addIngressRule(Peer.anyIpv4(), Port.tcp(443), "Allow HTTPS from everywhere");
        albSg.addIngressRule(Peer.anyIpv4(), Port.tcp(80), "Allow HTTP from everywhere");

        ecsSg = SecurityGroup.Builder.create(this, "EcsSg")
                .vpc(vpc)
                .securityGroupName("iam-ecs")
                .allowAllOutbound(true)
                .description("Security group for ECS")
                .build();
        ecsSg.addIngressRule(albSg, Port.tcp(8080), "Allow TCP 8080 from ALB");
        ecsSg.addIngressRule(albSg, Port.tcp(9000), "Allow TCP 9000 from ALB");

        albSg.addEgressRule(ecsSg,  Port.tcp(8080), "Allow TCP 8080 to ECS");
        albSg.addEgressRule(ecsSg,  Port.tcp(9000), "Allow TCP 9000 to ECS");

        rdsSg = SecurityGroup.Builder.create(this, "RdsSg")
                .vpc(vpc)
                .securityGroupName("iam-rds")
                .allowAllOutbound(true)
                .description("Security group for RDS")
                .build();
        rdsSg.addIngressRule(ecsSg, Port.tcp(5432), "Allow TCP 5432 from ECS");
    }

    public Vpc getVpc() {
        return vpc;
    }

    public SecurityGroup getAlbSg() {
        return albSg;
    }

    public SecurityGroup getEcsSg() {
        return ecsSg;
    }

    public SecurityGroup getRdsSg() {
        return rdsSg;
    }

}
