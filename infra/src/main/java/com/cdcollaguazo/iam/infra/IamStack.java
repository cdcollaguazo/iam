package com.cdcollaguazo.iam.infra;

import com.cdcollaguazo.iam.infra.construct.DatabaseConstruct;
import com.cdcollaguazo.iam.infra.construct.IngressConstruct;
import com.cdcollaguazo.iam.infra.construct.NetworkConstruct;
import com.cdcollaguazo.iam.infra.construct.ServiceConstruct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.constructs.Construct;

public class IamStack extends Stack {

    public IamStack(Construct scope, String id, StackProps props) {
        super(scope, id, props);

        NetworkConstruct networkConstruct = new NetworkConstruct(this, "Network");
        DatabaseConstruct databaseConstruct = new DatabaseConstruct(this, "Database",
                networkConstruct.getVpc(), networkConstruct.getRdsSg());
        ServiceConstruct serviceConstruct = new ServiceConstruct(this, "Service",
                networkConstruct.getVpc(), networkConstruct.getEcsSg(), databaseConstruct.getRds());
        new IngressConstruct(this, "Ingress",networkConstruct.getVpc(),
                networkConstruct.getAlbSg(), serviceConstruct.getKeycloakEcs());
    }

}
