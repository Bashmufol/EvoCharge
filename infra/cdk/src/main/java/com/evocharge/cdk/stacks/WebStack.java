package com.evocharge.cdk.stacks;

import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.cloudfront.AllowedMethods;
import software.amazon.awscdk.services.cloudfront.BehaviorOptions;
import software.amazon.awscdk.services.cloudfront.CachePolicy;
import software.amazon.awscdk.services.cloudfront.Distribution;
import software.amazon.awscdk.services.cloudfront.Function;
import software.amazon.awscdk.services.cloudfront.FunctionAssociation;
import software.amazon.awscdk.services.cloudfront.FunctionCode;
import software.amazon.awscdk.services.cloudfront.FunctionEventType;
import software.amazon.awscdk.services.cloudfront.FunctionRuntime;
import software.amazon.awscdk.services.cloudfront.OriginRequestPolicy;
import software.amazon.awscdk.services.cloudfront.ViewerProtocolPolicy;
import software.amazon.awscdk.services.cloudfront.OriginProtocolPolicy;
import software.amazon.awscdk.services.cloudfront.origins.HttpOrigin;
import software.amazon.awscdk.services.cloudfront.origins.S3Origin;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.Bucket;
import software.constructs.Construct;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** CloudFront distribution: static apps on S3, API requests proxied to the ALB over HTTP. */
public class WebStack extends Stack {

    public WebStack(Construct scope, String id, StackProps props, ApiStack api) {
        super(scope, id, props);

        String apiDns = api.getFargateService().getLoadBalancer().getLoadBalancerDnsName();

        Bucket webBucket = Bucket.Builder.create(this, "WebBucket")
                .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
                .removalPolicy(RemovalPolicy.DESTROY)
                .autoDeleteObjects(true)
                .build();

        HttpOrigin apiOrigin = HttpOrigin.Builder.create(apiDns)
                .protocolPolicy(OriginProtocolPolicy.HTTP_ONLY)
                .originPath("")
                .build();

        S3Origin s3Origin = S3Origin.Builder.create(webBucket).build();

        Function operatorRewriteFn = Function.Builder.create(this, "OperatorRewriteFn")
                .code(FunctionCode.fromInline("""
                        function handler(event) {
                            var request = event.request;
                            var uri = request.uri;
                            if (uri === '/operator' || uri === '/operator/') {
                                request.uri = '/operator/index.html';
                            }
                            return request;
                        }
                        """))
                .runtime(FunctionRuntime.JS_2_0)
                .build();

        List<FunctionAssociation> operatorFnAssociations = List.of(
                FunctionAssociation.builder()
                        .function(operatorRewriteFn)
                        .eventType(FunctionEventType.VIEWER_REQUEST)
                        .build()
        );

        BehaviorOptions apiBehavior = BehaviorOptions.builder()
                .origin(apiOrigin)
                .viewerProtocolPolicy(ViewerProtocolPolicy.HTTPS_ONLY)
                .allowedMethods(AllowedMethods.ALLOW_ALL)
                .cachePolicy(CachePolicy.CACHING_DISABLED)
                .originRequestPolicy(OriginRequestPolicy.ALL_VIEWER_EXCEPT_HOST_HEADER)
                .build();

        BehaviorOptions operatorBehavior = BehaviorOptions.builder()
                .origin(s3Origin)
                .viewerProtocolPolicy(ViewerProtocolPolicy.REDIRECT_TO_HTTPS)
                .cachePolicy(CachePolicy.CACHING_OPTIMIZED)
                .functionAssociations(operatorFnAssociations)
                .build();

        BehaviorOptions driverBehavior = BehaviorOptions.builder()
                .origin(s3Origin)
                .viewerProtocolPolicy(ViewerProtocolPolicy.REDIRECT_TO_HTTPS)
                .cachePolicy(CachePolicy.CACHING_OPTIMIZED)
                .functionAssociations(operatorFnAssociations)
                .build();

        Map<String, BehaviorOptions> additionalBehaviors = new LinkedHashMap<>();
        additionalBehaviors.put("/api/*", apiBehavior);
        additionalBehaviors.put("/operator/*", operatorBehavior);

        // API paths return JSON; SPA error-page rules are not applied to /api/*.
        Distribution webCdn = Distribution.Builder.create(this, "WebCdn")
                .defaultBehavior(driverBehavior)
                .additionalBehaviors(additionalBehaviors)
                .defaultRootObject("index.html")
                .build();

        String webUrl = "https://" + webCdn.getDistributionDomainName();

        software.amazon.awscdk.CfnOutput.Builder.create(this, "WebUrl")
                .value(webUrl)
                .description("Driver app at / and operator dashboard at /operator/")
                .build();
        software.amazon.awscdk.CfnOutput.Builder.create(this, "WebBucketName")
                .value(webBucket.getBucketName())
                .build();
        software.amazon.awscdk.CfnOutput.Builder.create(this, "DriverWebUrl")
                .value(webUrl)
                .description("Same as WebUrl - driver app served at /")
                .build();
        software.amazon.awscdk.CfnOutput.Builder.create(this, "OperatorWebUrl")
                .value(webUrl + "/operator/")
                .description("Operator dashboard path on the shared distribution")
                .build();
    }
}
