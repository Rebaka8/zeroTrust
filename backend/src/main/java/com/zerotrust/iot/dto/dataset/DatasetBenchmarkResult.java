package com.zerotrust.iot.dto.dataset;

import java.util.List;

public class DatasetBenchmarkResult {
    private String datasetName;
    private int totalRowsProcessed;
    private int benignPacketsCount;
    private int maliciousPacketsCount;
    private int truePositives;
    private int falsePositives;
    private int trueNegatives;
    private int falseNegatives;
    private double accuracyPercentage;
    private double precisionPercentage;
    private double recallPercentage;
    private double f1ScorePercentage;
    private double averageDetectionLatencyMs;
    private int automaticQuarantinesEnforced;
    private List<String> detectedThreatSignatures;
    private String defenseEvaluationSummary;

    public DatasetBenchmarkResult() {}

    public DatasetBenchmarkResult(String datasetName, int totalRowsProcessed, int benignPacketsCount, int maliciousPacketsCount, int truePositives, int falsePositives, int trueNegatives, int falseNegatives, double accuracyPercentage, double precisionPercentage, double recallPercentage, double f1ScorePercentage, double averageDetectionLatencyMs, int automaticQuarantinesEnforced, List<String> detectedThreatSignatures, String defenseEvaluationSummary) {
        this.datasetName = datasetName;
        this.totalRowsProcessed = totalRowsProcessed;
        this.benignPacketsCount = benignPacketsCount;
        this.maliciousPacketsCount = maliciousPacketsCount;
        this.truePositives = truePositives;
        this.falsePositives = falsePositives;
        this.trueNegatives = trueNegatives;
        this.falseNegatives = falseNegatives;
        this.accuracyPercentage = accuracyPercentage;
        this.precisionPercentage = precisionPercentage;
        this.recallPercentage = recallPercentage;
        this.f1ScorePercentage = f1ScorePercentage;
        this.averageDetectionLatencyMs = averageDetectionLatencyMs;
        this.automaticQuarantinesEnforced = automaticQuarantinesEnforced;
        this.detectedThreatSignatures = detectedThreatSignatures;
        this.defenseEvaluationSummary = defenseEvaluationSummary;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String datasetName;
        private int totalRowsProcessed;
        private int benignPacketsCount;
        private int maliciousPacketsCount;
        private int truePositives;
        private int falsePositives;
        private int trueNegatives;
        private int falseNegatives;
        private double accuracyPercentage;
        private double precisionPercentage;
        private double recallPercentage;
        private double f1ScorePercentage;
        private double averageDetectionLatencyMs;
        private int automaticQuarantinesEnforced;
        private List<String> detectedThreatSignatures;
        private String defenseEvaluationSummary;

        public Builder datasetName(String datasetName) { this.datasetName = datasetName; return this; }
        public Builder totalRowsProcessed(int totalRowsProcessed) { this.totalRowsProcessed = totalRowsProcessed; return this; }
        public Builder benignPacketsCount(int benignPacketsCount) { this.benignPacketsCount = benignPacketsCount; return this; }
        public Builder maliciousPacketsCount(int maliciousPacketsCount) { this.maliciousPacketsCount = maliciousPacketsCount; return this; }
        public Builder truePositives(int truePositives) { this.truePositives = truePositives; return this; }
        public Builder falsePositives(int falsePositives) { this.falsePositives = falsePositives; return this; }
        public Builder trueNegatives(int trueNegatives) { this.trueNegatives = trueNegatives; return this; }
        public Builder falseNegatives(int falseNegatives) { this.falseNegatives = falseNegatives; return this; }
        public Builder accuracyPercentage(double accuracyPercentage) { this.accuracyPercentage = accuracyPercentage; return this; }
        public Builder precisionPercentage(double precisionPercentage) { this.precisionPercentage = precisionPercentage; return this; }
        public Builder recallPercentage(double recallPercentage) { this.recallPercentage = recallPercentage; return this; }
        public Builder f1ScorePercentage(double f1ScorePercentage) { this.f1ScorePercentage = f1ScorePercentage; return this; }
        public Builder averageDetectionLatencyMs(double averageDetectionLatencyMs) { this.averageDetectionLatencyMs = averageDetectionLatencyMs; return this; }
        public Builder automaticQuarantinesEnforced(int automaticQuarantinesEnforced) { this.automaticQuarantinesEnforced = automaticQuarantinesEnforced; return this; }
        public Builder detectedThreatSignatures(List<String> detectedThreatSignatures) { this.detectedThreatSignatures = detectedThreatSignatures; return this; }
        public Builder defenseEvaluationSummary(String defenseEvaluationSummary) { this.defenseEvaluationSummary = defenseEvaluationSummary; return this; }

        public DatasetBenchmarkResult build() {
            return new DatasetBenchmarkResult(datasetName, totalRowsProcessed, benignPacketsCount, maliciousPacketsCount, truePositives, falsePositives, trueNegatives, falseNegatives, accuracyPercentage, precisionPercentage, recallPercentage, f1ScorePercentage, averageDetectionLatencyMs, automaticQuarantinesEnforced, detectedThreatSignatures, defenseEvaluationSummary);
        }
    }

