package com.app.moneyapp.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.app.moneyapp.AddInvestmentActivity;
import com.app.moneyapp.AddLoanActivity;
import com.app.moneyapp.R;
import com.app.moneyapp.ShoppingActivity;
import com.app.moneyapp.TransferActivity;

public class AddTransactionDialog extends DialogFragment {

    private static final String TAG = "AddTransactionDialog";

    private RelativeLayout shoping, investment, loan, transaction;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_transaction, null);
        shoping = view.findViewById(R.id.shoppingRelLayout);
        investment = (RelativeLayout) view.findViewById(R.id.investmentRelLayout);
        loan = (RelativeLayout) view.findViewById(R.id.loanRelLayout);
        transaction = (RelativeLayout) view.findViewById(R.id.transactionRelLayout);

        shoping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ShoppingActivity.class);
                startActivity(intent);
            }
        });

        investment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddInvestmentActivity.class);
                startActivity(intent);
            }
        });

        loan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddLoanActivity.class);
                startActivity(intent);
            }
        });

        transaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), TransferActivity.class);
                startActivity(intent);

            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Add Transaction")
                .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setView(view);

        return builder.create();
    }
}
