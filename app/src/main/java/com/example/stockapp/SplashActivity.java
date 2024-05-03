package com.example.stockapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.example.stockapp.MainActivity;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView loadingImage = findViewById(R.id.loading);
        RotateAnimation rotate = new RotateAnimation(
                0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotate.setDuration(1200);  // Set the duration of each rotation
        rotate.setRepeatCount(Animation.INFINITE);  // Set to repeat indefinitely
        rotate.setInterpolator(this, android.R.interpolator.linear);  // Linear interpolator for smooth rotation

        loadingImage.startAnimation(rotate);  // Start the animation

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2000); // Wait for 2 seconds
    }
}
