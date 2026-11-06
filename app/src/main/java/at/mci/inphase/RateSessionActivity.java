package at.mci.inphase;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

import at.mci.inphase.model.Workload;

/**
 * Aktivität zur Bewertung absolvierter Einheiten durch Athleten.
 * Erfasst den RPE-Belastungswert (Rating of Perceived Exertion, Skala 1-10) und die Trainingsdauer.
 */
public class RateSessionActivity extends AppCompatActivity {

    private TextView tvRpeLabel;
    private MaterialButtonToggleGroup tgRpe;
    private EditText etDuration;
    private Button btnSaveWorkload;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_session);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Kein angemeldeter Benutzer gefunden.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        sessionId = getIntent().getStringExtra("sessionId");
        String trainingTitle = getIntent().getStringExtra("trainingTitle");

        TextView tvTrainingInfo = findViewById(R.id.tvTrainingInfo);
        tvRpeLabel = findViewById(R.id.tvRpeLabel);
        tgRpe = findViewById(R.id.tgRpe);
        etDuration = findViewById(R.id.etDuration);
        btnSaveWorkload = findViewById(R.id.btnSaveWorkload);

        if (trainingTitle != null) {
            tvTrainingInfo.setText(getString(R.string.training_title_format, trainingTitle));
        } else {
            tvTrainingInfo.setText("Training nicht gefunden");
        }

        tgRpe.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked && checkedId != View.NO_ID) {
                Button b = findViewById(checkedId);
                if (b != null) {
                    tvRpeLabel.setText(String.format(Locale.getDefault(), "Ausgewählt: %s", b.getText().toString()));
                }
            }
        });

        btnSaveWorkload.setOnClickListener(v -> saveWorkload());

        checkIfAlreadyRated();
    }

    private int getButtonIdForRpe(int rpe) {
        switch (rpe) {
            case 1: return R.id.rpe1;
            case 2: return R.id.rpe2;
            case 3: return R.id.rpe3;
            case 4: return R.id.rpe4;
            case 5: return R.id.rpe5;
            case 6: return R.id.rpe6;
            case 7: return R.id.rpe7;
            case 8: return R.id.rpe8;
            case 9: return R.id.rpe9;
            case 10: return R.id.rpe10;
            default: return 0;
        }
    }

    private void checkIfAlreadyRated() {
        if (mAuth.getCurrentUser() == null || sessionId == null) return;

        String userId = mAuth.getCurrentUser().getUid();
        String workloadId = sessionId + "_" + userId;

        db.collection("workloads").document(workloadId).get().addOnCompleteListener(task -> {
            if (isDestroyed() || isFinishing()) return;
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                // Bereits bewertet, blockiere die Eingabefelder
                btnSaveWorkload.setEnabled(false);
                btnSaveWorkload.setText("Bereits bewertet");

                int childCount = tgRpe.getChildCount();
                for (int childIdx = 0; childIdx < childCount; childIdx++) {
                    View child = tgRpe.getChildAt(childIdx);
                    if (child != null) {
                        child.setEnabled(false);
                    }
                }
                etDuration.setEnabled(false);

                Long rpe = task.getResult().getLong("rpeScore");
                Long duration = task.getResult().getLong("duration");
                if (rpe != null) {
                    tvRpeLabel.setText(String.format(Locale.getDefault(), "RPE Wert: %d", rpe));
                    int buttonId = getButtonIdForRpe(rpe.intValue());
                    if (buttonId != 0) {
                        tgRpe.check(buttonId);
                    }
                }
                if (duration != null) {
                    etDuration.setText(String.valueOf(duration));
                }
            }
        });
    }

    private void saveWorkload() {
        if (sessionId == null) {
            Toast.makeText(this, "Kein Training ausgewählt", Toast.LENGTH_SHORT).show();
            return;
        }

        String durationStr = etDuration.getText().toString().trim();
        if (TextUtils.isEmpty(durationStr)) {
            Toast.makeText(this, "Bitte Dauer eingeben", Toast.LENGTH_SHORT).show();
            return;
        }

        int checkedId = tgRpe.getCheckedButtonId();
        if (checkedId == View.NO_ID) {
            Toast.makeText(this, "Bitte RPE Wert auswählen", Toast.LENGTH_SHORT).show();
            return;
        }

        Button b = findViewById(checkedId);
        if (b == null) {
            Toast.makeText(this, "Bitte RPE Wert auswählen", Toast.LENGTH_SHORT).show();
            return;
        }

        int rpeScore;
        try {
            rpeScore = Integer.parseInt(b.getText().toString());
        } catch (NumberFormatException e) {
            return;
        }

        int duration = Integer.parseInt(durationStr);
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        String workloadId = sessionId + "_" + userId; // Ein User kann eine Session nur einmal bewerten

        Workload workload = new Workload(workloadId, sessionId, userId, rpeScore, duration);

        btnSaveWorkload.setEnabled(false);

        db.collection("workloads").document(workloadId).set(workload)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Bewertung gespeichert!", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                btnSaveWorkload.setEnabled(true);
            });
    }
}
