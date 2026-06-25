package com.evocharge.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "evocharge")
public class EvoChargeProperties {

    private String storage = "local";
    private String seedPath = "../../data/seed";
    private String corsOrigins = "http://localhost:5173";
    private DynamoDb dynamodb = new DynamoDb();
    private Bedrock bedrock = new Bedrock();
    private Mistral mistral = new Mistral();
    private Seed seed = new Seed();
    private Pulse pulse = new Pulse();

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public String getSeedPath() {
        return seedPath;
    }

    public void setSeedPath(String seedPath) {
        this.seedPath = seedPath;
    }

    public String getCorsOrigins() {
        return corsOrigins;
    }

    public void setCorsOrigins(String corsOrigins) {
        this.corsOrigins = corsOrigins;
    }

    public DynamoDb getDynamodb() {
        return dynamodb;
    }

    public void setDynamodb(DynamoDb dynamodb) {
        this.dynamodb = dynamodb;
    }

    public Bedrock getBedrock() {
        return bedrock;
    }

    public void setBedrock(Bedrock bedrock) {
        this.bedrock = bedrock;
    }

    public Mistral getMistral() {
        return mistral;
    }

    public void setMistral(Mistral mistral) {
        this.mistral = mistral;
    }

    public Seed getSeed() {
        return seed;
    }

    public void setSeed(Seed seed) {
        this.seed = seed;
    }

    public Pulse getPulse() {
        return pulse;
    }

    public void setPulse(Pulse pulse) {
        this.pulse = pulse;
    }

    public static class DynamoDb {
        private String operatorsTable = "EvoCharge-Operators";
        private String stationsTable = "EvoCharge-Stations";

        public String getOperatorsTable() {
            return operatorsTable;
        }

        public void setOperatorsTable(String operatorsTable) {
            this.operatorsTable = operatorsTable;
        }

        public String getStationsTable() {
            return stationsTable;
        }

        public void setStationsTable(String stationsTable) {
            this.stationsTable = stationsTable;
        }
    }

    public static class Bedrock {
        private String modelId = "anthropic.claude-haiku-4-5-20251001-v1:0";
        private boolean enabled;

        public String getModelId() {
            return modelId;
        }

        public void setModelId(String modelId) {
            this.modelId = modelId;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Mistral {
        private boolean enabled = true;
        private String apiKey = "";
        private String model = "mistral-small-latest";
        private String baseUrl = "https://api.mistral.ai/v1";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }

    public static class Seed {
        private boolean resyncOnStartup;

        public boolean isResyncOnStartup() {
            return resyncOnStartup;
        }

        public void setResyncOnStartup(boolean resyncOnStartup) {
            this.resyncOnStartup = resyncOnStartup;
        }
    }

    public static class Pulse {
        private int intervalSeconds = 120;

        public int getIntervalSeconds() {
            return intervalSeconds;
        }

        public void setIntervalSeconds(int intervalSeconds) {
            this.intervalSeconds = intervalSeconds;
        }
    }
}
