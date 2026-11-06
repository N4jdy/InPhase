package at.mci.inphase;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import at.mci.inphase.model.TrainingSession;

public class TrainingAdapter extends RecyclerView.Adapter<TrainingAdapter.ViewHolder> {

    private final List<TrainingSession> trainings;

    public TrainingAdapter(List<TrainingSession> trainings) {
        this.trainings = trainings;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_training, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TrainingSession session = trainings.get(position);

        holder.tvTitle.setText(session.getTitle());

        String type = session.getSessionType();
        String typeText = java.util.Objects.requireNonNullElse(type, "");
        String details = (session.getNotes() != null && !session.getNotes().isEmpty()) 
                ? typeText + " • " + session.getNotes() 
                : typeText;
        holder.tvType.setText(details);

        SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm 'Uhr'", Locale.getDefault());
        holder.tvTimeBadge.setText(timeSdf.format(new Date(session.getDateTime())));

        // Set type icon & colors
        int iconColor = MonthCalendarAdapter.getTrainingColor(type);
        int iconBgColor = MonthCalendarAdapter.getTrainingBgColor(type);
        int iconRes = getIconForType(type);

        holder.frameBadge.setBackgroundTintList(ColorStateList.valueOf(iconBgColor));
        holder.imgIcon.setImageResource(iconRes);
        holder.imgIcon.setImageTintList(ColorStateList.valueOf(iconColor));
    }

    private int getIconForType(String type) {
        if (type == null) return R.drawable.ic_runner;
        switch (type) {
            case "Match":
                return R.drawable.ic_match;
            case "Gym":
                return R.drawable.ic_gym;
            case "Recovery":
                return R.drawable.ic_heart_pulse;
            case "Run":
            case "Team_Training":
            default:
                return R.drawable.ic_runner;
        }
    }

    @Override
    public int getItemCount() {
        return trainings.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final View frameBadge;
        final ImageView imgIcon;
        final TextView tvTitle;
        final TextView tvType;
        final TextView tvTimeBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            frameBadge = itemView.findViewById(R.id.frameTypeIconBadge);
            imgIcon = itemView.findViewById(R.id.imgTypeIcon);
            tvTitle = itemView.findViewById(R.id.tvTrainingTitle);
            tvType = itemView.findViewById(R.id.tvTrainingType);
            tvTimeBadge = itemView.findViewById(R.id.tvTrainingTimeBadge);
        }
    }
}
