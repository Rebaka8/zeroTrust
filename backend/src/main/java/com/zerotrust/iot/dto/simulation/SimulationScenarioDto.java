package com.zerotrust.iot.dto.simulation;

public class SimulationScenarioDto {
    private AttackType attackType;
    private String name;
    private String description;
    private String expectedDefenseOutcome;
    private String mitigationMechanism;

    public SimulationScenarioDto() {}

    public SimulationScenarioDto(AttackType attackType, String name, String description, String expectedDefenseOutcome, String mitigationMechanism) {
        this.attackType = attackType;
        this.name = name;
        this.description = description;
        this.expectedDefenseOutcome = expectedDefenseOutcome;
        this.mitigationMechanism = mitigationMechanism;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private AttackType attackType;
        private String name;
        private String description;
        private String expectedDefenseOutcome;
        private String mitigationMechanism;

        public Builder attackType(AttackType attackType) { this.attackType = attackType; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder expectedDefenseOutcome(String expectedDefenseOutcome) { this.expectedDefenseOutcome = expectedDefenseOutcome; return this; }
        public Builder mitigationMechanism(String mitigationMechanism) { this.mitigationMechanism = mitigationMechanism; return this; }

        public SimulationScenarioDto build() {
            return new SimulationScenarioDto(attackType, name, description, expectedDefenseOutcome, mitigationMechanism);
        }
    }

    public AttackType getAttackType() { return attackType; }
    public void setAttackType(AttackType attackType) { this.attackType = attackType; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getExpectedDefenseOutcome() { return expectedDefenseOutcome; }
    public void setExpectedDefenseOutcome(String expectedDefenseOutcome) { this.expectedDefenseOutcome = expectedDefenseOutcome; }

    public String getMitigationMechanism() { return mitigationMechanism; }
    public void setMitigationMechanism(String mitigationMechanism) { this.mitigationMechanism = mitigationMechanism; }
}
