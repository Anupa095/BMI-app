package com.example.fitmate.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
    private Button btnSubmitUserData, btnViewBMI, btnAction;
    private TextView tvBmiResult, tvBmiStatus, tvGreeting;

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
        btnAction = findViewById(R.id.btnAction);

        tvBmiResult = findViewById(R.id.tvBmiResult);
        tvBmiStatus = findViewById(R.id.tvBmiStatus);
        tvGreeting = findViewById(R.id.tvGreeting);

        db = FirebaseFirestore.getInstance();

        // Get user email
        String email = getIntent().getStringExtra("USER_EMAIL");
        if (email != null && !email.isEmpty()) {
            edtUserId.setText(email);
            fetchAndSetAge(email);
            fetchAndSetName(email);
        } else {
            setGreeting("User");
        }

        btnSubmitUserData.setOnClickListener(v -> {
            validateAndCalculateBMI();

            tvBmiResult.setText("BMI: " + String.format(Locale.US, "%.2f", bmi));
            tvBmiResult.setVisibility(TextView.VISIBLE);

            tvBmiStatus.setText(statusText);
            tvBmiStatus.setTextColor(color);
            tvBmiStatus.setVisibility(TextView.VISIBLE);

            btnViewBMI.setVisibility(Button.VISIBLE);
        });

        btnViewBMI.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterUserActivity.this, BMIResultActivity.class);
            intent.putExtra("BMI_VALUE", bmi);
            intent.putExtra("BMI_STATUS", statusText);
            startActivity(intent);
        });

        // Updated btnAction click listener to open HistoryActivity
        btnAction.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterUserActivity.this, HistoryActivity.class);
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
                                }
                            }
                            break;
                        }
                    }
                });
    }

    private void fetchAndSetName(String email) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        String name = doc.getString("name"); // adjust field name if needed
                        if (name != null && !name.isEmpty()) {
                            setGreeting(name);
                        } else {
                            setGreeting("User");
                        }
                    } else {
                        setGreeting("User");
                    }
                })
                .addOnFailureListener(e -> setGreeting("User"));
    }

    private void setGreeting(String name) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String greeting;

        if (hour < 12) {
            greeting = "Good Morning";
        } else if (hour < 17) {
            greeting = "Good Afternoon";
        } else {
            greeting = "Good Evening";
        }

        tvGreeting.setText(greeting + ", " + name);
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

            Map<String, Object> reportData = new HashMap<>();
            reportData.put("userId", userId);
            reportData.put("age", age);
            reportData.put("height", heightCm);
            reportData.put("weight", weightKg);
            reportData.put("bmi", bmi);
            reportData.put("bmiStatus", statusText);
            reportData.put("disease1", cbDiabetes.isChecked() ? "Diabetes" : "None");
            reportData.put("disease2", cbCholesterol.isChecked() ? "Cholesterol" : "None");
            reportData.put("disease3", cbHeart.isChecked() ? "Heart Disease" : "None");
            reportData.put("disease4", cbCancer.isChecked() ? "Cancer" : "None");
            reportData.put("date", new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));

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
