package com.app.moneyapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.app.moneyapp.Database.DatabaseHelper;
import com.app.moneyapp.Dialogs.AddTransactionDialog;
import com.app.moneyapp.Models.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AddInvestmentActivity extends AppCompatActivity {
    private static final String TAG = "AddInvestmentActivity";

    private EditText edtTxtName, edtTxtInitAmount, edtTxtROI, edtTxtInitDate, edtTxtFinishDate;
    private Button btnPickInitDate, btnPickFinishDate, btnAddInvestment;
    private TextView txtWarning;

    private Calendar initCalendar = Calendar.getInstance();
    private Calendar finishCalendar = Calendar.getInstance();

    private DatabaseHelper databaseHelper;

    private Utils utils;

    private AddTransaction addTransaction;
    private AddInvestment addInvestment;


    private DatePickerDialog.OnDateSetListener initDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            initCalendar.set(Calendar.YEAR, i);
            initCalendar.set(Calendar.MONTH, i1);
            initCalendar.set(Calendar.DAY_OF_MONTH, i2);
            edtTxtInitDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(initCalendar.getTime()));
        }
    };

    private DatePickerDialog.OnDateSetListener finishDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            finishCalendar.set(Calendar.YEAR, i);
            finishCalendar.set(Calendar.MONTH, i1);
            finishCalendar.set(Calendar.DAY_OF_MONTH, i2);
            edtTxtFinishDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(finishCalendar.getTime()));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_investment);

        initViews();

        utils = new Utils(this);

        databaseHelper = new DatabaseHelper(this);

        setOnClockListeners();
    }

    private void setOnClockListeners() {
        Log.d(TAG, "setOnClockListeners: started");
        btnPickInitDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(AddInvestmentActivity.this, initDateSetListener,
                        initCalendar.get(Calendar.YEAR), initCalendar.get(Calendar.MONTH), initCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        btnPickFinishDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(AddInvestmentActivity.this, finishDateSetListener,
                        finishCalendar.get(Calendar.YEAR), finishCalendar.get(Calendar.MONTH), finishCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        btnAddInvestment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateData()) {
                    txtWarning.setVisibility(View.GONE);
                    initAdding();
                } else {
                    txtWarning.setVisibility(View.VISIBLE);
                    txtWarning.setText("Please fill all the blanks");
                }
            }
        });
    }

    private void initAdding() {
        Log.d(TAG, "initAdding: started");
        addTransaction = new AddTransaction();
        User user = utils.isUserLoggedIn();
        if (null != user) {
            addTransaction.execute(user.get_id());
        }
    }

    private class AddTransaction extends AsyncTask<Integer, Void, Integer> {

        private String date, name;
        private double amount;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            this.date = edtTxtInitDate.getText().toString();
            this.name = edtTxtName.getText().toString();
            this.amount = -Double.valueOf(edtTxtInitAmount.getText().toString());
        }

        @Override
        protected Integer doInBackground(Integer... integers) {
            try {
                SQLiteDatabase db = databaseHelper.getWritableDatabase();

                ContentValues values = new ContentValues();
                values.put("amount", amount);
                values.put("recipient", name);
                values.put("date", date);
                values.put("description", "Initial amount for " + name + " investment");
                values.put("user_id", integers[0]);
                values.put("type", "investment");

                long id = db.insert("transactions", null, values);
                return (int) id;
            } catch (SQLException e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if (null != integer) {
                addInvestment = new AddInvestment();
                addInvestment.execute(integer);
            }
        }
    }

    private class AddInvestment extends AsyncTask<Integer, Void, Void> {

        private int userId;
        private String initDate,  finishDate, name;
        private double monthlyROI, amount;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            this.amount = Double.valueOf(edtTxtInitAmount.getText().toString());
            this.monthlyROI = Double.valueOf(edtTxtROI.getText().toString());
            this.name = edtTxtName.getText().toString();
            this.initDate = edtTxtInitDate.getText().toString();
            this.finishDate = edtTxtFinishDate.getText().toString();

            User user = utils.isUserLoggedIn();
            if (null != user) {
                this.userId = user.get_id();
            } else {
                this.userId = -1;
            }
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            if (userId != -1) {
                try {
                    SQLiteDatabase db = databaseHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put("name", name);
                    values.put("init_date", initDate);
                    values.put("finish_date", finishDate);
                    values.put("amount", amount);
                    values.put("monthly_roi", monthlyROI);
                    values.put("user_id", userId);
                    values.put("transaction_id", integers[0]);

                    long id = db.insert("investments", null, values);

                    if (id != -1) {
                        Cursor cursor = db.query("users", new String[] {"remained_amount"}, "_id=?",
                                new String[] {String.valueOf(userId)}, null, null, null);
                        if (null != cursor) {
                            if (cursor.moveToFirst()) {
                                @SuppressLint("Range") double currentRemainedAmount = cursor.getDouble(cursor.getColumnIndex("remained_amount"));
                                cursor.close();
                                ContentValues newValues = new ContentValues();
                                newValues.put("remained_amount", currentRemainedAmount - amount);
                                int affectedRows = db.update("users", newValues, "_id=?", new String[] {String.valueOf(userId)});
                                Log.d(TAG, "doInBackGround: updateRows: " + affectedRows);
                            } else {
                                cursor.close();
                            }
                        }
                    }

                    db.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date initDate = sdf.parse(edtTxtInitDate.getText().toString());
                calendar.setTime(initDate);
                int initMonths = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH);
                Date finishDate = sdf.parse(edtTxtFinishDate.getText().toString());
                calendar.setTime(finishDate);
                int finishMonths = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH);

                int difference = finishMonths - initMonths;

                int days = 0;

                for (int i = 0; i < difference; i++) {
                    days += 30;
                    Data data = new Data.Builder()
                            .putDouble("amount", amount * monthlyROI/100)
                            .putString("description", "Profit for " + name)
                            .putInt("user_id", userId)
                            .putString("recipient", name)
                            .build();
                    Constraints constraints = new Constraints.Builder()
                            .setRequiresBatteryNotLow(true)
                            .build();

                    OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(InvestmentWorker.class)
                            .setInputData(data)
                            .setConstraints(constraints)
                            .setInitialDelay(days, TimeUnit.DAYS)
                            .addTag("profit")
                            .build();

                    WorkManager.getInstance(AddInvestmentActivity.this).enqueue(request);
                    Intent intent = new Intent(AddInvestmentActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }


        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != addTransaction) {
            if (!addTransaction.isCancelled()) {
                addTransaction.cancel(true);
            }
        }

        if (null != addInvestment) {
            if (!addInvestment.isCancelled()) {
                addInvestment.cancel(true);
            }
        }
    }

    private boolean validateData() {
        Log.d(TAG, "validateData: started");
        if (edtTxtName.getText().toString().equals("")) {
            return false;
        }

        if (edtTxtInitDate.getText().toString().equals("")) {
            return false;
        }

        if (edtTxtFinishDate.getText().toString().equals("")) {
            return false;
        }

        if (edtTxtInitAmount.getText().toString().equals("")) {
            return false;
        }

        if (edtTxtROI.getText().toString().equals("")) {
            return false;
        }

        return true;
    }

    private void initViews() {
        Log.d(TAG, "initViews: started");

        edtTxtName = (EditText) findViewById(R.id.edtTxtName);
        edtTxtInitAmount = (EditText) findViewById(R.id.edtTxtInitAmount);
        edtTxtROI = (EditText) findViewById(R.id.edtTxtMonthlyROI);
        edtTxtInitDate = (EditText) findViewById(R.id.edtTxtInitDate);
        edtTxtFinishDate = (EditText) findViewById(R.id.edtTxtFinishDate);

        btnPickInitDate = (Button) findViewById(R.id.btnPickInitDate);
        btnPickFinishDate = (Button) findViewById(R.id.btnPickFinishDate);
        btnAddInvestment = (Button) findViewById(R.id.btnAddInvestment);

        txtWarning = (TextView) findViewById(R.id.txtWarning);
    }
}