package com.example.fitmate.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitmate.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;

public class HistoryActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private Spinner userEmailSpinner;
    private TextView historyTextView;

    private ArrayList<String> userEmailList = new ArrayList<>();
    private ArrayAdapter<String> emailAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        db = FirebaseFirestore.getInstance();

        userEmailSpinner = findViewById(R.id.userEmailSpinner);
        historyTextView = findViewById(R.id.historyTextView);

        emailAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, userEmailList);
        userEmailSpinner.setAdapter(emailAdapter);

        loadUserEmails(); // load list of emails from Firestore

        userEmailSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedEmail = userEmailList.get(position);
                fetchHistoryData(selectedEmail);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

    }

    private void loadUserEmails() {
        db.collection("History") // ✅ FIXED: Now uses top-level collection
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    HashSet<String> uniqueEmails = new HashSet<>();
                    for (DocumentSnapshot doc : querySnapshots) {
                        String email = doc.getString("userId");
                        if (email != null) {
                            uniqueEmails.add(email);
                        }
                    }
                    userEmailList.clear();
                    userEmailList.addAll(uniqueEmails);
                    emailAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load user emails", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error loading emails", e);
                });
    }

    private void fetchHistoryData(String userEmail) {
        db.collection("History") // ✅ FIXED: Now uses top-level collection
                .whereEqualTo("userId", userEmail)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    if (querySnapshots.isEmpty()) {
                        historyTextView.setText("No history data found.");
                        return;
                    }

                    StringBuilder builder = new StringBuilder();
                    for (DocumentSnapshot doc : querySnapshots) {
                        builder.append("📅 Date: ").append(doc.getString("date")).append("\n")
                                .append("🎂 Age: ").append(doc.getLong("age")).append("\n")
                                .append("⚖️ Weight: ").append(doc.getLong("weight")).append(" kg\n")
                                .append("📏 Height: ").append(doc.getLong("height")).append(" cm\n")
                                .append("🧮 BMI: ").append(doc.getDouble("bmi")).append("\n")
                                .append("📊 Status: ").append(doc.getString("bmiStatus")).append("\n")
                                .append("🦠 Disease 1: ").append(doc.getString("disease1")).append("\n")
                                .append("🦠 Disease 2: ").append(doc.getString("disease2")).append("\n")
                                .append("🦠 Disease 3: ").append(doc.getString("disease3")).append("\n")
                                .append("🦠 Disease 4: ").append(doc.getString("disease4")).append("\n")
                                .append("---------------------------------------------\n");
                    }
                    historyTextView.setText(builder.toString());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch history", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "History fetch error", e);
                });
    }
}