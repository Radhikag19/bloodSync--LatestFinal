package com.example.bloodsync.onboarding.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.bloodsync.R;

public class OnboardingSlide2Fragment extends Fragment {

    public static OnboardingSlide2Fragment newInstance() {
        return new OnboardingSlide2Fragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding_slide2, container, false);
        ImageView imageView = view.findViewById(R.id.onboardingImageView);
        TextView titleTextView = view.findViewById(R.id.onboardingTitleTextView);
        TextView descriptionTextView = view.findViewById(R.id.onboardingDescriptionTextView);

        imageView.setImageResource(R.drawable.onboarding_2); // Set your image
        titleTextView.setText(R.string.onboarding_title_2);      // Use string resources
        descriptionTextView.setText(R.string.onboarding_desc_2);  // Use string resources

        return view;
    }
}