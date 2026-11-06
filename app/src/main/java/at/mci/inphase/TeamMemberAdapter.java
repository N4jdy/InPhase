package at.mci.inphase;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import at.mci.inphase.model.AthleteStatusItem;
import at.mci.inphase.model.User;

/**
 * Adapter zur Darstellung der Teammitglieder in der Team-Verwaltung (CoachTeamFragment).
 * Ermöglicht dem Trainer den Klick auf jedes Mitglied, um dessen Individualansicht (AthleteDetailActivity) zu öffnen.
 */
public class TeamMemberAdapter extends RecyclerView.Adapter<TeamMemberAdapter.ViewHolder> {

    public interface OnMemberClickListener {
        void onMemberClick(User member);
    }

    private final List<AthleteStatusItem> members;
    private final OnMemberClickListener listener;

    public TeamMemberAdapter(List<AthleteStatusItem> members, OnMemberClickListener listener) {
        this.members = members;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AthleteStatusItem item = members.get(position);
        User user = item.getAthlete();

        if (user == null) return;

        holder.tvMemberName.setText(user.getName());

        String role = user.getRole();
        if ("Athlete".equals(role)) {
            holder.tvMemberRole.setText("Athletin");
        } else if ("Coach".equals(role)) {
            holder.tvMemberRole.setText("Coach / Trainer");
        } else {
            holder.tvMemberRole.setText(Objects.requireNonNullElse(role, ""));
        }

        holder.tvInitials.setText(getInitials(user.getName()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMemberClick(user);
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

    @Override
    public int getItemCount() {
        return members.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvInitials;
        final TextView tvMemberName;
        final TextView tvMemberRole;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitials = itemView.findViewById(R.id.tvInitials);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvMemberRole = itemView.findViewById(R.id.tvMemberRole);
        }
    }
}
