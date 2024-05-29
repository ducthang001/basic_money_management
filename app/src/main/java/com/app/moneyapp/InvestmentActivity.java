package com.app.moneyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.app.moneyapp.Adapters.InvestmentAdapter;
import com.app.moneyapp.Database.DatabaseHelper;
import com.app.moneyapp.Models.Investment;
import com.app.moneyapp.Models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class InvestmentActivity extends AppCompatActivity {

    private static final String TAG = "InvestmentActivity";

    private RecyclerView recyclerView;
    private BottomNavigationView bottomNavigationView;
    private DatabaseHelper databaseHelper;
    private GetInvestments getInvestments;
    private InvestmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_investment);

        initViews();
        initBottomNavView();

        adapter = new InvestmentAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        databaseHelper = new DatabaseHelper(this);

        Utils utils = new Utils(this);
        User user = utils.isUserLoggedIn();
        if (null != user) {
            getInvestments = new GetInvestments();
            getInvestments.execute(user.get_id());
        }
    }

    private class GetInvestments extends AsyncTask<Integer, Void, ArrayList<Investment>> {

        @SuppressLint("Range")
        @Override
        protected ArrayList<Investment> doInBackground(Integer... integers) {
            try {
                SQLiteDatabase db = databaseHelper.getReadableDatabase();
                Cursor cursor = db.query("investments", null, "user_id=?",
                        new String[] {String.valueOf(integers[0])}, null, null, "init_date DESC");
                if (null != cursor) {
                    if (cursor.moveToFirst()) {
                        ArrayList<Investment> investments = new ArrayList<>();
                        for (int i = 0; i < cursor.getCount(); i++) {
                            Investment investment = new Investment();
                            investment.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                            investment.setUser_id(cursor.getInt(cursor.getColumnIndex("user_id")));
                            investment.setTransaction_id(cursor.getInt(cursor.getColumnIndex("transaction_id")));
                            investment.setAmount(cursor.getDouble(cursor.getColumnIndex("amount")));
                            investment.setFinish_date(cursor.getString(cursor.getColumnIndex("finish_date")));
                            investment.setInit_date(cursor.getString(cursor.getColumnIndex("init_date")));
                            investment.setMonthly_roi(cursor.getDouble(cursor.getColumnIndex("monthly_roi")));
                            investment.setName(cursor.getString(cursor.getColumnIndex("name")));

                            investments.add(investment);
                            cursor.moveToNext();
                        }

                        cursor.close();
                        db.close();
                        return investments;
                    } else {
                        cursor.close();
                        db.close();
                        return null;
                    }
                } else {
                    db.close();
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Investment> investments) {
            super.onPostExecute(investments);

            if (null != investments) {
                Log.d(TAG, "investment set adapter");
                adapter.setInvestments(investments);
            } else {
                adapter.setInvestments(new ArrayList<Investment>());
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != getInvestments) {
            if (!getInvestments.isCancelled()) {
                getInvestments.cancel(true);
            }
        }
    }

    private void initViews() {
        Log.d(TAG, "initViews: started");
        recyclerView = (RecyclerView) findViewById(R.id.investmentRecView);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavView);
    }

    private void initBottomNavView() {
        Log.d(TAG, "initBottomNavView: started");
        bottomNavigationView.setSelectedItemId(R.id.menu_item_investment);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.menu_item_stats:
                        Intent statsIntent = new Intent(InvestmentActivity.this, StatsActivity.class);
                        statsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(statsIntent);
                        break;
                    case R.id.menu_item_transaction:
                        Intent transactionIntent = new Intent(InvestmentActivity.this, TransactionActivity.class);
                        transactionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(transactionIntent);
                        break;
                    case R.id.menu_item_home:
                        Intent homeIntent = new Intent(InvestmentActivity.this, MainActivity.class);
                        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(homeIntent);
                        break;
                    case R.id.menu_item_loan:
                        Intent loanIntent = new Intent(InvestmentActivity.this, LoanActivity.class);
                        loanIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(loanIntent);
                        break;
                    case R.id.menu_item_investment:

                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

}