package com.cdcollaguazo.iam.infra;

import com.cdcollaguazo.iam.infra.config.Config;
import com.cdcollaguazo.iam.infra.config.ConfigLoader;
import software.amazon.awscdk.*;

public class IamApp {

    public static void main(String[] args) {
        App app = new App(AppProps.builder().outdir("./cdk.out").build());

        // Add BootstraplessSynthesizer since we don't need to upload any assets
        // Only template creation is needed
        StackProps props = StackProps.builder().synthesizer(new BootstraplessSynthesizer()).build();

        Config config = ConfigLoader.loadConfig();

        IamBootstrapStack iamBootstrapStack = new IamBootstrapStack(app, "IamBootstrap", props, config);
        new IamServiceStack(app, "IamService", props, iamBootstrapStack.getKeycloakDbSecret(), config);

        app.synth();
    }

}
