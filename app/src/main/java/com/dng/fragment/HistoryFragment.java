package com.dng.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.dng.R;
import com.dng.adapter.HistoryAdpter;
import com.dng.helper.AppHelper;
import com.dng.helper.EndlessRecyclerViewScrollListener;
import com.dng.model.HistoryInfo;
import com.dng.server_task.WebService;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HistoryFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    Context mContext;
    private String mParam1;
    private String mParam2;
    private RecyclerView recycler_view;
    private HistoryAdpter historyAdpter;
    private ArrayList<HistoryInfo.DataBean> historyList;
    private ProgressBar progress;
    private TextView tv_no_history;
    private EndlessRecyclerViewScrollListener scrollListener;
    private LinearLayoutManager linearLayoutManager;
    private int start = 0;
    private int limite = 10;


    public HistoryFragment() {
        // Required empty public constructor
    }

    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
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

        View view = inflater.inflate(R.layout.fragment_history, container, false);

        historyList = new ArrayList<>();
        recycler_view = view.findViewById(R.id.recycler_view);
        progress = view.findViewById(R.id.progress);
        tv_no_history = view.findViewById(R.id.tv_no_history);

        linearLayoutManager = new LinearLayoutManager(mContext);


        // Retain an instance so that you can call `resetState()` for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                loadNextDataFromApi(page);

            }
        };

        recycler_view.setLayoutManager(linearLayoutManager);

        // Adds the scroll listener to RecyclerView
        recycler_view.addOnScrollListener(scrollListener);

        historyAdpter = new HistoryAdpter(historyList, mContext);
        recycler_view.setAdapter(historyAdpter);


        getHistory();
        return view;
    }

    private void getHistory() {
        progress.setVisibility(View.VISIBLE);
        if (AppHelper.isConnectingToInternet(mContext)) {

            WebService api = new WebService(mContext, "fds", new WebService.ResponceListner() {
                @Override
                public void onResponse(String response, String apiName) {

                    try {
                        JSONObject js = new JSONObject(response);
                        Log.e("HISTORY RESPONSE", js.toString());

                        String status = js.getString("status");
                        String massage = js.getString("message");

                        if (status.equals("success")) {
                            Gson gson = new Gson();
                            HistoryInfo historyInfo = gson.fromJson(response, HistoryInfo.class);
                            historyList.addAll(historyInfo.data);
                            historyAdpter.notifyDataSetChanged();

                            if (historyList.size() == 0) {
                                tv_no_history.setVisibility(View.VISIBLE);
                            } else {
                                tv_no_history.setVisibility(View.GONE);
                            }

                            progress.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(mContext, massage, Toast.LENGTH_SHORT).show();
                            progress.setVisibility(View.GONE);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.setVisibility(View.GONE);
                    }
                }


                @Override
                public void ErrorListener(VolleyError error) {
                    progress.setVisibility(View.GONE);
                }

            });
            api.callApi("user/deliveryHistory?start="+start+"&limit="+limite+"", Request.Method.GET, null);

        } else
            Toast.makeText(mContext, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    // Append the next page of data into the adapter
    // This method probably sends out a network request and appends new data items to your adapter.
    public void loadNextDataFromApi(int offset) {
        start = limite;
        limite = limite+10;
        getHistory();
    }

}
