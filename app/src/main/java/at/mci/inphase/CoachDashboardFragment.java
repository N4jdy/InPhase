package at.mci.inphase;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import at.mci.inphase.model.AthleteStatusItem;
import at.mci.inphase.model.User;

public class CoachDashboardFragment extends Fragment {

    private TextView tvTeamReadinessStatus;
    private TextView tvTeamReadinessSubtext;
    private CircularProgressIndicator progressTeamReadiness;
    private TextView tvTeamScoreValue;

    private TextView tvStatGreen, tvStatYellow, tvStatRed;
    private AthleteAdapter athleteAdapter;
    private final List<AthleteStatusItem> athleteList = new ArrayList<>();

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

        tvStatGreen = view.findViewById(R.id.tvStatGreen);
        tvStatYellow = view.findViewById(R.id.tvStatYellow);
        tvStatRed = view.findViewById(R.id.tvStatRed);
        RecyclerView rvAthletes = view.findViewById(R.id.rvAthletes);

        rvAthletes.setLayoutManager(new LinearLayoutManager(getContext()));
        athleteAdapter = new AthleteAdapter(athleteList, athlete -> {
            Intent intent = new Intent(getActivity(), AthleteDetailActivity.class);
            intent.putExtra("athleteId", athlete.getUserID());
            intent.putExtra("athleteName", athlete.getName());
            startActivity(intent);
        });
        rvAthletes.setAdapter(athleteAdapter);

        loadDashboardData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData();
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
                    athleteList.clear();
                    List<User> athletes = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        User athlete = doc.toObject(User.class);
                        if (athlete != null) {
                            athletes.add(athlete);
                            athleteList.add(new AthleteStatusItem(athlete, null, null));
                        }
                    }
                    athleteAdapter.notifyDataSetChanged();

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

                    athleteList.get(index).setTodayTrafficLight(trafficLight);
                    athleteList.get(index).setTodayScore(score);
                }
                
                athleteAdapter.notifyItemChanged(index);

                completedQueries[0]++;
                if (completedQueries[0] == athletes.size()) {
                    updateStatistics();
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

        for (AthleteStatusItem item : athleteList) {
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

        int totalAthletes = athleteList.size();
        tvStatGreen.setText(String.format(Locale.getDefault(), "%d", greenCount));
        tvStatYellow.setText(String.format(Locale.getDefault(), "%d", yellowCount));
        tvStatRed.setText(String.format(Locale.getDefault(), "%d", redCount));

        if (totalSubmitted > 0) {
            int avgTeamScore = (int) (totalScoreSum / totalSubmitted);
            progressTeamReadiness.setProgressCompat(avgTeamScore, true);
            tvTeamScoreValue.setText(String.format(Locale.getDefault(), "%d%%", avgTeamScore));
            tvTeamReadinessStatus.setText(String.format(Locale.getDefault(), "%d / %d Check-ins erfasst", totalSubmitted, totalAthletes));
            tvTeamReadinessSubtext.setText(String.format(Locale.getDefault(), "Ø Readiness: %d%% • %d Grün, %d Gelb, %d Rot", avgTeamScore, greenCount, yellowCount, redCount));
        } else {
            progressTeamReadiness.setProgressCompat(0, true);
            tvTeamScoreValue.setText("--");
            tvTeamReadinessStatus.setText(String.format(Locale.getDefault(), "0 / %d Check-ins erfasst", totalAthletes));
            tvTeamReadinessSubtext.setText("Noch keine Check-ins für heute erfasst");
        }
    }
}
