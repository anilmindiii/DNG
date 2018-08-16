package com.dng.adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dng.R;
import com.dng.model.HistoryInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by mindiii on 10/4/18.
 */

public class CustomerHistoryAdapter extends RecyclerView.Adapter<CustomerHistoryAdapter.ViewHolder> {
    HistoryInfo.DataBean customerHistoryList;
    Context mContext;
    private Dialog zoomImageDialog;
    String date;

    public CustomerHistoryAdapter(HistoryInfo.DataBean customerHistoryList, Context mContext) {
        this.customerHistoryList = customerHistoryList;
        this.mContext = mContext;
    }

    @Override
    public CustomerHistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_customer_recycler_history, parent, false);
        return new CustomerHistoryAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.tv_shift.setText((position + 1) + "");

        if (customerHistoryList.orderInfo.customerPay.pay.size() > 0 && !(position > (customerHistoryList.orderInfo.customerPay.pay.size() - 1))) {
            holder.tv_payment_method.setText(customerHistoryList.orderInfo.customerPay.pay.get(position).paymentMode);
            holder.tv_payment_type.setText(customerHistoryList.orderInfo.customerPay.pay.get(position).paymentType);
            holder.tv_customer_amount.setText("$" + customerHistoryList.orderInfo.customerPay.pay.get(position).amount);

            if (!customerHistoryList.orderInfo.customerPay.pay.get(position).receipt.equals("")) {
                Glide.with(mContext).load(customerHistoryList.orderInfo.customerPay.pay.get(position).receipt).into(holder.iv_customer_payment_receipt);
            }

            if (!customerHistoryList.orderInfo.customerPay.pay.get(position).description.equals("")) {
                holder.ed_description.setText(customerHistoryList.orderInfo.customerPay.pay.get(position).description);
            } else {
                holder.rl_description.setVisibility(View.GONE);
            }

            date = customerHistoryList.orderInfo.customerPay.pay.get(position).createDate;
            holder.customer_date.setText(getCustomerPaymentDate(date));
        } else {
            holder.rl_payment_method.setVisibility(View.GONE);
            holder.rl_payment_type.setVisibility(View.GONE);
            holder.rl_customer_payment.setVisibility(View.GONE);
            holder.rl_amount.setVisibility(View.GONE);
            holder.rl_description.setVisibility(View.GONE);
            holder.rl_customer_date.setVisibility(View.GONE);
        }

        if (!customerHistoryList.shiftReport.get(position).beforeDelivery.image.equals("")) {
            Glide.with(mContext).load(customerHistoryList.shiftReport.get(position).beforeDelivery.image).into(holder.iv_before_delivery);
        }

        if (!customerHistoryList.shiftReport.get(position).afterDelivery.image.equals("")) {
            Glide.with(mContext).load(customerHistoryList.shiftReport.get(position).afterDelivery.image).into(holder.iv_after_delivery);
        }

        if (!customerHistoryList.shiftReport.get(position).deliveryReceipt.image.equals("")) {
            Glide.with(mContext).load(customerHistoryList.shiftReport.get(position).deliveryReceipt.image).into(holder.iv_upload_receipt);
        }

        if (customerHistoryList.orderInfo.paymentMode.equals("CARD")) {
            holder.rl_payment_method.setVisibility(View.GONE);
            holder.rl_payment_type.setVisibility(View.GONE);
            holder.rl_customer_payment.setVisibility(View.GONE);
            holder.rl_amount.setVisibility(View.GONE);
            holder.rl_description.setVisibility(View.GONE);
            holder.rl_customer_date.setVisibility(View.GONE);
        }

        if (position == customerHistoryList.shiftReport.size() - 1) {
            holder.customer_recycler_view.setVisibility(View.GONE);
        }

    }

    private String getCustomerPaymentDate(String date) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat fmt2 = new SimpleDateFormat("dd-MM-yyyy");
        try {
            Date parsed_date = fmt.parse(date);

            return fmt2.format(parsed_date);
        } catch (ParseException pe) {
            return "Date";
        }
    }


    @Override
    public int getItemCount() {
        return customerHistoryList.shiftReport.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tv_shift, tv_payment_method, tv_payment_type, tv_customer_amount, ed_description, customer_date;
        ImageView iv_before_delivery, iv_after_delivery, iv_upload_receipt, iv_customer_payment_receipt;
        RelativeLayout rl_payment_method, rl_payment_type, rl_customer_payment, rl_amount, rl_description, rl_customer_date;
        View customer_recycler_view;

        public ViewHolder(View itemView) {
            super(itemView);

            tv_shift = itemView.findViewById(R.id.tv_shift);
            tv_payment_method = itemView.findViewById(R.id.tv_payment_method);
            tv_payment_type = itemView.findViewById(R.id.tv_payment_type);
            tv_customer_amount = itemView.findViewById(R.id.tv_customer_amount);
            iv_before_delivery = itemView.findViewById(R.id.iv_before_delivery);
            iv_after_delivery = itemView.findViewById(R.id.iv_after_delivery);
            iv_upload_receipt = itemView.findViewById(R.id.iv_upload_receipt);
            iv_customer_payment_receipt = itemView.findViewById(R.id.iv_customer_payment_receipt);
            ed_description = itemView.findViewById(R.id.ed_description);
            customer_date = itemView.findViewById(R.id.customer_date);

            rl_payment_method = itemView.findViewById(R.id.rl_payment_method);
            rl_payment_type = itemView.findViewById(R.id.rl_payment_type);
            rl_customer_payment = itemView.findViewById(R.id.rl_customer_payment);
            rl_amount = itemView.findViewById(R.id.rl_amount);
            rl_description = itemView.findViewById(R.id.rl_description);
            rl_customer_date = itemView.findViewById(R.id.rl_customer_date);

            customer_recycler_view = itemView.findViewById(R.id.customer_recycler_view);

            iv_before_delivery.setOnClickListener(this);
            iv_after_delivery.setOnClickListener(this);
            iv_customer_payment_receipt.setOnClickListener(this);
            iv_upload_receipt.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.iv_before_delivery:
                    openZoomImageDialog(customerHistoryList.shiftReport.get(getAdapterPosition()).beforeDelivery.image);
                    break;

                case R.id.iv_after_delivery:
                    openZoomImageDialog(customerHistoryList.shiftReport.get(getAdapterPosition()).afterDelivery.image);
                    break;

                case R.id.iv_customer_payment_receipt:
                    openZoomImageDialog(customerHistoryList.orderInfo.customerPay.pay.get(getAdapterPosition()).receipt);
                    break;

                case R.id.iv_upload_receipt:
                    openZoomImageDialog(customerHistoryList.shiftReport.get(getAdapterPosition()).deliveryReceipt.image);
                    break;
            }
        }
    }

    // Dialog to open customer delivery receipts in zoom view
    private void openZoomImageDialog(String url) {
        zoomImageDialog = new Dialog(mContext);
        zoomImageDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        zoomImageDialog.setContentView(R.layout.dialog_zoom_image);

        WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();
        windowParams.copyFrom(zoomImageDialog.getWindow().getAttributes());
        windowParams.width = WindowManager.LayoutParams.FILL_PARENT;
        windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        zoomImageDialog.getWindow().setAttributes(windowParams);

        ImageView mphoto_view = zoomImageDialog.findViewById(R.id.photo_view);
        ImageView mcancel_dialog_icon = zoomImageDialog.findViewById(R.id.cancel_dialog_icon);

        Glide.with(mContext).load(url).into(mphoto_view);

        mcancel_dialog_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoomImageDialog.dismiss();
            }
        });

        zoomImageDialog.getWindow().setGravity(Gravity.CENTER);

        if (!url.equals("")) {
            zoomImageDialog.show();
        }
    }
}
