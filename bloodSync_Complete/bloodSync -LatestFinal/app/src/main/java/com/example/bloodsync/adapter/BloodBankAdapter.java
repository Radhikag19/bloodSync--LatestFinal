package com.example.bloodsync.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodsync.R;
import com.example.bloodsync.model.BloodBank;

import java.util.ArrayList;
import java.util.List;

public class BloodBankAdapter extends RecyclerView.Adapter<BloodBankAdapter.ViewHolder> {

    private List<BloodBank> bloodBanks = new ArrayList<>();

    public void setBloodBanks(List<BloodBank> bloodBanks) {
        this.bloodBanks = bloodBanks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_blood_bank, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BloodBank bloodBank = bloodBanks.get(position);
        holder.nameTextView.setText(bloodBank.getName());
        holder.addressTextView.setText(bloodBank.getAddress());
        holder.distanceTextView.setText(String.format("Distance: %.1f KM", bloodBank.getDistance()));
    }

    @Override
    public int getItemCount() {
        return bloodBanks.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView addressTextView;
        TextView distanceTextView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.text_blood_bank_name);
            addressTextView = itemView.findViewById(R.id.text_blood_bank_address);
            distanceTextView = itemView.findViewById(R.id.text_blood_bank_distance);
        }
    }
}