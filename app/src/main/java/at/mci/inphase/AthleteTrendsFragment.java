package at.mci.inphase;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
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

public class AthleteTrendsFragment extends Fragment {

    private TextView tvKpiReadiness;
    private TextView tvKpiTrainings;
    private TextView tvKpiRpe;
    private TextView tvKpiCheckins;

    // Bar chart views arrays
    private final TextView[] tvBarScores = new TextView[7];
    private final View[] barFills = new View[7];
    private final TextView[] tvBarDays = new TextView[7];

    private TrendAdapter trendAdapter;
    private final List<DailyReadiness> trendList = new ArrayList<>();

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_athlete_trends, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvKpiReadiness = view.findViewById(R.id.tvKpiReadiness);
        tvKpiTrainings = view.findViewById(R.id.tvKpiTrainings);
        tvKpiRpe = view.findViewById(R.id.tvKpiRpe);
        tvKpiCheckins = view.findViewById(R.id.tvKpiCheckins);

        initBarChartViews(view);

        RecyclerView rvTrends = view.findViewById(R.id.rvTrends);
        rvTrends.setLayoutManager(new LinearLayoutManager(getContext()));
        trendAdapter = new TrendAdapter(trendList);
        rvTrends.setAdapter(trendAdapter);

        loadTrends();

        return view;
    }

    private void initBarChartViews(View view) {
        tvBarScores[0] = view.findViewById(R.id.tvBarScore1);
        tvBarScores[1] = view.findViewById(R.id.tvBarScore2);
        tvBarScores[2] = view.findViewById(R.id.tvBarScore3);
        tvBarScores[3] = view.findViewById(R.id.tvBarScore4);
        tvBarScores[4] = view.findViewById(R.id.tvBarScore5);
        tvBarScores[5] = view.findViewById(R.id.tvBarScore6);
        tvBarScores[6] = view.findViewById(R.id.tvBarScore7);

        barFills[0] = view.findViewById(R.id.barFill1);
        barFills[1] = view.findViewById(R.id.barFill2);
        barFills[2] = view.findViewById(R.id.barFill3);
        barFills[3] = view.findViewById(R.id.barFill4);
        barFills[4] = view.findViewById(R.id.barFill5);
        barFills[5] = view.findViewById(R.id.barFill6);
        barFills[6] = view.findViewById(R.id.barFill7);

        tvBarDays[0] = view.findViewById(R.id.tvBarDay1);
        tvBarDays[1] = view.findViewById(R.id.tvBarDay2);
        tvBarDays[2] = view.findViewById(R.id.tvBarDay3);
        tvBarDays[3] = view.findViewById(R.id.tvBarDay4);
        tvBarDays[4] = view.findViewById(R.id.tvBarDay5);
        tvBarDays[5] = view.findViewById(R.id.tvBarDay6);
        tvBarDays[6] = view.findViewById(R.id.tvBarDay7);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTrends();
    }

    private void loadTrends() {
        if (mAuth.getCurrentUser() == null) return;
        String athleteId = mAuth.getCurrentUser().getUid();

        // 1. Lade Readiness Einträge
        db.collection("readiness")
          .whereEqualTo("userID", athleteId)
          .get()
          .addOnSuccessListener(queryDocumentSnapshots -> {
              trendList.clear();
              Map<String, DailyReadiness> dateMap = new HashMap<>();

              for (DocumentSnapshot doc : queryDocumentSnapshots) {
                  DailyReadiness readiness = doc.toObject(DailyReadiness.class);
                  if (readiness != null) {
                      trendList.add(readiness);
                      if (readiness.getDate() != null) {
                          dateMap.put(readiness.getDate(), readiness);
                      }
                  }
              }

              // Lokal nach Datum absteigend sortieren
              trendList.sort((r1, r2) -> {
                  if (r1.getDate() == null || r2.getDate() == null) return 0;
                  return r2.getDate().compareTo(r1.getDate());
              });

              trendAdapter.notifyDataSetChanged();

              updateReadinessKpisAndChart(dateMap);
          })
          .addOnFailureListener(e -> {
              if (getContext() != null) {
                  Toast.makeText(getContext(), "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show();
              }
          });

        // 2. Lade Workload Einträge für Workload KPIs
        db.collection("workloads")
          .whereEqualTo("userID", athleteId)
          .get()
          .addOnSuccessListener(workloadSnaps -> {
              int totalTrainings = workloadSnaps.size();
              tvKpiTrainings.setText(String.format(Locale.getDefault(), "%d", totalTrainings));

              if (totalTrainings > 0) {
                  double totalRpe = workloadSnaps.getDocuments().stream()
                          .map(doc -> doc.toObject(Workload.class))
                          .mapToDouble(w -> w != null ? w.getRpeScore() : 0.0)
                          .sum();
                  double avgRpe = totalRpe / totalTrainings;
                  tvKpiRpe.setText(String.format(Locale.getDefault(), "%.1f", avgRpe));
              } else {
                  tvKpiRpe.setText("--");
              }
          });
    }

    private void updateReadinessKpisAndChart(Map<String, DailyReadiness> dateMap) {
        tvKpiCheckins.setText(String.format(Locale.getDefault(), "%d", trendList.size()));

        if (!trendList.isEmpty()) {
            long totalScore = trendList.stream().mapToLong(DailyReadiness::getReadinessScore).sum();
            long avg = totalScore / trendList.size();
            tvKpiReadiness.setText(String.format(Locale.getDefault(), "%d%%", avg));
        } else {
            tvKpiReadiness.setText("--");
        }

        // Binde 7-Tage Wochen-Balkendiagramm (Montag bis Sonntag der aktuellen Woche)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        SimpleDateFormat dayNameSdf = new SimpleDateFormat("EEE", Locale.GERMAN);
        SimpleDateFormat dateKeySdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        float maxBarHeightPx = 110 * getResources().getDisplayMetrics().density;

        for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
            Date dayDate = cal.getTime();
            String dayName = dayNameSdf.format(dayDate).replace(".", "");
            String dateKey = dateKeySdf.format(dayDate);

            tvBarDays[dayIndex].setText(dayName);

            DailyReadiness readiness = dateMap.get(dateKey);
            if (readiness != null) {
                int score = readiness.getReadinessScore();
                tvBarScores[dayIndex].setText(String.format(Locale.getDefault(), "%d%%", score));

                // Dynamische Höhe proportional zum Score
                int heightPx = Math.max(12, (int) (maxBarHeightPx * (score / 100.0f)));
                ViewGroup.LayoutParams params = barFills[dayIndex].getLayoutParams();
                params.height = heightPx;
                barFills[dayIndex].setLayoutParams(params);

                // Tonal InPhase Green Farbpalette basierend auf Erholungs-Level
                if (score >= 75) {
                    barFills[dayIndex].getBackground().setTint(Color.parseColor("#2D6A4F")); // Waldgrün (Peak)
                } else if (score >= 50) {
                    barFills[dayIndex].getBackground().setTint(Color.parseColor("#52B788")); // Frisches Mint
                } else {
                    barFills[dayIndex].getBackground().setTint(Color.parseColor("#74C69D")); // Sanftes Mint
                }
            } else {
                tvBarScores[dayIndex].setText("--");
                ViewGroup.LayoutParams params = barFills[dayIndex].getLayoutParams();
                params.height = (int) (12 * getResources().getDisplayMetrics().density);
                barFills[dayIndex].setLayoutParams(params);
                barFills[dayIndex].getBackground().setTint(Color.parseColor("#EAF0EA"));
            }

            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
    }
}
