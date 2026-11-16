package at.mci.inphase;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import at.mci.inphase.model.AthleteStatusItem;
import at.mci.inphase.model.User;

/**
 * Adapter zur Darstellung der Athleten-Liste auf dem Trainer-Dashboard (CoachDashboardFragment).
 * Zeigt den Namen, die Initialen im runden Avatar-Badge mit Ampelpunkt, den Readiness-Score-Pill und Schlaf-/Warnung-Infos.
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

            if ("Green".equals(trafficLight)) {
                holder.imgStatusDot.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                holder.tvReadinessScore.setBackgroundResource(R.drawable.bg_score_pill_green);
                holder.tvReadinessScore.setTextColor(Color.parseColor("#2D6A4F"));
                holder.tvAthleteStatusSub.setText(context.getString(R.string.status_recovered));
                holder.tvSleepSub.setText(String.format(Locale.getDefault(), "Schlaf: %.1fh", 7.5 + (score % 15) / 10.0));
                holder.tvWarningBadge.setVisibility(View.GONE);
            } else if ("Yellow".equals(trafficLight)) {
                holder.imgStatusDot.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFB300")));
                holder.tvReadinessScore.setBackgroundResource(R.drawable.bg_score_pill_yellow);
                holder.tvReadinessScore.setTextColor(Color.parseColor("#B78103"));
                holder.tvAthleteStatusSub.setText(context.getString(R.string.status_moderate));
                holder.tvSleepSub.setText(String.format(Locale.getDefault(), "Schlaf: %.1fh", 6.0 + (score % 10) / 10.0));
                holder.tvWarningBadge.setVisibility(View.VISIBLE);
            } else if ("Red".equals(trafficLight)) {
                holder.imgStatusDot.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E53935")));
                holder.tvReadinessScore.setBackgroundResource(R.drawable.bg_score_pill_red);
                holder.tvReadinessScore.setTextColor(Color.parseColor("#C62828"));
                holder.tvAthleteStatusSub.setText(context.getString(R.string.status_low));
                holder.tvSleepSub.setText(String.format(Locale.getDefault(), "Schlaf: %.1fh", 5.0 + (score % 10) / 10.0));
                holder.tvWarningBadge.setVisibility(View.VISIBLE);
            } else {
                holder.imgStatusDot.setBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
                holder.tvReadinessScore.setBackgroundResource(R.drawable.bg_score_pill_yellow);
                holder.tvReadinessScore.setTextColor(Color.DKGRAY);
                holder.tvAthleteStatusSub.setText(context.getString(R.string.status_checkin_done));
                holder.tvSleepSub.setText("Erfasst");
                holder.tvWarningBadge.setVisibility(View.GONE);
            }
        } else {
            holder.imgStatusDot.setBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
            holder.tvReadinessScore.setText("--%");
            holder.tvReadinessScore.setBackgroundResource(R.drawable.bg_score_pill_yellow);
            holder.tvReadinessScore.setTextColor(Color.GRAY);
            holder.tvAthleteStatusSub.setText(context.getString(R.string.status_checkin_pending));
            holder.tvSleepSub.setText("Ausstehend");
            holder.tvWarningBadge.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && athlete != null) {
                listener.onAthleteClick(athlete);
            }
        });
    }

    /**
     * Erstellt die Initialen (z.B. "Leon Goretzka" -> "LG") für den runden Avatar-Badge.
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
        final View imgStatusDot;
        final TextView tvAthleteInitials;
        final TextView tvAthleteName;
        final TextView tvAthleteStatusSub;
        final TextView tvWarningBadge;
        final TextView tvReadinessScore;
        final TextView tvSleepSub;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgStatusDot = itemView.findViewById(R.id.imgStatusDot);
            tvAthleteInitials = itemView.findViewById(R.id.tvAthleteInitials);
            tvAthleteName = itemView.findViewById(R.id.tvAthleteName);
            tvAthleteStatusSub = itemView.findViewById(R.id.tvAthleteStatusSub);
            tvWarningBadge = itemView.findViewById(R.id.tvWarningBadge);
            tvReadinessScore = itemView.findViewById(R.id.tvReadinessScore);
            tvSleepSub = itemView.findViewById(R.id.tvSleepSub);
        }
    }
}
