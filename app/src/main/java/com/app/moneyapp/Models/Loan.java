package com.app.moneyapp.Models;

public class Loan {
    private int _id;
    private String name;
    private double init_amount;
    private double remained_amount;
    private double monthly_roi;
    private double monthly_payment;
    private int user_id;
    private int transaction_id;
    private String init_date;
    private String finish_date;

    public Loan() {
    }

    public Loan(int _id, String name, double init_amount, double remained_amount, double monthly_roi, double monthly_payment, int user_id, int transaction_id, String init_date, String finish_date) {
        this._id = _id;
        this.name = name;
        this.init_amount = init_amount;
        this.remained_amount = remained_amount;
        this.monthly_roi = monthly_roi;
        this.monthly_payment = monthly_payment;
        this.user_id = user_id;
        this.transaction_id = transaction_id;
        this.init_date = init_date;
        this.finish_date = finish_date;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getInit_amount() {
        return init_amount;
    }

    public void setInit_amount(double init_amount) {
        this.init_amount = init_amount;
    }

    public double getRemained_amount() {
        return remained_amount;
    }

    public void setRemained_amount(double remained_amount) {
        this.remained_amount = remained_amount;
    }

    public double getMonthly_roi() {
        return monthly_roi;
    }

    public void setMonthly_roi(double monthly_roi) {
        this.monthly_roi = monthly_roi;
    }

    public double getMonthly_payment() {
        return monthly_payment;
    }

    public void setMonthly_payment(double monthly_payment) {
        this.monthly_payment = monthly_payment;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(int transaction_id) {
        this.transaction_id = transaction_id;
    }

    public String getInit_date() {
        return init_date;
    }

    public void setInit_date(String init_date) {
        this.init_date = init_date;
    }

    public String getFinish_date() {
        return finish_date;
    }

    public void setFinish_date(String finish_date) {
        this.finish_date = finish_date;
    }

    @Override
    public String toString() {
        return "Loan{" +
                "_id=" + _id +
                ", name='" + name + '\'' +
                ", init_amount=" + init_amount +
                ", remained_amount=" + remained_amount +
                ", monthly_roi=" + monthly_roi +
                ", monthly_payment=" + monthly_payment +
                ", user_id=" + user_id +
                ", transaction_id=" + transaction_id +
                ", init_date='" + init_date + '\'' +
                ", finish_date='" + finish_date + '\'' +
                '}';
    }
}
