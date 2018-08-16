package com.dng.fragment;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
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

import com.dng.helper.AppHelper;
import com.dng.helper.Constant;
import com.dng.helper.ErrorDialog;
import com.dng.image.picker.ImagePicker;
import com.dng.model.RequestInfo;
import com.dng.server_task.WebService;
import com.dng.service.MyService;
import com.dng.session.Session;

import com.view.cropper.CropImage;
import com.view.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.dng.activity.MainActivity.current_lat;
import static com.dng.activity.MainActivity.current_lng;

public class DeliveryDetailsFragment extends Fragment implements View.OnClickListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Context mContext;

    private TextView tv_order_num, tv_customer_name, tv_qty, tv_product, tv_payment_mode, tv_customer_amount, btn_close_order;
    private LinearLayout ry_call_customer;
    private CardView cv_cash, cv_credit_card, cv_skip, cv_cheque, cv_upload_receipt;
    private TextView tv_cash_mode, tv_credit_mode, tv_cheque_mode, tv_skip_mode;
    private ImageView iv_cash, iv_credit_card, iv_skip, iv_cheque;

    private ProgressBar progress;
    private Session session;
    private RequestInfo requestInfo;
    private Bitmap receiptImage, beforeBitmap, afterBitmap, customer_paymentBitmap;

    private String imageUploadType = "";
    private String deliveryId = "";
    private int numberofshift = 0, paymentShift = 0;
    private int count = -1;
    private int totalShift;
    private String paymentMode = "";

    private ImageView iv_upload_receipt, iv_before_delivery, iv_after_delivery, iv_customer_payment_receipt;
    private ImageView before_receipt_cancel, after_receipt_cancel, iv_upload_receipt_cancel, iv_customer_payment_cancel;
    private EditText ed_description;
    private Dialog chequeDialog, fullMinPaymentDialog, zoomImageDialog;
    private EditText ed_cheque_number;

    private String payAmount = "", chequeNumber = "";
    private EditText ed_total_amount;

    private RadioGroup rg_payment_type;
    private RadioButton rb_full_pay, rb_partial_pay;

    private TextView tv_total_amount, tv_minimum_payment;
    private double amt;

    private RelativeLayout rl_customer_payment_status, rl_customer_payment;
    private LinearLayout ly_customer_payment_mode;
    private String paymentType;
    private Bitmap profileImageBitmap;
    private boolean isClickedSkip = false;
    private boolean isPaymentSelected;


    public DeliveryDetailsFragment() {
        // Required empty public constructor
    }

    public static DeliveryDetailsFragment newInstance(String param1, String param2) {
        DeliveryDetailsFragment fragment = new DeliveryDetailsFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delivery_details, container, false);

        session = new Session(mContext);
        requestInfo = session.getRequestInfo();
        init(view);
        session.setScreen("DeliveryDetailsFragment");

        startStopRoute();

        if (requestInfo != null) {
            deliveryId = requestInfo.data.deliveryId;

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
                    if (requestInfo.data.orderInfo.customerPaymentStatus != null) {
                        if (requestInfo.data.orderInfo.customerPay.pay.size() != 0) {
                            paymentShift = requestInfo.data.orderInfo.customerPay.pay.size() - 1;
                        } else {
                            paymentShift = Integer.parseInt(requestInfo.data.completeShift);
                        }
                    }
                }
            }

            // Hide skip mode in single shift or last shift
            if (requestInfo.data.totalShift.equals("1")) {
                cv_skip.setVisibility(View.GONE);
            } else if (numberofshift == totalShift) {
                cv_skip.setVisibility(View.GONE);
            } else {
                cv_skip.setVisibility(View.VISIBLE);
            }

            // Hide all payment modes when Credit Card
            if (requestInfo.data.orderInfo.paymentMode.equals("CARD")) {
                ly_customer_payment_mode.setVisibility(View.GONE);
                rl_customer_payment.setVisibility(View.GONE);
            } else {
                ly_customer_payment_mode.setVisibility(View.VISIBLE);
                rl_customer_payment.setVisibility(View.VISIBLE);
            }


            // Set prefilled data when customer payment is completed and its status is 1
            // And when payment status is 0 , have to complete payment procedure
            if (requestInfo.data.orderInfo.customerPaymentStatus != null) {
                if (requestInfo.data.orderInfo.customerPaymentStatus.equals("1")) {
                    rl_customer_payment_status.setVisibility(View.VISIBLE);

                    cv_cash.setEnabled(false);
                    cv_cheque.setEnabled(false);
                    cv_credit_card.setEnabled(false);
                    cv_skip.setEnabled(false);
                    cv_skip.setVisibility(View.GONE);
                    ed_description.setInputType(InputType.TYPE_NULL);

                    if (requestInfo.data.orderInfo.customerPay.pay.size() != 0) {
                        if (requestInfo.data.orderInfo.customerPay.pay.get(paymentShift).receipt != null) {
                            String receipt = requestInfo.data.orderInfo.customerPay.pay.get(paymentShift).receipt;
                            String paymentMode = requestInfo.data.orderInfo.customerPay.pay.get(paymentShift).paymentMode;
                            String description = requestInfo.data.orderInfo.customerPay.pay.get(paymentShift).description;

                            session.setCustomerPaymentReceiptBitmap(receipt);
                            session.setDeliveryDetailReceipt(paymentMode, description);
                        }
                    }


                } else if (requestInfo.data.orderInfo.customerPaymentStatus.equals("0")) {
                    rl_customer_payment_status.setVisibility(View.GONE);

                    cv_cash.setEnabled(true);
                    cv_credit_card.setEnabled(true);
                    cv_skip.setEnabled(true);
                    cv_cheque.setEnabled(true);

                    iv_upload_receipt.setEnabled(true);

                    ed_description.setEnabled(true);

                    session.setDeliveryChequeNumber("");
                }

                // Set delivery data from session or response
                setDeliveryData();

            }
        }

        cv_cash.setOnClickListener(this);
        cv_credit_card.setOnClickListener(this);
        cv_skip.setOnClickListener(this);
        cv_cheque.setOnClickListener(this);

        ry_call_customer.setOnClickListener(this);

        iv_before_delivery.setOnClickListener(this);
        iv_after_delivery.setOnClickListener(this);
        iv_upload_receipt.setOnClickListener(this);
        iv_customer_payment_receipt.setOnClickListener(this);

        before_receipt_cancel.setOnClickListener(this);
        after_receipt_cancel.setOnClickListener(this);
        iv_upload_receipt_cancel.setOnClickListener(this);
        iv_customer_payment_cancel.setOnClickListener(this);

        btn_close_order.setOnClickListener(this);

        return view;
    }

    private void setDeliveryData() {
        // Before Delivery Image
        if (!session.getBeforeDeliveryBitmap().equals("")) {
            Glide.with(mContext).load(session.getBeforeDeliveryBitmap()).into(iv_before_delivery);
            iv_before_delivery.setOnClickListener(this);
        }

        // After Delivery Image
        if (!session.getAfterDeliveryBitmap().equals("")) {
            Glide.with(mContext).load(session.getAfterDeliveryBitmap()).into(iv_after_delivery);
            iv_after_delivery.setOnClickListener(this);
        }

        // Delivery Waiver Receipt
        if (!session.getWaiverReceiptBitmap().equals("")) {
            Glide.with(mContext).load(session.getWaiverReceiptBitmap()).into(iv_upload_receipt);
            iv_upload_receipt.setEnabled(true);
            iv_upload_receipt.setOnClickListener(this);
        }

        // Customer Payment Receipt
        if (!session.getCustomerPaymentReceiptBitmap().equals("")) {
            Glide.with(mContext).load(session.getCustomerPaymentReceiptBitmap()).into(iv_customer_payment_receipt);

            iv_customer_payment_receipt.setEnabled(true);
            iv_customer_payment_receipt.setOnClickListener(this);
        } else {
            if (requestInfo.data.orderInfo.customerPaymentStatus.equals("1")) {
                if (requestInfo.data.orderInfo.customerPay.pay.size() != 0) {
                    if (requestInfo.data.orderInfo.customerPay.pay.get(paymentShift).receipt.equals("")) {
                        iv_customer_payment_receipt.setImageDrawable(getResources().getDrawable(R.drawable.upload));
                    } else {
                        String receiptUrl = requestInfo.data.orderInfo.customerPay.pay.get(paymentShift).receipt;
                        Glide.with(mContext).load(receiptUrl).into(iv_customer_payment_receipt);
                    }
                } else {
                    iv_customer_payment_receipt.setImageDrawable(getResources().getDrawable(R.drawable.upload));
                }
            }

        }

        // Setting Customer Payment Mode
        if (!session.getDeliveryDetailPaymentMode().equals("")) {
            if (session.getDeliveryDetailPaymentMode().equals("CASH")) {
                setDeliveryCashModeActive();
            } else if (session.getDeliveryDetailPaymentMode().equals("CREDITCARD")) {
                setDeliveryCreditModeActive();

            } else if (session.getDeliveryDetailPaymentMode().equals("CHEQUE")) {
                setDeliveryChequeModeActive();

            }
            isPaymentSelected = true;
        }

        //  // Setting Customer Description
        if (!session.getDeliveryDetailDescription().equals("")) {
            ed_description.setText(session.getDeliveryDetailDescription());
        }
    }

    void init(View view) {
        progress = view.findViewById(R.id.progress);

        iv_upload_receipt = view.findViewById(R.id.iv_upload_receipt);
        ry_call_customer = view.findViewById(R.id.ry_call_customer);
        ed_description = view.findViewById(R.id.ed_description);

        tv_order_num = view.findViewById(R.id.tv_order_num);
        tv_customer_name = view.findViewById(R.id.tv_customer_name);
        tv_qty = view.findViewById(R.id.tv_qty);
        tv_product = view.findViewById(R.id.tv_product);
        tv_payment_mode = view.findViewById(R.id.tv_payment_mode);
        tv_customer_amount = view.findViewById(R.id.tv_customer_amount);

        cv_cash = view.findViewById(R.id.cv_cash);
        cv_credit_card = view.findViewById(R.id.cv_credit_card);
        cv_skip = view.findViewById(R.id.cv_skip);
        cv_cheque = view.findViewById(R.id.cv_cheque);

        iv_cash = view.findViewById(R.id.iv_cash);
        iv_credit_card = view.findViewById(R.id.iv_credit_card);
        iv_cheque = view.findViewById(R.id.iv_cheque);
        iv_skip = view.findViewById(R.id.iv_skip);

        tv_cash_mode = view.findViewById(R.id.tv_cash_mode);
        tv_credit_mode = view.findViewById(R.id.tv_credit_mode);
        tv_cheque_mode = view.findViewById(R.id.tv_cheque_mode);
        tv_skip_mode = view.findViewById(R.id.tv_skip_mode);

        cv_upload_receipt = view.findViewById(R.id.cv_upload_receipt);

        iv_before_delivery = view.findViewById(R.id.iv_before_delivery);
        iv_after_delivery = view.findViewById(R.id.iv_after_delivery);

        btn_close_order = view.findViewById(R.id.btn_close_order);

        iv_upload_receipt_cancel = view.findViewById(R.id.iv_upload_receipt_cancel);
        after_receipt_cancel = view.findViewById(R.id.after_receipt_cancel);
        before_receipt_cancel = view.findViewById(R.id.before_receipt_cancel);

        iv_customer_payment_receipt = view.findViewById(R.id.iv_customer_payment_receipt);
        iv_customer_payment_cancel = view.findViewById(R.id.iv_customer_payment_cancel);

        ly_customer_payment_mode = view.findViewById(R.id.ly_customer_payment_mode);
        rl_customer_payment_status = view.findViewById(R.id.rl_customer_payment_status);

        rl_customer_payment = view.findViewById(R.id.rl_customer_payment);


        // Setting Data
        if (requestInfo.data != null) {
            tv_order_num.setText(requestInfo.data.invoiceOrderId);
            tv_customer_name.setText(requestInfo.data.orderInfo.customerName);

            tv_qty.setText(requestInfo.data.productInfo.quantity + " " + requestInfo.data.productInfo.unitType);
            tv_product.setText(requestInfo.data.productInfo.productName);
            tv_customer_amount.setText("$" + requestInfo.data.orderInfo.totalAmount);

            if (requestInfo.data.orderInfo.paymentMode.equals("COD")) {
                tv_payment_mode.setText("C.O.D");
            } else {
                tv_payment_mode.setText(requestInfo.data.orderInfo.paymentMode);
            }
        }

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
        mContext = context;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.pickImageFromCamera(DeliveryDetailsFragment.this);
                } else {
                    Toast.makeText(mContext, "YOU DENIED PERMISSION CANNOT SELECT IMAGE", Toast.LENGTH_LONG).show();
                }
                break;
            }

            case Constant.MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.pickImageFromCamera(DeliveryDetailsFragment.this);

                } else {
                    Toast.makeText(mContext, "YOUR  PERMISSION DENIED ", Toast.LENGTH_LONG).show();
                }
                break;
            }
            case Constant.CALL_PHONE: {
                //  getPermissionMakeCall(phone_number);
                break;
            }

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
                //Bitmap bitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
                Uri imageUri = ImagePicker.getImageURIFromResult(mContext, requestCode, resultCode, data);
                if (imageUri != null) {
                    CropImage.activity(imageUri).setCropShape(CropImageView.CropShape.RECTANGLE)
                            .setAspectRatio(4, 4)
                            .start(mContext, DeliveryDetailsFragment.this);
                } else {
                    Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();
                }

            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                try {
                    if (result != null)
                        profileImageBitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), result.getUri());

                    if (profileImageBitmap != null) {
                        switch (imageUploadType) {
                            case "iv_upload_receipt": {
                                receiptImage = profileImageBitmap;
                                iv_upload_receipt.setImageBitmap(profileImageBitmap);
                                iv_upload_receipt_cancel.setVisibility(View.VISIBLE);

                                //      uploadTypeofImages(deliveryId, numberofshift, "deliveryReceipt");
                                break;
                            }
                            case "iv_before_delivery": {
                                beforeBitmap = profileImageBitmap;
                                iv_before_delivery.setImageBitmap(profileImageBitmap);
                                before_receipt_cancel.setVisibility(View.VISIBLE);

                                uploadTypeofImages(deliveryId, numberofshift, "beforeDelivery");
                                break;
                            }
                            case "iv_after_delivery": {
                                afterBitmap = profileImageBitmap;
                                iv_after_delivery.setImageBitmap(profileImageBitmap);
                                after_receipt_cancel.setVisibility(View.VISIBLE);

                                uploadTypeofImages(deliveryId, numberofshift, "afterDelivery");
                                break;
                            }

                            case "iv_customer_payment_receipt": {
                                customer_paymentBitmap = profileImageBitmap;
                                iv_customer_payment_receipt.setImageBitmap(profileImageBitmap);
                                iv_customer_payment_cancel.setVisibility(View.VISIBLE);

                                break;
                            }
                        }
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

    // Api call to complete customer payment
    private void customerPayment(String orderId, final String amount, String paymentType, final String description) {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.setVisibility(View.VISIBLE);
            Map<String, String> map = new HashMap<>();
            map.put("orderId", orderId);
            map.put("paymentMode", paymentMode);
            map.put("amount", amount);
            map.put("paymentType", paymentType);
            map.put("description", description);

            Map<String, Bitmap> bitmapList = new HashMap<>();

            if (customer_paymentBitmap == null) {
                map.put("receiptImage", "");
            } else {
                bitmapList.put("receiptImage", customer_paymentBitmap);
            }

            WebService api = new WebService(mContext, "fds", new WebService.ResponceListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        Log.e("CUSTOMER PAYMENT", response.toString());

                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");

                        if (status.equals("success")) {
                            if (message.equals("Customer payment paid successfully done.")) {
                                JSONObject data = jsonObject.getJSONObject("data");
                                JSONArray payArray = data.getJSONArray("pay");

                                cv_cash.setEnabled(false);
                                cv_credit_card.setEnabled(false);
                                cv_cheque.setEnabled(false);
                                cv_skip.setEnabled(false);

                                ed_description.setInputType(InputType.TYPE_NULL);

                                if (data.getString("dueAmount").equals("0")) {
                                    requestInfo.data.orderInfo.customerPaymentStatus = "1";
                                    session.createRequest(requestInfo);

                                    String imageReceipt = payArray.getJSONObject(payArray.length() - 1).getString("receipt");
                                    session.setCustomerPaymentReceiptBitmap(imageReceipt);
                                    session.setDeliveryDetailReceipt(paymentMode, description);
                                }


                                uploadTypeofImages(deliveryId, numberofshift, "deliveryReceipt");

                            } else if (message.equals("Customer payment already paid.")) {
                                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                                uploadTypeofImages(deliveryId, numberofshift, "deliveryReceipt");
                            }

                        } else {
                            progress.setVisibility(View.GONE);
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        progress.setVisibility(View.GONE);
                        Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.setVisibility(View.GONE);
                    ErrorDialog.showSessionError(error, mContext);
                }
            });
            api.callMultiPartApi("user/customerPayment", map, bitmapList);
        } else {
            Toast.makeText(mContext, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
        }
    }

    // Api call to upload before delivery, after delivery and waiver receipt(Close order on uploading waiver receipt)
    private void uploadTypeofImages(String deliveryId, int shiftNumber, final String reportType) {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.setVisibility(View.VISIBLE);

            Map<String, Bitmap> bitmapList = null;
            Map<String, String> map = new HashMap<>();
            map.put("deliveryId", deliveryId);
            map.put("shiftNumber", String.valueOf(shiftNumber));
            map.put("reportType", reportType);

            if (reportType.equals("beforeDelivery")) {
                if (beforeBitmap == null) {
                    map.put("reportImage", "");
                } else {
                    bitmapList = new HashMap<>();
                    bitmapList.put("reportImage", beforeBitmap);
                }
            } else if (reportType.equals("afterDelivery")) {
                if (afterBitmap == null) {
                    map.put("reportImage", "");
                } else {
                    bitmapList = new HashMap<>();
                    bitmapList.put("reportImage", afterBitmap);
                }
            } else if (reportType.equals("deliveryReceipt")) {
                if (receiptImage == null) {
                    map.put("reportImage", "");
                    bitmapList = new HashMap<>();
                } else {
                    bitmapList = new HashMap<>();
                    bitmapList.put("reportImage", receiptImage);
                }
            }

            WebService api = new WebService(mContext, "fds", new WebService.ResponceListner() {
                @Override
                public void onResponse(String response, String apiName) {
                    progress.setVisibility(View.GONE);
                    try {

                         JSONObject jsonObject = new JSONObject(response);

                        Log.e("UPLOAD IMAGES RESPONSE", response.toString());
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");
                        JSONArray jsonArray = jsonObject.getJSONArray("data");

                        if (status.equals("success")) {
                            if (reportType.equals("beforeDelivery")) {
                                int numofShift = numberofshift - 1;

                                if (requestInfo.data != null) {
                                    requestInfo.data.shiftReport.get(numofShift).beforeDelivery.image = "done";
                                    RequestInfo requestInfo = session.getRequestInfo();
                                    requestInfo.data.shiftReport.get(numofShift).beforeDelivery.image = "done";

                                    session.createRequest(requestInfo);
                                }

                                JSONObject object = jsonArray.getJSONObject(numofShift);
                                JSONObject beforeDelivery = object.getJSONObject("beforeDelivery");

                                session.setBeforeDeliveryBitmap(beforeDelivery.getString("image"));

                            } else if (reportType.equals("afterDelivery")) {
                                int numofShift = numberofshift - 1;

                                if (requestInfo.data != null) {
                                    requestInfo.data.shiftReport.get(numofShift).afterDelivery.image = "done";
                                    RequestInfo requestInfo = session.getRequestInfo();
                                    requestInfo.data.shiftReport.get(numofShift).afterDelivery.image = "done";

                                    session.createRequest(requestInfo);
                                }

                                JSONObject object = jsonArray.getJSONObject(numofShift);
                                JSONObject afterDelivery = object.getJSONObject("afterDelivery");

                                session.setAfterDeliveryBitmap(afterDelivery.getString("image"));

                                // After uploading after delivery image, before delivery image can't be cancelled
                                before_receipt_cancel.setVisibility(View.GONE);

                            } else if (reportType.equals("deliveryReceipt")) {
                                int numofShift = numberofshift - 1;

                                if (requestInfo.data != null) {
                                    requestInfo.data.shiftReport.get(numofShift).deliveryReceipt.image = "done";
                                    RequestInfo requestInfo = session.getRequestInfo();
                                    requestInfo.data.shiftReport.get(numofShift).deliveryReceipt.image = "done";

                                    session.createRequest(requestInfo);
                                }

                                JSONObject object = jsonArray.getJSONObject(numofShift);
                                JSONObject deliveryReceipt = object.getJSONObject("deliveryReceipt");

                                session.setWaiverReceiptBitmap(deliveryReceipt.getString("image"));

                                // Set false for launch app dialog of start route to delivery.
                                session.setIsDialogOpen(false);
                                if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
                                   AppHelper.stopAlarmService(mContext);
                                }else {
                                    mContext.stopService(new Intent(mContext, MyService.class));
                                }

                                // Clear request info from session.
                                RequestInfo request = new RequestInfo();
                                session.createRequest(null);

                                session.getRequestInfo();

                                session.setLoadingRouteReceipt("", "", "");
                                session.setLoadingChequeNumber("");
                                session.setBeforeDeliveryBitmap("");
                                session.setAfterDeliveryBitmap("");
                                session.setWaiverReceiptBitmap("");
                                session.setCustomerPaymentReceiptBitmap("");
                                session.setDeliveryDetailReceipt("", "");

                                // Restarting new task after closing order
                                replaceFragment(NewTaskFragment.newInstance(current_lat, current_lng), false, R.id.fragment_place);

                            }

                        } else {
                            btn_close_order.setEnabled(true);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        btn_close_order.setEnabled(true);
                        progress.setVisibility(View.GONE);
                        Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.setVisibility(View.GONE);
                    btn_close_order.setEnabled(true);
                    ErrorDialog.showSessionError(error, mContext);
                }
            });
            api.callMultiPartApi("user/deliveryReport", map, bitmapList);
        } else {
            Toast.makeText(mContext, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
        }
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.cv_cash:
                isClickedSkip = false;
                cv_upload_receipt.setVisibility(View.VISIBLE);
                openFullMinimumPaymentDialog(R.id.cv_cash);
                break;

            case R.id.cv_credit_card:
                isClickedSkip = false;
                cv_upload_receipt.setVisibility(View.VISIBLE);
                openFullMinimumPaymentDialog(R.id.cv_credit_card);
                break;

            case R.id.cv_cheque:
                isClickedSkip = false;
                cv_upload_receipt.setVisibility(View.VISIBLE);
                openChequeDialog();
                break;

            case R.id.ry_call_customer:
                yesToCallDialog("Do you want to make call to " + requestInfo.data.orderInfo.customerPhoneNumber + " ?", requestInfo.data.orderInfo.customerPhoneNumber);
                break;

            case R.id.iv_before_delivery:
                if (AppHelper.isConnectingToInternet(mContext)) {
                    if (beforeBitmap == null && session.getBeforeDeliveryBitmap().equals("")) {
                        imageUploadType = "iv_before_delivery";
                        if (Build.VERSION.SDK_INT >= 23) {
                            if (mContext.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(
                                        new String[]{Manifest.permission.CAMERA,
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        Constant.MY_PERMISSIONS_REQUEST_CAMERA);
                            } else {
                                ImagePicker.pickImageFromCamera(DeliveryDetailsFragment.this);
                            }
                        } else {
                            ImagePicker.pickImageFromCamera(DeliveryDetailsFragment.this);
                        }

                    } else {
                        openZoomImageDialog(beforeBitmap, session.getBeforeDeliveryBitmap());
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.iv_customer_payment_receipt:
                if (isPaymentSelected) {
                    if (requestInfo.data != null && requestInfo.data.orderInfo != null) {
                        if (requestInfo.data.orderInfo.customerPaymentStatus.equals("0")) {
                            if (customer_paymentBitmap == null && session.getCustomerPaymentReceiptBitmap().equals("")) {
                                imageUploadType = "iv_customer_payment_receipt";
                                if (Build.VERSION.SDK_INT >= 23) {
                                    if (mContext.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                        requestPermissions(
                                                new String[]{Manifest.permission.CAMERA,
                                                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                Constant.MY_PERMISSIONS_REQUEST_CAMERA);
                                    } else {
                                        ImagePicker.pickImageFromCamera(DeliveryDetailsFragment.this);
                                    }
                                } else {
                                    ImagePicker.pickImageFromCamera(DeliveryDetailsFragment.this);
                                }
                            } else {
                                openZoomImageDialog(customer_paymentBitmap, session.getCustomerPaymentReceiptBitmap());
                            }
                        } else if (requestInfo.data.orderInfo.customerPaymentStatus.equals("1")) {
                            if (session.getCustomerPaymentReceiptBitmap().equals("")) {
                                Toast.makeText(mContext, "Customer payment completed from CRM", Toast.LENGTH_SHORT).show();
                            } else {
                                openZoomImageDialog(customer_paymentBitmap, session.getCustomerPaymentReceiptBitmap());
                            }
                        }
                    }
                } else {
                    Toast.makeText(mContext, getResources().getString(R.string.payment_info_null), Toast.LENGTH_SHORT).show();
                }

                break;


            case R.id.iv_after_delivery:
                if (AppHelper.isConnectingToInternet(mContext)) {
                    if (session.getBeforeDeliveryBitmap() != null && !session.getBeforeDeliveryBitmap().equals("")) {
                        if (afterBitmap == null && session.getAfterDeliveryBitmap().equals("")) {
                            imageUploadType = "iv_after_delivery";
                            if (Build.VERSION.SDK_INT >= 23) {
                                if (mContext.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                    requestPermissions(
                                            new String[]{Manifest.permission.CAMERA,
                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            Constant.MY_PERMISSIONS_REQUEST_CAMERA);
                                } else {
                                    ImagePicker.pickImageFromCamera(DeliveryDetailsFragment.this);
                                }
                            } else {
                                ImagePicker.pickImageFromCamera(DeliveryDetailsFragment.this);
                            }
                        } else {

                            openZoomImageDialog(afterBitmap, session.getAfterDeliveryBitmap());

                        }
                    } else {
                        Toast.makeText(mContext, getResources().getString(R.string.toast_enter_before_image), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.cv_skip:
                isClickedSkip = true;
                ly_customer_payment_mode.setVisibility(View.GONE);
                rl_customer_payment.setVisibility(View.GONE);
                tv_skip_mode.setTextColor(getResources().getColor(R.color.colorPrimary));
                iv_skip.setImageDrawable(getResources().getDrawable(R.drawable.active_skip));
                break;

            case R.id.iv_upload_receipt:
                if (receiptImage == null) {
                    imageUploadType = "iv_upload_receipt";
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (mContext.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(
                                    new String[]{Manifest.permission.CAMERA,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    Constant.MY_PERMISSIONS_REQUEST_CAMERA);
                        } else {
                            ImagePicker.pickImageFromCamera(DeliveryDetailsFragment.this);
                        }
                    } else {
                        ImagePicker.pickImageFromCamera(DeliveryDetailsFragment.this);
                    }
                } else {
                    openZoomImageDialog(receiptImage, session.getCustomerPaymentReceiptBitmap());
                }
                break;

            case R.id.btn_close_order:
                /*if (requestInfo.data.orderInfo.customerPaymentStatus.equals("0")) {
                    if (isClickedSkip) {
                        if (beforeBitmap == null && session.getBeforeDeliveryBitmap().equals("")) {
                            Toast.makeText(mContext, getResources().getString(R.string.before_upload_image_null), Toast.LENGTH_SHORT).show();
                            return;
                        } else if (afterBitmap == null && session.getAfterDeliveryBitmap().equals("")) {
                            Toast.makeText(mContext, getResources().getString(R.string.after_upload_image_null), Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            btn_close_order.setEnabled(false);
                            uploadTypeofImages(deliveryId, numberofshift, "deliveryReceipt");
                        }

                    } else {
                        if ((payAmount == null) || (payAmount.equals(""))) {
                            Toast.makeText(mContext, getResources().getString(R.string.payment_info_null), Toast.LENGTH_SHORT).show();
                        } else if (beforeBitmap == null && session.getBeforeDeliveryBitmap().equals("")) {
                            Toast.makeText(mContext, getResources().getString(R.string.before_upload_image_null), Toast.LENGTH_SHORT).show();
                            return;
                        } else if (afterBitmap == null && session.getAfterDeliveryBitmap().equals("")) {
                            Toast.makeText(mContext, getResources().getString(R.string.after_upload_image_null), Toast.LENGTH_SHORT).show();
                            return;
                        } else if (customer_paymentBitmap == null) {
                            Toast.makeText(mContext, getResources().getString(R.string.customer_payment_receipt_null), Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            btn_close_order.setEnabled(false);
                            customerPayment(requestInfo.data.order, payAmount, paymentType, ed_description.getText().toString());
                        }
                    }
                } else if (requestInfo.data.orderInfo.customerPaymentStatus.equals("1")) {
                    if (beforeBitmap == null && session.getBeforeDeliveryBitmap().equals("")) {
                        Toast.makeText(mContext, getResources().getString(R.string.before_upload_image_null), Toast.LENGTH_SHORT).show();
                        return;
                    } else if (afterBitmap == null && session.getAfterDeliveryBitmap().equals("")) {
                        Toast.makeText(mContext, getResources().getString(R.string.after_upload_image_null), Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        btn_close_order.setEnabled(false);
                        uploadTypeofImages(deliveryId, numberofshift, "deliveryReceipt");
                    }
                }*/


                if (requestInfo.data.orderInfo.customerPaymentStatus.equals("0")) {
                    if (isClickedSkip) {
                            btn_close_order.setEnabled(false);
                            uploadTypeofImages(deliveryId, numberofshift, "deliveryReceipt");
                    } else {
                        if ((payAmount == null) || (payAmount.equals(""))) {
                            Toast.makeText(mContext, getResources().getString(R.string.payment_info_null), Toast.LENGTH_SHORT).show();
                        }  else if (customer_paymentBitmap == null) {
                            Toast.makeText(mContext, getResources().getString(R.string.customer_payment_receipt_null), Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            btn_close_order.setEnabled(false);
                            customerPayment(requestInfo.data.order, payAmount, paymentType, ed_description.getText().toString());
                        }
                    }
                } else if (requestInfo.data.orderInfo.customerPaymentStatus.equals("1")) {
                        btn_close_order.setEnabled(false);
                        uploadTypeofImages(deliveryId, numberofshift, "deliveryReceipt");
                }

                break;

            case R.id.after_receipt_cancel:
                iv_after_delivery.setImageDrawable(getResources().getDrawable(R.drawable.upload));
                afterBitmap = null;
                after_receipt_cancel.setVisibility(View.GONE);
                session.setAfterDeliveryBitmap("");

                break;

            case R.id.before_receipt_cancel:
                iv_before_delivery.setImageDrawable(getResources().getDrawable(R.drawable.upload));
                beforeBitmap = null;
                before_receipt_cancel.setVisibility(View.GONE);

                iv_after_delivery.setImageDrawable(getResources().getDrawable(R.drawable.upload));
                afterBitmap = null;
                after_receipt_cancel.setVisibility(View.GONE);
                session.setBeforeDeliveryBitmap("");

                break;

            case R.id.iv_upload_receipt_cancel:
                iv_upload_receipt.setImageDrawable(getResources().getDrawable(R.drawable.upload));
                receiptImage = null;
                iv_upload_receipt_cancel.setVisibility(View.GONE);
                session.setWaiverReceiptBitmap("");

                break;

            case R.id.iv_customer_payment_cancel:
                iv_customer_payment_receipt.setImageDrawable(getResources().getDrawable(R.drawable.upload));
                customer_paymentBitmap = null;
                iv_customer_payment_cancel.setVisibility(View.GONE);
                session.setCustomerPaymentReceiptBitmap("");
                btn_close_order.setEnabled(true);
                break;
        }
    }

    // Dialog to open delivery receipts in zoom view
    private void openZoomImageDialog(Bitmap bitmap, String loadingImage) {
        zoomImageDialog = new Dialog(mContext);
        zoomImageDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        zoomImageDialog.setContentView(R.layout.dialog_zoom_image);

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(zoomImageDialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.FILL_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        zoomImageDialog.getWindow().setAttributes(lWindowParams);

        ImageView photo_view = zoomImageDialog.findViewById(R.id.photo_view);
        ImageView cancel_dialog_icon = zoomImageDialog.findViewById(R.id.cancel_dialog_icon);

        if (bitmap != null) {
            photo_view.setImageBitmap(bitmap);
        } else if (!loadingImage.equals("")) {
            Glide.with(mContext).load(loadingImage).into(photo_view);
        }

        cancel_dialog_icon.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {
                zoomImageDialog.dismiss();
            }
        });

        zoomImageDialog.getWindow().

                setGravity(Gravity.CENTER);
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

        if (!session.getDeliveryChequeNumber().equals("")) {
            ed_cheque_number.setText(session.getDeliveryChequeNumber());
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
                    session.setDeliveryChequeNumber(chequeNumber);
                    chequeDialog.dismiss();
                    openFullMinimumPaymentDialog(R.id.cv_cheque);
                }
            }
        });

        chequeDialog.getWindow().setGravity(Gravity.CENTER);
        chequeDialog.show();
    }

    // Dialog to select payment type - Full or partial and enter amount for customer payment
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
        rg_payment_type = fullMinPaymentDialog.findViewById(R.id.rg_payment_type);
        rb_full_pay = rg_payment_type.findViewById(R.id.rb_full_pay);
        rb_partial_pay = rg_payment_type.findViewById(R.id.rb_partial_pay);
        tv_total_amount = fullMinPaymentDialog.findViewById(R.id.tv_total_amount);
        tv_minimum_payment = fullMinPaymentDialog.findViewById(R.id.tv_minimum_payment);

        tv_total_amount.setText("$ " + String.valueOf(requestInfo.data.orderInfo.customerPay.totalAmount));

        tv_minimum_payment.setText("$ " + String.valueOf(requestInfo.data.orderInfo.customerPay.dueAmount));

        ed_total_amount.setText(String.valueOf(requestInfo.data.orderInfo.customerPay.dueAmount));

        amt = requestInfo.data.orderInfo.customerPay.totalAmount;

        if (requestInfo.data.totalShift.equals("1")) {
            ed_total_amount.setEnabled(false);
            rb_full_pay.setChecked(true);
            rb_partial_pay.setClickable(false);

        } else if (numberofshift == totalShift) {
            ed_total_amount.setText(String.valueOf(requestInfo.data.orderInfo.customerPay.dueAmount));
            rb_partial_pay.setClickable(false);
            ed_total_amount.setEnabled(false);
        } else {
            if (rb_full_pay.isChecked()) {
                ed_total_amount.setEnabled(false);
            } else if (rb_partial_pay.isChecked()) {
                ed_total_amount.setEnabled(true);
                inputFilter(ed_total_amount);
                amt = requestInfo.data.orderInfo.customerPay.totalAmount;
                rb_partial_pay.setClickable(true);
            }
        }

        rg_payment_type.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.rb_full_pay) {
                    ed_total_amount.setEnabled(false);
                    ed_total_amount.setText(String.valueOf(requestInfo.data.orderInfo.customerPay.dueAmount));
                } else if (i == R.id.rb_partial_pay) {
                    ed_total_amount.setEnabled(true);
                    inputFilter(ed_total_amount);
                    amt = requestInfo.data.orderInfo.customerPay.totalAmount;
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
                String customerPaymentAmount = ed_total_amount.getText().toString().trim();

                if (customerPaymentAmount.equals("")) {
                    Toast.makeText(mContext, getResources().getString(R.string.amount_null), Toast.LENGTH_SHORT).show();
                } else if (!isValidAmount(ed_total_amount)) {
                    Toast.makeText(mContext, getResources().getString(R.string.amount_invalid), Toast.LENGTH_SHORT).show();
                } else if (numberofshift != Integer.parseInt(requestInfo.data.totalShift)) {
                    if (customerPaymentAmount != null) {
                        if (Double.parseDouble(customerPaymentAmount) > amt) {
                            Toast.makeText(mContext, getResources().getString(R.string.amount_invalid), Toast.LENGTH_SHORT).show();
                        } else if (Integer.parseInt(requestInfo.data.totalShift) > 1) {

                            if (rb_full_pay.isChecked()) {
                                paymentType = "FULL";
                            } else if (rb_partial_pay.isChecked()) {
                                paymentType = "PARTIAL";
                            }

                            payAmount = customerPaymentAmount;

                            fullMinPaymentDialog.dismiss();

                            if (cv_cash == R.id.cv_cash) {
                                paymentMode = "CASH";
                            } else if (cv_cash == R.id.cv_credit_card) {
                                paymentMode = "CREDITCARD";
                            } else if (cv_cash == R.id.cv_cheque) {
                                paymentMode = "CHEQUE";
                            }

                            session.setPaymentMode(paymentMode);

                            iv_customer_payment_receipt.setEnabled(true);

                            iv_customer_payment_receipt.setImageDrawable(getResources().getDrawable(R.drawable.upload));
                            customer_paymentBitmap = null;
                            iv_customer_payment_cancel.setVisibility(View.GONE);

                            isPaymentSelected = true;

                            if (paymentMode.equals("CASH")) {
                                setDeliveryCashModeActive();

                                if (ed_cheque_number != null) {
                                    ed_cheque_number.setText("");
                                    session.setDeliveryChequeNumber("");
                                }

                            } else if (paymentMode.equals("CREDITCARD")) {
                                setDeliveryCreditModeActive();

                                if (ed_cheque_number != null) {
                                    ed_cheque_number.setText("");
                                    session.setDeliveryChequeNumber("");
                                }


                            } else if (paymentMode.equals("CHEQUE")) {
                                setDeliveryChequeModeActive();

                            }
                        }
                    }
                } else {
                    payAmount = ed_total_amount.getText().toString().trim();

                    fullMinPaymentDialog.dismiss();

                    if (cv_cash == R.id.cv_cash) {
                        paymentMode = "CASH";
                    } else if (cv_cash == R.id.cv_credit_card) {
                        paymentMode = "CREDITCARD";
                    } else if (cv_cash == R.id.cv_cheque) {
                        paymentMode = "CHEQUE";
                    }

                    if (rb_full_pay.isChecked()) {
                        paymentType = "FULL";
                    } else if (rb_partial_pay.isChecked()) {
                        paymentType = "PARTIAL";
                    }

                    session.setPaymentMode(paymentMode);

                    isPaymentSelected = true;

                    iv_customer_payment_receipt.setEnabled(true);
                    iv_customer_payment_receipt.setImageDrawable(getResources().getDrawable(R.drawable.upload));
                    customer_paymentBitmap = null;
                    iv_customer_payment_cancel.setVisibility(View.GONE);

                    if (paymentMode.equals("CASH")) {
                        setDeliveryCashModeActive();

                        if (ed_cheque_number != null) {
                            ed_cheque_number.setText("");
                            session.setDeliveryChequeNumber("");
                        }

                    } else if (paymentMode.equals("CREDITCARD")) {
                        setDeliveryCreditModeActive();

                        if (ed_cheque_number != null) {
                            ed_cheque_number.setText("");
                            session.setDeliveryChequeNumber("");
                        }


                    } else if (paymentMode.equals("CHEQUE")) {
                        setDeliveryChequeModeActive();

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

    // Set Cash Customer Payment Mode active in case of payment mode completed
    private void setDeliveryCashModeActive() {
        iv_cash.setImageDrawable(getResources().getDrawable(R.drawable.active_cash));
        iv_credit_card.setImageDrawable(getResources().getDrawable(R.drawable.card_icon));
        iv_cheque.setImageDrawable(getResources().getDrawable(R.drawable.cheque_icon));

        tv_cash_mode.setTextColor(getResources().getColor(R.color.colorPrimary));
        tv_credit_mode.setTextColor(getResources().getColor(R.color.black));
        tv_cheque_mode.setTextColor(getResources().getColor(R.color.black));
    }

    // Set Credit Customer Payment Mode active in case of payment mode completed
    private void setDeliveryCreditModeActive() {
        iv_cash.setImageDrawable(getResources().getDrawable(R.drawable.cash_icon));
        iv_credit_card.setImageDrawable(getResources().getDrawable(R.drawable.active_credit_card));
        iv_cheque.setImageDrawable(getResources().getDrawable(R.drawable.cheque_icon));

        tv_cash_mode.setTextColor(getResources().getColor(R.color.black));
        tv_credit_mode.setTextColor(getResources().getColor(R.color.colorPrimary));
        tv_cheque_mode.setTextColor(getResources().getColor(R.color.black));
    }

    // Set Cheque Customer Payment Mode active in case of payment mode completed
    private void setDeliveryChequeModeActive() {
        iv_cash.setImageDrawable(getResources().getDrawable(R.drawable.cash_icon));
        iv_credit_card.setImageDrawable(getResources().getDrawable(R.drawable.card_icon));
        iv_cheque.setImageDrawable(getResources().getDrawable(R.drawable.active_check));

        tv_cash_mode.setTextColor(getResources().getColor(R.color.black));
        tv_credit_mode.setTextColor(getResources().getColor(R.color.black));
        tv_cheque_mode.setTextColor(getResources().getColor(R.color.colorPrimary));
    }

    // Api call to set ON Route or OFF Route
    private void startStopRoute() {
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
                            if (session.getRequestInfo() != null) {
                                session.getRequestInfo().data.driverInfo.onRoute = "1";
                                RequestInfo requestInfo = session.getRequestInfo();
                                requestInfo.data.driverInfo.onRoute = "1";
                                session.createRequest(requestInfo);
                            }

                            if (Build.VERSION.SDK_INT >= 23) {
                                if (!Settings.canDrawOverlays(mContext)) {
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:" + mContext.getPackageName()));
                                    startActivityForResult(intent, 1234);
                                }
                            }
                            startStopRoute();

                        } else if (route.equals("OFF ROUTE")) {
                            if (session.getRequestInfo() != null) {
                                session.getRequestInfo().data.driverInfo.onRoute = "0";
                                RequestInfo requestInfo = session.getRequestInfo();
                                requestInfo.data.driverInfo.onRoute = "0";
                                session.createRequest(requestInfo);
                            }

                        }

                    } else {
                        Toast.makeText(mContext, massage, Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
            }

        });
        api.callApi("user/route", Request.Method.GET, null, true);
    }
}
