package com.dng.adapter;

import android.app.Dialog;
import android.content.Context;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
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
import java.util.ArrayList;
import java.util.Date;


public class PitHistoryAdapter extends RecyclerView.Adapter<PitHistoryAdapter.ViewHolder> {
    ArrayList<HistoryInfo.DataBean.PitInfoBean.PitPayBean.PayBeanX> pitHistoryList;
    Context mContext;
    private Dialog zoomImageDialog;
    String date;

    public PitHistoryAdapter(ArrayList<HistoryInfo.DataBean.PitInfoBean.PitPayBean.PayBeanX> pitHistoryList, Context mContext) {
        this.pitHistoryList = pitHistoryList;
        this.mContext = mContext;
    }

    @Override
    public PitHistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_pit_recycler_history, parent, false);
        return new PitHistoryAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.tv_shift.setText((position + 1) + "");
        holder.tv_pit_payment_method.setText(pitHistoryList.get(position).paymentMode);
        holder.tv_pit_payment_type.setText(pitHistoryList.get(position).paymentType);
        holder.tv_pit_amount.setText("$" + pitHistoryList.get(position).amount);

        if (!pitHistoryList.get(position).receipt.equals("")) {
            Glide.with(mContext).load(pitHistoryList.get(position).receipt).into(holder.iv_pit_upload_receipt);
        }

        if (!pitHistoryList.get(position).description.equals("")) {
            holder.ed_pit_description.setText(pitHistoryList.get(position).description);
        } else {
            holder.rl_pit_description.setVisibility(View.GONE);
        }

        date = pitHistoryList.get(position).createDate;
        holder.pit_date.setText(getPitPaymentDate(date));

        if (position == pitHistoryList.size() - 1) {
            holder.pit_recycler_view.setVisibility(View.GONE);
        }

    }

    private String getPitPaymentDate(String date) {
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
        return pitHistoryList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tv_shift, tv_pit_payment_method, tv_pit_payment_type, tv_pit_amount, ed_pit_description, pit_date;
        ImageView iv_pit_upload_receipt;
        RelativeLayout rl_pit_description, rl_pit_date;
        View pit_recycler_view;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_shift = itemView.findViewById(R.id.tv_shift);
            tv_pit_payment_method = itemView.findViewById(R.id.tv_pit_payment_method);
            tv_pit_payment_type = itemView.findViewById(R.id.tv_pit_payment_type);
            tv_pit_amount = itemView.findViewById(R.id.tv_pit_amount);
            ed_pit_description = itemView.findViewById(R.id.ed_pit_description);
            iv_pit_upload_receipt = itemView.findViewById(R.id.iv_pit_upload_receipt);
            pit_date = itemView.findViewById(R.id.pit_date);

            rl_pit_description = itemView.findViewById(R.id.rl_pit_description);
            pit_recycler_view = itemView.findViewById(R.id.pit_recycler_view);
            rl_pit_date = itemView.findViewById(R.id.rl_pit_date);

            iv_pit_upload_receipt.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.iv_pit_upload_receipt:
                    openZoomImageDialog(pitHistoryList.get(getAdapterPosition()).receipt);
                    break;
            }
        }

    }

    // Dialog to open pit payment receipt in zoom view
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
