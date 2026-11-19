
package com.example.healthmate;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Button btnAddRecord, btnViewRecords;
    private TextView txtTodayDate;
    private TextView txtSteps, txtCalories, txtWater;
    private HealthChartView chartSteps;
    private HealthChartView chartCalories;
    private HealthChartView chartWater;
    private DatabaseHelper databaseHelper;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);
        btnAddRecord = findViewById(R.id.btnAddRecord);
        btnViewRecords = findViewById(R.id.btnViewRecords);
        txtTodayDate = findViewById(R.id.txtTodayDate);
        txtSteps = findViewById(R.id.txtSteps);
        txtCalories = findViewById(R.id.txtCalories);
        txtWater = findViewById(R.id.txtWater);

        chartSteps = findViewById(R.id.chartSteps);
        chartCalories = findViewById(R.id.chartCalories);
        chartWater = findViewById(R.id.chartWater);

        btnAddRecord.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this,AddRecordActivity.class);
            startActivity(intent);
        });
        btnViewRecords.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this,RecordsActivity.class);
            startActivity(intent);
        });
    }
    protected void onResume() {
        super.onResume();
        loadTodaySummary();
        loadWeeklyCharts();
    }
    private void loadTodaySummary() {
        String today = dateFormat.format( Calendar.getInstance().getTime());
        txtTodayDate.setText("Today: " + today);
        long totalSteps = 0;
        long totalCalories = 0;
        long totalWater = 0;

        Cursor cursor = databaseHelper.getTodayRecord(today);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    totalSteps += cursor.getInt( cursor.getColumnIndexOrThrow("steps"));
                    totalCalories += cursor.getInt( cursor.getColumnIndexOrThrow("calories"));
                    totalWater += cursor.getInt( cursor.getColumnIndexOrThrow("water"));
                }
            } finally {
                cursor.close();
            }
        }
        txtSteps.setText(totalSteps + " steps");
        txtCalories.setText(totalCalories + " kcal");
        txtWater.setText(totalWater + " ml");
    }
    private void loadWeeklyCharts() {
        Calendar calendar = Calendar.getInstance();
        String today = dateFormat.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_YEAR, -6);
        String startDate = dateFormat.format(calendar.getTime());
        Map<String, long[]> dailyTotals = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            String date = dateFormat.format(calendar.getTime());
            dailyTotals.put(date, new long[]{0, 0, 0});
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        Cursor cursor = databaseHelper.getRecordsForDateRange(startDate, today);
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    String date = cursor.getString( cursor.getColumnIndexOrThrow("date"));
                    long steps = cursor.getLong( cursor.getColumnIndexOrThrow("steps"));
                    long calories = cursor.getLong( cursor.getColumnIndexOrThrow("calories"));
                    long water = cursor.getLong( cursor.getColumnIndexOrThrow("water"));

                    long[] totals = dailyTotals.get(date);
                    if (totals != null) {
                        totals[0] += steps;
                        totals[1] += calories;
                        totals[2] += water;
                    }
                }
            } finally {
                cursor.close();
            }
        }
        String[] labels = new String[7];
        long[] stepsValues = new long[7];
        long[] caloriesValues = new long[7];
        long[] waterValues = new long[7];

        Calendar chartCalendar = Calendar.getInstance();
        chartCalendar.add(Calendar.DAY_OF_YEAR, -6);

        for (int i = 0; i < 7; i++) {
            String date = dateFormat.format( chartCalendar.getTime());
            labels[i] = dayFormat.format( chartCalendar.getTime());
            long[] totals = dailyTotals.get(date);
            if (totals != null) {
                stepsValues[i] = totals[0];
                caloriesValues[i] = totals[1];
                waterValues[i] = totals[2];
            }
            chartCalendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        chartSteps.setData( labels, stepsValues, Color.rgb(37, 99, 235));
        chartCalories.setData( labels, caloriesValues, Color.rgb(232, 93, 93));
        chartWater.setData( labels, waterValues, Color.rgb(8, 145, 178));
    }
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
