package at.mci.inphase;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;
import java.util.Locale;

import at.mci.inphase.model.AthleteStatusItem;
import at.mci.inphase.model.User;

/**
 * Darstellung der Athleten-Liste auf dem Trainer-Dashboard (CoachDashboardFragment).
 * Namen, die Initialen im runden Avatar-Badge, den Readiness-Status und den Erholungsbalken an
 */
public class AthleteAdapter extends RecyclerView.Adapter<AthleteAdapter.ViewHolder> {

    public interface OnAthleteClickListener {
        void onAthleteClick(User athlete);
    }

    private final List<AthleteStatusItem> athletes;
    private final OnAthleteClickListener listener;

    public AthleteAdapter(List<AthleteStatusItem> athletes, OnAthleteClickListener listener) {
        this.athletes = athletes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_athlete_status, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AthleteStatusItem item = athletes.get(position);
        User athlete = item.getAthlete();
        Context context = holder.itemView.getContext();

        String name = athlete != null ? athlete.getName() : "Athlet";
        holder.tvAthleteName.setText(name);
        holder.tvAthleteInitials.setText(getInitials(name));

        String trafficLight = item.getTodayTrafficLight();
        if (item.getTodayScore() != null) {
            int score = item.getTodayScore().intValue();
            holder.tvReadinessScore.setText(String.format(Locale.getDefault(), "%d%%", score));
            holder.progressScore.setProgressCompat(score, true);

            if ("Green".equals(trafficLight)) {
                holder.progressScore.setIndicatorColor(Color.parseColor("#4CAF50"));
                holder.tvReadinessScore.setTextColor(Color.parseColor("#2D6A4F"));
                holder.tvAthleteStatusSub.setText(context.getString(R.string.status_recovered));
            } else if ("Yellow".equals(trafficLight)) {
                holder.progressScore.setIndicatorColor(Color.parseColor("#FFC107"));
                holder.tvReadinessScore.setTextColor(Color.parseColor("#F57F17"));
                holder.tvAthleteStatusSub.setText(context.getString(R.string.status_moderate));
            } else if ("Red".equals(trafficLight)) {
                holder.progressScore.setIndicatorColor(Color.parseColor("#F44336"));
                holder.tvReadinessScore.setTextColor(Color.parseColor("#C62828"));
                holder.tvAthleteStatusSub.setText(context.getString(R.string.status_low));
            } else {
                holder.progressScore.setIndicatorColor(Color.LTGRAY);
                holder.tvReadinessScore.setTextColor(Color.DKGRAY);
                holder.tvAthleteStatusSub.setText(context.getString(R.string.status_checkin_done));
            }
        } else {
            holder.tvReadinessScore.setText("--%");
            holder.progressScore.setProgressCompat(0, true);
            holder.progressScore.setIndicatorColor(Color.LTGRAY);
            holder.tvReadinessScore.setTextColor(Color.GRAY);
            holder.tvAthleteStatusSub.setText(context.getString(R.string.status_checkin_pending));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && athlete != null) {
                listener.onAthleteClick(athlete);
            }
        });
    }

    /**
     * Erstellt die Initialen (z.B. "Sandra Miller" -> "SM") für den runden Avatar-Badge.
     */
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
        return athletes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvAthleteInitials;
        final TextView tvAthleteName;
        final TextView tvAthleteStatusSub;
        final TextView tvReadinessScore;
        final LinearProgressIndicator progressScore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAthleteInitials = itemView.findViewById(R.id.tvAthleteInitials);
            tvAthleteName = itemView.findViewById(R.id.tvAthleteName);
            tvAthleteStatusSub = itemView.findViewById(R.id.tvAthleteStatusSub);
            tvReadinessScore = itemView.findViewById(R.id.tvReadinessScore);
            progressScore = itemView.findViewById(R.id.progressScore);
        }
    }
}
