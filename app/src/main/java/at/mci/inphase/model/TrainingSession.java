package at.mci.inphase.model;

import androidx.annotation.Keep;

@Keep
public class TrainingSession {
    private String sessionID;
    private String teamID;
    private long dateTime; // Timestamp
    private String sessionType; // "Team_Training", "Match", "Gym", "Recovery", "Run"
    private String title;
    private String notes; // Optional

    // Leerer Konstruktor für Firebase
    public TrainingSession() {
    }

    // Getter und Setter
    public String getSessionID() { return sessionID; }
    public void setSessionID(String sessionID) { this.sessionID = sessionID; }

    public String getTeamID() { return teamID; }
    public void setTeamID(String teamID) { this.teamID = teamID; }

    public long getDateTime() { return dateTime; }
    public void setDateTime(long dateTime) { this.dateTime = dateTime; }

    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
