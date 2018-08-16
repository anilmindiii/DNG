package com.dng.fragment;

import android.Manifest;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.bumptech.glide.Glide;
import com.dng.R;
import com.dng.activity.MainActivity;
import com.dng.app.DNG;

import com.dng.helper.AppHelper;
import com.view.cropper.CropImage;
import com.view.cropper.CropImageView;
import com.dng.helper.Constant;
import com.dng.helper.ErrorDialog;
import com.dng.helper.LocationRuntimePermission;
import com.dng.helper.TimerAlertBroadcastReceiver;
import com.dng.image.picker.ImagePicker;
import com.dng.model.RequestInfo;
import com.dng.server_task.WebService;
import com.dng.service.MyService;
import com.dng.session.Session;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.TimeUnit;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.ALARM_SERVICE;


public class LoadingDetailsFragment extends Fragment implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final String TAG = LoadingDetailsFragment.class.getName();
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String paymentMode = "";
    private String payAmount = "", chequeNumber = "";
    private int numberofshift = 0;
    private Context mContext;

    private int count = -1;

    private ImageView iv_upload_receipt, iv_upload_receipt_cancel;
    private TextView btn_out_for_delivery;
    private TextView btn_pay_now;
    private TextView btn_goto_delivery_detail;
    private LinearLayout ry_call_customer;
    private CardView cv_cash, cv_credit_card, cv_account, cv_cheque;
    private EditText ed_description;
    private ProgressBar progress;
    private Session session;
    private RequestInfo requestInfo;

    private Bitmap receiptImageBitMap;
    private Dialog chequeDialog;
    private EditText ed_cheque_number;
    private EditText ed_total_amount;
    private RadioButton rb_full_pay, rb_partial_pay;

    private Dialog zoomImageDialog;

    private ImageView iv_cash, iv_credit_card, iv_account, iv_cheque;
    private TextView tv_cash_mode, tv_credit_mode, tv_account_mode, tv_cheque_mode, payment_already_done;
    private CardView cv_upload_receipt;
    private RelativeLayout rl_pit_payment_status;

    public static TextView tv_timer_;

    private Dialog start_route_dialog;

    private CountDownTimer countDownTimer;
    boolean timerIsRunning = false;

    private boolean paymentSelected = false;

    private Double amt;
    private String paymentType;
    private Dialog fullMinPaymentDialog;
    private int totalShift;
    private int paymentShift = 0;

    private Boolean isGPSEnable;
    private LocationManager lmgr;

    // Get Current Location
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;
    private LocationRequest mLocationRequest;
    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FASTEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters

    private Bitmap profileImageBitmap;
    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;


    public LoadingDetailsFragment() {
        // Required empty public constructor
    }


    public static LoadingDetailsFragment newInstance(String param1, String param2) {
        LoadingDetailsFragment fragment = new LoadingDetailsFragment();
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
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loading_details, container, false);

        session = new Session(mContext);
        // Get Order info from Session
        requestInfo = session.getRequestInfo();
        init(view);
        session.setScreen("LoadingDetailsFragment");

        lmgr = (LocationManager) ((MainActivity) mContext).getSystemService(Context.LOCATION_SERVICE);

        if (requestInfo != null) {
            // Display Shifts
            numberofshift = Integer.parseInt(requestInfo.data.completeShift) + 1;
            if (numberofshift == 0) {
                numberofshift = numberofshift + 1;
            } else if (numberofshift <= Integer.parseInt(requestInfo.data.totalShift)) {
                numberofshift = (Integer.parseInt(requestInfo.data.completeShift) + 1);
            }

            // To set data from response
            if (requestInfo.data.totalShift != null) {
                totalShift = Integer.parseInt(requestInfo.data.totalShift);

                if (totalShift == 1) {
                    paymentShift = 0;
                } else {
                    if (requestInfo.data.pitInfo.pitPayStatus != null) {
                        if (requestInfo.data.pitInfo.pitPay.pay.size() != 0) {
                            paymentShift = requestInfo.data.pitInfo.pitPay.pay.size() - 1;
                        } else {
                            paymentShift = Integer.parseInt(requestInfo.data.completeShift);
                        }
                    }
                }
            }

            // Set prefilled data when pit payment is completed and its status is 1
            // And when payment status is 0 , have to complete payment procedure
            if (requestInfo.data.pitInfo.pitPayStatus != null) {
                if (requestInfo.data.pitInfo.pitPayStatus.equals("1")) {
                    rl_pit_payment_status.setVisibility(View.VISIBLE);
                    cv_cash.setEnabled(false);
                    cv_credit_card.setEnabled(false);
                    cv_account.setEnabled(false);
                    cv_cheque.setEnabled(false);

                    iv_upload_receipt_cancel.setEnabled(false);
                    ed_description.setInputType(InputType.TYPE_NULL);

                    btn_pay_now.setVisibility(View.GONE);
                    btn_out_for_delivery.setVisibility(View.VISIBLE);

                    //Set data from session or response
                    setLoadingData();

                } else if (requestInfo != null && requestInfo.data.pitInfo.pitPayStatus.equals("0")) {

                    if (totalShift > 1) {
                        if (requestInfo.data.pitInfo.pitPay.dueAmount != 0) {
                            rl_pit_payment_status.setVisibility(View.VISIBLE);
                            payment_already_done.setText(getResources().getString(R.string.pit_payment_in_progress));
                        } else {
                            rl_pit_payment_status.setVisibility(View.GONE);
                        }
                    } else if (totalShift == 1) {
                        rl_pit_payment_status.setVisibility(View.GONE);
                    }

                    cv_cash.setEnabled(true);
                    cv_credit_card.setEnabled(true);
                    cv_account.setEnabled(true);
                    cv_cheque.setEnabled(true);

                    ed_description.setEnabled(true);

                    btn_pay_now.setVisibility(View.VISIBLE);
                    btn_out_for_delivery.setVisibility(View.GONE);

                    session.setLoadingChequeNumber("");
                }
            }
        }

        cv_cash.setOnClickListener(this);
        cv_credit_card.setOnClickListener(this);
        cv_account.setOnClickListener(this);
        cv_cheque.setOnClickListener(this);

        ry_call_customer.setOnClickListener(this);
        iv_upload_receipt.setOnClickListener(this);
        btn_pay_now.setOnClickListener(this);
        iv_upload_receipt_cancel.setOnClickListener(this);
        btn_out_for_delivery.setOnClickListener(this);

        isInternetisGpsEnable();

        // Get Latitude and Longitude
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
            createLocationRequest();
        }

        // Broadcast pending intent to launch pop up for launch app after finish 5 mins
        Intent myIntent = new Intent(mContext, TimerAlertBroadcastReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(mContext, 234324243, myIntent, 0);
        alarmManager = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);

        return view;
    }

    // Set prefilled data when pit payment is completed and its status is 1
    private void setLoadingData() {

        // Setting Loading Pit Payment Receipt
        if (!session.getLoadingRouteReceipt().equals("")) {
            String image = session.getLoadingRouteReceipt();

            Glide.with(mContext).load(image).into(iv_upload_receipt);
            iv_upload_receipt.setOnClickListener(this);

        } else {
            if (requestInfo.data.pitInfo.pitPay.pay.size() != 0) {
                if (requestInfo.data.pitInfo.pitPay.pay.get(paymentShift).receipt.equals("")) {
                    iv_upload_receipt.setImageDrawable(getResources().getDrawable(R.drawable.upload));
                } else {
                    String receiptUrl = requestInfo.data.pitInfo.pitPay.pay.get(paymentShift).receipt;
                    Glide.with(mContext).load(receiptUrl).into(iv_upload_receipt);
                    session.setLoadingRouteReceipt(receiptUrl, requestInfo.data.pitInfo.pitPay.pay.get(paymentShift).paymentMode, requestInfo.data.pitInfo.pitPay.pay.get(paymentShift).description);
                }
            } else {
                iv_upload_receipt.setImageDrawable(getResources().getDrawable(R.drawable.upload));
            }
        }

        // Setting Pit Payment Mode
        if (!session.getLoadingRoutePaymentMode().equals("")) {
            if (session.getPaymentMode().equals("CASH")) {
                setCashModeActive();

            } else if (session.getLoadingRoutePaymentMode().equals("ACCOUNT")) {
                setAccountModeActive();

            } else if (session.getLoadingRoutePaymentMode().equals("CREDITCARD")) {
                setCreditModeActive();

            } else if (session.getLoadingRoutePaymentMode().equals("CHEQUE")) {
                setChequeModeActive();

            }

            // Since Payment already done, setting payment selected as true
            paymentSelected = true;
        } else if (requestInfo.data.pitInfo.pitPay.pay.size() != 0) {
            if (!requestInfo.data.pitInfo.pitPay.pay.get(paymentShift).paymentMode.equals("")) {
                String paymentMode = requestInfo.data.pitInfo.pitPay.pay.get(paymentShift).paymentMode;

                if (paymentMode.equals("CASH")) {
                    setCashModeActive();

                } else if (paymentMode.equals("ACCOUNT")) {
                    setAccountModeActive();

                } else if (paymentMode.equals("CREDITCARD")) {
                    setCreditModeActive();

                } else if (paymentMode.equals("CHEQUE")) {
                    setChequeModeActive();

                }

                paymentSelected = true;
            }
        }

        // Setting Pit payment description
        if (!session.getLoadingRouteDescription().equals("")) {
            ed_description.setText(session.getLoadingRouteDescription());
        } else if (requestInfo.data.pitInfo.pitPay.pay.size() != 0) {
            if (!requestInfo.data.pitInfo.pitPay.pay.get(paymentShift).description.equals("")) {
                String description = requestInfo.data.pitInfo.pitPay.pay.get(paymentShift).description;

                ed_description.setText(description);
            }
        }
    }

    // Input filter used to restrict amount input to be round off to 2 decimal places
    private void inputFilter(final EditText et) {
        et.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                if (ed_total_amount.getText().toString().contains(".")) {
                    if (ed_total_amount.getText().toString().substring(ed_total_amount.getText().toString().indexOf(".") + 1, ed_total_amount.length()).length() == 2) {
                        InputFilter[] fArray = new InputFilter[1];
                        fArray[0] = new InputFilter.LengthFilter(arg0.length());
                        et.setFilters(fArray);
                    }
                }
            }

            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }

            public void afterTextChanged(Editable arg0) {
                if (arg0.length() > 0) {
                    String str = et.getText().toString();
                    et.setOnKeyListener(new View.OnKeyListener() {
                        public boolean onKey(View v, int keyCode, KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_DEL) {
                                count--;
                                InputFilter[] fArray = new InputFilter[1];
                                fArray[0] = new InputFilter.LengthFilter(100);
                                et.setFilters(fArray);
                                //change the edittext's maximum length to 100.
                                //If we didn't change this the edittext's maximum length will
                                //be number of digits we previously entered.
                            }
                            return false;
                        }
                    });
                    char t = str.charAt(arg0.length() - 1);
                    if (t == '.') {
                        count = 0;
                    }
                    if (count >= 0) {
                        if (count == 2) {
                            InputFilter[] fArray = new InputFilter[1];
                            fArray[0] = new InputFilter.LengthFilter(arg0.length());
                            et.setFilters(fArray);
                            //prevent the edittext from accessing digits
                            //by setting maximum length as total number of digits we typed till now.
                        }
                        count++;
                    }
                }
            }
        });
    }

    void init(View view) {
        progress = view.findViewById(R.id.progress);
        iv_upload_receipt = view.findViewById(R.id.iv_upload_receipt);
        ry_call_customer = view.findViewById(R.id.ry_call_customer);
        ed_description = view.findViewById(R.id.ed_description);

        TextView tv_po_number = view.findViewById(R.id.tv_po_number);

        TextView tv_order_num = view.findViewById(R.id.tv_order_num);
        TextView tv_customer_name = view.findViewById(R.id.tv_customer_name);
        TextView tv_qty = view.findViewById(R.id.tv_qty);
        TextView tv_product = view.findViewById(R.id.tv_product);

        if (requestInfo.data != null) {
            tv_order_num.setText(requestInfo.data.invoiceOrderId);
            tv_customer_name.setText(requestInfo.data.orderInfo.customerName);
            tv_qty.setText(requestInfo.data.productInfo.quantity + " " + requestInfo.data.productInfo.unitType);

            tv_po_number.setText(requestInfo.data.mainOrderId);
            tv_product.setText(requestInfo.data.productInfo.productName);
        }

        cv_cash = view.findViewById(R.id.cv_cash);
        cv_credit_card = view.findViewById(R.id.cv_credit_card);
        cv_account = view.findViewById(R.id.cv_account);
        cv_cheque = view.findViewById(R.id.cv_cheque);

        btn_out_for_delivery = view.findViewById(R.id.btn_out_for_delivery);
        btn_goto_delivery_detail = view.findViewById(R.id.btn_goto_delivery_detail);

        iv_cash = view.findViewById(R.id.iv_cash);
        iv_credit_card = view.findViewById(R.id.iv_credit_card);
        iv_account = view.findViewById(R.id.iv_account);
        iv_cheque = view.findViewById(R.id.iv_cheque);

        tv_cash_mode = view.findViewById(R.id.tv_cash_mode);
        tv_credit_mode = view.findViewById(R.id.tv_credit_mode);
        tv_account_mode = view.findViewById(R.id.tv_account_mode);
        tv_cheque_mode = view.findViewById(R.id.tv_cheque_mode);

        cv_upload_receipt = view.findViewById(R.id.cv_upload_receipt);

        tv_timer_ = view.findViewById(R.id.tv_timer);

        payment_already_done = view.findViewById(R.id.payment_already_done);
        rl_pit_payment_status = view.findViewById(R.id.rl_pit_payment_status);

        btn_pay_now = view.findViewById(R.id.btn_pay_now);

        iv_upload_receipt_cancel = view.findViewById(R.id.iv_upload_receipt_cancel);
    }

    private void yesToCallDialog(String msg, final String phone_number) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Make Call");
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getPermissionMakeCall(phone_number);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.setTitle("Make Call");
        alert.show();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {

            case Constant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.pickImageFromCamera(LoadingDetailsFragment.this);
                } else {
                    Toast.makeText(mContext, "YOU DENIED PERMISSION CANNOT SELECT IMAGE", Toast.LENGTH_LONG).show();
                }
            }
            break;

            case Constant.MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.pickImageFromCamera(LoadingDetailsFragment.this);
                } else {
                    Toast.makeText(mContext, "YOUR  PERMISSION DENIED ", Toast.LENGTH_LONG).show();
                }
            }
            break;
        }
    }

    private void makeCall(String phone_number) {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone_number));
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == 234) {
                Uri imageUri = ImagePicker.getImageURIFromResult(mContext, requestCode, resultCode, data);
                if (imageUri != null) {
                    CropImage.activity(imageUri).setCropShape(CropImageView.CropShape.RECTANGLE)
                            .setAspectRatio(4, 4)
                            .start(mContext, LoadingDetailsFragment.this);
                } else {
                    Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();
                }

            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                try {
                    if (result != null)
                        profileImageBitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), result.getUri());

                    if (profileImageBitmap != null) {
                        receiptImageBitMap = profileImageBitmap;
                        iv_upload_receipt_cancel.setVisibility(View.VISIBLE);
                        iv_upload_receipt.setImageBitmap(profileImageBitmap);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public void getPermissionMakeCall(String phone_number) {

        if (Build.VERSION.SDK_INT >= 23) {
            if (mContext.checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.CALL_PHONE},
                        Constant.CALL_PHONE);
            } else {
                makeCall(phone_number);
            }
        } else makeCall(phone_number);
    }

    // Api call to complete pit payment
    private void pitPayment(String subOrderId, final String payAmount, String paymentType, final String description) {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.setVisibility(View.VISIBLE);
            Map<String, String> map = new HashMap<>();
            map.put("subOrderId", subOrderId);
            map.put("paymentMode", paymentMode);
            map.put("amount", payAmount);
            map.put("paymentType", paymentType);
            map.put("description", description);
            map.put("assignPitId", requestInfo.data.pitInfo.assignPitId);

            Map<String, Bitmap> bitmapList = new HashMap<>();
            bitmapList.put("receiptImage", receiptImageBitMap);

            WebService api = new WebService(mContext, "fds", new WebService.ResponceListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    progress.setVisibility(View.GONE);
                    try {
                        JSONObject jsonObject = new JSONObject(response);

                        Log.e("PIT PAYMENT RESPONSE", response);
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");


                        if (status.equals("success")) {
                            btn_pay_now.setEnabled(true);
                            if (message.equals("Pit payment paid successfully done.")) {
                                JSONObject data = jsonObject.getJSONObject("data");
                                JSONArray payArray = data.getJSONArray("pay");

                                if (data.getString("dueAmount").equals("0")) {

                                    btn_pay_now.setVisibility(View.GONE);
                                    btn_out_for_delivery.setVisibility(View.VISIBLE);

                                    cv_cash.setEnabled(false);
                                    cv_cheque.setEnabled(false);
                                    cv_credit_card.setEnabled(false);
                                    cv_account.setEnabled(false);

                                    iv_upload_receipt_cancel.setEnabled(false);
                                    iv_upload_receipt_cancel.setVisibility(View.GONE);

                                    ed_description.setInputType(InputType.TYPE_NULL);

                                } else {
                                    btn_pay_now.setVisibility(View.GONE);
                                    btn_out_for_delivery.setVisibility(View.VISIBLE);

// When partial payment and data to be shown prefilled, used pitPayStatus as 1

                                    cv_cash.setEnabled(false);
                                    cv_cheque.setEnabled(false);
                                    cv_credit_card.setEnabled(false);
                                    cv_account.setEnabled(false);

                                    iv_upload_receipt_cancel.setEnabled(false);
                                    iv_upload_receipt_cancel.setVisibility(View.GONE);

                                    ed_description.setInputType(InputType.TYPE_NULL);

                                }

                                requestInfo.data.pitInfo.pitPayStatus = "1";
                                session.createRequest(requestInfo);

                                String imageReceipt = payArray.getJSONObject(payArray.length() - 1).getString("receipt");
                                session.setLoadingRouteReceipt(imageReceipt, paymentMode, description);

                            } else if (message.equals("Pit payment already paid.")) {
                                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                                btn_pay_now.setVisibility(View.GONE);
                                btn_out_for_delivery.setVisibility(View.VISIBLE);
                            }

                        } else {
                            btn_pay_now.setEnabled(true);
                            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        btn_pay_now.setEnabled(true);
                        Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.setVisibility(View.GONE);
                    btn_pay_now.setEnabled(true);
                    ErrorDialog.showSessionError(error, mContext);
                }
            });
            api.callMultiPartApi("user/pitPayment", map, bitmapList);
        } else {
            Toast.makeText(mContext, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
        }
    }

    // Dialog to open Start route to pit or remember for 5 mins
    private void openStartRouteDialog() {
        start_route_dialog = new Dialog(mContext);
        start_route_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        start_route_dialog.setContentView(R.layout.dialog_start_route_to_the_pit);

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(start_route_dialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.FILL_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        start_route_dialog.getWindow().setAttributes(lWindowParams);

        final TextView start_route_button = start_route_dialog.findViewById(R.id.start_route_button);
        final TextView remember_me_button = start_route_dialog.findViewById(R.id.remember_me_button);

        start_route_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelPendingTask();
                startStopRoute();


                boolean isAppInstalledWaze = appInstalledOrNot("com.waze");
                boolean isAppInstalledGmail = appInstalledOrNot("com.google.android.apps.maps");

                String delivery_lat = session.getRequestInfo().data.orderInfo.deliveryLatitude;
                String delivery_lng = session.getRequestInfo().data.orderInfo.deliveryLongitude;

                if (timerIsRunning && countDownTimer != null) {
                    countDownTimer.cancel();
                }
                if ((tv_timer_.getVisibility() == View.VISIBLE) && (!tv_timer_.getText().toString().equals(""))) {
                    tv_timer_.setVisibility(View.GONE);
                    tv_timer_.setText("");
                }

                if (session.getDefaultMap() == 0) {

                    //googleMap
                    if (isAppInstalledGmail) {
                        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + delivery_lat + ",+" + delivery_lng);
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

                        mapIntent.setPackage("com.google.android.apps.maps");
                        mapIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mapIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        mapIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        if (isAdded()) {
                            mContext.startActivity(mapIntent);
                        }

                        start_route_dialog.dismiss();
                    } else {
                        Toast.makeText(mContext, "google map is not installed please install first", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    //Waze
                    if (isAppInstalledWaze) {
                        String uri = "waze://?ll=" + delivery_lat + ", " + delivery_lng + "&navigate=yes";
                        Intent wazeIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
                        wazeIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        wazeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        wazeIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        if (isAdded()) {
                            mContext.startActivity(wazeIntent);
                        }

                        start_route_dialog.dismiss();
                    } else {
                        Toast.makeText(mContext, "Waze is not installed please install first", Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });

        remember_me_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                if (timerIsRunning) {
                    countDownTimer.cancel();
                    startRemainingTimer();
                } else {
                    startRemainingTimer();
                }
                start_route_dialog.dismiss();
            }
        });

        start_route_dialog.getWindow().setGravity(Gravity.CENTER);
        start_route_dialog.show();
    }

    // Api call to set ON Route or OFF Route
    private void startStopRoute() {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.setVisibility(View.VISIBLE);

            WebService api = new WebService(mContext, "fds", new WebService.ResponceListner() {
                @Override
                public void onResponse(String response, String apiName) {

                    try {
                        JSONObject js = new JSONObject(response);

                        String status = js.getString("status");
                        String massage = js.getString("message");

                        if (status.equals("success")) {

                            String route = js.getString("route");

                            if (route.equals("ON ROUTE")) {
                                //  tv_route.setText("Stop Route");
                                session.getRequestInfo().data.driverInfo.onRoute = "1";
                                RequestInfo requestInfo = session.getRequestInfo();
                                requestInfo.data.driverInfo.onRoute = "1";
                                session.createRequest(requestInfo);

                                if (Build.VERSION.SDK_INT >= 23) {
                                    if (!Settings.canDrawOverlays(mContext)) {
                                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                Uri.parse("package:" + mContext.getPackageName()));
                                        startActivityForResult(intent, 1234);
                                    } else {

                                        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                                            AppHelper.startAlarmService(mContext);
                                            session.setPopUpForCustomerArea("Yes");
                                        }else {
                                            Intent intent = new Intent(mContext, MyService.class);
                                            intent.putExtra("PopUpForCustomerArea", "Yes");
                                            mContext.startService(intent);
                                        }


                                    }
                                } else {
                                    if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                                        AppHelper.startAlarmService(mContext);
                                        session.setPopUpForCustomerArea("Yes");
                                    }else {
                                        Intent intent = new Intent(mContext, MyService.class);
                                        intent.putExtra("PopUpForCustomerArea", "Yes");
                                        mContext.startService(intent);
                                    }
                                }


                            } else if (route.equals("OFF ROUTE")) {
                                startStopRoute();

                                session.getRequestInfo().data.driverInfo.onRoute = "0";
                                RequestInfo requestInfo = session.getRequestInfo();
                                requestInfo.data.driverInfo.onRoute = "0";
                                session.createRequest(requestInfo);

                            }

                        } else {
                            Toast.makeText(mContext, massage, Toast.LENGTH_SHORT).show();
                        }

                        progress.setVisibility(View.GONE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.setVisibility(View.GONE);
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    ErrorDialog.showSessionError(error, mContext);
                    progress.setVisibility(View.GONE);
                }

            });
            api.callApi("user/route", Request.Method.GET, null, true);
        } else {
            Toast.makeText(mContext, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = mContext.getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return false;
    }

    // Timer for remember for 5 mins
    private void startRemainingTimer() {
        // Timer for 5min
        if (DNG.countDownTimer != null) {
            DNG.countDownTimer.cancel();
            DNG.countDownTimer = null;
        }

        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (300003), pendingIntent);
        }

        DNG.countDownTimer = new CountDownTimer(300000, 1000) { //300000
            public void onTick(long millis) {
                timerIsRunning = true;
                tv_timer_.setVisibility(View.VISIBLE);
                tv_timer_.setText("" + String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                        TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                        TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))));
            }

            public void onFinish() {
                if (DNG.isActivityVisible()) {
                    cancelPendingTask();
                }
                timerIsRunning = false;
                tv_timer_.setText("");
                tv_timer_.setVisibility(View.GONE);
                if (mContext != null && isAdded()) {
                    openStartRouteDialog();
                }
            }
        };
        DNG.countDownTimer.start();

    }

    // Cancel timer broadcast pending intent
    private void cancelPendingTask() {
        assert alarmManager != null;
        alarmManager.cancel(pendingIntent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cv_cash:
                cv_upload_receipt.setVisibility(View.VISIBLE);
                openFullMinimumPaymentDialog(R.id.cv_cash);
                break;

            case R.id.cv_account:
                cv_upload_receipt.setVisibility(View.VISIBLE);
                openFullMinimumPaymentDialog(R.id.cv_account);
                break;

            case R.id.cv_credit_card:
                cv_upload_receipt.setVisibility(View.VISIBLE);
                openFullMinimumPaymentDialog(R.id.cv_credit_card);
                break;

            case R.id.cv_cheque:
                cv_upload_receipt.setVisibility(View.VISIBLE);
                openChequeDialog();
                break;

            case R.id.ry_call_customer:
                yesToCallDialog("Do you want to make call to " + requestInfo.data.orderInfo.customerPhoneNumber + " ?", requestInfo.data.orderInfo.customerPhoneNumber);
                break;

            case R.id.iv_upload_receipt:
                if (paymentSelected) {
                    if (requestInfo.data != null && requestInfo.data.pitInfo != null) {
                        if (requestInfo.data.pitInfo.pitPayStatus.equals("0")) {
                            if (receiptImageBitMap == null && session.getLoadingRouteReceipt().equals("")) {
                                String imageUploadType = "iv_upload_receipt";

                                if (Build.VERSION.SDK_INT >= 23) {
                                    if (mContext.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                        requestPermissions(
                                                new String[]{Manifest.permission.CAMERA,
                                                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                Constant.MY_PERMISSIONS_REQUEST_CAMERA);
                                    } else {
                                        ImagePicker.pickImageFromCamera(LoadingDetailsFragment.this);
                                    }
                                } else {
                                    ImagePicker.pickImageFromCamera(LoadingDetailsFragment.this);
                                }
                            } else {
                                openZoomImageDialog(receiptImageBitMap);
                            }

                        } else if (requestInfo.data.pitInfo.pitPayStatus.equals("1")) {
                            if (session.getLoadingRouteReceipt().equals("")) {
                                Toast.makeText(mContext, "Pit payment completed from CRM", Toast.LENGTH_SHORT).show();
                            } else {
                                openZoomImageDialog(receiptImageBitMap);
                            }

                        }
                    }
                } else {
                    Toast.makeText(mContext, getResources().getString(R.string.payment_info_null), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.btn_out_for_delivery:
                if (Build.VERSION.SDK_INT >= 23) {
                    if (!Settings.canDrawOverlays(mContext)) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + mContext.getPackageName()));
                        startActivityForResult(intent, 1234);
                    } else {
                        if (mContext != null) {
                            openStartRouteDialog();
                        }
                    }
                } else {
                    if (mContext != null) {
                        openStartRouteDialog();
                    }
                }
                break;

            case R.id.btn_pay_now:
                if (requestInfo.data.pitInfo.pitPayStatus.equals("0")) {
                    if ((payAmount == null) || (payAmount.equals(""))) {
                        Toast.makeText(mContext, getResources().getString(R.string.payment_info_null), Toast.LENGTH_SHORT).show();
                    } else if (receiptImageBitMap == null) {
                        Toast.makeText(mContext, getResources().getString(R.string.upload_receipt_image_null), Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        String subOrderId = requestInfo.data.subOrderId;
                        btn_pay_now.setEnabled(false);
                        pitPayment(subOrderId, payAmount, paymentType, ed_description.getText().toString());
                    }

                } else if (requestInfo.data.pitInfo.pitPayStatus.equals("1")) {
                    btn_out_for_delivery.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.iv_upload_receipt_cancel:

                iv_upload_receipt.setImageDrawable(getResources().getDrawable(R.drawable.upload));
                receiptImageBitMap = null;
                iv_upload_receipt_cancel.setVisibility(View.GONE);
                session.setLoadingRouteReceipt("", paymentMode, ed_description.getText().toString());
                break;

            case R.id.btn_goto_delivery_detail:
                startStopRoute();
                replaceFragment(DeliveryDetailsFragment.newInstance("", ""), false, R.id.fragment_place);
                break;
        }
    }

    // Dialog to open pit payment receipt in zoom view
    private void openZoomImageDialog(Bitmap bitmap) {
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


        if (bitmap != null) {
            mphoto_view.setImageBitmap(bitmap);
        } else if (!session.getLoadingRouteReceipt().equals("")) {
            Glide.with(mContext).load(session.getLoadingRouteReceipt()).into(mphoto_view);
        }


        mcancel_dialog_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                zoomImageDialog.dismiss();
            }
        });

        zoomImageDialog.getWindow().setGravity(Gravity.CENTER);
        zoomImageDialog.show();

    }

    // Dialog to enter Cheque number
    private void openChequeDialog() {
        chequeDialog = new Dialog(mContext);
        chequeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        chequeDialog.setContentView(R.layout.dialog_cheque_payment);

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(chequeDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.FILL_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        chequeDialog.getWindow().setAttributes(lWindowParams);

        ed_cheque_number = chequeDialog.findViewById(R.id.ed_cheque_number);
        ImageView dialog_decline_button = chequeDialog.findViewById(R.id.dialog_decline_button);
        TextView btn_pay = chequeDialog.findViewById(R.id.btn_pay);

        if (!session.getLoadingChequeNumber().equals("")) {
            ed_cheque_number.setText(session.getLoadingChequeNumber());
            ed_cheque_number.setInputType(InputType.TYPE_NULL);
        }

        dialog_decline_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chequeDialog.dismiss();
            }
        });

        btn_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ed_cheque_number.getText().toString().equals("")) {
                    Toast.makeText(mContext, getResources().getString(R.string.cheque_number_null), Toast.LENGTH_SHORT).show();
                } else if (ed_cheque_number.getText().toString().length() > 0 && ed_cheque_number.getText().toString().length() < 6) {
                    Toast.makeText(mContext, getResources().getString(R.string.cheque_number_invalid), Toast.LENGTH_SHORT).show();
                } else {
                    chequeNumber = ed_cheque_number.getText().toString().trim();
                    session.setLoadingChequeNumber(chequeNumber);
                    chequeDialog.dismiss();
                    openFullMinimumPaymentDialog(R.id.cv_cheque);
                }
            }
        });

        chequeDialog.getWindow().setGravity(Gravity.CENTER);
        chequeDialog.show();
    }

    // Dialog to select payment type - Full or partial and enter amount for pit payment
    private void openFullMinimumPaymentDialog(final int cv_cash) {
        fullMinPaymentDialog = new Dialog(mContext);
        fullMinPaymentDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        fullMinPaymentDialog.setContentView(R.layout.dialog_full_min_payment_details);

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(fullMinPaymentDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.FILL_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        fullMinPaymentDialog.getWindow().setAttributes(lWindowParams);

        ed_total_amount = fullMinPaymentDialog.findViewById(R.id.ed_total_amount);
        RadioGroup rg_payment_type = fullMinPaymentDialog.findViewById(R.id.rg_payment_type);
        rb_full_pay = rg_payment_type.findViewById(R.id.rb_full_pay);
        rb_partial_pay = rg_payment_type.findViewById(R.id.rb_partial_pay);
        TextView tv_total_amount = fullMinPaymentDialog.findViewById(R.id.tv_total_amount);
        TextView tv_minimum_payment = fullMinPaymentDialog.findViewById(R.id.tv_minimum_payment);

        tv_total_amount.setText("$ " + String.valueOf(requestInfo.data.pitInfo.pitPay.totalAmount));

        tv_minimum_payment.setText("$ " + String.valueOf(requestInfo.data.pitInfo.pitPay.dueAmount));

        ed_total_amount.setText(String.valueOf(requestInfo.data.pitInfo.pitPay.dueAmount));
        amt = requestInfo.data.pitInfo.pitPay.totalAmount;

        if (requestInfo.data.totalShift.equals("1")) {
            ed_total_amount.setEnabled(false);
            rb_full_pay.setChecked(true);
            rb_partial_pay.setClickable(false);

        } else if (numberofshift == totalShift) {
            ed_total_amount.setText(String.valueOf(requestInfo.data.pitInfo.pitPay.dueAmount));
            rb_partial_pay.setClickable(false);
            ed_total_amount.setEnabled(false);
        } else {
            if (rb_full_pay.isChecked()) {
                ed_total_amount.setEnabled(false);
            } else if (rb_partial_pay.isChecked()) {
                ed_total_amount.setEnabled(true);
                inputFilter(ed_total_amount);
                amt = requestInfo.data.pitInfo.pitPay.totalAmount;
                rb_partial_pay.setClickable(true);
            }
        }

        rg_payment_type.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.rb_full_pay) {
                    ed_total_amount.setEnabled(false);
                    ed_total_amount.setText(String.valueOf(requestInfo.data.pitInfo.pitPay.dueAmount));
                } else if (i == R.id.rb_partial_pay) {
                    ed_total_amount.setEnabled(true);
                    inputFilter(ed_total_amount);
                    amt = requestInfo.data.pitInfo.pitPay.totalAmount;
                    rb_partial_pay.setClickable(true);
                }
            }
        });

        if (numberofshift == totalShift) {
            rb_partial_pay.setClickable(false);
        } else {
            rb_partial_pay.setClickable(true);
        }

        ImageView dialog_decline_button = fullMinPaymentDialog.findViewById(R.id.dialog_decline_button);
        TextView btn_pay = fullMinPaymentDialog.findViewById(R.id.btn_pay);

        dialog_decline_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fullMinPaymentDialog.dismiss();
            }
        });

        btn_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pitPaymentAmount = ed_total_amount.getText().toString().trim();

                if (pitPaymentAmount.equals("")) {
                    Toast.makeText(mContext, getResources().getString(R.string.amount_null), Toast.LENGTH_SHORT).show();
                } else if (!isValidAmount(ed_total_amount)) {
                    Toast.makeText(mContext, getResources().getString(R.string.amount_invalid), Toast.LENGTH_SHORT).show();
                } else if (numberofshift != Integer.parseInt(requestInfo.data.totalShift)) {
                    if (pitPaymentAmount != null) {
                        if (Double.parseDouble(pitPaymentAmount) > amt) {
                            Toast.makeText(mContext, getResources().getString(R.string.amount_invalid), Toast.LENGTH_SHORT).show();
                        } else {
                            payAmount = ed_total_amount.getText().toString().trim();

                            if (rb_full_pay.isChecked()) {
                                paymentType = "FULL";
                            } else if (rb_partial_pay.isChecked()) {
                                paymentType = "PARTIAL";
                            }

                            if (cv_cash == R.id.cv_cash) {
                                paymentMode = "CASH";
                            } else if (cv_cash == R.id.cv_account) {
                                paymentMode = "ACCOUNT";
                            } else if (cv_cash == R.id.cv_credit_card) {
                                paymentMode = "CREDITCARD";
                            } else if (cv_cash == R.id.cv_cheque) {
                                paymentMode = "CHEQUE";
                            }

                            session.setPaymentMode(LoadingDetailsFragment.this.paymentMode);

                            paymentSelected = true;
                            iv_upload_receipt.setClickable(true);
                            iv_upload_receipt.setImageDrawable(getResources().getDrawable(R.drawable.upload));
                            receiptImageBitMap = null;
                            iv_upload_receipt_cancel.setVisibility(View.GONE);

                            if (LoadingDetailsFragment.this.paymentMode.equals("CASH")) {
                                setCashModeActive();

                                if (ed_cheque_number != null) {
                                    ed_cheque_number.setText("");
                                    session.setLoadingChequeNumber("");
                                }

                            } else if (LoadingDetailsFragment.this.paymentMode.equals("ACCOUNT")) {
                                setAccountModeActive();

                                if (ed_cheque_number != null) {
                                    ed_cheque_number.setText("");
                                    session.setLoadingChequeNumber("");
                                }

                            } else if (LoadingDetailsFragment.this.paymentMode.equals("CREDITCARD")) {
                                setCreditModeActive();

                                if (ed_cheque_number != null) {
                                    ed_cheque_number.setText("");
                                    session.setLoadingChequeNumber("");
                                }

                            } else if (LoadingDetailsFragment.this.paymentMode.equals("CHEQUE")) {
                                setChequeModeActive();
                            }
                            fullMinPaymentDialog.dismiss();
                        }
                    }
                } else {
                    payAmount = ed_total_amount.getText().toString().trim();
                    fullMinPaymentDialog.dismiss();

                    if (rb_full_pay.isChecked()) {
                        paymentType = "FULL";
                    } else if (rb_partial_pay.isChecked()) {
                        paymentType = "PARTIAL";
                    }

                    if (cv_cash == R.id.cv_cash) {
                        paymentMode = "CASH";
                    } else if (cv_cash == R.id.cv_account) {
                        paymentMode = "ACCOUNT";
                    } else if (cv_cash == R.id.cv_credit_card) {
                        paymentMode = "CREDITCARD";
                    } else if (cv_cash == R.id.cv_cheque) {
                        paymentMode = "CHEQUE";
                    }

                    session.setPaymentMode(LoadingDetailsFragment.this.paymentMode);

                    paymentSelected = true;
                    iv_upload_receipt.setImageDrawable(getResources().getDrawable(R.drawable.upload));
                    receiptImageBitMap = null;
                    iv_upload_receipt_cancel.setVisibility(View.GONE);

                    if (LoadingDetailsFragment.this.paymentMode.equals("CASH")) {
                        setCashModeActive();

                        if (ed_cheque_number != null) {
                            ed_cheque_number.setText("");
                            session.setLoadingChequeNumber("");
                        }

                    } else if (LoadingDetailsFragment.this.paymentMode.equals("ACCOUNT")) {
                        setAccountModeActive();

                        if (ed_cheque_number != null) {
                            ed_cheque_number.setText("");
                            session.setLoadingChequeNumber("");
                        }

                    } else if (LoadingDetailsFragment.this.paymentMode.equals("CREDITCARD")) {
                        setCreditModeActive();

                        if (ed_cheque_number != null) {
                            ed_cheque_number.setText("");
                            session.setLoadingChequeNumber("");
                        }

                    } else if (LoadingDetailsFragment.this.paymentMode.equals("CHEQUE")) {
                        setChequeModeActive();

                    }
                }
            }
        });


        fullMinPaymentDialog.getWindow().setGravity(Gravity.CENTER);
        fullMinPaymentDialog.show();
    }

    // Validation for amount not to accept 0
    public boolean isValidAmount(EditText editText) {
        double amount = Double.parseDouble(editText.getText().toString().trim());

        if (amount == 0.0) {
            return false;
        }

        return true;
    }

    public boolean isInternetisGpsEnable() {
        isGPSEnable = lmgr.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!isGPSEnable) {

            AlertDialog.Builder ab = new AlertDialog.Builder(mContext);
            ab.setTitle("GPS not enable");
            ab.setMessage("Do you want to enable");
            ab.setPositiveButton("settings"
                    , new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isGPSEnable = true;
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    });

            ab.setNegativeButton("cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    dialog.dismiss();
                }
            });
            ab.show();

        }
        return false;
    }

    public void replaceFragment(Fragment fragment, boolean addToBackStack, int containerId) {
        String backStackName = fragment.getClass().getName();
        FragmentManager fm = getFragmentManager();
        boolean fragmentPopped = getFragmentManager().popBackStackImmediate(backStackName, 0);
        if (fm != null)
            if (!fragmentPopped) {
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(containerId, fragment, backStackName).setTransition(FragmentTransaction.TRANSIT_UNSET);
                if (addToBackStack)
                    transaction.addToBackStack(backStackName);
                transaction.commit();
            }
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        if (LocationRuntimePermission.checkLocationPermission(mContext)) {

            if (isInternetisGpsEnable()) {
                Location startPoint = new Location("locationA");
                startPoint.setLatitude(location.getLatitude());
                startPoint.setLongitude(location.getLongitude());

                String cust_lat = requestInfo.data.orderInfo.deliveryLatitude;
                String cust_lng = requestInfo.data.orderInfo.deliveryLongitude;

                Location endPoint = new Location("locationA");
                endPoint.setLatitude(Double.parseDouble(cust_lat));
                endPoint.setLongitude(Double.parseDouble(cust_lng));

                double distance2 = startPoint.distanceTo(endPoint);
                double customerValueNearLocation = Double.parseDouble(new DecimalFormat("##.##").format(distance2));

                if (customerValueNearLocation <= 200.00) {
                    btn_out_for_delivery.setVisibility(View.VISIBLE);
                   /* btn_goto_delivery_detail.setVisibility(View.VISIBLE);

                    btn_goto_delivery_detail.setOnClickListener(this);*/
                }
            }
        }


    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //displayCurrentLocation();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    /**
     * Method to verify google play services on the device
     */

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(mContext);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {

            }
            return false;
        }
        return true;
    }


    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPlayServices();
        // Resuming the periodic location updates
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }

        RequestInfo requestInfo = session.getRequestInfo();
        if (requestInfo.isInCustomerArea.equals("yesInCustomerArea")) {
            btn_out_for_delivery.setVisibility(View.VISIBLE);
            btn_goto_delivery_detail.setVisibility(View.VISIBLE);

            btn_goto_delivery_detail.setOnClickListener(this);
        }

    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) getActivity());
    }


    private void stopLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener) getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    // Set Cash Pit Payment Mode active in case of payment mode completed
    private void setCashModeActive() {
        iv_cash.setImageDrawable(getResources().getDrawable(R.drawable.active_cash));
        iv_credit_card.setImageDrawable(getResources().getDrawable(R.drawable.card_icon));
        iv_account.setImageDrawable(getResources().getDrawable(R.drawable.bank_icon));
        iv_cheque.setImageDrawable(getResources().getDrawable(R.drawable.cheque_icon));

        tv_cash_mode.setTextColor(getResources().getColor(R.color.colorPrimary));
        tv_credit_mode.setTextColor(getResources().getColor(R.color.black));
        tv_account_mode.setTextColor(getResources().getColor(R.color.black));
        tv_cheque_mode.setTextColor(getResources().getColor(R.color.black));
    }

    // Set Credit Pit Payment Mode active in case of payment mode completed
    private void setCreditModeActive() {
        iv_cash.setImageDrawable(getResources().getDrawable(R.drawable.cash_icon));
        iv_credit_card.setImageDrawable(getResources().getDrawable(R.drawable.active_credit_card));
        iv_account.setImageDrawable(getResources().getDrawable(R.drawable.bank_icon));
        iv_cheque.setImageDrawable(getResources().getDrawable(R.drawable.cheque_icon));

        tv_cash_mode.setTextColor(getResources().getColor(R.color.black));
        tv_credit_mode.setTextColor(getResources().getColor(R.color.colorPrimary));
        tv_account_mode.setTextColor(getResources().getColor(R.color.black));
        tv_cheque_mode.setTextColor(getResources().getColor(R.color.black));
    }

    // Set Account Pit Payment Mode active in case of payment mode completed
    private void setAccountModeActive() {
        iv_cash.setImageDrawable(getResources().getDrawable(R.drawable.cash_icon));
        iv_credit_card.setImageDrawable(getResources().getDrawable(R.drawable.card_icon));
        iv_account.setImageDrawable(getResources().getDrawable(R.drawable.active_bank));
        iv_cheque.setImageDrawable(getResources().getDrawable(R.drawable.cheque_icon));

        tv_cash_mode.setTextColor(getResources().getColor(R.color.black));
        tv_credit_mode.setTextColor(getResources().getColor(R.color.black));
        tv_account_mode.setTextColor(getResources().getColor(R.color.colorPrimary));
        tv_cheque_mode.setTextColor(getResources().getColor(R.color.black));
    }

    // Set Cheque Pit Payment Mode active in case of payment mode completed
    private void setChequeModeActive() {
        iv_cash.setImageDrawable(getResources().getDrawable(R.drawable.cash_icon));
        iv_credit_card.setImageDrawable(getResources().getDrawable(R.drawable.card_icon));
        iv_account.setImageDrawable(getResources().getDrawable(R.drawable.bank_icon));
        iv_cheque.setImageDrawable(getResources().getDrawable(R.drawable.active_check));

        tv_cash_mode.setTextColor(getResources().getColor(R.color.black));
        tv_credit_mode.setTextColor(getResources().getColor(R.color.black));
        tv_account_mode.setTextColor(getResources().getColor(R.color.black));
        tv_cheque_mode.setTextColor(getResources().getColor(R.color.colorPrimary));
    }
}
