package at.mci.inphase;

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

import at.mci.inphase.model.DailyReadiness;

public class TrendAdapter extends RecyclerView.Adapter<TrendAdapter.ViewHolder> {

    private final List<DailyReadiness> trends;

    public TrendAdapter(List<DailyReadiness> trends) {
        this.trends = trends;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DailyReadiness item = trends.get(position);

        holder.tvTrendDate.setText(item.getDate());
        holder.tvTrendScore.setText(String.format(Locale.getDefault(), "%d%%", item.getReadinessScore()));

        int scoreVal = item.getReadinessScore();
        holder.progressScore.setProgressCompat(scoreVal, true);

        String trafficLight = item.getTrafficLight();
        if (trafficLight == null) {
            holder.progressScore.setIndicatorColor(Color.LTGRAY);
        } else {
            switch (trafficLight) {
                case "Green":
                    holder.progressScore.setIndicatorColor(Color.parseColor("#4CAF50"));
                    break;
                case "Yellow":
                    holder.progressScore.setIndicatorColor(Color.parseColor("#FFC107"));
                    break;
                case "Red":
                    holder.progressScore.setIndicatorColor(Color.parseColor("#F44336"));
                    break;
                default:
                    holder.progressScore.setIndicatorColor(Color.LTGRAY);
                    break;
            }
        }

        if (item.getSleepQuality() > 0) {
            holder.tvTrendSub.setVisibility(View.VISIBLE);
            holder.tvTrendSub.setText(String.format(Locale.getDefault(),
                    "Schlaf: %d/5 • Müdigkeit: %d/5 • Muskelkater: %d/5",
                    item.getSleepQuality(), item.getFatigue(), item.getMuscleSoreness()));
        } else {
            holder.tvTrendSub.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return trends.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTrendDate;
        final TextView tvTrendScore;
        final TextView tvTrendSub;
        final LinearProgressIndicator progressScore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTrendDate = itemView.findViewById(R.id.tvTrendDate);
            tvTrendScore = itemView.findViewById(R.id.tvTrendScore);
            tvTrendSub = itemView.findViewById(R.id.tvTrendSub);
            progressScore = itemView.findViewById(R.id.progressScore);
        }
    }
}
