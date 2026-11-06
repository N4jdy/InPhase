package at.mci.inphase;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import at.mci.inphase.model.TrainingSession;

/**
 * Adapter zur Darstellung des 7-Spalten Monatskalenders (Monatsraster) für Sportler und Trainer.
 * Rendert Tageszahlen, Heutige/Selektierte Auszeichnungen sowie farbige Typen-Farbpunkte für anstehende Trainingseinheiten.
 */
public class MonthCalendarAdapter extends RecyclerView.Adapter<MonthCalendarAdapter.ViewHolder> {

    public interface OnDayClickListener {
        void onDayClick(Calendar date);
    }

    private Calendar monthCalendar;
    private Calendar selectedDate;
    private List<TrainingSession> allTrainings;
    private final OnDayClickListener listener;
    private final List<Calendar> daysInMonth = new ArrayList<>();

    public MonthCalendarAdapter(Calendar monthCalendar, Calendar selectedDate, List<TrainingSession> allTrainings, OnDayClickListener listener) {
        this.monthCalendar = (Calendar) monthCalendar.clone();
        this.selectedDate = (Calendar) selectedDate.clone();
        this.allTrainings = new ArrayList<>(allTrainings != null ? allTrainings : new ArrayList<>());
        this.listener = listener;
        rebuildMonthGrid();
    }

    public void updateData(Calendar monthCalendar, Calendar selectedDate, List<TrainingSession> allTrainings) {
        this.monthCalendar = (Calendar) monthCalendar.clone();
        this.selectedDate = (Calendar) selectedDate.clone();
        this.allTrainings = new ArrayList<>(allTrainings != null ? allTrainings : new ArrayList<>());
        rebuildMonthGrid();
        notifyDataSetChanged();
    }

    private void rebuildMonthGrid() {
        daysInMonth.clear();

        Calendar cal = (Calendar) monthCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int offset = (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;

        // Führende Leerzellen einfügen
        for (int emptyIdx = 0; emptyIdx < offset; emptyIdx++) {
            daysInMonth.add(null);
        }

        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int day = 1; day <= maxDay; day++) {
            Calendar dayCal = (Calendar) cal.clone();
            dayCal.set(Calendar.DAY_OF_MONTH, day);
            daysInMonth.add(dayCal);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_month_calendar_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Calendar dayCal = daysInMonth.get(position);

        if (dayCal == null) {
            holder.frameDayCircle.setVisibility(View.INVISIBLE);
            holder.layoutDotsContainer.removeAllViews();
            holder.itemView.setOnClickListener(null);
            return;
        }

        holder.frameDayCircle.setVisibility(View.VISIBLE);
        int dayNum = dayCal.get(Calendar.DAY_OF_MONTH);
        holder.tvDayNumber.setText(String.valueOf(dayNum));

        boolean isSelected = isSameDay(dayCal, selectedDate);
        boolean isToday = isSameDay(dayCal, Calendar.getInstance());

        if (isSelected) {
            holder.frameDayCircle.setBackgroundResource(R.drawable.bg_calendar_day_selected);
            holder.tvDayNumber.setTextColor(Color.WHITE);
        } else if (isToday) {
            holder.frameDayCircle.setBackgroundResource(R.drawable.bg_calendar_day_unselected);
            holder.tvDayNumber.setTextColor(Color.parseColor("#2D6A4F"));
        } else {
            holder.frameDayCircle.setBackgroundResource(0);
            holder.tvDayNumber.setTextColor(Color.parseColor("#212529"));
        }

        // Rendere farbige Punkte für alle Trainingstypen an diesem Tag
        holder.layoutDotsContainer.removeAllViews();
        List<String> dayTypes = getTrainingTypesForDay(dayCal);

        int maxDots = Math.min(dayTypes.size(), 3);
        for (int idx = 0; idx < maxDots; idx++) {
            String type = dayTypes.get(idx);
            View dot = new View(holder.itemView.getContext());
            int dotSizePx = (int) (5 * holder.itemView.getContext().getResources().getDisplayMetrics().density);
            int marginPx = (int) (2 * holder.itemView.getContext().getResources().getDisplayMetrics().density);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dotSizePx, dotSizePx);
            params.setMargins(marginPx, 0, marginPx, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.dot_colored);

            int color = getTrainingColor(type);
            dot.setBackgroundTintList(ColorStateList.valueOf(color));

            holder.layoutDotsContainer.addView(dot);
        }

        holder.itemView.setOnClickListener(v -> {
            selectedDate = (Calendar) dayCal.clone();
            notifyDataSetChanged();
            if (listener != null) {
                listener.onDayClick(dayCal);
            }
        });
    }

    private List<String> getTrainingTypesForDay(Calendar dayCal) {
        List<String> types = new ArrayList<>();
        if (allTrainings == null) return types;

        for (TrainingSession session : allTrainings) {
            Calendar sessionCal = Calendar.getInstance();
            sessionCal.setTimeInMillis(session.getDateTime());

            if (isSameDay(dayCal, sessionCal)) {
                types.add(session.getSessionType());
            }
        }
        return types;
    }

    public static int getTrainingColor(String type) {
        if (type == null) return Color.parseColor("#2D6A4F");
        switch (type) {
            case "Match":
                return Color.parseColor("#E63946");
            case "Gym":
                return Color.parseColor("#F4A261");
            case "Recovery":
                return Color.parseColor("#52B788");
            case "Run":
                return Color.parseColor("#3A86FF");
            case "Team_Training":
            default:
                return Color.parseColor("#2D6A4F");
        }
    }

    public static int getTrainingBgColor(String type) {
        if (type == null) return Color.parseColor("#D8F3DC");
        switch (type) {
            case "Match":
                return Color.parseColor("#FFD8D8");
            case "Gym":
                return Color.parseColor("#FFE8D6");
            case "Recovery":
                return Color.parseColor("#D8F3DC");
            case "Run":
                return Color.parseColor("#D0E1FD");
            case "Team_Training":
            default:
                return Color.parseColor("#D8F3DC");
        }
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) return false;
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    @Override
    public int getItemCount() {
        return daysInMonth.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final View frameDayCircle;
        final TextView tvDayNumber;
        final LinearLayout layoutDotsContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            frameDayCircle = itemView.findViewById(R.id.frameDayCircle);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
            layoutDotsContainer = itemView.findViewById(R.id.layoutDotsContainer);
        }
    }
}
