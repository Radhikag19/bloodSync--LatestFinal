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

public class OnboardingSlide3Fragment extends Fragment {

    public static OnboardingSlide3Fragment newInstance() {
        return new OnboardingSlide3Fragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding_slide3, container, false);
        ImageView imageView = view.findViewById(R.id.onboardingImageView);
        TextView titleTextView = view.findViewById(R.id.onboardingTitleTextView);
        TextView descriptionTextView = view.findViewById(R.id.onboardingDescriptionTextView);

        imageView.setImageResource(R.drawable.onboarding_3); // Set your image
        titleTextView.setText(R.string.onboarding_title_3);      // Use string resources
        descriptionTextView.setText(R.string.onboarding_desc_3);  // Use string resources

        return view;
    }
}
