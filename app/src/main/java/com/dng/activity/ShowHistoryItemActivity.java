package com.dng.activity;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.dng.R;
import com.dng.adapter.CustomerHistoryAdapter;
import com.dng.adapter.PitHistoryAdapter;
import com.dng.model.HistoryInfo;

public class ShowHistoryItemActivity extends AppCompatActivity implements View.OnClickListener {
    private HistoryInfo.DataBean historyInfo;
    private TextView tv_order_number, tv_pit_name, tv_pitAddress, tv_pit_payment_status, tv_customer_name, tv_delivery_add, tv_payment_method;
    private TextView tv_quantity, tv_product, tv_amount, tv_customer_payment_status;
    private RecyclerView pit_recycler_view, customer_recycler_view;
    private PitHistoryAdapter pitHistoryAdapter;
    private CustomerHistoryAdapter customerHistoryAdapter;
    private ImageView iv_back;
    private ScrollView scrollView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_history_item);

        init();

        if (!getIntent().getSerializableExtra("history_item").equals("")) {
            historyInfo = (HistoryInfo.DataBean) getIntent().getSerializableExtra("history_item");
            setData(historyInfo);

            pitHistoryAdapter = new PitHistoryAdapter(historyInfo.pitInfo.pitPay.pay, this);
            pit_recycler_view.setAdapter(pitHistoryAdapter);

            customerHistoryAdapter = new CustomerHistoryAdapter(historyInfo, this);
            customer_recycler_view.setAdapter(customerHistoryAdapter);

            pitHistoryAdapter.notifyDataSetChanged();
            customerHistoryAdapter.notifyDataSetChanged();

            iv_back.setOnClickListener(this);
        }
    }

    private void init() {
        tv_order_number = findViewById(R.id.tv_order_number);
        tv_pit_name = findViewById(R.id.tv_pit_name);
        tv_pitAddress = findViewById(R.id.tv_pitAddress);
        tv_pit_payment_status = findViewById(R.id.tv_pit_payment_status);
        tv_customer_name = findViewById(R.id.tv_customer_name);
        tv_delivery_add = findViewById(R.id.tv_delivery_add);
        tv_quantity = findViewById(R.id.tv_quantity);
        tv_product = findViewById(R.id.tv_product);
        tv_amount = findViewById(R.id.tv_amount);
        tv_customer_payment_status = findViewById(R.id.tv_customer_payment_status);
        tv_payment_method = findViewById(R.id.tv_payment_method);
        iv_back = findViewById(R.id.iv_back);

        pit_recycler_view = findViewById(R.id.pit_recycler_view);
        customer_recycler_view = findViewById(R.id.customer_recycler_view);

        scrollView = findViewById(R.id.main_view);
        pit_recycler_view.setHasFixedSize(true);
        customer_recycler_view.setHasFixedSize(true);
    }

    // Set data from response of history
    private void setData(HistoryInfo.DataBean historyInfo) {
        scrollView.smoothScrollTo(0,0);
        if (historyInfo != null && historyInfo.orderInfo != null) {
            tv_order_number.setText(historyInfo.invoiceOrderId);
            tv_pit_name.setText(historyInfo.pitInfo.pitName);
            tv_pitAddress.setText(historyInfo.pitInfo.pitAddress);

            if (historyInfo.pitInfo.pitPayStatus.equals("1")) {
                tv_pit_payment_status.setVisibility(View.VISIBLE);
            } else if (historyInfo.pitInfo.pitPayStatus.equals("0")) {
                tv_pit_payment_status.setText(getResources().getString(R.string.payment_status_pending));
            }

            tv_customer_name.setText(historyInfo.orderInfo.customerName);
            tv_delivery_add.setText(historyInfo.orderInfo.deliveryAddress);

            tv_quantity.setText(historyInfo.productInfo.quantity + historyInfo.productInfo.unitType);
            tv_product.setText(historyInfo.productInfo.productName);

            tv_amount.setText(historyInfo.orderInfo.customerPay.totalAmount);

            if (historyInfo.orderInfo.customerPaymentStatus.equals("1")) {
                tv_customer_payment_status.setVisibility(View.VISIBLE);
            } else if (historyInfo.orderInfo.customerPaymentStatus.equals("0")) {
                tv_customer_payment_status.setText(getResources().getString(R.string.payment_status_pending));
            }

            if (historyInfo.orderInfo.paymentMode != null) {
                if (historyInfo.orderInfo.paymentMode.equals("COD")) {
                    tv_payment_method.setText("C.O.D");
                } else {
                    tv_payment_method.setText(historyInfo.orderInfo.paymentMode);
                }
            }

           /* new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    scrollView.smoothScrollTo(0,0);
                    //ObjectAnimator.ofInt(scrollView, "scrollY",  0).setDuration(300).start();
                }
            },100);*/

        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
        }
    }
}
