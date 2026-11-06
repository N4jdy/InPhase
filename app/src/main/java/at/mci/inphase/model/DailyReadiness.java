package at.mci.inphase.model;

import androidx.annotation.Keep;

@Keep
public class DailyReadiness {
    private String readinessID;
    private String userID;
    private String date; // Format: "YYYY-MM-DD"
    private long submittedAt;

    // Subjektive Werte (Skala 1-5 oder 1-10)
    private int sleepQuality;
    private int fatigue;
    private int muscleSoreness;
    private int stressLevel;
    private int mood;

    // Wearable / Import
    private int restingHeartRate;
    private double sleepDurationHours;

    // Zyklusangaben & Privatsphäre
    private String menstrualCyclePhase; // "Follicular", "Ovulation", "Luteal", "Menstruation"
    private boolean isCycleDataPrivate; // true = Trainer sieht Zyklusphase nicht!

    // Berechnetes Ergebnis
    private int readinessScore;
    private String trafficLight; // "Green", "Yellow", "Red"

    // Leerer Konstruktor für Firebase
    public DailyReadiness() {
    }

    // Getter und Setter
    public String getReadinessID() { return readinessID; }
    public void setReadinessID(String readinessID) { this.readinessID = readinessID; }

    public String getUserID() { return userID; }
    public void setUserID(String userID) { this.userID = userID; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public long getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(long submittedAt) { this.submittedAt = submittedAt; }

    public int getSleepQuality() { return sleepQuality; }
    public void setSleepQuality(int sleepQuality) { this.sleepQuality = sleepQuality; }

    public int getFatigue() { return fatigue; }
    public void setFatigue(int fatigue) { this.fatigue = fatigue; }

    public int getMuscleSoreness() { return muscleSoreness; }
    public void setMuscleSoreness(int muscleSoreness) { this.muscleSoreness = muscleSoreness; }

    public int getStressLevel() { return stressLevel; }
    public void setStressLevel(int stressLevel) { this.stressLevel = stressLevel; }

    public int getMood() { return mood; }
    public void setMood(int mood) { this.mood = mood; }

    public int getRestingHeartRate() { return restingHeartRate; }
    public void setRestingHeartRate(int restingHeartRate) { this.restingHeartRate = restingHeartRate; }

    public double getSleepDurationHours() { return sleepDurationHours; }
    public void setSleepDurationHours(double sleepDurationHours) { this.sleepDurationHours = sleepDurationHours; }

    public String getMenstrualCyclePhase() { return menstrualCyclePhase; }
    public void setMenstrualCyclePhase(String menstrualCyclePhase) { this.menstrualCyclePhase = menstrualCyclePhase; }

    public boolean isCycleDataPrivate() { return isCycleDataPrivate; }
    public void setCycleDataPrivate(boolean cycleDataPrivate) { isCycleDataPrivate = cycleDataPrivate; }

    public int getReadinessScore() { return readinessScore; }
    public void setReadinessScore(int readinessScore) { this.readinessScore = readinessScore; }

    public String getTrafficLight() { return trafficLight; }
    public void setTrafficLight(String trafficLight) { this.trafficLight = trafficLight; }
}