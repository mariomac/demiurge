package es.bsc.clurge.core.vmm;

public enum DeploymentEngine {
    LEGACY("legacy"), OPTAPLANNER("optaPlanner");
    private String name;

    private DeploymentEngine(String name) {
        this.name = name;
    }

    public static DeploymentEngine fromName(String name) {
        return DeploymentEngine.valueOf(name.toUpperCase());
    }

    @Override
    public String toString() {
        return name;
    }
}
