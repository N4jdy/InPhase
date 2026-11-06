package at.mci.inphase;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import at.mci.inphase.model.WorkloadItem;

public class WorkloadAdapter extends RecyclerView.Adapter<WorkloadAdapter.ViewHolder> {

    private final List<WorkloadItem> workloads;

    public WorkloadAdapter(List<WorkloadItem> workloads) {
        this.workloads = workloads;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workload, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkloadItem item = workloads.get(position);
        holder.tvWorkloadAthleteName.setText(item.getAthleteName());
        holder.tvWorkloadRpe.setText(holder.itemView.getContext().getString(R.string.workload_rpe_format, item.getWorkload().getRpeScore()));
        holder.tvWorkloadTotal.setText(holder.itemView.getContext().getString(R.string.workload_total_format, item.getWorkload().getDuration(), item.getWorkload().getWorkload()));
    }

    @Override
    public int getItemCount() {
        return workloads.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvWorkloadAthleteName;
        final TextView tvWorkloadRpe;
        final TextView tvWorkloadTotal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWorkloadAthleteName = itemView.findViewById(R.id.tvWorkloadAthleteName);
            tvWorkloadRpe = itemView.findViewById(R.id.tvWorkloadRpe);
            tvWorkloadTotal = itemView.findViewById(R.id.tvWorkloadTotal);
        }
    }
}