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
import com.app.moneyapp.Models.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AddLoanActivity extends AppCompatActivity {

    private static final String TAG = "AddLoanActivity";

    private EditText edtTxtName, edtTxtInitAmount, edtTxtROI, edtTxtInitDate, edtTxtFinishDate, edtTxtMonthlyPayment;
    private Button btnPickInitDate, btnPickFinishDate, btnAddLoan;
    private TextView txtWarning;

    private Calendar initCalendar = Calendar.getInstance();
    private Calendar finishCalendar = Calendar.getInstance();

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

    private Utils utils;

    private DatabaseHelper databaseHelper;

    private AddTransaction addTransaction;
    private AddLoan addLoan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_loan);

        initViews();

        databaseHelper = new DatabaseHelper(this);

        utils = new Utils(this);

        setOnClickListeners();
    }

    private void setOnClickListeners() {
        Log.d(TAG, "setOnClickListener: started");

        btnPickInitDate.setOnClickListener(view -> new DatePickerDialog(AddLoanActivity.this, initDateSetListener,
                initCalendar.get(Calendar.YEAR), initCalendar.get(Calendar.MONTH), initCalendar.get(Calendar.DAY_OF_MONTH)).show());

        btnPickFinishDate.setOnClickListener(view -> new DatePickerDialog(AddLoanActivity.this, finishDateSetListener,
                finishCalendar.get(Calendar.YEAR), finishCalendar.get(Calendar.MONTH), finishCalendar.get(Calendar.DAY_OF_MONTH)).show());

        btnAddLoan.setOnClickListener(v -> {
            if (validateData()) {
                txtWarning.setVisibility(View.GONE);
                initAdding();
            } else {
                txtWarning.setVisibility(View.VISIBLE);
                txtWarning.setText("Please fill all the blanks");
            }
        });

    }

    private void initAdding() {
        Log.d(TAG, "initAdding: started");
        User user = utils.isUserLoggedIn();
        if (null != user) {
            addTransaction = new AddTransaction();
            addTransaction.execute(user.get_id());
        }
    }

    private class AddTransaction extends AsyncTask<Integer, Void, Integer> {

        private double amount;
        private String date, name;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            this.amount = Double.valueOf(edtTxtInitAmount.getText().toString());
            this.name = edtTxtName.getText().toString();
            //this.amount = Double.valueOf(edtTxtInitAmount.getText().toString());
            this.date = edtTxtInitDate.getText().toString();

        }

        @Override
        protected Integer doInBackground(Integer... integers) {
            try {
                SQLiteDatabase db = databaseHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("amount", amount);
                values.put("recipient", name);
                values.put("date", date);
                values.put("user_id", integers[0]);
                values.put("description", "Received amount for " + name + " Loan");
                values.put("type", "loan");

                long transactionId = db.insert("transactions", null, values);
                db.close();
                return (int) transactionId;

            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            if (null != integer) {
                addLoan = new AddLoan();
                addLoan.execute(integer);
            }
        }
    }

    private class AddLoan extends AsyncTask<Integer, Void, Integer> {

        private int userId;
        private String name, initDate, finishDate;
        private double initAmount, monthlyROI, monthlyPayment;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            this.name = edtTxtName.getText().toString();
            this.initDate = edtTxtInitDate.getText().toString();
            this.finishDate = edtTxtFinishDate.getText().toString();
            this.initAmount = Double.valueOf(edtTxtInitAmount.getText().toString());
            this.monthlyROI = Double.valueOf(edtTxtROI.getText().toString());
            this.monthlyPayment = Double.valueOf(edtTxtMonthlyPayment.getText().toString());

            User user = utils.isUserLoggedIn();
            if (null != user) {
                this.userId = user.get_id();
            } else {
                this.userId = -1;
            }
        }

        @Override
        protected Integer doInBackground(Integer... integers) {

            if (userId != -1) {
                try {
                    SQLiteDatabase db = databaseHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put("name", name);
                    values.put("init_date", initDate);
                    values.put("finish_date", finishDate);
                    values.put("init_amount", initAmount);
                    values.put("remained_amount", initAmount);
                    values.put("monthly_roi", monthlyROI);
                    values.put("monthly_payment", monthlyPayment);
                    values.put("user_id", userId);
                    values.put("transaction_id", integers[0]);

                    long loanId = db.insert("loans", null, values);

                    if (loanId != -1) {
                        Cursor cursor = db.query("users", new String[] {"remained_amount"}, "_id=?",
                                new String[] {String.valueOf(userId)}, null, null, null);

                        if (null != cursor) {
                            if (cursor.moveToFirst()) {
                                @SuppressLint("Range") double currentRemainedAmount = cursor.getDouble(cursor.getColumnIndex("remained_amount"));
                                ContentValues newValues = new ContentValues();
                                newValues.put("remained_amount", currentRemainedAmount + initAmount);
                                db.update("users", newValues, "_id=?",new String[] {String.valueOf(userId)});
                                cursor.close();
                                db.close();
                                return (int) loanId;
                            } else {
                                cursor.close();
                                db.close();
                                return null;
                            }
                        } else {
                            db.close();
                            return null;
                        }
                    } else {
                        db.close();
                        return null;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            if (null != integer) {
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                try {
                    Date initDate = sdf.parse(this.initDate);
                    calendar.setTime(initDate);
                    int initMonth = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH);

                    Date finishDate = sdf.parse(this.finishDate);
                    calendar.setTime(finishDate);
                    int finishMonth = calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH);

                    int months = finishMonth - initMonth;

                    int days = 0;

                    for (int i = 0; i < months; i++) {
                        days += 30;

                        Data data = new Data.Builder()
                                .putInt("loan_id", integer)
                                .putInt("user_id", userId)
                                .putDouble("monthly_payment", monthlyPayment)
                                .putString("name", name)
                                .build();

                        Constraints constraints = new Constraints.Builder()
                                .setRequiresBatteryNotLow(true)
                                .build();

                        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(LoanWorker.class)
                                .setInputData(data)
                                .setConstraints(constraints)
                                .setInitialDelay(days, TimeUnit.DAYS)
                                .addTag("loan_payment")
                                .build();

                        WorkManager.getInstance(AddLoanActivity.this).enqueue(request);
                        Intent intent = new Intent(AddLoanActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
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

        if (null != addLoan) {
            if (!addLoan.isCancelled()) {
                addLoan.cancel(true);
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

        if (edtTxtMonthlyPayment.getText().toString().equals("")) {
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
        edtTxtMonthlyPayment = (EditText) findViewById(R.id.edtTxtMonthlyPayment);

        btnPickInitDate = (Button) findViewById(R.id.btnPickInitDate);
        btnPickFinishDate = (Button) findViewById(R.id.btnPickFinishDate);
        btnAddLoan = (Button) findViewById(R.id.btnAddLoan);

        txtWarning = (TextView) findViewById(R.id.txtWarning);
    }
}