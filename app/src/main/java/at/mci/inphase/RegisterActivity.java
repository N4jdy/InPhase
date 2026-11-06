package at.mci.inphase;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

import at.mci.inphase.model.Team;
import at.mci.inphase.model.User;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etTeamCode, etTeamName;
    private RadioButton rbCoach;
    private Button btnRegister;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etTeamCode = findViewById(R.id.etTeamCode);
        etTeamName = findViewById(R.id.etTeamName);
        View tilTeamCode = findViewById(R.id.tilTeamCode);
        View tilTeamName = findViewById(R.id.tilTeamName);
        RadioGroup rgRole = findViewById(R.id.rgRole);
        rbCoach = findViewById(R.id.rbCoach);
        btnRegister = findViewById(R.id.btnRegister);
        Button btnBackToLogin = findViewById(R.id.btnBackToLogin);
        progressBar = findViewById(R.id.progressBar);

        // Dynamische Eingabefelder je nach Rolle umschalten
        rgRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbCoach) {
                tilTeamName.setVisibility(View.VISIBLE);
                tilTeamCode.setVisibility(View.GONE);
            } else {
                tilTeamName.setVisibility(View.GONE);
                tilTeamCode.setVisibility(View.VISIBLE);
            }
        });

        btnRegister.setOnClickListener(v -> performRegistration());
        btnBackToLogin.setOnClickListener(v -> finish());
    }

    private void performRegistration() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        boolean isCoach = rbCoach.isChecked();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Bitte fülle alle Pflichtfelder aus.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Passwort muss mind. 6 Zeichen lang sein.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        if (isCoach) {
            String teamName = etTeamName.getText().toString().trim();
            if (TextUtils.isEmpty(teamName)) {
                Toast.makeText(this, "Bitte gib einen Teamnamen an.", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                btnRegister.setEnabled(true);
                return;
            }
            registerCoachWithNewTeam(name, email, password, teamName);
        } else {
            String teamCode = etTeamCode.getText().toString().trim();
            if (TextUtils.isEmpty(teamCode)) {
                Toast.makeText(this, "Bitte gib den Team-Code ein.", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                btnRegister.setEnabled(true);
                return;
            }
            registerAthleteWithTeamCode(name, email, password, teamCode);
        }
    }

    private void registerCoachWithNewTeam(String name, String email, String password, String teamName) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null || task.getResult().getUser() == null) {
                showError(task.getException() != null ? task.getException().getMessage() : "Registrierung fehlgeschlagen");
                return;
            }

            String userId = task.getResult().getUser().getUid();
            String teamId = db.collection("teams").document().getId();
            String inviteCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

            Team newTeam = new Team(teamId, teamName, inviteCode);
            newTeam.getCoachID().add(userId);

            db.collection("teams").document(teamId).set(newTeam).addOnSuccessListener(aVoid -> {
                User user = new User(userId, name, email, "Coach", teamId);
                saveUserAndFinish(user);
            }).addOnFailureListener(e -> showError(e.getMessage()));
        });
    }

    private void registerAthleteWithTeamCode(String name, String email, String password, String teamCode) {
        // Prüfen, ob der Team-Code existiert
        db.collection("teams").whereEqualTo("inviteCode", teamCode).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null || task.getResult().isEmpty()) {
                showError("Ungültiger Team-Code.");
                return;
            }

            String teamId = task.getResult().getDocuments().get(0).getString("teamID");

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(authTask -> {
                if (!authTask.isSuccessful() || authTask.getResult() == null || authTask.getResult().getUser() == null) {
                    showError(authTask.getException() != null ? authTask.getException().getMessage() : "Registrierung fehlgeschlagen");
                    return;
                }

                String userId = authTask.getResult().getUser().getUid();
                User user = new User(userId, name, email, "Athlete", teamId);
                saveUserAndFinish(user);
            });
        });
    }

    private void saveUserAndFinish(User user) {
        db.collection("users").document(user.getUserID()).set(user).addOnSuccessListener(aVoid -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(RegisterActivity.this, "Erfolgreich registriert!", Toast.LENGTH_SHORT).show();
            // Weiterleitung zur entsprechenden Rolle
            Intent intent;
            if ("Coach".equals(user.getRole())) {
                intent = new Intent(RegisterActivity.this, CoachMainActivity.class);
            } else {
                intent = new Intent(RegisterActivity.this, AthleteMainActivity.class);
            }
            startActivity(intent);
            finish();
        }).addOnFailureListener(e -> showError(e.getMessage()));
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        btnRegister.setEnabled(true);
        Toast.makeText(this, "Fehler: " + message, Toast.LENGTH_LONG).show();
    }
}
