package com.example.interestapp;

import static android.net.Uri.*;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
    private ImageView refreshButton,openLinkButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        amountInput = findViewById(R.id.amountInput);
        rateInput = findViewById(R.id.rateInput);
        dateTakenInput = findViewById(R.id.dateTakenInput);
        dateReturnedInput = findViewById(R.id.dateReturnedInput);
        Button calculateButton = findViewById(R.id.calculateButton);
        refreshButton = findViewById(R.id.refreshButton);
        openLinkButton = findViewById(R.id.openLinkButton);
        refreshButton.setOnClickListener(v -> resetFields());
        openLinkButton.setOnClickListener(v -> openWebLink());
        // Add currency formatting to amount field
        addCurrencyFormatting(amountInput);

        // Add automatic date formatting
        setupDateInput(dateTakenInput);
        setupDateInput(dateReturnedInput);

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
                    } catch (NumberFormatException ignored) {}

                    editText.addTextChangedListener(this);
                }
            }
        });
    }

    private void setupDateInput(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private String current = "";
            private final String format = "DD/MM/YYYY";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString().replace("/", "");

                if (!input.equals(current)) {
                    editText.removeTextChangedListener(this);

                    String formattedDate = "";
                    if (input.length() <= 2) {
                        formattedDate = input;
                    } else if (input.length() <= 4) {
                        formattedDate = input.substring(0, 2) + "/" + input.substring(2);
                    } else if (input.length() <= 8) {
                        formattedDate = input.substring(0, 2) + "/" + input.substring(2, 4) + "/" + input.substring(4);
                    } else {
                        formattedDate = input.substring(0, 2) + "/" + input.substring(2, 4) + "/" + input.substring(4, 8);
                    }

                    current = formattedDate;
                    editText.setText(formattedDate);
                    editText.setSelection(formattedDate.length());

                    editText.addTextChangedListener(this);
                }
            }
        });
    }
    private void resetFields() {
        amountInput.setText("");
        rateInput.setText("");
        dateTakenInput.setText("");
        dateReturnedInput.setText("");

        amountInput.requestFocus(); // Set focus back to first input field

        Toast.makeText(this, "All fields have been reset!", Toast.LENGTH_SHORT).show();
    }
    private void openWebLink() {
        String url = "https://drive.google.com/uc?export=download&id=1nRexTqODkN9v2MiIZZb9PsC4gi6D54Ub"; // Replace with your desired URL
        Intent intent = new Intent(Intent.ACTION_VIEW, parse(url));
        startActivity(intent);
    }
    private void calculateInterest() {
        String amountStr = amountInput.getText().toString().replace(",", "").trim();
        String rateStr = rateInput.getText().toString().trim();
        String dateTaken = dateTakenInput.getText().toString().trim();
        String dateReturned = dateReturnedInput.getText().toString().trim();

        if (amountStr.isEmpty() || rateStr.isEmpty() || dateTaken.isEmpty() || dateReturned.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            double rate = Double.parseDouble(rateStr);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            sdf.setLenient(false); // Ensures strict date format validation

            Date startDate = sdf.parse(dateTaken);
            Date endDate = sdf.parse(dateReturned);

            if (endDate.before(startDate)) {
                Toast.makeText(this, "Date Returned cannot be before Date Taken!", Toast.LENGTH_SHORT).show();
                return;
            }

            Calendar startCalendar = Calendar.getInstance();
            Calendar endCalendar = Calendar.getInstance();
            startCalendar.setTime(startDate);
            endCalendar.setTime(endDate);

            int yearsDifference = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
            int monthsDifference = endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
            int totalMonths = yearsDifference * 12 + monthsDifference;

            int daysDifference = endCalendar.get(Calendar.DAY_OF_MONTH) - startCalendar.get(Calendar.DAY_OF_MONTH);
            if (daysDifference < 0) {
                totalMonths -= 1;
                Calendar tempCalendar = (Calendar) endCalendar.clone();
                tempCalendar.add(Calendar.MONTH, -1);
                daysDifference += tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            }

            double interest = (amount * rate * (totalMonths + (double) daysDifference / 30)) / 100;
            double total = amount + interest;

            String interestFormatted = NumberFormat.getCurrencyInstance(new Locale("en", "IN")).format(interest);
            String totalFormatted = NumberFormat.getCurrencyInstance(new Locale("en", "IN")).format(total);

            showResultDialog(interestFormatted, totalFormatted, totalMonths, daysDifference);
        } catch (NumberFormatException | ParseException e) {
            Toast.makeText(this, "Invalid input or date format (dd/MM/yyyy)", Toast.LENGTH_SHORT).show();
        }
    }

    private void showResultDialog(String interest, String total, int months, int days) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Calculation Result")
                .setMessage(String.format(Locale.US, "%d years, %d Months, %d days\n\nInterest: %s\n\nTotal Amount: %s",
                        months / 12, months % 12, days, interest, total))
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }
}
