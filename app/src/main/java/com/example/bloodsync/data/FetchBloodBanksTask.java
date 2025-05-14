package com.example.bloodsync.data;

import android.os.AsyncTask;
import android.util.Log;

import com.example.bloodsync.model.BloodBank;
import com.example.bloodsync.model.BloodBankData;

import java.io.IOException;
import java.util.List;

public class FetchBloodBanksTask extends AsyncTask<Double, Void, List<BloodBank>> {
    private static final String TAG = "FetchBloodBanksTask";
    private BloodBankListener listener;
    private Exception exception;

    // Interface for callback
    public interface BloodBankListener {
        void onBloodBanksFetched(List<BloodBank> bloodBanks);
        void onError(String error);
    }

    public FetchBloodBanksTask(BloodBankListener listener) {
        this.listener = listener;
    }

    @Override
    protected List<BloodBank> doInBackground(Double... params) {
        if (params.length < 2) {
            exception = new IllegalArgumentException("Latitude and longitude required");
            return null;
        }

        double latitude = params[0];
        double longitude = params[1];

        try {
            String url = BloodBankData.getBloodBanksUrl(latitude, longitude);
            String jsonData = BloodBankData.downloadUrl(url);
            return BloodBankData.parseBloodBanks(jsonData, latitude, longitude);
        } catch (IOException e) {
            Log.e(TAG, "Error fetching blood banks: " + e.getMessage());
            exception = e;
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<BloodBank> bloodBanks) {
        if (listener != null) {
            if (bloodBanks != null) {
                listener.onBloodBanksFetched(bloodBanks);
            } else {
                listener.onError(exception != null ? exception.getMessage() : "Unknown error");
            }
        }
    }
}