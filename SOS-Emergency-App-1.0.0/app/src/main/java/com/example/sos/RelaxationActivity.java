package com.example.sos;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class RelaxationActivity extends AppCompatActivity {

    private View breathingCircle;
    private TextView instructionText, timerText, cycleProgressText;
    private ProgressBar progressBar;
    private int inhaleTime = 4000;  // Inhale duration in milliseconds
    private int exhaleTime = 4000; // Exhale duration in milliseconds
    private int totalTime = 60000; // Total session time in milliseconds
    private int cyclesCompleted = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relaxation);

        // Initialize views
        breathingCircle = findViewById(R.id.breathing_circle);
        instructionText = findViewById(R.id.instruction_text);
        timerText = findViewById(R.id.timer_text);
        cycleProgressText = findViewById(R.id.cycle_progress_text);
        progressBar = findViewById(R.id.progress_bar);

        // Configure progress bar
        progressBar.setMax(totalTime / (inhaleTime + exhaleTime));
        progressBar.setProgress(0);

        // Start breathing exercise
        startBreathingExercise();
    }

    private void startBreathingExercise() {
        // Create a countdown timer for the total session
        new CountDownTimer(totalTime, inhaleTime + exhaleTime) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Start inhale-exhale animation for each cycle
                startInhaleExhaleAnimation();
            }

            @Override
            public void onFinish() {
                // Session completed
                timerText.setText("Session Completed");
                instructionText.setText("Great Job!");
                breathingCircle.setBackgroundColor(Color.GREEN);
            }
        }.start();
    }

    private void startInhaleExhaleAnimation() {
        // Update cycle progress
        cyclesCompleted++;
        progressBar.setProgress(cyclesCompleted);
        cycleProgressText.setText("Cycle: " + cyclesCompleted);

        // Inhale phase
        ValueAnimator inhaleAnimator = ValueAnimator.ofFloat(1f, 1.5f);
        inhaleAnimator.setDuration(inhaleTime);
        inhaleAnimator.setInterpolator(new LinearInterpolator());
        inhaleAnimator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            breathingCircle.setScaleX(scale);
            breathingCircle.setScaleY(scale);
            instructionText.setText("Inhale");
            breathingCircle.setBackgroundColor(Color.CYAN);
        });

        // Exhale phase
        ValueAnimator exhaleAnimator = ValueAnimator.ofFloat(1.5f, 1f);
        exhaleAnimator.setDuration(exhaleTime);
        exhaleAnimator.setInterpolator(new LinearInterpolator());
        exhaleAnimator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            breathingCircle.setScaleX(scale);
            breathingCircle.setScaleY(scale);
            instructionText.setText("Exhale");
            breathingCircle.setBackgroundColor(Color.MAGENTA);
        });

        // Chain inhale and exhale animations
        inhaleAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                exhaleAnimator.start();
            }
        });

        inhaleAnimator.start();
    }
}
