package com.cdcollaguazo.iam.infra;

import com.cdcollaguazo.iam.infra.config.Config;
import com.cdcollaguazo.iam.infra.construct.KeycloakConstruct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ClusterAttributes;
import software.amazon.awscdk.services.ecs.ICluster;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationListener;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationListenerAttributes;
import software.amazon.awscdk.services.elasticloadbalancingv2.IApplicationListener;
import software.amazon.awscdk.services.secretsmanager.ISecret;
import software.amazon.awscdk.services.ssm.StringParameter;
import software.constructs.Construct;

import java.util.List;

public class IamServiceStack extends Stack {

    private final String platformName;

    public IamServiceStack(Construct scope, String id, StackProps props, ISecret keycloakDbSecret, Config config) {
        super(scope, id, props);

        platformName = config.platformName();

        String vpcId = getValueForParameter("vpc", "vpc-id");

        String az1 = getValueForParameter("vpc", "az-1");
        String az2 = getValueForParameter("vpc", "az-2");

        String privateSubnet1Id = getValueForParameter("vpc", "private-subnet-1-id");
        String privateSubnet2Id = getValueForParameter("vpc", "private-subnet-2-id");

         IVpc vpc = Vpc.fromVpcAttributes(this, "Vpc", VpcAttributes.builder()
                 .vpcId(vpcId)
                 .availabilityZones(List.of(az1, az2))
                 .privateSubnetIds(List.of(privateSubnet1Id, privateSubnet2Id))
                 .build());

        String ecsClusterArn = getValueForParameter("ecs", "cluster-arn");
        String ecsClusterName = getValueForParameter("ecs", "cluster-name");
        ICluster ecsCluster = Cluster.fromClusterAttributes(this, "EcsCluster", ClusterAttributes.builder()
                .vpc(vpc)
                .clusterArn(ecsClusterArn)
                .clusterName(ecsClusterName)
                .build());

        String ecsSgId = getValueForParameter("vpc", "ecs-sg-id");
        ISecurityGroup ecsSg = SecurityGroup.fromSecurityGroupId(this, "EcsSg", ecsSgId);

        String albSgId = getValueForParameter("vpc", "alb-sg-id");
        ISecurityGroup albSg = SecurityGroup.fromSecurityGroupId(this, "AlbSg", albSgId);

        String albHttpListenerArn = getValueForParameter("alb", "http-listener-arn");
        IApplicationListener albListener = ApplicationListener.fromApplicationListenerAttributes(this, "AlbListener", ApplicationListenerAttributes.builder()
                .listenerArn(albHttpListenerArn)
                .securityGroup(albSg)
                .build());

        String rdsHost = getValueForParameter("rds", "instance-host");

        String rdsPort = getValueForParameter("rds", "instance-port");

        new KeycloakConstruct(this, "Keycloak", vpc, ecsCluster, ecsSg, albListener, keycloakDbSecret,
                rdsHost, rdsPort, config);
    }

    private String getValueForParameter(String module, String parameter) {
        return StringParameter.valueForStringParameter(this,
                "/" + platformName + "/" + module + "/" + parameter);
    }

}
