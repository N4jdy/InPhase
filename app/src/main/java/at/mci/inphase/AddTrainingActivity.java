package at.mci.inphase;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import at.mci.inphase.model.TrainingSession;

/**
 * Anlegen neuer Trainingseinheiten für Trainer (Coach).
 * Konfigurieren von Datum, Uhrzeit, Trainingstyp, Titel, Notizen und Wiederholungszyklen.
 */
public class AddTrainingActivity extends AppCompatActivity {

    private Spinner spTrainingType, spRecurrence;
    private Button btnSelectDate, btnSelectTime, btnSaveTraining;
    private TextView tvSelectedDateTime;
    private EditText etTrainingTitle, etTrainingNotes, etRecurrenceCount;

    private Calendar calendar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_training);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        calendar = Calendar.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etTrainingTitle = findViewById(R.id.etTrainingTitle);
        spTrainingType = findViewById(R.id.spTrainingType);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectTime = findViewById(R.id.btnSelectTime);
        tvSelectedDateTime = findViewById(R.id.tvSelectedDateTime);
        spRecurrence = findViewById(R.id.spRecurrence);
        etRecurrenceCount = findViewById(R.id.etRecurrenceCount);
        etTrainingNotes = findViewById(R.id.etTrainingNotes);
        btnSaveTraining = findViewById(R.id.btnSaveTraining);

        setupSpinners();
        updateDateTimeButtons();

        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnSelectTime.setOnClickListener(v -> showTimePicker());
        btnSaveTraining.setOnClickListener(v -> saveTrainingSession());
    }

    private void setupSpinners() {
        String[] types = {"Team_Training", "Match", "Gym", "Recovery", "Run"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        spTrainingType.setAdapter(typeAdapter);

        String[] recurrences = {"Einmalig", "Wöchentlich", "Monatlich"};
        ArrayAdapter<String> recurrenceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, recurrences);
        spRecurrence.setAdapter(recurrenceAdapter);

        spRecurrence.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    etRecurrenceCount.setVisibility(View.VISIBLE);
                } else {
                    etRecurrenceCount.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateTimeButtons();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            updateDateTimeButtons();
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private void updateDateTimeButtons() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm 'Uhr'", Locale.getDefault());

        btnSelectDate.setText(sdfDate.format(calendar.getTime()));
        btnSelectTime.setText(sdfTime.format(calendar.getTime()));

        SimpleDateFormat sdfFull = new SimpleDateFormat("EEEE, dd. MMMM yyyy - HH:mm 'Uhr'", Locale.GERMAN);
        tvSelectedDateTime.setText(sdfFull.format(calendar.getTime()));
    }

    private void saveTrainingSession() {
        String title = etTrainingTitle.getText().toString().trim();
        String notes = etTrainingNotes.getText().toString().trim();
        String type = spTrainingType.getSelectedItem().toString();
        String recurrence = spRecurrence.getSelectedItem().toString();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Bitte einen Titel eingeben", Toast.LENGTH_SHORT).show();
            return;
        }

        final int count;
        if (!"Einmalig".equals(recurrence)) {
            String countStr = etRecurrenceCount.getText().toString().trim();
            if (TextUtils.isEmpty(countStr)) {
                Toast.makeText(this, "Bitte die Anzahl der Wiederholungen eingeben", Toast.LENGTH_SHORT).show();
                return;
            }
            count = Math.max(1, Integer.parseInt(countStr));
        } else {
            count = 1;
        }

        if (mAuth.getCurrentUser() == null) return;
        String coachId = mAuth.getCurrentUser().getUid();

        btnSaveTraining.setEnabled(false);

        db.collection("users").document(coachId).get().addOnSuccessListener(doc -> {
            String teamId = doc.getString("teamID");
            if (teamId == null) {
                Toast.makeText(this, "Team nicht gefunden", Toast.LENGTH_SHORT).show();
                btnSaveTraining.setEnabled(true);
                return;
            }

            for (int stepIdx = 0; stepIdx < count; stepIdx++) {
                String sessionId = db.collection("trainings").document().getId();
                TrainingSession session = new TrainingSession();
                session.setSessionID(sessionId);
                session.setTeamID(teamId);

                Calendar sessionCal = (Calendar) calendar.clone();
                switch (recurrence) {
                    case "Wöchentlich":
                        sessionCal.add(Calendar.WEEK_OF_YEAR, stepIdx);
                        break;
                    case "Monatlich":
                        sessionCal.add(Calendar.MONTH, stepIdx);
                        break;
                    default:
                        break;
                }
                session.setDateTime(sessionCal.getTimeInMillis());

                session.setSessionType(type);
                session.setTitle(title);
                session.setNotes(notes);

                db.collection("trainings").document(sessionId).set(session);
            }

            Toast.makeText(this, "Einheit(en) gespeichert!", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            btnSaveTraining.setEnabled(true);
            Toast.makeText(this, "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
