package com.example.interestapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText amountInput, rateInput, dateTakenInput, dateReturnedInput;
    private String dateTaken, dateReturned;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        amountInput = findViewById(R.id.amountInput);
        rateInput = findViewById(R.id.rateInput);
        dateTakenInput = findViewById(R.id.dateTakenInput);
        dateReturnedInput = findViewById(R.id.dateReturnedInput);
        Button calculateButton = findViewById(R.id.calculateButton);

        // Add formatting to the amount field
        addCurrencyFormatting(amountInput);

        // Set up date pickers for EditTexts
        dateTakenInput.setOnClickListener(v -> showDatePicker((date) -> {
            dateTaken = date;
            dateTakenInput.setText(date);
        }));

        dateReturnedInput.setOnClickListener(v -> showDatePicker((date) -> {
            dateReturned = date;
            dateReturnedInput.setText(date);
        }));

        calculateButton.setOnClickListener(v -> calculateInterest());
    }

    private void addCurrencyFormatting(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    editText.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[₹,]", "");
                    try {
                        double parsed = cleanString.isEmpty() ? 0 : Double.parseDouble(cleanString);
                        String formatted = NumberFormat.getNumberInstance(new Locale("en", "IN"))
                                .format(parsed);

                        current = formatted;
                        editText.setText(formatted);
                        editText.setSelection(formatted.length());
                    } catch (NumberFormatException e) {
                        // Handle invalid input gracefully
                    }

                    editText.addTextChangedListener(this);
                }
            }
        });
    }

    private void showDatePicker(DatePickerCallback callback) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.US, "%02d/%02d/%04d", dayOfMonth, month1 + 1, year1);
                    callback.onDateSelected(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void calculateInterest() {
        String amountStr = amountInput.getText().toString().replace(",", "");
        String rateStr = rateInput.getText().toString();

        if (amountStr.isEmpty() || rateStr.isEmpty() || dateTaken == null || dateReturned == null) {
            Toast.makeText(this, "Please fill all fields and select dates", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            double rate = Double.parseDouble(rateStr);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            Date startDate = sdf.parse(dateTaken);
            Date endDate = sdf.parse(dateReturned);

            Calendar startCalendar = Calendar.getInstance();
            Calendar endCalendar = Calendar.getInstance();
            startCalendar.setTime(startDate);
            endCalendar.setTime(endDate);

            int yearsDifference = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
            int monthsDifference = endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
            int totalMonths = yearsDifference * 12 + monthsDifference;

            int daysDifference = endCalendar.get(Calendar.DAY_OF_MONTH) - startCalendar.get(Calendar.DAY_OF_MONTH);
            if (daysDifference < 0) {
                // Borrow a month
                totalMonths -= 1;
                Calendar tempCalendar = (Calendar) endCalendar.clone();
                tempCalendar.add(Calendar.MONTH, -1);
                daysDifference += tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            }

            double interest = (amount * rate * (totalMonths + (double) daysDifference / 30)) / 100;
            double total = amount + interest;

            // Format results in Indian currency
            String interestFormatted = NumberFormat.getCurrencyInstance(new Locale("en", "IN"))
                    .format(interest);
            String totalFormatted = NumberFormat.getCurrencyInstance(new Locale("en", "IN"))
                    .format(total);

            // Show the result in an AlertDialog
            showResultDialog(interestFormatted, totalFormatted, totalMonths, daysDifference);
        } catch (NumberFormatException | ParseException e) {
            Toast.makeText(this, "Invalid input or date format", Toast.LENGTH_SHORT).show();
        }
    }

    private void showResultDialog(String interest, String total, int months, int days) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Calculation Result")
                .setMessage(String.format(Locale.US, "%d years, %d Months, %d days\n\nInterest: %s\n\nTotal Amount: %s",
                        months/12,months%12, days, interest, total))
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private interface DatePickerCallback {
        void onDateSelected(String date);
    }
}
