
package com.example.healthmate;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class RecordsActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private ListView listHealthRecords;
    private TextView txtRecordCount;
    private TextView txtTotalSteps, txtTotalCalories, txtTotalWater;
    private EditText edtSearchDate;
    private Button btnBack, btnSearch, btnClearFilter;
    private ArrayList<HealthRecord> healthRecords = new ArrayList<>();
    private String activeDateFilter = null;
    private static class HealthRecord {
        int id;
        String date;
        int steps;
        int calories;
        int water;
        HealthRecord(int id, String date, int steps,int calories, int water) {
            this.id = id;
            this.date = date;
            this.steps = steps;
            this.calories = calories;
            this.water = water;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);
        databaseHelper = new DatabaseHelper(this);
        listHealthRecords = findViewById(R.id.listHealthRecords);
        txtRecordCount = findViewById(R.id.txtRecordCount);
        txtTotalSteps = findViewById(R.id.txtTotalSteps);
        txtTotalCalories = findViewById(R.id.txtTotalCalories);
        txtTotalWater = findViewById(R.id.txtTotalWater);
        edtSearchDate = findViewById(R.id.edtSearchDate);
        btnBack = findViewById(R.id.btnBack);
        btnSearch = findViewById(R.id.btnSearch);
        btnClearFilter = findViewById(R.id.btnClearFilter);
        btnBack.setOnClickListener(v -> finish());
        edtSearchDate.setOnClickListener(v -> showDatePicker());
        btnSearch.setOnClickListener(v -> searchRecords());
        btnClearFilter.setOnClickListener(v -> {
            edtSearchDate.setText("");
            activeDateFilter = null;
            loadHealthRecords(null);
        });
        loadHealthRecords(null);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String date = String.format(Locale.US,"%04d-%02d-%02d",year,month + 1,dayOfMonth);
                    edtSearchDate.setText(date); },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.getDatePicker().setMaxDate( System.currentTimeMillis());
        datePicker.show();
    }
    private void searchRecords() {
        String date = edtSearchDate.getText().toString().trim();
        if (date.isEmpty()) {
            Toast.makeText(this,"Please select a date",Toast.LENGTH_SHORT).show();
            return;
        }
        activeDateFilter = date;
        loadHealthRecords(date);
    }
    private void loadHealthRecords(String dateFilter) {
        healthRecords.clear();
        long totalSteps = 0;
        long totalCalories = 0;
        long totalWater = 0;
        Cursor cursor;
        if (dateFilter == null) {
            cursor = databaseHelper.getAllRecords();
        } else {
            cursor = databaseHelper.getTodayRecord(dateFilter);
        }
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                    int steps = cursor.getInt(cursor.getColumnIndexOrThrow("steps"));
                    int calories = cursor.getInt(cursor.getColumnIndexOrThrow("calories"));
                    int water = cursor.getInt(cursor.getColumnIndexOrThrow("water"));
                    healthRecords.add(new HealthRecord(id, date, steps, calories, water));
                    totalSteps += steps;
                    totalCalories += calories;
                    totalWater += water;
                }
            } finally {
                cursor.close();
            }
        }
        txtRecordCount.setText("Total Records: " + healthRecords.size());
        txtTotalSteps.setText(String.format(Locale.getDefault(), "%,d", totalSteps));
        txtTotalCalories.setText(String.format(Locale.getDefault(), "%,d", totalCalories));
        txtTotalWater.setText(String.format(Locale.getDefault(), "%,d", totalWater));
        if (healthRecords.isEmpty()) {
            healthRecords.add(new HealthRecord(-1,"No health records found",0,0,0));
        }
        ArrayAdapter<HealthRecord> adapter = new ArrayAdapter<HealthRecord>(this, 0, healthRecords) {

                    public View getView(int position,View convertView,ViewGroup parent) {
                        View row = convertView;
                        if (row == null) {
                            row = LayoutInflater.from(RecordsActivity.this)
                                    .inflate(R.layout.item_health_record,parent,false);
                        }
                        HealthRecord record = getItem(position);
                        TextView txtNumber = row.findViewById(R.id.txtRecordNumber);
                        TextView txtDate = row.findViewById(R.id.txtRecordDate);
                        TextView txtSteps = row.findViewById(R.id.txtRecordSteps);
                        TextView txtCalories = row.findViewById(R.id.txtRecordCalories);
                        TextView txtWater = row.findViewById(R.id.txtRecordWater);
                        Button btnEdit = row.findViewById(R.id.btnEditRecord);
                        Button btnDelete = row.findViewById(R.id.btnDeleteRecord);

                        if (record.id == -1) {
                            txtNumber.setText(record.date);
                            txtDate.setVisibility(View.GONE);
                            txtSteps.setVisibility(View.GONE);
                            txtCalories.setVisibility(View.GONE);
                            txtWater.setVisibility(View.GONE);
                            btnEdit.setVisibility(View.GONE);
                            btnDelete.setVisibility(View.GONE);
                        } else {
                            txtNumber.setText("Health Record #" + record.id);
                            txtDate.setVisibility(View.VISIBLE);
                            txtSteps.setVisibility(View.VISIBLE);
                            txtCalories.setVisibility(View.VISIBLE);
                            txtWater.setVisibility(View.VISIBLE);
                            btnEdit.setVisibility(View.VISIBLE);
                            btnDelete.setVisibility(View.VISIBLE);
                            txtDate.setText("Date: " + record.date);
                            txtSteps.setText("👟 Steps: " + record.steps);
                            txtCalories.setText("🔥 Calories: " + record.calories + " kcal");
                            txtWater.setText("💧 Water: " + record.water + " ml");
                            btnEdit.setOnClickListener(v ->openEditScreen(record.id));
                            btnDelete.setOnClickListener(v ->confirmDelete(record.id));
                        }
                        return row;
                    }
                };

        listHealthRecords.setAdapter(adapter);
        listHealthRecords.setOnItemClickListener(
                (parent, view, position, id) -> {
                    HealthRecord record = healthRecords.get(position);
                    if (record.id != -1) {
                        showRecordDetails(record);
                    }
                }
        );
    }

    private void showRecordDetails(HealthRecord record) {
        String details = "Date: " + record.date + "\n\n"
                        + "Steps: " + record.steps + "\n\n"
                        + "Calories Burned: " + record.calories + " kcal\n\n"
                        + "Water Intake: " + record.water + " ml";

        new AlertDialog.Builder(this).setTitle("Health Record #" + record.id).setMessage(details)
                .setPositiveButton("Edit", (dialog, which) ->openEditScreen(record.id))
                .setNegativeButton("Delete", (dialog, which) ->confirmDelete(record.id))
                .setNeutralButton("Close", null).show();
    }

    private void openEditScreen(int recordId) {
        Intent intent = new Intent(RecordsActivity.this,AddRecordActivity.class);
        intent.putExtra("record_id", recordId);
        startActivity(intent);
    }
    private void confirmDelete(int recordId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Health Record")
                .setMessage("Are you sure you want to delete this record?")
                .setPositiveButton("Yes, Delete", (dialog, which) -> {
                    boolean deleted = databaseHelper.deleteRecord(recordId);
                    if (deleted) {
                        Toast.makeText(this,"Record deleted successfully",Toast.LENGTH_SHORT).show();
                        loadHealthRecords(activeDateFilter);
                    } else {
                        Toast.makeText(this,"Failed to delete record",Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null).show();
    }
    protected void onResume() {
        super.onResume();
        if (databaseHelper != null && listHealthRecords != null) {
            loadHealthRecords(activeDateFilter);
        }
    }
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
