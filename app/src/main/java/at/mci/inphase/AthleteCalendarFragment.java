package at.mci.inphase;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import at.mci.inphase.model.TrainingSession;

public class AthleteCalendarFragment extends Fragment {

    private TextView tvMonthTitle;
    private TextView tvSelectedDateLabel;
    private MaterialCardView cardCalendarEmpty;

    private MonthCalendarAdapter monthAdapter;

    private TrainingAdapter trainingAdapter;
    private final List<TrainingSession> allTrainings = new ArrayList<>();
    private final List<TrainingSession> dailyTrainings = new ArrayList<>();

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Calendar monthCalendar;
    private Calendar selectedDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_athlete_calendar, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        monthCalendar = Calendar.getInstance();
        selectedDate = Calendar.getInstance();

        tvMonthTitle = view.findViewById(R.id.tvMonthTitle);
        tvSelectedDateLabel = view.findViewById(R.id.tvSelectedDateLabel);
        cardCalendarEmpty = view.findViewById(R.id.cardCalendarEmpty);

        ImageButton btnPrevMonth = view.findViewById(R.id.btnPrevMonth);
        ImageButton btnNextMonth = view.findViewById(R.id.btnNextMonth);

        btnPrevMonth.setOnClickListener(v -> {
            monthCalendar.add(Calendar.MONTH, -1);
            updateMonthCalendar();
        });

        btnNextMonth.setOnClickListener(v -> {
            monthCalendar.add(Calendar.MONTH, 1);
            updateMonthCalendar();
        });

        RecyclerView rvMonthGrid = view.findViewById(R.id.rvMonthGrid);
        rvMonthGrid.setLayoutManager(new GridLayoutManager(getContext(), 7));

        monthAdapter = new MonthCalendarAdapter(monthCalendar, selectedDate, allTrainings, clickedDate -> {
            selectedDate = (Calendar) clickedDate.clone();
            updateSelectedDateLabel();
            filterTrainingsForSelectedDate();
        });
        rvMonthGrid.setAdapter(monthAdapter);

        RecyclerView rvAthleteTrainings = view.findViewById(R.id.rvAthleteTrainings);
        rvAthleteTrainings.setLayoutManager(new LinearLayoutManager(getContext()));
        trainingAdapter = new TrainingAdapter(dailyTrainings);
        rvAthleteTrainings.setAdapter(trainingAdapter);

        // Click training item -> Open RateSessionActivity
        rvAthleteTrainings.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), rvAthleteTrainings, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position >= 0 && position < dailyTrainings.size()) {
                    TrainingSession session = dailyTrainings.get(position);
                    Intent intent = new Intent(getActivity(), RateSessionActivity.class);
                    intent.putExtra("sessionId", session.getSessionID());
                    intent.putExtra("trainingTitle", session.getTitle());
                    startActivity(intent);
                }
            }

            @Override
            public void onLongItemClick(View view, int position) {}
        }));

        loadTrainings();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTrainings();
    }

    private void updateMonthCalendar() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.GERMAN);
        tvMonthTitle.setText(sdf.format(monthCalendar.getTime()));
        monthAdapter.updateData(monthCalendar, selectedDate, allTrainings);
    }

    private void updateSelectedDateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("d. MMMM", Locale.GERMAN);
        String formattedDate = sdf.format(selectedDate.getTime());
        tvSelectedDateLabel.setText(getString(R.string.calendar_selected_date_format, formattedDate));
    }

    private void loadTrainings() {
        if (mAuth.getCurrentUser() == null) return;
        String athleteId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(athleteId).get().addOnSuccessListener(athleteDoc -> {
            if (!athleteDoc.exists()) return;
            String teamId = athleteDoc.getString("teamID");

            if (teamId != null) {
                db.collection("trainings")
                  .whereEqualTo("teamID", teamId)
                  .get()
                  .addOnSuccessListener(queryDocumentSnapshots -> {
                      allTrainings.clear();
                      for (DocumentSnapshot doc : queryDocumentSnapshots) {
                          TrainingSession session = doc.toObject(TrainingSession.class);
                          if (session != null) {
                              allTrainings.add(session);
                          }
                      }
                      allTrainings.sort(Comparator.comparingLong(TrainingSession::getDateTime));

                      updateMonthCalendar();
                      updateSelectedDateLabel();
                      filterTrainingsForSelectedDate();
                  })
                  .addOnFailureListener(e -> {
                      if (getContext() != null) {
                          Toast.makeText(getContext(), "Fehler beim Laden: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                      }
                  });
            }
        });
    }

    private void filterTrainingsForSelectedDate() {
        dailyTrainings.clear();
        for (TrainingSession session : allTrainings) {
            Calendar sessionCal = Calendar.getInstance();
            sessionCal.setTimeInMillis(session.getDateTime());

            if (sessionCal.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                sessionCal.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR)) {
                dailyTrainings.add(session);
            }
        }
        trainingAdapter.notifyDataSetChanged();

        if (dailyTrainings.isEmpty()) {
            cardCalendarEmpty.setVisibility(View.VISIBLE);
        } else {
            cardCalendarEmpty.setVisibility(View.GONE);
        }
    }
}
