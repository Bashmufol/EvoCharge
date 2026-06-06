package com.evocharge.cdk;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Tags;
import software.constructs.Construct;

public final class TagsUtil {

    /** Compulsory — Arthurite / ONE WITH AI hackathon partner ID */
    public static final String APN_ID_KEY = "aws-apn-id";
    public static final String APN_ID_VALUE = "pc:8l8gcn23lmlgammd8572tk6va";

    /** Compulsory — hackathon event identifier */
    public static final String EVENT_KEY = "event";
    public static final String EVENT_VALUE = "oneWithAI";

    /** Optional — Gen AI partner ID (Bedrock / generative AI workloads) */
    public static final String GEN_AI_APN_ID_VALUE = "pc:a8xnp70u5w0s41039u52e6iuj";

    private TagsUtil() {}

    /**
     * Applies compulsory hackathon tags to every resource in the app.
     */
    public static void applyCompulsoryTags(App app) {
        Tags.of(app).add(APN_ID_KEY, APN_ID_VALUE);
        Tags.of(app).add(EVENT_KEY, EVENT_VALUE);
        Tags.of(app).add("project", "evocharge");
        Tags.of(app).add("environment", "hackathon");
        Tags.of(app).add("managed-by", "cdk");
    }

    /**
     * Applies the Gen AI partner tag to resources that invoke Amazon Bedrock.
     * Uses the same {@code aws-apn-id} key with the Gen AI program value on this construct subtree.
     */
    public static void applyGenAiTags(Construct scope) {
        Tags.of(scope).add(APN_ID_KEY, GEN_AI_APN_ID_VALUE);
        Tags.of(scope).add("gen-ai", "true");
    }
}