    public String getDatasetName() { return datasetName; }
    public void setDatasetName(String datasetName) { this.datasetName = datasetName; }

    public int getTotalRowsProcessed() { return totalRowsProcessed; }
    public void setTotalRowsProcessed(int totalRowsProcessed) { this.totalRowsProcessed = totalRowsProcessed; }

    public int getBenignPacketsCount() { return benignPacketsCount; }
    public void setBenignPacketsCount(int benignPacketsCount) { this.benignPacketsCount = benignPacketsCount; }

    public int getMaliciousPacketsCount() { return maliciousPacketsCount; }
    public void setMaliciousPacketsCount(int maliciousPacketsCount) { this.maliciousPacketsCount = maliciousPacketsCount; }

    public int getTruePositives() { return truePositives; }
    public void setTruePositives(int truePositives) { this.truePositives = truePositives; }

    public int getFalsePositives() { return falsePositives; }
    public void setFalsePositives(int falsePositives) { this.falsePositives = falsePositives; }

    public int getTrueNegatives() { return trueNegatives; }
    public void setTrueNegatives(int trueNegatives) { this.trueNegatives = trueNegatives; }

    public int getFalseNegatives() { return falseNegatives; }
    public void setFalseNegatives(int falseNegatives) { this.falseNegatives = falseNegatives; }

    public double getAccuracyPercentage() { return accuracyPercentage; }
    public void setAccuracyPercentage(double accuracyPercentage) { this.accuracyPercentage = accuracyPercentage; }

    public double getPrecisionPercentage() { return precisionPercentage; }
    public void setPrecisionPercentage(double precisionPercentage) { this.precisionPercentage = precisionPercentage; }

    public double getRecallPercentage() { return recallPercentage; }
    public void setRecallPercentage(double recallPercentage) { this.recallPercentage = recallPercentage; }

    public double getF1ScorePercentage() { return f1ScorePercentage; }
    public void setF1ScorePercentage(double f1ScorePercentage) { this.f1ScorePercentage = f1ScorePercentage; }

    public double getAverageDetectionLatencyMs() { return averageDetectionLatencyMs; }
    public void setAverageDetectionLatencyMs(double averageDetectionLatencyMs) { this.averageDetectionLatencyMs = averageDetectionLatencyMs; }

    public int getAutomaticQuarantinesEnforced() { return automaticQuarantinesEnforced; }
    public void setAutomaticQuarantinesEnforced(int automaticQuarantinesEnforced) { this.automaticQuarantinesEnforced = automaticQuarantinesEnforced; }

    public List<String> getDetectedThreatSignatures() { return detectedThreatSignatures; }
    public void setDetectedThreatSignatures(List<String> detectedThreatSignatures) { this.detectedThreatSignatures = detectedThreatSignatures; }

    public String getDefenseEvaluationSummary() { return defenseEvaluationSummary; }
    public void setDefenseEvaluationSummary(String defenseEvaluationSummary) { this.defenseEvaluationSummary = defenseEvaluationSummary; }
}
