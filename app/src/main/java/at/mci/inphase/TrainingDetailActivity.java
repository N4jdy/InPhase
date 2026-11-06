package at.mci.inphase;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.mci.inphase.model.User;
import at.mci.inphase.model.Workload;
import at.mci.inphase.model.WorkloadItem;

public class TrainingDetailActivity extends AppCompatActivity {

    private WorkloadAdapter workloadAdapter;
    private final List<WorkloadItem> workloadItemList = new ArrayList<>();
    private FirebaseFirestore db;

    private String sessionId;
    private String teamId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_detail);

        db = FirebaseFirestore.getInstance();

        sessionId = getIntent().getStringExtra("sessionId");
        teamId = getIntent().getStringExtra("teamId");
        String trainingTitle = getIntent().getStringExtra("trainingTitle");

        TextView tvDetailTitle = findViewById(R.id.tvDetailTitle);
        RecyclerView rvWorkloads = findViewById(R.id.rvWorkloads);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> finish());

        if (trainingTitle != null) {
            tvDetailTitle.setText(trainingTitle);
        }

        rvWorkloads.setLayoutManager(new LinearLayoutManager(this));
        workloadAdapter = new WorkloadAdapter(workloadItemList);
        rvWorkloads.setAdapter(workloadAdapter);

        loadWorkloads();
    }

    private void loadWorkloads() {
        if (sessionId == null || teamId == null) return;

        // 1. Hole alle Athleten des Teams, um ID -> Name zu verknüpfen
        db.collection("users")
            .whereEqualTo("teamID", teamId)
            .whereEqualTo("role", "Athlete")
            .get()
            .addOnSuccessListener(userSnaps -> {
                Map<String, String> userNames = new HashMap<>();
                for (DocumentSnapshot doc : userSnaps) {
                    User u = doc.toObject(User.class);
                    if (u != null) {
                        userNames.put(u.getUserID(), u.getName());
                    }
                }

                // 2. Hole alle Workloads für dieses Training
                db.collection("workloads")
                    .whereEqualTo("sessionID", sessionId)
                    .get()
                    .addOnSuccessListener(workloadSnaps -> {
                        workloadItemList.clear();
                        for (DocumentSnapshot doc : workloadSnaps) {
                            Workload w = doc.toObject(Workload.class);
                            if (w != null) {
                                String name = userNames.getOrDefault(w.getUserID(), "Unbekannt");
                                workloadItemList.add(new WorkloadItem(name, w));
                            }
                        }
                        workloadAdapter.notifyDataSetChanged();
                    });
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
