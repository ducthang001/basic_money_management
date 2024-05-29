package com.app.moneyapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.app.moneyapp.Database.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LoanWorker extends Worker {
    private static final String TAG = "LoanWorker";

    private DatabaseHelper databaseHelper;

    public LoanWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        databaseHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data data = getInputData();
        int loanId = data.getInt("loan_id", -1);
        int userId = data.getInt("user_id", -1);
        double monthlyPayment = data.getDouble("monthly_payment", 0.0);
        String name = data.getString("name");

        if (loanId == -1 || userId == -1 || monthlyPayment == 0.0) {
            return Result.failure();
        }

        try {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            ContentValues transactionValues = new ContentValues();
            transactionValues.put("amount", -monthlyPayment);
            transactionValues.put("user_id", userId);
            transactionValues.put("type", "loan_payment");
            transactionValues.put("description", "Monthly payment for " + name + " Loan");
            transactionValues.put("recipient", name);

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String date = sdf.format(calendar.getTime());
            transactionValues.put("date", date);

            long transactionId = db.insert("transactions", null, transactionValues);

            if (transactionId == -1) {
                return Result.failure();
            } else {
                Cursor cursor = db.query("users", new String[] {"remained_amount"}, "_id=?",
                        new String[] {String.valueOf(userId)}, null, null, null);
                if (null != cursor) {
                    @SuppressLint("Range") double currentRemainedAmount = cursor.getDouble(cursor.getColumnIndex("remained_amount"));
                    ContentValues values = new ContentValues();
                    values.put("remained_amount", currentRemainedAmount - monthlyPayment);
                    db.update("users", values, "_id=?", new String[] {String.valueOf(userId)});

                    cursor.close();

                    Cursor secondCursor = db.query("loans", new String[] {"remained_amount"}, "_id=?",
                            new String[] {String.valueOf(loanId)}, null, null, null);
                    if (null != secondCursor) {
                        if (secondCursor.moveToFirst()) {
                            @SuppressLint("Range") double currentLoanAmount = secondCursor.getDouble(secondCursor.getColumnIndex("remained_amount"));
                            ContentValues secondValues = new ContentValues();
                            secondValues.put("remained_amount", currentLoanAmount - monthlyPayment);
                            db.update("loans", secondValues, "_id=?", new String[] {String.valueOf(loanId)});
                            secondCursor.close();
                            db.close();
                            return Result.success();
                        } else {
                            secondCursor.close();
                            db.close();
                            return Result.failure();
                        }
                    } else {
                        db.close();
                        return Result.failure();
                    }

                } else {
                    db.close();
                    return Result.failure();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
}
