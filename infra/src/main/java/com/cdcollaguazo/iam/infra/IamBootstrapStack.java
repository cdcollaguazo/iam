package com.cdcollaguazo.iam.infra;

import com.cdcollaguazo.iam.infra.config.Config;
import com.cdcollaguazo.iam.infra.construct.DatabaseBootstrapConstruct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.secretsmanager.ISecret;
import software.amazon.awscdk.services.secretsmanager.Secret;
import software.amazon.awscdk.services.ssm.StringParameter;
import software.constructs.Construct;

public class IamBootstrapStack extends Stack {

    private final ISecret keycloakDbSecret;
    private final String platformName;

    public IamBootstrapStack(Construct scope, String id, StackProps props, Config config) {
        super(scope, id, props);

        platformName = config.platformName();

        String rdsSecretArn = StringParameter.valueForStringParameter(this, buildParameterName("secret-arn"));
        ISecret rdsSecret = Secret.fromSecretCompleteArn(this, "RdsSecret", rdsSecretArn);

        String rdsEndpointAddress = StringParameter.valueForStringParameter(this,
                buildParameterName("instance-host"));

        DatabaseBootstrapConstruct databaseBootstrapConstruct = new DatabaseBootstrapConstruct(this,
                "DatabaseBootstrap", rdsSecret, rdsEndpointAddress, config);
        keycloakDbSecret = databaseBootstrapConstruct.getKeycloakDbSecret();
    }

    private String buildParameterName(String parameter) {
        return "/" + platformName + "/rds/" + parameter;
    }

    public ISecret getKeycloakDbSecret() {
        return keycloakDbSecret;
    }

}
