package com.example.bloodsync.onboarding;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.bloodsync.R;
import com.example.bloodsync.auth.ChooseProfileActivity;
import com.example.bloodsync.auth.SignupActivity;
import com.example.bloodsync.onboarding.fragments.OnboardingSlide1Fragment;
import com.example.bloodsync.onboarding.fragments.OnboardingSlide2Fragment;
import com.example.bloodsync.onboarding.fragments.OnboardingSlide3Fragment;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 onboardingViewPager;
    private TabLayout tabLayout;
    private TextView skipButton;
    private ImageView nextButton;
    private OnboardingPagerAdapter pagerAdapter;

    private static final String PREF_ONBOARDING_COMPLETE = "onboarding_complete";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        onboardingViewPager = findViewById(R.id.onboardingViewPager);
        tabLayout = findViewById(R.id.tabLayout);
        skipButton = findViewById(R.id.skipButton);
        nextButton = findViewById(R.id.nextButton);

        List<Fragment> onboardingSlides = new ArrayList<>();
        onboardingSlides.add(OnboardingSlide1Fragment.newInstance());
        onboardingSlides.add(OnboardingSlide2Fragment.newInstance());
        onboardingSlides.add(OnboardingSlide3Fragment.newInstance());

        pagerAdapter = new OnboardingPagerAdapter(this, onboardingSlides);
        onboardingViewPager.setAdapter(pagerAdapter);

        // Link TabLayout with ViewPager2
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                onboardingViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Not needed
            }
        });

        onboardingViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
                if (position == onboardingSlides.size() - 1) {
                    nextButton.setImageResource(R.drawable.ic_arrow_forward); // Or a "Finish" icon
                } else {
                    nextButton.setImageResource(R.drawable.ic_arrow_forward);
                }
            }
        });

        skipButton.setOnClickListener(v -> navigateToAuth());

        nextButton.setOnClickListener(v -> {
            int currentItem = onboardingViewPager.getCurrentItem();
            if (currentItem < onboardingSlides.size() - 1) {
                onboardingViewPager.setCurrentItem(currentItem + 1);
            } else {
                navigateToAuth();
            }
        });
    }

    private void navigateToAuth() {
        SharedPreferences preferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
        preferences.edit().putBoolean(PREF_ONBOARDING_COMPLETE, true).apply();

        // Navigate to Signup  for new users
        Intent intent = new Intent(OnboardingActivity.this, SignupActivity.class);
        startActivity(intent);
        finish();
    }
}