package com.zerotrust.iot.dto.dataset;

public class DatasetPresetDto {
    private String id;
    private String name;
    private String source;
    private String description;
    private String attackTypes;
    private int sampleRowsCount;

    public DatasetPresetDto() {}

    public DatasetPresetDto(String id, String name, String source, String description, String attackTypes, int sampleRowsCount) {
        this.id = id;
        this.name = name;
        this.source = source;
        this.description = description;
        this.attackTypes = attackTypes;
        this.sampleRowsCount = sampleRowsCount;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAttackTypes() { return attackTypes; }
    public void setAttackTypes(String attackTypes) { this.attackTypes = attackTypes; }

    public int getSampleRowsCount() { return sampleRowsCount; }
    public void setSampleRowsCount(int sampleRowsCount) { this.sampleRowsCount = sampleRowsCount; }
}
