package at.mci.inphase;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import at.mci.inphase.model.AthleteStatusItem;
import at.mci.inphase.model.User;

public class CoachTeamFragment extends Fragment {

    private TextView tvTeamInviteCode;

    private TeamMemberAdapter memberAdapter;
    private final List<AthleteStatusItem> memberList = new ArrayList<>();

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coach_team, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvTeamInviteCode = view.findViewById(R.id.tvTeamInviteCode);
        ImageButton btnCopyCode = view.findViewById(R.id.btnCopyCode);
        RecyclerView rvTeamMembers = view.findViewById(R.id.rvTeamMembers);

        rvTeamMembers.setLayoutManager(new LinearLayoutManager(getContext()));
        memberAdapter = new TeamMemberAdapter(memberList, member -> {
            if (member != null) {
                Intent intent = new Intent(getActivity(), AthleteDetailActivity.class);
                intent.putExtra("athleteId", member.getUserID());
                intent.putExtra("athleteName", member.getName());
                startActivity(intent);
            }
        });
        rvTeamMembers.setAdapter(memberAdapter);

        btnCopyCode.setOnClickListener(v -> {
            String code = tvTeamInviteCode.getText().toString();
            if (!"LÄDT...".equals(code) && !"Nicht gefunden".equals(code)) {
                ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Team Code", code);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), "Code kopiert! ✓", Toast.LENGTH_SHORT).show();
            }
        });

        loadTeamData();

        return view;
    }

    private void loadTeamData() {
        if (mAuth.getCurrentUser() == null) return;
        String coachId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(coachId).get().addOnSuccessListener(coachDoc -> {
            if (!coachDoc.exists()) return;
            String teamId = coachDoc.getString("teamID");

            if (teamId != null) {
                // Lade Team Code
                db.collection("teams").document(teamId).get().addOnSuccessListener(teamDoc -> {
                    if (teamDoc.exists()) {
                        String code = teamDoc.getString("inviteCode");
                        tvTeamInviteCode.setText(Objects.requireNonNullElse(code, "Nicht gefunden"));
                    }
                });

                // Lade Mitglieder
                db.collection("users")
                  .whereEqualTo("teamID", teamId)
                  .get()
                  .addOnSuccessListener(queryDocumentSnapshots -> {
                      memberList.clear();
                      for (DocumentSnapshot doc : queryDocumentSnapshots) {
                          User user = doc.toObject(User.class);
                          if (user != null) {
                              memberList.add(new AthleteStatusItem(user, null, null));
                          }
                      }
                      memberAdapter.notifyDataSetChanged();
                  })
                  .addOnFailureListener(e -> {
                      if (getContext() != null) {
                          Toast.makeText(getContext(), "Fehler: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                      }
                  });
            }
        });
    }
}
