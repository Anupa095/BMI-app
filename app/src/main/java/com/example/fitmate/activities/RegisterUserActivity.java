package com.example.fitmate.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitmate.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RegisterUserActivity extends AppCompatActivity {

    private EditText edtUserId, edtAge, edtHeight, edtWeight;
    private CheckBox cbCancer, cbHeart, cbDiabetes, cbCholesterol;
    private Button btnSubmitUserData, btnViewBMI;
    private TextView tvBmiResult, tvBmiStatus;

    private FirebaseFirestore db;
    private float bmi = 0f;
    private String statusText = "";
    private int color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        edtUserId = findViewById(R.id.edtUserId);
        edtAge = findViewById(R.id.edtAge);
        edtHeight = findViewById(R.id.edtHeight);
        edtWeight = findViewById(R.id.edtWeight);

        cbCancer = findViewById(R.id.cbCancer);
        cbHeart = findViewById(R.id.cbHeart);
        cbDiabetes = findViewById(R.id.cbDiabetes);
        cbCholesterol = findViewById(R.id.cbCholesterol);

        btnSubmitUserData = findViewById(R.id.btnSubmitUserData);
        btnViewBMI = findViewById(R.id.btnViewBMI);
        tvBmiResult = findViewById(R.id.tvBmiResult);
        tvBmiStatus = findViewById(R.id.tvBmiStatus);

        db = FirebaseFirestore.getInstance();

        // Get user email if passed
        String email = getIntent().getStringExtra("USER_EMAIL");
        if (email != null && !email.isEmpty()) {
            edtUserId.setText(email);
            fetchAndSetAge(email);
        }

        btnSubmitUserData.setOnClickListener(v -> {
            validateAndCalculateBMI();

            // Show BMI immediately
            tvBmiResult.setText("BMI: " + String.format(Locale.US, "%.2f", bmi));
            tvBmiResult.setVisibility(View.VISIBLE);

            tvBmiStatus.setText(statusText);
            tvBmiStatus.setTextColor(color);
            tvBmiStatus.setVisibility(View.VISIBLE);

            btnViewBMI.setVisibility(View.VISIBLE); // Show "View More" button
        });

        btnViewBMI.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterUserActivity.this, BMIResultActivity.class);
            intent.putExtra("BMI_VALUE", bmi);
            intent.putExtra("BMI_STATUS", statusText);
            startActivity(intent);
        });
    }

    private void fetchAndSetAge(String userEmail) {
        db.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            String dob = document.getString("dob");
                            if (dob != null && !dob.isEmpty()) {
                                int age = calculateAgeFromDOB(dob);
                                if (age != -1) {
                                    edtAge.setText(String.valueOf(age));
                                } else {
                                    Toast.makeText(this, "Failed to parse DOB", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(this, "DOB field not found", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                    } else {
                        Toast.makeText(this, "User not found in database", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private int calculateAgeFromDOB(String dob) {
        try {
            String[] parts = dob.split("/");
            if (parts.length != 3) return -1;

            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int year = Integer.parseInt(parts[2]);

            Calendar dobCal = Calendar.getInstance();
            dobCal.set(year, month - 1, day);

            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR);
            if (today.get(Calendar.MONTH) < dobCal.get(Calendar.MONTH) ||
                    (today.get(Calendar.MONTH) == dobCal.get(Calendar.MONTH) &&
                            today.get(Calendar.DAY_OF_MONTH) < dobCal.get(Calendar.DAY_OF_MONTH))) {
                age--;
            }

            return age >= 0 ? age : -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void validateAndCalculateBMI() {
        String userId = edtUserId.getText().toString().trim();
        String ageStr = edtAge.getText().toString().trim();
        String heightStr = edtHeight.getText().toString().trim();
        String weightStr = edtWeight.getText().toString().trim();

        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(ageStr)
                || TextUtils.isEmpty(heightStr) || TextUtils.isEmpty(weightStr)) {
            Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int age = Integer.parseInt(ageStr);
            float heightCm = Float.parseFloat(heightStr);
            float weightKg = Float.parseFloat(weightStr);

            if (age <= 0 || heightCm <= 0 || weightKg <= 0) {
                Toast.makeText(this, "Invalid input values", Toast.LENGTH_SHORT).show();
                return;
            }

            float heightM = heightCm / 100f;
            bmi = weightKg / (heightM * heightM);

            if (bmi < 18.5) {
                statusText = "Underweight – Eat more!";
                color = getResources().getColor(android.R.color.holo_blue_dark);
            } else if (bmi < 25) {
                statusText = "Normal – Keep it up!";
                color = getResources().getColor(android.R.color.holo_green_dark);
            } else if (bmi < 30) {
                statusText = "Overweight – Time to exercise!";
                color = getResources().getColor(android.R.color.holo_orange_dark);
            } else {
                statusText = "Obese – High risk!";
                color = getResources().getColor(android.R.color.holo_red_dark);
            }

            String disease1 = cbDiabetes.isChecked() ? "Diabetes" : "None";
            String disease2 = cbCholesterol.isChecked() ? "Cholesterol" : "None";
            String disease3 = cbHeart.isChecked() ? "Heart Disease" : "None";
            String disease4 = cbCancer.isChecked() ? "Cancer" : "None";

            String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

            Map<String, Object> reportData = new HashMap<>();
            reportData.put("userId", userId);
            reportData.put("age", age);
            reportData.put("height", heightCm);
            reportData.put("weight", weightKg);
            reportData.put("bmi", bmi);
            reportData.put("bmiStatus", statusText);
            reportData.put("disease1", disease1);
            reportData.put("disease2", disease2);
            reportData.put("disease3", disease3);
            reportData.put("disease4", disease4);
            reportData.put("date", currentDate);

            db.collection("Reports")
                    .document(userId)
                    .set(reportData)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this, "Report saved & updated!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to update report: " + e.getMessage(), Toast.LENGTH_SHORT).show());

            db.collection("History")
                    .add(reportData)
                    .addOnSuccessListener(documentReference ->
                            Toast.makeText(this, "History entry saved", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to save history: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
        }
    }
}
