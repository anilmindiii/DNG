package com.dng.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.dng.R;
import com.dng.helper.AppHelper;
import com.dng.model.User;
import com.dng.server_task.WebService;
import com.dng.service.MyService;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FuelCostActivity extends AppCompatActivity {

    private TextView btn_add;
    private EditText ed_fuel_cost;
    private ProgressDialog progressDialog;
    private ImageView iv_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_cost);
        btn_add = findViewById(R.id.btn_add);
        ed_fuel_cost = findViewById(R.id.ed_fuel_cost);
        iv_back = findViewById(R.id.iv_back);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fuelCost = ed_fuel_cost.getText().toString().trim();
                if(!fuelCost.equals("")){
                    addFuelCost(fuelCost);
                }else {
                    Toast.makeText(FuelCostActivity.this, "Please enter fuel cost", Toast.LENGTH_SHORT).show();
                }

            }
        });
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void addFuelCost(final String fuelCost) {
        progressDialog.show();
        Map<String, String> map = new HashMap<>();
        map.put("fuelCost", fuelCost);

        WebService api = new WebService(FuelCostActivity.this, "fds", new WebService.ResponceListner() {
            @Override
            public void onResponse(String response, String apiName) {
                progressDialog.dismiss();
                try {
                    JSONObject js = new JSONObject(response);
                    Log.e("LOGIN RESPONSE", response.toString());

                    String status = js.getString("status");
                    String massage = js.getString("message");

                    if (status.equals("success")) {
                        finish();

                    } else {
                        Toast.makeText(FuelCostActivity.this, massage, Toast.LENGTH_SHORT).show();
                    }


                } catch (JSONException e) {
                    progressDialog.dismiss();
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(FuelCostActivity.this, "Something went wrong, please try again later.", Toast.LENGTH_SHORT).show();
            }
        });
        api.callApi("user/driverForFuelCost", Request.Method.POST, map);
    }
}
