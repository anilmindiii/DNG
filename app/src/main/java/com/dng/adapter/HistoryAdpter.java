package com.dng.adapter;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dng.R;
import com.dng.activity.ShowHistoryItemActivity;
import com.dng.model.HistoryInfo;

import java.util.List;

public class HistoryAdpter extends RecyclerView.Adapter<HistoryAdpter.ViewHolder> {

    List<HistoryInfo.DataBean> historyList;
    Context mContext;

    public HistoryAdpter(List<HistoryInfo.DataBean> historyList, Context mContext) {
        this.historyList = historyList;
        this.mContext = mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if (!historyList.get(position).orderInfo.cutomerImage.equals("")) {
            Glide.with(mContext).load(historyList.get(position).orderInfo.cutomerImage).into(holder.profile);
        }

        holder.user_name.setText(historyList.get(position).orderInfo.customerName);
        holder.tv_quantity.setText(historyList.get(position).productInfo.quantity +" " + historyList.get(position).productInfo.unitType);
        holder.tv_address.setText(historyList.get(position).orderInfo.billingAddress);
        holder.tv_price.setText("$" + historyList.get(position).orderInfo.customerPay.totalAmount);
        holder.tv_date.setText(historyList.get(position).deliverydate);

    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView profile;
        TextView user_name, tv_quantity, tv_address, tv_price, tv_date;
        LinearLayout history_item;

        public ViewHolder(View itemView) {
            super(itemView);
            profile = itemView.findViewById(R.id.iv_user_image);
            user_name = itemView.findViewById(R.id.user_name);
            tv_quantity = itemView.findViewById(R.id.tv_quantity);
            tv_address = itemView.findViewById(R.id.tv_address);
            tv_price = itemView.findViewById(R.id.tv_price);
            tv_date = itemView.findViewById(R.id.tv_date);
            history_item = itemView.findViewById(R.id.history_item);

            history_item.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.history_item:
                    displayHistoryItem(getAdapterPosition());
                    break;
            }
        }

    }

    private void displayHistoryItem(int adapterPosition) {
        Intent intent = new Intent(mContext, ShowHistoryItemActivity.class);
        intent.putExtra("history_item", historyList.get(adapterPosition));
        mContext.startActivity(intent);
    }
}
