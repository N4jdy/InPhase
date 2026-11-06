package at.mci.inphase.model;

public class AthleteStatusItem {
    private final User athlete;
    private String todayTrafficLight;
    private Long todayScore;

    public AthleteStatusItem(User athlete, String todayTrafficLight, Long todayScore) {
        this.athlete = athlete;
        this.todayTrafficLight = todayTrafficLight;
        this.todayScore = todayScore;
    }

    public User getAthlete() {
        return athlete;
    }

    public String getTodayTrafficLight() {
        return todayTrafficLight;
    }
    
    public void setTodayTrafficLight(String todayTrafficLight) {
        this.todayTrafficLight = todayTrafficLight;
    }

    public Long getTodayScore() {
        return todayScore;
    }
    
    public void setTodayScore(Long todayScore) {
        this.todayScore = todayScore;
    }
}
