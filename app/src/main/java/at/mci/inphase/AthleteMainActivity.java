package at.mci.inphase;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class AthleteMainActivity extends AppCompatActivity {

    private View tabAthleteToday, tabAthleteCalendar, tabAthleteTrends, tabAthleteProfile;
    private ImageView iconAthleteToday, iconAthleteCalendar, iconAthleteTrends, iconAthleteProfile;

    private static final int COLOR_ACTIVE = Color.parseColor("#2D6A4F");
    private static final int COLOR_INACTIVE = Color.parseColor("#8E9AAF");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_athlete_main);

        tabAthleteToday = findViewById(R.id.tabAthleteToday);
        tabAthleteCalendar = findViewById(R.id.tabAthleteCalendar);
        tabAthleteTrends = findViewById(R.id.tabAthleteTrends);
        tabAthleteProfile = findViewById(R.id.tabAthleteProfile);

        iconAthleteToday = findViewById(R.id.iconAthleteToday);
        iconAthleteCalendar = findViewById(R.id.iconAthleteCalendar);
        iconAthleteTrends = findViewById(R.id.iconAthleteTrends);
        iconAthleteProfile = findViewById(R.id.iconAthleteProfile);

        if (savedInstanceState == null) {
            selectTab(0, new AthleteTodayFragment());
        }

        tabAthleteToday.setOnClickListener(v -> selectTab(0, new AthleteTodayFragment()));
        tabAthleteCalendar.setOnClickListener(v -> selectTab(1, new AthleteCalendarFragment()));
        tabAthleteTrends.setOnClickListener(v -> selectTab(2, new AthleteTrendsFragment()));
        tabAthleteProfile.setOnClickListener(v -> selectTab(3, new AthleteProfileFragment()));
    }

    private void selectTab(int index, Fragment fragment) {
        // Reset all tabs
        tabAthleteToday.setBackgroundResource(0);
        tabAthleteCalendar.setBackgroundResource(0);
        tabAthleteTrends.setBackgroundResource(0);
        tabAthleteProfile.setBackgroundResource(0);

        iconAthleteToday.setImageTintList(ColorStateList.valueOf(COLOR_INACTIVE));
        iconAthleteCalendar.setImageTintList(ColorStateList.valueOf(COLOR_INACTIVE));
        iconAthleteTrends.setImageTintList(ColorStateList.valueOf(COLOR_INACTIVE));
        iconAthleteProfile.setImageTintList(ColorStateList.valueOf(COLOR_INACTIVE));

        // Set active tab
        switch (index) {
            case 0:
                tabAthleteToday.setBackgroundResource(R.drawable.bg_active_nav_tab);
                iconAthleteToday.setImageTintList(ColorStateList.valueOf(COLOR_ACTIVE));
                break;
            case 1:
                tabAthleteCalendar.setBackgroundResource(R.drawable.bg_active_nav_tab);
                iconAthleteCalendar.setImageTintList(ColorStateList.valueOf(COLOR_ACTIVE));
                break;
            case 2:
                tabAthleteTrends.setBackgroundResource(R.drawable.bg_active_nav_tab);
                iconAthleteTrends.setImageTintList(ColorStateList.valueOf(COLOR_ACTIVE));
                break;
            case 3:
                tabAthleteProfile.setBackgroundResource(R.drawable.bg_active_nav_tab);
                iconAthleteProfile.setImageTintList(ColorStateList.valueOf(COLOR_ACTIVE));
                break;
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }
}
