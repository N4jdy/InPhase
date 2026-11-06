package at.mci.inphase;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            checkUserRoleAndNavigate(mAuth.getCurrentUser().getUid());
            return;
        }

        setupWelcomeView();
    }

    private void setupWelcomeView() {
        setContentView(R.layout.activity_welcome);

        Button btnRegister = findViewById(R.id.btnWelcomeRegister);
        Button btnLogin = findViewById(R.id.btnWelcomeLogin);

        btnRegister.setOnClickListener(v -> startActivity(new Intent(WelcomeActivity.this, RegisterActivity.class)));
        btnLogin.setOnClickListener(v -> startActivity(new Intent(WelcomeActivity.this, LoginActivity.class)));
    }

    private void checkUserRoleAndNavigate(String userId) {
        FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (isDestroyed() || isFinishing()) return;
                    if (doc.exists()) {
                        String role = doc.getString("role");
                        Intent intent;
                        if ("Coach".equals(role)) {
                            intent = new Intent(WelcomeActivity.this, CoachMainActivity.class);
                        } else {
                            intent = new Intent(WelcomeActivity.this, AthleteMainActivity.class);
                        }
                        startActivity(intent);
                        finish();
                    } else {
                        setupWelcomeView();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isDestroyed() || isFinishing()) return;
                    setupWelcomeView();
                });
    }
}
