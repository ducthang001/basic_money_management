package com.app.moneyapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
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
import android.widget.RadioGroup;
import android.widget.TextView;

import com.app.moneyapp.Database.DatabaseHelper;
import com.app.moneyapp.Models.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TransferActivity extends AppCompatActivity {

    private static final String TAG = "TransferActivity";

    private EditText edtTxtAmount, edtTxtDescription, edtTxtRecipient, edtTxtDate;
    private TextView txtWarning;
    private Button btnPickDate, btnAdd;
    private RadioGroup rgType;

    private DatabaseHelper databaseHelper;

    private AddTransaction addTransaction;

    private Calendar calendar = Calendar.getInstance();

    private DatePickerDialog.OnDateSetListener initDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            calendar.set(Calendar.YEAR, i);
            calendar.set(Calendar.MONTH, i1);
            calendar.set(Calendar.DAY_OF_MONTH, i2);
            edtTxtDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime()));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        initViews();

        setOnClickListeners();
    }

    private void setOnClickListeners() {
        Log.d(TAG, "setOnClickListeners: started");
        btnPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(TransferActivity.this, initDateSetListener,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateData()) {
                    txtWarning.setVisibility(View.GONE);
                    initAdding();
                } else {
                    txtWarning.setVisibility(View.VISIBLE);
                    txtWarning.setText("Please fill the blanks");
                }
            }
        });
    }

    private boolean validateData() {
        Log.d(TAG, "validateData: started");
        if (edtTxtAmount.getText().toString().equals("")) {
            return false;
        }

        if (edtTxtDate.getText().toString().equals("")) {
            return false;
        }

        if (edtTxtRecipient.getText().toString().equals("")) {
            return false;
        }

        return true;
    }

    private void initAdding() {
        Log.d(TAG, "initAdding: started");
        Utils utils = new Utils(this);
        User user = utils.isUserLoggedIn();
        if (null != user) {
            addTransaction = new AddTransaction();
            addTransaction.execute(user.get_id());
        }
    }

    private class AddTransaction extends AsyncTask<Integer, Void, Void> {

        private double amount;
        private String recipient, date, description, type;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            this.amount = Double.valueOf(edtTxtAmount.getText().toString());
            this.recipient = edtTxtRecipient.getText().toString();
            this.date = edtTxtDate.getText().toString();
            this.description = edtTxtDescription.getText().toString();

            int rbId = rgType.getCheckedRadioButtonId();
            switch (rbId) {
                case R.id.btnReceive:

                    type = "receive";
                    break;
                case R.id.btnSend:

                    type = "send";
                    amount = -amount;
                    break;
                default:
                    break;
            }

            databaseHelper = new DatabaseHelper(TransferActivity.this);

        }

        @Override
        protected Void doInBackground(Integer... integers) {
            try {
                SQLiteDatabase db = databaseHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("amount", this.amount);
                values.put("recipient", recipient);
                values.put("date", date);
                values.put("type", type);
                values.put("description", description);
                values.put("user_id", integers[0]);

                long id = db.insert("transactions", null, values);
                Log.d(TAG, "doInBackground: new Transaction id: " + id);

                if (id != -1) {
                    Cursor cursor = db.query("users", new String[] {"remained_amount"}, "_id=?",
                            new String[] {String.valueOf(integers[0])}, null, null, null);

                    if (null != cursor) {
                        if (cursor.moveToFirst()) {
                            @SuppressLint("Range") double currentRemainedAmount = cursor.getDouble(cursor.getColumnIndex("remained_amount"));
                            cursor.close();
                            ContentValues newValues = new ContentValues();
                            newValues.put("remained_amount", currentRemainedAmount + amount);
                            int affectedRows = db.update("users", newValues, "_id=?", new String[] {String.valueOf(integers[0])});
                            Log.d(TAG, "doInBackGround: updateRows: " + affectedRows);

                            db.close();
                        } else {
                            cursor.close();
                            db.close();

                        }
                    } else {
                        db.close();
                    }
                } else {
                    db.close();
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);

            Intent intent = new Intent(TransferActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
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
    }

    private void initViews() {
        Log.d(TAG, "initViews: started");
        edtTxtAmount = (EditText) findViewById(R.id.edtTxtAmount);
        edtTxtDescription = (EditText) findViewById(R.id.edtTxtDesc);
        edtTxtRecipient = (EditText) findViewById(R.id.edtTxtRecipient);
        edtTxtDate = (EditText) findViewById(R.id.edtTxtDate);

        txtWarning = (TextView) findViewById(R.id.txtWarning);

        btnPickDate = (Button) findViewById(R.id.btnPickDate);

        btnAdd = (Button) findViewById(R.id.btnAddTransfer);

        rgType = (RadioGroup) findViewById(R.id.rgType);
    }
}