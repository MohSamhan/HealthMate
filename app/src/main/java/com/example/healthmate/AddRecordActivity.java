
package com.example.healthmate;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddRecordActivity extends AppCompatActivity {

    private EditText edtDate, edtSteps, edtCalories, edtWater;
    private Button btnSaveRecord, btnCancel;
    private DatabaseHelper databaseHelper;
    private int recordId = -1;
    private boolean isEditMode = false;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_record);
        dateFormat.setLenient(false);
        databaseHelper = new DatabaseHelper(this);
        edtDate = findViewById(R.id.edtDate);
        edtSteps = findViewById(R.id.edtSteps);
        edtCalories = findViewById(R.id.edtCalories);
        edtWater = findViewById(R.id.edtWater);
        btnSaveRecord = findViewById(R.id.btnSaveRecord);
        btnCancel = findViewById(R.id.btnCancel);
        recordId = getIntent().getIntExtra("record_id", -1);
        isEditMode = recordId != -1;
        if (isEditMode) {
            loadRecordForEditing();
            btnSaveRecord.setText("Update Record");
        } else {
            setCurrentDate();
        }
        edtDate.setOnClickListener(v -> showDatePicker());
        btnSaveRecord.setOnClickListener(v -> saveRecord());
        btnCancel.setOnClickListener(v -> finish());
    }

    // SET CURRENT DATE
    private void setCurrentDate() {
        edtDate.setText( dateFormat.format(Calendar.getInstance().getTime()));
    }
    // LOAD EXISTING RECORD
    private void loadRecordForEditing() {
        Cursor cursor = databaseHelper.getRecordById(recordId);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {

                    edtDate.setText(cursor.getString(
                            cursor.getColumnIndexOrThrow("date")));
                    edtSteps.setText(String.valueOf(cursor.getInt(
                            cursor.getColumnIndexOrThrow("steps"))));
                    edtCalories.setText(String.valueOf(cursor.getInt(
                            cursor.getColumnIndexOrThrow("calories"))));
                    edtWater.setText(String.valueOf(cursor.getInt(
                            cursor.getColumnIndexOrThrow("water"))));
                } else {
                    Toast.makeText(this,
                            "Record not found",
                            Toast.LENGTH_SHORT).show();

                    finish();
                }

            } finally {
                cursor.close();
            }
        }
    }

    // SHOW DATE PICKER
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format(
                            Locale.US,
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            dayOfMonth);
                    edtDate.setText(selectedDate);
                    edtDate.setError(null); },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePicker.getDatePicker().setMaxDate( System.currentTimeMillis());
        datePicker.show();
    }

    private boolean isValidDate(String date) {

        ParsePosition position = new ParsePosition(0);
        java.util.Date parsedDate = dateFormat.parse(date, position);
        if (parsedDate == null || position.getIndex() != date.length()) {
            return false;
        }
        String today = dateFormat.format( Calendar.getInstance().getTime());
        return date.compareTo(today) <= 0;
    }

     private Integer getValidatedNumber(EditText editText, String fieldName) {

        String value = editText.getText().toString().trim();
        if (value.isEmpty()) {
            editText.setError("Please enter " + fieldName);
            editText.requestFocus();
            return null;
        }
        try {
            int number = Integer.parseInt(value);
            if (number < 0) {
                editText.setError("Value cannot be negative");
                editText.requestFocus();
                return null;
            }
            editText.setError(null);
            return number;
        } catch (NumberFormatException e) {
            editText.setError("Please enter a valid whole number");
            editText.requestFocus();
            return null;
        }
    }

    private void saveRecord() {

        String date = edtDate.getText().toString().trim();
        if (date.isEmpty()) {
            edtDate.setError("Please select a date");
            edtDate.requestFocus();
            return;
        }
        if (!isValidDate(date)) {
            edtDate.setError("Enter a valid date (yyyy-MM-dd), not a future date");
            edtDate.requestFocus();
            return;
        }
        Integer stepsValue = getValidatedNumber(
                edtSteps, "steps walked");
        if (stepsValue == null) {
            return;
        }
        Integer caloriesValue = getValidatedNumber(
                edtCalories, "calories burned");
        if (caloriesValue == null) {
            return;
        }
        Integer waterValue = getValidatedNumber(
                edtWater, "water intake");
        if (waterValue == null) {
            return;
        }
        boolean success;
        if (isEditMode) {
            success = databaseHelper.updateRecord(
                    recordId,date,stepsValue,caloriesValue,waterValue);
        } else {
            success = databaseHelper.insertRecord(
                    date,stepsValue,caloriesValue,waterValue);
        }
        if (success) {
            String message = isEditMode
                    ? "Health record updated successfully!"
                    : "Health record saved successfully!";
            Toast.makeText(this,message,Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this,isEditMode
                            ? "Failed to update health record"
                            : "Failed to save health record",
                    Toast.LENGTH_SHORT).show();
        }
    }

    protected void onDestroy() {
        super.onDestroy();

        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
