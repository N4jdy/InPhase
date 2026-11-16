package at.mci.inphase;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import at.mci.inphase.model.AthleteStatusItem;
import at.mci.inphase.model.User;

/**
 * Trainer-Dashboard (CoachDashboardFragment).
 * Zeigt die Team-Readiness im zentralen Ring, die horizontale Status-Filterleiste (Alle, Bereit, Eingeschränkt, Pause),
 * sowie die sortierte Athletenliste mit ausstehenden Check-in-Erinnerungen.
 */
public class CoachDashboardFragment extends Fragment {

    private TextView tvTeamReadinessStatus;
    private TextView tvTeamReadinessSubtext;
    private CircularProgressIndicator progressTeamReadiness;
    private TextView tvTeamScoreValue;

    // Filter Chips
    private LinearLayout chipFilterAll, chipFilterGreen, chipFilterYellow, chipFilterRed;
    private TextView tvFilterAllTitle, tvFilterAllBadge;
    private TextView tvFilterGreenTitle, tvFilterYellowTitle, tvFilterRedTitle;
    private TextView tvDisplayedCountBadge, tvResetFilterLink;

    private MaterialCardView cardPendingCheckins;
    private TextView tvPendingCountBadge, tvPendingSubtext;

    private AthleteAdapter athleteAdapter;
    private final List<AthleteStatusItem> fullAthleteList = new ArrayList<>();
    private final List<AthleteStatusItem> displayedAthleteList = new ArrayList<>();

    private String selectedFilter = null; // null = Alle, "Green", "Yellow", "Red"

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coach_dashboard, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvTeamReadinessStatus = view.findViewById(R.id.tvTeamReadinessStatus);
        tvTeamReadinessSubtext = view.findViewById(R.id.tvTeamReadinessSubtext);
        progressTeamReadiness = view.findViewById(R.id.progressTeamReadiness);
        tvTeamScoreValue = view.findViewById(R.id.tvTeamScoreValue);

        // Chips
        chipFilterAll = view.findViewById(R.id.chipFilterAll);
        chipFilterGreen = view.findViewById(R.id.chipFilterGreen);
        chipFilterYellow = view.findViewById(R.id.chipFilterYellow);
        chipFilterRed = view.findViewById(R.id.chipFilterRed);

        tvFilterAllTitle = view.findViewById(R.id.tvFilterAllTitle);
        tvFilterAllBadge = view.findViewById(R.id.tvFilterAllBadge);
        tvFilterGreenTitle = view.findViewById(R.id.tvFilterGreenTitle);
        tvFilterYellowTitle = view.findViewById(R.id.tvFilterYellowTitle);
        tvFilterRedTitle = view.findViewById(R.id.tvFilterRedTitle);

        tvDisplayedCountBadge = view.findViewById(R.id.tvDisplayedCountBadge);
        tvResetFilterLink = view.findViewById(R.id.tvResetFilterLink);

        cardPendingCheckins = view.findViewById(R.id.cardPendingCheckins);
        tvPendingCountBadge = view.findViewById(R.id.tvPendingCountBadge);
        tvPendingSubtext = view.findViewById(R.id.tvPendingSubtext);
        MaterialButton btnRemindPending = view.findViewById(R.id.btnRemindPending);

        RecyclerView rvAthletes = view.findViewById(R.id.rvAthletes);
        rvAthletes.setLayoutManager(new LinearLayoutManager(getContext()));
        athleteAdapter = new AthleteAdapter(displayedAthleteList, athlete -> {
            Intent intent = new Intent(getActivity(), AthleteDetailActivity.class);
            intent.putExtra("athleteId", athlete.getUserID());
            intent.putExtra("athleteName", athlete.getName());
            startActivity(intent);
        });
        rvAthletes.setAdapter(athleteAdapter);

