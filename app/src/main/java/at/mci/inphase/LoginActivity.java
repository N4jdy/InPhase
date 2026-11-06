package at.mci.inphase;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        Button btnGoToRegister = findViewById(R.id.btnGoToRegister);
        progressBar = findViewById(R.id.loginProgressBar);

        btnLogin.setOnClickListener(v -> performLogin());

        btnGoToRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Bitte E-Mail und Passwort eingeben.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);

            if (task.isSuccessful()) {
                Toast.makeText(LoginActivity.this, "Erfolgreich angemeldet!", Toast.LENGTH_SHORT).show();
                goToMainActivity();
            } else {
                String error = task.getException() != null ? task.getException().getMessage() : "Anmeldung fehlgeschlagen";
                Toast.makeText(LoginActivity.this, "Fehler: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void goToMainActivity() {
        if (mAuth.getCurrentUser() == null) return;
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);
        String userId = mAuth.getCurrentUser().getUid();
        
        FirebaseFirestore.getInstance().collection("users").document(userId).get()
            .addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    String role = task.getResult().getString("role");
                    Intent intent;
                    if ("Coach".equals(role)) {
                        intent = new Intent(LoginActivity.this, CoachMainActivity.class);
                    } else {
                        intent = new Intent(LoginActivity.this, AthleteMainActivity.class);
                    }
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Benutzerdaten konnten nicht geladen werden", Toast.LENGTH_SHORT).show();
                    mAuth.signOut();
                }
            });
    }
}