package com.dng.fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.dng.R;
import com.dng.model.RouteInfo;

public class TutsPlusBottomSheetDialogFragment extends BottomSheetDialogFragment {
    private static final String ARG_PARAM1 = "param1";
    RouteInfo routeInfo;

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }

        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    public static TutsPlusBottomSheetDialogFragment newInstance(RouteInfo routeInfo) {

        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, routeInfo);
        TutsPlusBottomSheetDialogFragment fragment = new TutsPlusBottomSheetDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            routeInfo = (RouteInfo) getArguments().getSerializable(ARG_PARAM1);
            Log.d("",""+routeInfo);

        }
    }

    @Override
    public void setupDialog(final Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.map_route_info_layout, null);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView tv_from_distance = contentView.findViewById(R.id.tv_from_distance);
        TextView tv_to_distance = contentView.findViewById(R.id.tv_to_distance);
        TextView tv_total_distance = contentView.findViewById(R.id.tv_total_distance);
        TextView tv_aprox_time = contentView.findViewById(R.id.tv_aprox_time);
        ImageView iv_back = contentView.findViewById(R.id.iv_back);

        tv_from_distance.setText(routeInfo.Start_Address);
        tv_to_distance.setText(routeInfo.End_Address);
        tv_aprox_time.setText(routeInfo.TotalDuration);
        tv_total_distance.setText(routeInfo.TotalDistance);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.setContentView(contentView);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if( behavior != null && behavior instanceof BottomSheetBehavior ) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
    }
}