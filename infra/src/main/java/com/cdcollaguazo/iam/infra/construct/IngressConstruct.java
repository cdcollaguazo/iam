package com.cdcollaguazo.iam.infra.construct;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.certificatemanager.Certificate;
import software.amazon.awscdk.services.certificatemanager.CertificateValidation;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecs.FargateService;
import software.amazon.awscdk.services.elasticloadbalancingv2.*;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.route53.*;
import software.amazon.awscdk.services.route53.targets.LoadBalancerTarget;
import software.constructs.Construct;

import java.util.List;

import static com.cdcollaguazo.iam.infra.config.EnvironmentConfig.HOSTED_ZONE_ID;
import static com.cdcollaguazo.iam.infra.config.EnvironmentConfig.MAIN_DOMAIN;

public class IngressConstruct extends Construct {

    private static final String IAM_PREFIX = "auth";

    public IngressConstruct(Construct scope, String id, Vpc vpc, SecurityGroup albSg, FargateService keycloakEcs) {
        super(scope, id);

        // Hosted Zone
        IHostedZone hostedZone = HostedZone.fromHostedZoneAttributes(this, "HostedZone",
                HostedZoneAttributes.builder()
                        .hostedZoneId(HOSTED_ZONE_ID)
                        .zoneName(MAIN_DOMAIN)
                        .build());

        // Certificate
        Certificate certificate = Certificate.Builder.create(this, "Certificate")
                .domainName(IAM_PREFIX + "." + MAIN_DOMAIN)
                .validation(CertificateValidation.fromDns(hostedZone))
                .build();

        // ALB
        ApplicationLoadBalancer alb = ApplicationLoadBalancer.Builder.create(this, "Alb")
                .loadBalancerName("iam")
                .internetFacing(true)
                .ipAddressType(IpAddressType.IPV4)
                .vpc(vpc)
                .vpcSubnets(SubnetSelection.builder()
                        .subnetType(SubnetType.PUBLIC)
                        .build())
                .securityGroup(albSg)
                .build();

        alb.addListener("HttpListener",
                ApplicationListenerProps.builder()
                        .loadBalancer(alb)
                        .protocol(ApplicationProtocol.HTTP)
                        .port(80)
                        .defaultAction(
                                ListenerAction.redirect(
                                        RedirectOptions.builder()
                                                .protocol("HTTPS")
                                                .port("443")
                                                .build()))
                        .build());

        ApplicationTargetGroup keycloakTg = ApplicationTargetGroup.Builder.create(this, "KeycloakTg")
                .targetGroupName("iam-keycloak")
                .targets(List.of(keycloakEcs))
                .targetType(TargetType.IP)
                .protocol(ApplicationProtocol.HTTP)
                .port(8080)
                .ipAddressType(TargetGroupIpAddressType.IPV4)
                .vpc(vpc)
                .protocolVersion(ApplicationProtocolVersion.HTTP1)
                .healthCheck(HealthCheck.builder()
                        .protocol(software.amazon.awscdk.services.elasticloadbalancingv2.Protocol.HTTP)
                        .port("9000")
                        .path("/health/live")
                        .healthyThresholdCount(3)
                        .unhealthyThresholdCount(3)
                        .timeout(Duration.seconds(55))
                        .interval(Duration.seconds(60))
                        .healthyHttpCodes("200")
                        .build())
                .build();

        alb.addListener("HttpsListener",
                ApplicationListenerProps.builder()
                        .loadBalancer(alb)
                        .protocol(ApplicationProtocol.HTTPS)
                        .port(443)
                        .certificates(List.of(
                                ListenerCertificate.fromArn(certificate.getCertificateArn())))
                        .defaultAction(ListenerAction.forward(
                                List.of(keycloakTg)))
                        .build());

        // DNS Record
        new ARecord(this, "DnsRecord", ARecordProps.builder()
                .zone(hostedZone)
                .recordName(IAM_PREFIX)
                .target(RecordTarget.fromAlias(new LoadBalancerTarget(alb)))
                .build());
    }

}
