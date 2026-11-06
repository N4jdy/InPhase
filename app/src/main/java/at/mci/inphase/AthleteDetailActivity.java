package at.mci.inphase;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import at.mci.inphase.model.DailyReadiness;
import at.mci.inphase.model.Workload;
import at.mci.inphase.model.WorkloadItem;

public class AthleteDetailActivity extends AppCompatActivity {

    private TextView tvDetailInitials, tvAthleteDetailName, tvDetailSub;
    private TextView tvDetailKpiReadiness, tvDetailKpiCheckins, tvDetailKpiRpe;

    // Bar chart views
    private final TextView[] tvBarScores = new TextView[7];
    private final View[] barFills = new View[7];

    private TrendAdapter trendAdapter;
    private final List<DailyReadiness> readinessList = new ArrayList<>();

    private WorkloadAdapter workloadAdapter;
    private final List<WorkloadItem> workloadList = new ArrayList<>();

    private FirebaseFirestore db;
    private String athleteId;
    private String athleteName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_athlete_detail);

        db = FirebaseFirestore.getInstance();
        athleteId = getIntent().getStringExtra("athleteId");
        athleteName = getIntent().getStringExtra("athleteName");

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvDetailInitials = findViewById(R.id.tvDetailInitials);
        tvAthleteDetailName = findViewById(R.id.tvAthleteDetailName);
        tvDetailSub = findViewById(R.id.tvDetailSub);

        tvDetailKpiReadiness = findViewById(R.id.tvDetailKpiReadiness);
        tvDetailKpiCheckins = findViewById(R.id.tvDetailKpiCheckins);
        tvDetailKpiRpe = findViewById(R.id.tvDetailKpiRpe);

        initBarChartViews();

        if (athleteName != null && !athleteName.isEmpty()) {
            tvAthleteDetailName.setText(athleteName);
            tvDetailInitials.setText(getInitials(athleteName));
            tvDetailSub.setText("Athletin • InPhase Team");
        }

        RecyclerView rvAthleteReadiness = findViewById(R.id.rvAthleteReadiness);
        rvAthleteReadiness.setLayoutManager(new LinearLayoutManager(this));
        trendAdapter = new TrendAdapter(readinessList);
        rvAthleteReadiness.setAdapter(trendAdapter);

        RecyclerView rvAthleteWorkloads = findViewById(R.id.rvAthleteWorkloads);
        rvAthleteWorkloads.setLayoutManager(new LinearLayoutManager(this));
        workloadAdapter = new WorkloadAdapter(workloadList);
        rvAthleteWorkloads.setAdapter(workloadAdapter);

        loadAthleteData();
    }

    private void initBarChartViews() {
        tvBarScores[0] = findViewById(R.id.tvDetailBarScore1);
        tvBarScores[1] = findViewById(R.id.tvDetailBarScore2);
        tvBarScores[2] = findViewById(R.id.tvDetailBarScore3);
        tvBarScores[3] = findViewById(R.id.tvDetailBarScore4);
        tvBarScores[4] = findViewById(R.id.tvDetailBarScore5);
        tvBarScores[5] = findViewById(R.id.tvDetailBarScore6);
        tvBarScores[6] = findViewById(R.id.tvDetailBarScore7);

        barFills[0] = findViewById(R.id.detailBarFill1);
        barFills[1] = findViewById(R.id.detailBarFill2);
        barFills[2] = findViewById(R.id.detailBarFill3);
        barFills[3] = findViewById(R.id.detailBarFill4);
        barFills[4] = findViewById(R.id.detailBarFill5);
        barFills[5] = findViewById(R.id.detailBarFill6);
        barFills[6] = findViewById(R.id.detailBarFill7);
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

    private void loadAthleteData() {
        if (athleteId == null) return;

        // 1. Readiness abrufen
        db.collection("readiness").whereEqualTo("userID", athleteId)
          .get()
          .addOnSuccessListener(queryDocumentSnapshots -> {
              readinessList.clear();
              Map<String, DailyReadiness> dateMap = new HashMap<>();

              for (DocumentSnapshot doc : queryDocumentSnapshots) {
                  DailyReadiness r = doc.toObject(DailyReadiness.class);
                  if (r != null) {
                      readinessList.add(r);
                      if (r.getDate() != null) {
                          dateMap.put(r.getDate(), r);
                      }
                  }
              }

              readinessList.sort((a, b) -> {
                  if (a.getDate() == null || b.getDate() == null) return 0;
                  return b.getDate().compareTo(a.getDate());
              });

              trendAdapter.notifyDataSetChanged();
              updateReadinessKpisAndChart(dateMap);
          })
          .addOnFailureListener(e -> Toast.makeText(this, "Fehler Readiness: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        // 2. Workloads abrufen
        db.collection("workloads").whereEqualTo("userID", athleteId)
          .get()
          .addOnSuccessListener(queryDocumentSnapshots -> {
              workloadList.clear();
              for (DocumentSnapshot doc : queryDocumentSnapshots) {
                  Workload w = doc.toObject(Workload.class);
                  if (w != null) {
                      workloadList.add(new WorkloadItem(athleteName, w));
                  }
              }
              workloadAdapter.notifyDataSetChanged();

              if (!workloadList.isEmpty()) {
                  double totalRpe = queryDocumentSnapshots.getDocuments().stream()
                          .map(doc -> doc.toObject(Workload.class))
                          .mapToDouble(w -> w != null ? w.getRpeScore() : 0.0)
                          .sum();
                  double avgRpe = totalRpe / workloadList.size();
                  tvDetailKpiRpe.setText(String.format(Locale.getDefault(), "%.1f", avgRpe));
              } else {
                  tvDetailKpiRpe.setText("--");
              }
          })
          .addOnFailureListener(e -> Toast.makeText(this, "Fehler Workloads: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateReadinessKpisAndChart(Map<String, DailyReadiness> dateMap) {
        tvDetailKpiCheckins.setText(String.format(Locale.getDefault(), "%d", readinessList.size()));

        if (!readinessList.isEmpty()) {
            long totalScore = readinessList.stream().mapToLong(DailyReadiness::getReadinessScore).sum();
            long avg = totalScore / readinessList.size();
            tvDetailKpiReadiness.setText(String.format(Locale.getDefault(), "%d%%", avg));
        } else {
            tvDetailKpiReadiness.setText("--");
        }

        // Binde 7-Tage Wochen-Balkendiagramm (Montag bis Sonntag der aktuellen Woche)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        SimpleDateFormat dateKeySdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        float maxBarHeightPx = 90 * getResources().getDisplayMetrics().density;

        for (int dayIdx = 0; dayIdx < 7; dayIdx++) {
            Date dayDate = cal.getTime();
            String dateKey = dateKeySdf.format(dayDate);

            DailyReadiness readiness = dateMap.get(dateKey);
            if (readiness != null) {
                int score = readiness.getReadinessScore();
                tvBarScores[dayIdx].setText(String.format(Locale.getDefault(), "%d%%", score));

                int heightPx = Math.max(10, (int) (maxBarHeightPx * (score / 100.0f)));
                ViewGroup.LayoutParams params = barFills[dayIdx].getLayoutParams();
                params.height = heightPx;
                barFills[dayIdx].setLayoutParams(params);

                if (score >= 75) {
                    barFills[dayIdx].getBackground().setTint(Color.parseColor("#2D6A4F"));
                } else if (score >= 50) {
                    barFills[dayIdx].getBackground().setTint(Color.parseColor("#52B788"));
                } else {
                    barFills[dayIdx].getBackground().setTint(Color.parseColor("#74C69D"));
                }
            } else {
                tvBarScores[dayIdx].setText("--");
                ViewGroup.LayoutParams params = barFills[dayIdx].getLayoutParams();
                params.height = (int) (10 * getResources().getDisplayMetrics().density);
                barFills[dayIdx].setLayoutParams(params);
                barFills[dayIdx].getBackground().setTint(Color.parseColor("#EAF0EA"));
            }

            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
    }
}
