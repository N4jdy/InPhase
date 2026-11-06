package at.mci.inphase;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarStripAdapter extends RecyclerView.Adapter<CalendarStripAdapter.ViewHolder> {

    public interface OnDateClickListener {
        void onDateClick(Calendar selectedCalendar);
    }

    private final List<Calendar> days;
    private Calendar selectedCalendar;
    private final OnDateClickListener listener;

    public CalendarStripAdapter(List<Calendar> days, Calendar initialSelected, OnDateClickListener listener) {
        this.days = days;
        this.selectedCalendar = initialSelected;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Calendar dayCal = days.get(position);

        SimpleDateFormat dayNameSdf = new SimpleDateFormat("EEE", Locale.GERMAN);
        SimpleDateFormat dayNumSdf = new SimpleDateFormat("dd", Locale.getDefault());

        holder.tvDayName.setText(dayNameSdf.format(dayCal.getTime()).replace(".", ""));
        holder.tvDayNumber.setText(dayNumSdf.format(dayCal.getTime()));

        boolean isSelected = isSameDay(dayCal, selectedCalendar);

        if (isSelected) {
            holder.layoutPill.setBackgroundResource(R.drawable.bg_calendar_day_selected);
            holder.tvDayName.setTextColor(Color.parseColor("#D8E2DC"));
            holder.tvDayNumber.setTextColor(Color.WHITE);
        } else {
            holder.layoutPill.setBackgroundResource(R.drawable.bg_calendar_day_unselected);
            holder.tvDayName.setTextColor(Color.parseColor("#6C757D"));
            holder.tvDayNumber.setTextColor(Color.parseColor("#212529"));
        }

        holder.itemView.setOnClickListener(v -> {
            selectedCalendar = dayCal;
            notifyDataSetChanged();
            if (listener != null) {
                listener.onDateClick(dayCal);
            }
        });
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) return false;
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final View layoutPill;
        final TextView tvDayName;
        final TextView tvDayNumber;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutPill = itemView.findViewById(R.id.layoutDayPill);
            tvDayName = itemView.findViewById(R.id.tvDayName);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
        }
    }
}
