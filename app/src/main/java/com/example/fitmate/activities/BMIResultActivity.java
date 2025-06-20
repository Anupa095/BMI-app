package com.example.fitmate.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fitmate.R;

public class BMIResultActivity extends AppCompatActivity {

    private TextView tvBmiValue, tvBmiStatus;
    private Button btnMealPlans, btnExercisePlans;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi_result);

        tvBmiValue = findViewById(R.id.tvBmiValue);
        tvBmiStatus = findViewById(R.id.tvBmiStatus);
        btnMealPlans = findViewById(R.id.btnMealPlans);
        btnExercisePlans = findViewById(R.id.btnExercisePlans);

        // Get BMI value and status from intent
        float bmi = getIntent().getFloatExtra("BMI_VALUE", 0f);
        String status = getIntent().getStringExtra("BMI_STATUS");

        // Display them
        tvBmiValue.setText(String.format("Your BMI: %.2f", bmi));
        tvBmiStatus.setText("Status: " + status);

        // Button click listeners
        btnMealPlans.setOnClickListener(v -> {
            // TODO: Replace with your actual MealPlansActivity class
            Intent intent = new Intent(BMIResultActivity.this, MealPlansActivity.class);
            startActivity(intent);
        });

        btnExercisePlans.setOnClickListener(v -> {
            // TODO: Replace with your actual ExercisePlansActivity class
            Intent intent = new Intent(BMIResultActivity.this, ExercisePlansActivity.class);
            startActivity(intent);
        });
    }
}
