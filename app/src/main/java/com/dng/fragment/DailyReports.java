package com.dng.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.dng.R;
import com.dng.activity.FuelCostActivity;
import com.dng.app.DNG;
import com.dng.model.DailyReportInfo;
import com.dng.server_task.WebService;
import com.google.gson.Gson;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DailyReports extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private ImageView iv_back;
    private TextView tv_no_of_delivery, tv_fuel_cost,tv_no_of_loading, tv_money_paid_to_owner, tv_money_paid_to_manager, btn_submit, tv_date;
    private EditText ed_total_amount;
    private DailyReportInfo dailyReportInfo;
    private LinearLayout main_view;
    private ProgressBar progressBar;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private RelativeLayout ly_date_filter;
    private DatePickerDialog fromDate;
    private CardView cv_fuel_cost;
    private String date = "";
    Context mContext;


    public static DailyReports newInstance(String param1, String param2) {
        DailyReports fragment = new DailyReports();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_daily_reports, container, false);
        iv_back = view.findViewById(R.id.iv_back);
        cv_fuel_cost = view.findViewById(R.id.cv_fuel_cost);
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceFragment(new ProfileTerminalFragment(), false, R.id.fragment_place);
            }
        });

        init(view);

       // DailyReport(date);

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status = "";
                String totalamount = ed_total_amount.getText().toString().trim();
                if (!totalamount.equals("")) {
                    int selectedId = radioGroup.getCheckedRadioButtonId();
                    radioButton = radioGroup.findViewById(selectedId);
                    Log.d("Radio", "" + radioButton.getText());

                    if (radioButton.getText().toString().equals("Add Amount")) {
                        status = "credit";

                    } else if (radioButton.getText().toString().equals("Return Amount")) {
                        status = "debit";
                    }
                    submitTotalAmount(totalamount, status);

                } else {
                    ed_total_amount.setFocusable(true);
                    Toast.makeText(mContext, "Enter total amount", Toast.LENGTH_SHORT).show();
                }


            }
        });

        ly_date_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDateField();
            }
        });

        cv_fuel_cost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mContext,FuelCostActivity.class));
            }
        });
        return view;
    }

    private void init(View view) {
        tv_no_of_delivery = view.findViewById(R.id.tv_no_of_delivery);
        tv_no_of_loading = view.findViewById(R.id.tv_no_of_loading);
        tv_money_paid_to_owner = view.findViewById(R.id.tv_money_paid_to_owner);
        tv_money_paid_to_manager = view.findViewById(R.id.tv_money_paid_to_manager);
        ed_total_amount = view.findViewById(R.id.ed_total_amount);
        btn_submit = view.findViewById(R.id.btn_submit);
        main_view = view.findViewById(R.id.main_view);
        progressBar = view.findViewById(R.id.progress);
        ly_date_filter = view.findViewById(R.id.ly_date_filter);
        radioGroup = view.findViewById(R.id.radioGroup);
        tv_date = view.findViewById(R.id.tv_date);
        tv_fuel_cost = view.findViewById(R.id.tv_fuel_cost);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public void replaceFragment(Fragment fragment, boolean addToBackStack, int containerId) {
        String backStackName = fragment.getClass().getName();
        FragmentManager fm = getFragmentManager();
       /* int i = fm.getBackStackEntryCount();
        while (i > 0) {
            fm.popBackStackImmediate();
            i--;
        }*/
        boolean fragmentPopped = getFragmentManager().popBackStackImmediate(backStackName, 0);
        if (!fragmentPopped) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(containerId, fragment, backStackName).setTransition(FragmentTransaction.TRANSIT_UNSET);
            if (addToBackStack)
                transaction.addToBackStack(backStackName);
            transaction.commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        DailyReport(date);
    }

    private void DailyReport(String date) {
        progressBar.setVisibility(View.VISIBLE);
        WebService api = new WebService(mContext, "fds", new WebService.ResponceListner() {
            @Override
            public void onResponse(String response, String apiName) {

                try {
                    JSONObject js = new JSONObject(response);

                    String status = js.getString("status");
                    String massage = js.getString("message");

                    if (status.equals("success")) {
                        Gson gson = new Gson();
                        dailyReportInfo = gson.fromJson(response, DailyReportInfo.class);
                        setData(dailyReportInfo);
                        main_view.setVisibility(View.VISIBLE);
                    } else Toast.makeText(mContext, massage, Toast.LENGTH_SHORT).show();

                    progressBar.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressBar.setVisibility(View.GONE);
                    Log.d("error", e + "");
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                progressBar.setVisibility(View.GONE);
            }

        });
        api.callApi("user/dailyReport?dateOfReport=" + date + "", Request.Method.GET, null, true);
    }

    private void setData(DailyReportInfo dailyReportInfo) {
        tv_no_of_delivery.setText(dailyReportInfo.data.numberOfDelivery);
        tv_no_of_loading.setText(dailyReportInfo.data.numberofLoading);

        if (dailyReportInfo.data.moneyReturnDriver.contains("-")) {
            String temp = dailyReportInfo.data.moneyReturnDriver.replace("-", "");
            tv_money_paid_to_owner.setText("$" + temp);
        } else tv_money_paid_to_owner.setText("$" + dailyReportInfo.data.moneyReturnDriver);

        if (dailyReportInfo.data.moneyFromManager.contains("-")) {
            String temp = dailyReportInfo.data.moneyFromManager.replace("-", "");
            tv_money_paid_to_manager.setText("$" + temp);

        } else tv_money_paid_to_manager.setText("$" + dailyReportInfo.data.moneyFromManager);

        tv_fuel_cost.setText(dailyReportInfo.data.fuelCost+"");
    }

    private void submitTotalAmount(String moneyFromManager, String status) {
        progressBar.setVisibility(View.VISIBLE);
        Map<String, String> map = new HashMap<>();
        map.put("moneyFromManager", moneyFromManager);
        map.put("moneyStatus", status);

        WebService api = new WebService(mContext, "fds", new WebService.ResponceListner() {
            @Override
            public void onResponse(String response, String apiName) {

                try {
                    JSONObject js = new JSONObject(response);

                    String status = js.getString("status");
                    String massage = js.getString("message");

                    if (status.equals("success")) {
                        Toast.makeText(mContext, massage, Toast.LENGTH_SHORT).show();
                        ed_total_amount.setText("");
                        DailyReport(date);

                    } else Toast.makeText(mContext, massage, Toast.LENGTH_SHORT).show();

                    progressBar.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("error", e + "");
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                progressBar.setVisibility(View.GONE);
            }

        });
        api.callApi("user/moneyFromManager", Request.Method.POST, map, true);
    }


    private void setDateField() {
        Calendar now = Calendar.getInstance();
        fromDate = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog datePickerDialog, int year, int monthOfYear, int dayOfMonth) {
                // date = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;

                String day, month;
                day = (dayOfMonth < 10) ? "0" + dayOfMonth : String.valueOf(dayOfMonth);
                monthOfYear += 1;
                month = (monthOfYear < 10) ? "0" + monthOfYear : String.valueOf(monthOfYear);

                date = year + "-" + month + "-" + day;
                DailyReport(date);
                tv_date.setText(date);


            }
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));

        fromDate.show(getActivity().getFragmentManager(), "");
        fromDate.setMaxDate(now);
        fromDate.setAccentColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        fromDate.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                Log.d("TimePicker", "Dialog was cancelled");
                fromDate.dismiss();
            }
        });
    }


}
