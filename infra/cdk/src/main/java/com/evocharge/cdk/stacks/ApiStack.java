package com.evocharge.cdk.stacks;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.LogDrivers;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.events.Rule;
import software.amazon.awscdk.services.events.Schedule;
import software.amazon.awscdk.services.events.targets.LambdaFunction;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

public class ApiStack extends Stack {

    private final ApplicationLoadBalancedFargateService fargateService;

    public ApiStack(Construct scope, String id, StackProps props,
                    NetworkStack network, DataStack data) {
        super(scope, id, props);

        Repository ecrRepo = Repository.Builder.create(this, "ApiRepo")
                .repositoryName("evocharge-api")
                .build();

        Cluster cluster = Cluster.Builder.create(this, "Cluster")
                .vpc(network.getVpc())
                .build();

        LogGroup logGroup = LogGroup.Builder.create(this, "ApiLogGroup")
                .retention(RetentionDays.ONE_WEEK)
                .build();

        fargateService = ApplicationLoadBalancedFargateService.Builder.create(this, "ApiService")
                .cluster(cluster)
                .cpu(512)
                .memoryLimitMiB(1024)
                .desiredCount(1)
                .publicLoadBalancer(true)
                .taskImageOptions(
                        software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions.builder()
                                .image(ContainerImage.fromRegistry("public.ecr.aws/docker/library/eclipse-temurin:21-jre-alpine"))
                                .containerPort(8080)
                                .environment(Map.of(
                                        "SPRING_PROFILES_ACTIVE", "aws",
                                        "EVOCHARGE_STORAGE", "dynamodb",
                                        "EVOCHARGE_DYNAMODB_OPERATORS_TABLE", data.getOperatorsTable().getTableName(),
                                        "EVOCHARGE_DYNAMODB_STATIONS_TABLE", data.getStationsTable().getTableName(),
                                        "EVOCHARGE_BEDROCK_ENABLED", "true",
                                        "EVOCHARGE_BEDROCK_MODEL_ID", "anthropic.claude-haiku-4-5-20251001-v1:0"
                                ))
                                .logDriver(LogDrivers.awsLogs(
                                        software.amazon.awscdk.services.ecs.AwsLogDriverProps.builder()
                                                .logGroup(logGroup)
                                                .streamPrefix("evocharge-api")
                                                .build()))
                                .build())
                .build();

        fargateService.getTargetGroup().configureHealthCheck(
                software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck.builder()
                        .path("/api/v1/health")
                        .healthyHttpCodes("200")
                        .interval(Duration.seconds(30))
                        .build());

        data.getOperatorsTable().grantReadWriteData(fargateService.getTaskDefinition().getTaskRole());
        data.getStationsTable().grantReadWriteData(fargateService.getTaskDefinition().getTaskRole());
        data.getDataBucket().grantRead(fargateService.getTaskDefinition().getTaskRole());

        fargateService.getTaskDefinition().getTaskRole().addToPrincipalPolicy(
                PolicyStatement.Builder.create()
                        .effect(Effect.ALLOW)
                        .actions(List.of(
                                "bedrock:InvokeModel",
                                "geo:GetMap*",
                                "geo:SearchPlaceIndexForText",
                                "geo:CalculateRoute"
                        ))
                        .resources(List.of("*"))
                        .build());

        Function pulseFn = Function.Builder.create(this, "NetworkPulseFn")
                .runtime(Runtime.PYTHON_3_12)
                .handler("index.handler")
                .code(Code.fromInline("""
                        import json, urllib.request, os
                        def handler(event, context):
                            api = os.environ.get('API_URL', '')
                            if api:
                                try:
                                    req = urllib.request.Request(api + '/api/v1/health')
                                    urllib.request.urlopen(req, timeout=5)
                                except Exception as e:
                                    print(str(e))
                            return {'statusCode': 200}
                        """))
                .environment(Map.of(
                        "API_URL", "http://" + fargateService.getLoadBalancer().getLoadBalancerDnsName()
                ))
                .timeout(Duration.seconds(30))
                .build();

        Rule.Builder.create(this, "NetworkPulseRule")
                .schedule(Schedule.rate(Duration.minutes(2)))
                .targets(List.of(new LambdaFunction(pulseFn)))
                .build();

        software.amazon.awscdk.CfnOutput.Builder.create(this, "ApiUrl")
                .value("http://" + fargateService.getLoadBalancer().getLoadBalancerDnsName())
                .build();
        software.amazon.awscdk.CfnOutput.Builder.create(this, "EcrRepoUri")
                .value(ecrRepo.getRepositoryUri())
                .build();
    }

    public ApplicationLoadBalancedFargateService getFargateService() {
        return fargateService;
    }
}
