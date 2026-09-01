package com.cdcollaguazo.iam.infra;

import software.amazon.awscdk.*;

public class IacApp extends App {

    public IacApp() {
        super(AppProps.builder().outdir("./cdk.out").build());

        // Add BootstraplessSynthesizer since we don't need to upload any assets
        // Only template creation is needed
        StackProps props = StackProps.builder().synthesizer(new BootstraplessSynthesizer()).build();

        new IamStack(this, "Iam", props);
    }

    public static void main(String[] args) {
        System.out.println("Synthesizing IAM Infrastructure...");
        new IacApp().synth();
    }

}
