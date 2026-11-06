package at.mci.inphase;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class CoachMainActivity extends AppCompatActivity {

    private View tabCoachDashboard, tabCoachTraining, tabCoachTeam, tabCoachProfile;
    private ImageView iconCoachDashboard, iconCoachTraining, iconCoachTeam, iconCoachProfile;

    private static final int COLOR_ACTIVE = Color.parseColor("#2D6A4F");
    private static final int COLOR_INACTIVE = Color.parseColor("#8E9AAF");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coach_main);

        tabCoachDashboard = findViewById(R.id.tabCoachDashboard);
        tabCoachTraining = findViewById(R.id.tabCoachTraining);
        tabCoachTeam = findViewById(R.id.tabCoachTeam);
        tabCoachProfile = findViewById(R.id.tabCoachProfile);

        iconCoachDashboard = findViewById(R.id.iconCoachDashboard);
        iconCoachTraining = findViewById(R.id.iconCoachTraining);
        iconCoachTeam = findViewById(R.id.iconCoachTeam);
        iconCoachProfile = findViewById(R.id.iconCoachProfile);

        if (savedInstanceState == null) {
            selectTab(0, new CoachDashboardFragment());
        }

        tabCoachDashboard.setOnClickListener(v -> selectTab(0, new CoachDashboardFragment()));
        tabCoachTraining.setOnClickListener(v -> selectTab(1, new CoachTrainingFragment()));
        tabCoachTeam.setOnClickListener(v -> selectTab(2, new CoachTeamFragment()));
        tabCoachProfile.setOnClickListener(v -> selectTab(3, new CoachProfileFragment()));
    }

    private void selectTab(int index, Fragment fragment) {
        // Reset all tabs
        tabCoachDashboard.setBackgroundResource(0);
        tabCoachTraining.setBackgroundResource(0);
        tabCoachTeam.setBackgroundResource(0);
        tabCoachProfile.setBackgroundResource(0);

        iconCoachDashboard.setImageTintList(ColorStateList.valueOf(COLOR_INACTIVE));
        iconCoachTraining.setImageTintList(ColorStateList.valueOf(COLOR_INACTIVE));
        iconCoachTeam.setImageTintList(ColorStateList.valueOf(COLOR_INACTIVE));
        iconCoachProfile.setImageTintList(ColorStateList.valueOf(COLOR_INACTIVE));

        // Set active tab
        switch (index) {
            case 0:
                tabCoachDashboard.setBackgroundResource(R.drawable.bg_active_nav_tab);
                iconCoachDashboard.setImageTintList(ColorStateList.valueOf(COLOR_ACTIVE));
                break;
            case 1:
                tabCoachTraining.setBackgroundResource(R.drawable.bg_active_nav_tab);
                iconCoachTraining.setImageTintList(ColorStateList.valueOf(COLOR_ACTIVE));
                break;
            case 2:
                tabCoachTeam.setBackgroundResource(R.drawable.bg_active_nav_tab);
                iconCoachTeam.setImageTintList(ColorStateList.valueOf(COLOR_ACTIVE));
                break;
            case 3:
                tabCoachProfile.setBackgroundResource(R.drawable.bg_active_nav_tab);
                iconCoachProfile.setImageTintList(ColorStateList.valueOf(COLOR_ACTIVE));
                break;
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }
}
