package at.mci.inphase.model;

import androidx.annotation.Keep;

@Keep
public class Workload {
    private String workloadID;
    private String sessionID;
    private String userID;
    private int rpeScore; // Skala 1-10
    private int duration; // Dauer in Minuten
    private int workload; // Berechnet: rpeScore * duration

    // Leerer Konstruktor für Firebase
    public Workload() {
    }

    public Workload(String workloadID, String sessionID, String userID, int rpeScore, int duration) {
        this.workloadID = workloadID;
        this.sessionID = sessionID;
        this.userID = userID;
        this.rpeScore = rpeScore;
        this.duration = duration;
        this.workload = rpeScore * duration; // Automatische Berechnung
    }

    // Getter und Setter
    public String getWorkloadID() { return workloadID; }
    public void setWorkloadID(String workloadID) { this.workloadID = workloadID; }

    public String getSessionID() { return sessionID; }
    public void setSessionID(String sessionID) { this.sessionID = sessionID; }

    public String getUserID() { return userID; }
    public void setUserID(String userID) { this.userID = userID; }

    public int getRpeScore() { return rpeScore; }
    public void setRpeScore(int rpeScore) {
        this.rpeScore = rpeScore;
        this.workload = this.rpeScore * this.duration;
    }

    public int getDuration() { return duration; }
    public void setDuration(int duration) {
        this.duration = duration;
        this.workload = this.rpeScore * this.duration;
    }

    public int getWorkload() { return workload; }
    public void setWorkload(int workload) { this.workload = workload; }
}
