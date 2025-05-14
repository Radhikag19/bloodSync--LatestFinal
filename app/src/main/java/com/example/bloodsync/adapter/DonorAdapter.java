package com.example.bloodsync.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodsync.R;
import com.example.bloodsync.model.User;
import com.example.bloodsync.util.DistanceCalculator;

import java.util.ArrayList;
import java.util.List;

public class DonorAdapter extends RecyclerView.Adapter<DonorAdapter.DonorViewHolder> {

    private List<User> donors = new ArrayList<>();
    private double currentLatitude;
    private double currentLongitude;
    private OnDonorClickListener listener;

    public interface OnDonorClickListener {
        void onDonorClick(User donor);
    }

    public void setDonors(List<User> donors, double latitude, double longitude) {
        this.donors = donors;
        this.currentLatitude = latitude;
        this.currentLongitude = longitude;
        notifyDataSetChanged();
    }

    public void setOnDonorClickListener(OnDonorClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public DonorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_donor, parent, false);
        return new DonorViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DonorViewHolder holder, int position) {
        User currentDonor = donors.get(position);
        holder.textDonorName.setText(currentDonor.getName());
        holder.textDonorLocation.setText(currentDonor.getLocation());

        // Calculate distance
        double distance = DistanceCalculator.calculateDistance(
                currentLatitude, currentLongitude,
                currentDonor.getLatitude(), currentDonor.getLongitude());

        holder.textDonorDistance.setText(String.format("%.1f km", distance));
        holder.textDonorBloodGroup.setText(currentDonor.getBloodGroup());
    }

    @Override
    public int getItemCount() {
        return donors.size();
    }

    class DonorViewHolder extends RecyclerView.ViewHolder {
        private TextView textDonorName;
        private TextView textDonorLocation;
        private TextView textDonorDistance;
        private TextView textDonorBloodGroup;

        public DonorViewHolder(View itemView) {
            super(itemView);
            textDonorName = itemView.findViewById(R.id.text_donor_name);
            textDonorLocation = itemView.findViewById(R.id.text_donor_location);
            textDonorDistance = itemView.findViewById(R.id.text_donor_distance);
            textDonorBloodGroup = itemView.findViewById(R.id.text_donor_blood_group);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onDonorClick(donors.get(position));
                }
            });
        }
    }
}