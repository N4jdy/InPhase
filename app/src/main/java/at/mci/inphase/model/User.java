package at.mci.inphase.model;

import androidx.annotation.Keep;

@Keep
public class User {
    private String userID;
    private String name;
    private String email;
    private String role; // "Athlete" oder "Coach"
    private String teamID;

    // Optionale Profilangaben
    private long birthDate; // Timestamp
    private int height;
    private double weight;

    // Wichtig für Firebase: Leerer Standardkonstruktor
    public User() {
    }

    public User(String userID, String name, String email, String role, String teamID) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.role = role;
        this.teamID = teamID;
    }

    // Getter und Setter
    public String getUserID() { return userID; }
    public void setUserID(String userID) { this.userID = userID; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getTeamID() { return teamID; }
    public void setTeamID(String teamID) { this.teamID = teamID; }

    public long getBirthDate() { return birthDate; }
    public void setBirthDate(long birthDate) { this.birthDate = birthDate; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
}