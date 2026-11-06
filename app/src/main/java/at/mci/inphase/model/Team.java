package at.mci.inphase.model;

import androidx.annotation.Keep;

import java.util.ArrayList;
import java.util.List;

@Keep
public class Team {
    private String teamID;
    private String name;
    private List<String> coachID = new ArrayList<>();
    private String inviteCode;

    // Leerer Konstruktor für Firebase
    public Team() {
    }

    public Team(String teamID, String name, String inviteCode) {
        this.teamID = teamID;
        this.name = name;
        this.inviteCode = inviteCode;
    }

    // Getter und Setter
    public String getTeamID() { return teamID; }
    public void setTeamID(String teamID) { this.teamID = teamID; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<String> getCoachID() { return coachID; }
    public void setCoachID(List<String> coachID) { this.coachID = coachID; }

    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
}
