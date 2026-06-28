package com.evocharge.cdk;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Tags;
import software.constructs.Construct;

/** AWS resource tags required for hackathon and Gen AI program tracking. */
public final class TagsUtil {

    public static final String APN_ID_KEY = "aws-apn-id";
    public static final String APN_ID_VALUE = "pc:8l8gcn23lmlgammd8572tk6va";

    public static final String EVENT_KEY = "event";
    public static final String EVENT_VALUE = "oneWithAI";

    public static final String GEN_AI_APN_ID_VALUE = "pc:a8xnp70u5w0s41039u52e6iuj";

    private TagsUtil() {}

    /** Tags every stack resource with project and hackathon metadata. */
    public static void applyCompulsoryTags(App app) {
        Tags.of(app).add(APN_ID_KEY, APN_ID_VALUE);
        Tags.of(app).add(EVENT_KEY, EVENT_VALUE);
        Tags.of(app).add("project", "evocharge");
        Tags.of(app).add("environment", "hackathon");
        Tags.of(app).add("managed-by", "cdk");
    }

    /** Tags Bedrock-related resources with the Gen AI partner program ID. */
    public static void applyGenAiTags(Construct scope) {
        Tags.of(scope).add(APN_ID_KEY, GEN_AI_APN_ID_VALUE);
        Tags.of(scope).add("gen-ai", "true");
    }
}
