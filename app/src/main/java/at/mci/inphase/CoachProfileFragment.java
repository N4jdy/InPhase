package at.mci.inphase;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CoachProfileFragment extends Fragment {

    private TextView tvHeaderInitials, tvHeaderName, tvHeaderRoleEmail;
    private TextView tvStat1Value, tvStat1Label, tvStat2Value, tvStat2Label, tvStat3Value, tvStat3Label;

    private EditText etProfileName, etProfileHeight, etProfileWeight;
    private TextView tvProfileEmail;
    private Button btnProfileBirthDate, btnProfileSave;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Calendar birthDateCalendar;
    private long birthDateTimestamp = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        birthDateCalendar = Calendar.getInstance();

        tvHeaderInitials = view.findViewById(R.id.tvHeaderInitials);
        tvHeaderName = view.findViewById(R.id.tvHeaderName);
        tvHeaderRoleEmail = view.findViewById(R.id.tvHeaderRoleEmail);

        tvStat1Value = view.findViewById(R.id.tvStat1Value);
        tvStat1Label = view.findViewById(R.id.tvStat1Label);
        tvStat2Value = view.findViewById(R.id.tvStat2Value);
        tvStat2Label = view.findViewById(R.id.tvStat2Label);
        tvStat3Value = view.findViewById(R.id.tvStat3Value);
        tvStat3Label = view.findViewById(R.id.tvStat3Label);

        etProfileName = view.findViewById(R.id.etProfileName);
        etProfileHeight = view.findViewById(R.id.etProfileHeight);
        etProfileWeight = view.findViewById(R.id.etProfileWeight);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        btnProfileBirthDate = view.findViewById(R.id.btnProfileBirthDate);
        btnProfileSave = view.findViewById(R.id.btnProfileSave);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        // Labels for Coach
        tvStat1Label.setText(getString(R.string.profile_stat_role));
        tvStat2Label.setText(getString(R.string.profile_stat_team_code));
        tvStat3Label.setText(getString(R.string.profile_stat_athletes_count));

        loadUserProfile();

        btnProfileBirthDate.setOnClickListener(v -> showDatePicker());
        btnProfileSave.setOnClickListener(v -> saveUserProfile());
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }

    private void loadUserProfile() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String name = doc.getString("name");
                String email = doc.getString("email");
                String teamId = doc.getString("teamID");
                Long bDate = doc.getLong("birthDate");
                Long height = doc.getLong("height");
                Double weight = doc.getDouble("weight");

                if (name != null) {
                    etProfileName.setText(name);
                    tvHeaderName.setText(name);
                    tvHeaderInitials.setText(getInitials(name));
                }

                if (email != null) {
                    tvProfileEmail.setText(email);
                    tvHeaderRoleEmail.setText(getString(R.string.profile_role_coach_format, email));
                }

                tvStat1Value.setText("Coach");

                if (teamId != null) {
                    db.collection("teams").document(teamId).get().addOnSuccessListener(teamDoc -> {
                        if (teamDoc.exists()) {
                            String inviteCode = teamDoc.getString("inviteCode");
                            tvStat2Value.setText(inviteCode != null ? inviteCode : "N/A");
                        }
                    });

                    db.collection("users").whereEqualTo("teamID", teamId).whereEqualTo("role", "Athlete")
                      .get().addOnSuccessListener(athletes -> {
                          tvStat3Value.setText(String.valueOf(athletes.size()));
                      });
                } else {
                    tvStat2Value.setText("--");
                    tvStat3Value.setText("0");
                }

                if (bDate != null && bDate > 0) {
                    birthDateTimestamp = bDate;
                    birthDateCalendar.setTimeInMillis(bDate);
                    updateBirthDateButtonText();
                }

                if (height != null && height > 0) {
                    etProfileHeight.setText(Long.toString(height));
                }

                if (weight != null && weight > 0) {
                    etProfileWeight.setText(String.valueOf(weight));
                }
            }
        });
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        if (parts.length > 0 && !parts[0].isEmpty()) {
            initials.append(parts[0].substring(0, 1).toUpperCase(Locale.getDefault()));
        }
        if (parts.length > 1 && !parts[1].isEmpty()) {
            initials.append(parts[1].substring(0, 1).toUpperCase(Locale.getDefault()));
        }
        return initials.toString();
    }

    private void showDatePicker() {
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            birthDateCalendar.set(Calendar.YEAR, year);
            birthDateCalendar.set(Calendar.MONTH, month);
            birthDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            birthDateTimestamp = birthDateCalendar.getTimeInMillis();
            updateBirthDateButtonText();
        }, birthDateCalendar.get(Calendar.YEAR), birthDateCalendar.get(Calendar.MONTH), birthDateCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateBirthDateButtonText() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        btnProfileBirthDate.setText(sdf.format(birthDateCalendar.getTime()));
    }

    private void saveUserProfile() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        String name = etProfileName.getText().toString().trim();
        String heightStr = etProfileHeight.getText().toString().trim();
        String weightStr = etProfileWeight.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getContext(), "Name darf nicht leer sein", Toast.LENGTH_SHORT).show();
            return;
        }

        int height = TextUtils.isEmpty(heightStr) ? 0 : Integer.parseInt(heightStr);
        double weight = TextUtils.isEmpty(weightStr) ? 0.0 : Double.parseDouble(weightStr);

        btnProfileSave.setEnabled(false);

        db.collection("users").document(userId)
                .update(
                        "name", name,
                        "birthDate", birthDateTimestamp,
                        "height", height,
                        "weight", weight
                )
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Profil aktualisiert!", Toast.LENGTH_SHORT).show();
                        btnProfileSave.setEnabled(true);
                        loadUserProfile();
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnProfileSave.setEnabled(true);
                    }
                });
    }
}
