package com.evocharge.cdk.stacks;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.SubnetType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

public class NetworkStack extends Stack {

    private final Vpc vpc;

    public NetworkStack(Construct scope, String id, StackProps props) {
        super(scope, id, props);

        vpc = Vpc.Builder.create(this, "Vpc")
                .maxAzs(2)
                .natGateways(1)
                .build();

        software.amazon.awscdk.CfnOutput.Builder.create(this, "VpcId")
                .value(vpc.getVpcId())
                .build();
    }

    public Vpc getVpc() {
        return vpc;
    }

    public SubnetSelection publicSubnets() {
        return SubnetSelection.builder().subnetType(SubnetType.PUBLIC).build();
    }

    public SubnetSelection privateSubnets() {
        return SubnetSelection.builder().subnetType(SubnetType.PRIVATE_WITH_EGRESS).build();
    }
}