        // Click Listeners für Filter Chips
        chipFilterAll.setOnClickListener(v -> toggleFilter(null));
        chipFilterGreen.setOnClickListener(v -> toggleFilter("Green"));
        chipFilterYellow.setOnClickListener(v -> toggleFilter("Yellow"));
        chipFilterRed.setOnClickListener(v -> toggleFilter("Red"));
        tvResetFilterLink.setOnClickListener(v -> toggleFilter(null));

        btnRemindPending.setOnClickListener(v -> {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Erinnerung an ausstehende Athletinnen gesendet! 🔔", Toast.LENGTH_SHORT).show();
            }
        });

        loadDashboardData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void toggleFilter(String trafficLight) {
        if (Objects.equals(trafficLight, selectedFilter)) {
            selectedFilter = null; // Toggle off -> back to all
        } else {
            selectedFilter = trafficLight;
        }
        applyCurrentFilter();
    }

    private void applyCurrentFilter() {
        displayedAthleteList.clear();

        // Reset all chip styles to white outlined
        chipFilterAll.setBackgroundResource(R.drawable.bg_chip_white_outlined);
        tvFilterAllTitle.setTextColor(Color.parseColor("#334155"));

        chipFilterGreen.setBackgroundResource(R.drawable.bg_chip_white_outlined);
        tvFilterGreenTitle.setTextColor(Color.parseColor("#334155"));

        chipFilterYellow.setBackgroundResource(R.drawable.bg_chip_white_outlined);
        tvFilterYellowTitle.setTextColor(Color.parseColor("#334155"));

        chipFilterRed.setBackgroundResource(R.drawable.bg_chip_white_outlined);
        tvFilterRedTitle.setTextColor(Color.parseColor("#334155"));

        // Highlight selected chip
        if (selectedFilter == null) {
            chipFilterAll.setBackgroundResource(R.drawable.bg_chip_dark_active);
            tvFilterAllTitle.setTextColor(Color.WHITE);
            displayedAthleteList.addAll(fullAthleteList);
        } else {
            for (AthleteStatusItem item : fullAthleteList) {
                if (Objects.equals(selectedFilter, item.getTodayTrafficLight())) {
                    displayedAthleteList.add(item);
                }
            }

            switch (selectedFilter) {
                case "Green":
                    chipFilterGreen.setBackgroundResource(R.drawable.bg_chip_dark_active);
                    tvFilterGreenTitle.setTextColor(Color.WHITE);
                    break;
                case "Yellow":
                    chipFilterYellow.setBackgroundResource(R.drawable.bg_chip_dark_active);
                    tvFilterYellowTitle.setTextColor(Color.WHITE);
                    break;
                case "Red":
                    chipFilterRed.setBackgroundResource(R.drawable.bg_chip_dark_active);
                    tvFilterRedTitle.setTextColor(Color.WHITE);
                    break;
            }
        }

        // Sort score descending
        displayedAthleteList.sort((a, b) -> {
            Long scoreA = a.getTodayScore();
            Long scoreB = b.getTodayScore();
            if (scoreA == null && scoreB == null) return 0;
            if (scoreA == null) return 1;
            if (scoreB == null) return -1;
            return Long.compare(scoreB, scoreA);
        });

        tvDisplayedCountBadge.setText(String.format(Locale.getDefault(), "%d angezeigt", displayedAthleteList.size()));
        athleteAdapter.notifyDataSetChanged();
    }

    private void loadDashboardData() {
        if (mAuth.getCurrentUser() == null) return;
        String coachId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(coachId).get().addOnSuccessListener(coachDoc -> {
            if (!coachDoc.exists()) return;
            String teamId = coachDoc.getString("teamID");

            loadAthletesAndStatus(teamId);
        });
    }

    private void loadAthletesAndStatus(String teamId) {
        if (teamId == null) return;

        db.collection("users")
                .whereEqualTo("teamID", teamId)
                .whereEqualTo("role", "Athlete")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    fullAthleteList.clear();
                    List<User> athletes = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        User athlete = doc.toObject(User.class);
                        if (athlete != null) {
                            athletes.add(athlete);
                            fullAthleteList.add(new AthleteStatusItem(athlete, null, null));
                        }
                    }
                    applyCurrentFilter();

                    fetchTodayReadinessForAthletes(athletes);
                });
    }

    private void fetchTodayReadinessForAthletes(List<User> athletes) {
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (athletes.isEmpty()) {
            updateStatistics();
            return;
        }

        final int[] completedQueries = {0};

        int athleteIndex = 0;
        for (User athlete : athletes) {
            final int index = athleteIndex++;
            String readinessDocId = athlete.getUserID() + "_" + todayDate;

            db.collection("readiness").document(readinessDocId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    DocumentSnapshot doc = task.getResult();
                    String trafficLight = doc.getString("trafficLight");
                    Long score = doc.getLong("readinessScore");

                    fullAthleteList.get(index).setTodayTrafficLight(trafficLight);
                    fullAthleteList.get(index).setTodayScore(score);
                }

                completedQueries[0]++;
                if (completedQueries[0] == athletes.size()) {
                    updateStatistics();
                    applyCurrentFilter();
                }
            });
        }
    }

    private void updateStatistics() {
        int greenCount = 0;
        int yellowCount = 0;
        int redCount = 0;
        long totalScoreSum = 0;
        int totalSubmitted = 0;

        for (AthleteStatusItem item : fullAthleteList) {
            if (item.getTodayTrafficLight() != null) {
                totalSubmitted++;
                if (item.getTodayScore() != null) {
                    totalScoreSum += item.getTodayScore();
                }
                switch (item.getTodayTrafficLight()) {
                    case "Green": greenCount++; break;
                    case "Yellow": yellowCount++; break;
                    case "Red": redCount++; break;
                }
            }
        }

        int totalAthletes = fullAthleteList.size();
        int pendingCount = totalAthletes - totalSubmitted;

        tvFilterAllBadge.setText(String.format(Locale.getDefault(), "%d", totalAthletes));
        tvFilterGreenTitle.setText(String.format(Locale.getDefault(), "%d Bereit", greenCount));
        tvFilterYellowTitle.setText(String.format(Locale.getDefault(), "%d Eingeschränkt", yellowCount));
        tvFilterRedTitle.setText(String.format(Locale.getDefault(), "%d Pause", redCount));

        if (totalSubmitted > 0) {
            int avgTeamScore = (int) (totalScoreSum / totalSubmitted);
            progressTeamReadiness.setProgressCompat(avgTeamScore, true);
            tvTeamScoreValue.setText(String.format(Locale.getDefault(), "%d%%", avgTeamScore));
            tvTeamReadinessStatus.setText(String.format(Locale.getDefault(), "%d / %d Athleten erfasst", totalSubmitted, totalAthletes));
            tvTeamReadinessSubtext.setText(String.format(Locale.getDefault(), "Ø Readiness: %d%% • %d Grün, %d Gelb", avgTeamScore, greenCount, yellowCount));
        } else {
            progressTeamReadiness.setProgressCompat(0, true);
            tvTeamScoreValue.setText("--");
            tvTeamReadinessStatus.setText(String.format(Locale.getDefault(), "0 / %d Athleten erfasst", totalAthletes));
            tvTeamReadinessSubtext.setText("Noch keine Check-ins für heute erfasst");
        }

        // Steuere die Karte für ausstehende Check-ins
        if (pendingCount > 0) {
            cardPendingCheckins.setVisibility(View.VISIBLE);
            tvPendingCountBadge.setText(String.format(Locale.getDefault(), "%d", pendingCount));
            tvPendingSubtext.setText(String.format(Locale.getDefault(), "%d %s stehen heute noch aus",
                    pendingCount, pendingCount == 1 ? "Eintrag" : "Einträge"));
        } else {
            cardPendingCheckins.setVisibility(View.GONE);
        }
    }
}
