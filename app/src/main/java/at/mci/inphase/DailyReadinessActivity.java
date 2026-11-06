package at.mci.inphase;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import at.mci.inphase.model.DailyReadiness;

/**
 * Aktivität für den täglichen Morning Check-in der Athleten.
 * Erfasst 5 subjektive Befindlichkeits-Kategorien (Schlaf, Ermüdung, Muskelkater, Stress, Stimmung),
 * berechnet den Tages-Readiness-Score (0-100%) und ordnet diesen einer Ampelfarbe (Grün, Gelb, Rot) zu.
 */
public class DailyReadinessActivity extends AppCompatActivity {

    private MaterialButtonToggleGroup tgSleepQuality, tgFatigue, tgSoreness, tgStress, tgMood;
    private EditText etRestingHR, etSleepHours;
    private Spinner spCyclePhase;
    private SwitchMaterial switchCyclePrivate;
    private Button btnSubmitCheckin;
    private ProgressBar pbCheckin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_readiness);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // UI-Referenzen
        tgSleepQuality = findViewById(R.id.tgSleepQuality);
        tgFatigue = findViewById(R.id.tgFatigue);
        tgSoreness = findViewById(R.id.tgSoreness);
        tgStress = findViewById(R.id.tgStress);
        tgMood = findViewById(R.id.tgMood);

        etRestingHR = findViewById(R.id.etRestingHR);
        etSleepHours = findViewById(R.id.etSleepHours);
        spCyclePhase = findViewById(R.id.spCyclePhase);
        switchCyclePrivate = findViewById(R.id.switchCyclePrivate);
        btnSubmitCheckin = findViewById(R.id.btnSubmitCheckin);
        pbCheckin = findViewById(R.id.pbCheckin);

        setupCycleSpinner();

        btnSubmitCheckin.setOnClickListener(v -> submitDailyCheckin());
    }

    private void setupCycleSpinner() {
        String[] cyclePhases = {"Bitte auswählen", "Menstruation", "Follikelphase", "Ovulation", "Lutealphase"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cyclePhases);
        spCyclePhase.setAdapter(adapter);
    }

    private int getScoreFromToggleGroup(MaterialButtonToggleGroup tg) {
        int checkedId = tg.getCheckedButtonId();
        if (checkedId == View.NO_ID) {
            return 0;
        }

        Button b = findViewById(checkedId);
        try {
            return Integer.parseInt(b.getText().toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void submitDailyCheckin() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Kein Nutzer angemeldet.", Toast.LENGTH_SHORT).show();
            return;
        }

        int sleepVal = getScoreFromToggleGroup(tgSleepQuality);
        int fatigueVal = getScoreFromToggleGroup(tgFatigue);
        int sorenessVal = getScoreFromToggleGroup(tgSoreness);
        int stressVal = getScoreFromToggleGroup(tgStress);
        int moodVal = getScoreFromToggleGroup(tgMood);

        if (sleepVal == 0 || fatigueVal == 0 || sorenessVal == 0 || stressVal == 0 || moodVal == 0) {
            Toast.makeText(this, "Bitte bewerte alle 5 Kategorien des subjektiven Befindens.", Toast.LENGTH_LONG).show();
            return;
        }

        pbCheckin.setVisibility(View.VISIBLE);
        btnSubmitCheckin.setEnabled(false);

        // Score von 5 bis 25 auf 0 bis 100 normieren
        int totalSum = sleepVal + fatigueVal + sorenessVal + stressVal + moodVal;
        int readinessScore = (int) (((totalSum - 5) / 20.0) * 100);

        String trafficLight;
        if (readinessScore < 40) {
            trafficLight = "Red";
        } else if (readinessScore < 70) {
            trafficLight = "Yellow";
        } else {
            trafficLight = "Green";
        }

        String userId = mAuth.getCurrentUser().getUid();
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String readinessDocId = userId + "_" + todayDate;

        DailyReadiness entry = new DailyReadiness();
        entry.setReadinessID(readinessDocId);
        entry.setUserID(userId);
        entry.setDate(todayDate);
        entry.setSubmittedAt(System.currentTimeMillis());
        entry.setSleepQuality(sleepVal);
        entry.setFatigue(fatigueVal);
        entry.setMuscleSoreness(sorenessVal);
        entry.setStressLevel(stressVal);
        entry.setMood(moodVal);
        entry.setReadinessScore(readinessScore);
        entry.setTrafficLight(trafficLight);

        String hrStr = etRestingHR.getText().toString().trim();
        if (!TextUtils.isEmpty(hrStr)) {
            entry.setRestingHeartRate(Integer.parseInt(hrStr));
        }

        String sleepHoursStr = etSleepHours.getText().toString().trim();
        if (!TextUtils.isEmpty(sleepHoursStr)) {
            entry.setSleepDurationHours(Double.parseDouble(sleepHoursStr));
        }

        String phase = spCyclePhase.getSelectedItem().toString();
        if (!"Bitte auswählen".equals(phase)) {
            entry.setMenstrualCyclePhase(phase);
        }
        entry.setCycleDataPrivate(switchCyclePrivate.isChecked());

        db.collection("readiness").document(readinessDocId).set(entry)
                .addOnSuccessListener(aVoid -> {
                    pbCheckin.setVisibility(View.GONE);
                    Toast.makeText(this, "Check-in gespeichert!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    pbCheckin.setVisibility(View.GONE);
                    btnSubmitCheckin.setEnabled(true);
                    Toast.makeText(this, "Fehler beim Speichern: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
