package com.evocharge.cdk;

import com.evocharge.cdk.stacks.ApiStack;
import com.evocharge.cdk.stacks.DataStack;
import com.evocharge.cdk.stacks.NetworkStack;
import com.evocharge.cdk.stacks.WebStack;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public class EvoChargeCdkApp {

    public static void main(String[] args) {
        App app = new App();

        Environment env = Environment.builder()
                .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                .region(System.getenv().getOrDefault("CDK_DEFAULT_REGION", "us-east-1"))
                .build();

        StackProps props = StackProps.builder().env(env).build();
        TagsUtil.applyCompulsoryTags(app);

        NetworkStack network = new NetworkStack(app, "EvoCharge-Network", props);
        DataStack data = new DataStack(app, "EvoCharge-Data", props);
        ApiStack api = new ApiStack(app, "EvoCharge-Api", props, network, data);
        TagsUtil.applyGenAiTags(api);
        WebStack web = new WebStack(app, "EvoCharge-Web", props, api);

        app.synth();
    }
}
