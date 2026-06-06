package com.evocharge.cdk.stacks;

import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.Bucket;
import software.constructs.Construct;

public class DataStack extends Stack {

    private final Table operatorsTable;
    private final Table stationsTable;
    private final Bucket dataBucket;

    public DataStack(Construct scope, String id, StackProps props) {
        super(scope, id, props);

        operatorsTable = Table.Builder.create(this, "OperatorsTable")
                .tableName("EvoCharge-Operators")
                .partitionKey(Attribute.builder().name("id").type(AttributeType.STRING).build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();

        stationsTable = Table.Builder.create(this, "StationsTable")
                .tableName("EvoCharge-Stations")
                .partitionKey(Attribute.builder().name("id").type(AttributeType.STRING).build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();

        dataBucket = Bucket.Builder.create(this, "DataBucket")
                .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
                .removalPolicy(RemovalPolicy.DESTROY)
                .autoDeleteObjects(true)
                .build();

        software.amazon.awscdk.CfnOutput.Builder.create(this, "OperatorsTableName")
                .value(operatorsTable.getTableName())
                .build();
        software.amazon.awscdk.CfnOutput.Builder.create(this, "StationsTableName")
                .value(stationsTable.getTableName())
                .build();
        software.amazon.awscdk.CfnOutput.Builder.create(this, "DataBucketName")
                .value(dataBucket.getBucketName())
                .build();
    }

    public Table getOperatorsTable() {
        return operatorsTable;
    }

    public Table getStationsTable() {
        return stationsTable;
    }

    public Bucket getDataBucket() {
        return dataBucket;
    }
}
