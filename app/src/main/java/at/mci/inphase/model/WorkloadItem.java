package at.mci.inphase.model;

public class WorkloadItem {
    private final String athleteName;
    private final Workload workload;

    public WorkloadItem(String athleteName, Workload workload) {
        this.athleteName = athleteName;
        this.workload = workload;
    }

    public String getAthleteName() { return athleteName; }
    public Workload getWorkload() { return workload; }
}
