package com.dng.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.dng.R;
import com.dng.activity.MainActivity;
import com.dng.activity.RegisterActivity;
import com.dng.activity.util.UsPhoneNumberFormatter;
import com.dng.helper.AppHelper;
import com.dng.helper.Constant;
import com.dng.helper.ErrorDialog;
import com.dng.helper.Validation;
import com.dng.model.User;
import com.dng.server_task.WebService;
import com.dng.session.Session;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import static com.dng.helper.AppHelper.getEditTextFilterEmoji;

public class PersonalInfoFragment extends Fragment implements View.OnClickListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private EditText ed_update_username, ed_update_contact, ed_update_address;
    private TextView btn_update;
    private ImageView iv_back;
    private Context mContext;
    private ProgressBar progress;
    private Session session;
    private User userInfo;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    public PersonalInfoFragment() {

    }

    public static PersonalInfoFragment newInstance(String param1, String param2) {
        PersonalInfoFragment fragment = new PersonalInfoFragment();
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
        View view = inflater.inflate(R.layout.fragment_personal_info, container, false);
        session = new Session(mContext);
        userInfo = session.getUser();
        init(view);

        iv_back.setOnClickListener(this);
        btn_update.setOnClickListener(this);
        ed_update_address.setOnClickListener(this);
        return view;
    }

    private void init(View view) {
        ed_update_username = view.findViewById(R.id.ed_update_username);
        ed_update_contact = view.findViewById(R.id.ed_update_contact);

        ed_update_address = view.findViewById(R.id.ed_update_address);

        iv_back = view.findViewById(R.id.iv_back);
        progress = view.findViewById(R.id.progress);
        btn_update = view.findViewById(R.id.btn_update);


        stopEmojisAndCopyPast(ed_update_username);
        stopEmojisAndCopyPast(ed_update_contact);
        stopEmojisAndCopyPast(ed_update_address);

        // set data
        if (userInfo.data != null) {
            ed_update_username.setText(userInfo.data.fullName);
            ed_update_contact.setText(userInfo.data.contact+"");
            ed_update_address.setText(userInfo.data.address);
        }

        ed_update_contact.addTextChangedListener(new UsPhoneNumberFormatter(new WeakReference<>(ed_update_contact)));

    }

    private void stopEmojisAndCopyPast(EditText editText){
        InputFilter[] filterArray = new InputFilter[] {
                getEditTextFilterEmoji()
        };
        editText.setFilters(filterArray);

        editText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //  Do Something or Don't
                return true;
            }
        });
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                replaceFragment(new ProfileTerminalFragment(), false, R.id.fragment_place);
                break;

            case R.id.ed_update_address:
                try {
                    ed_update_address.setEnabled(false);
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(getActivity());
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
                break;

            case R.id.btn_update:
                Constant.hideSoftKeyboard(getActivity());
                if (isInfoValid()) {
                    String name = ed_update_username.getText().toString().trim();
                    String contact = ed_update_contact.getText().toString().trim();
                    String address = ed_update_address.getText().toString().trim();
                    updatePersonalInfo(name, contact, address);
                }
                break;
        }
    }

    private void updatePersonalInfo(String name, String contact, String address) {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.setVisibility(View.VISIBLE);
            Map<String, String> map = new HashMap<>();
            map.put("fullName", name);
            map.put("contact", contact);
            map.put("address", address);

            WebService api = new WebService(mContext, "fds", new WebService.ResponceListner() {
                @Override
                public void onResponse(final String response, String apiName) {
                    progress.setVisibility(View.GONE);
                    try {
                        final JSONObject jsonObject = new JSONObject(response);

                        Log.e("PERSONAL INFO RESPONSE", response.toString());
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");


                        if (status.equals("success")) {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setTitle("Alert");
                            builder.setCancelable(false);
                            builder.setMessage(getResources().getString(R.string.personal_profile_update_success));
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();

                                    Gson gson = new Gson();
                                    User user = gson.fromJson(String.valueOf(jsonObject), User.class);
                                    session.createSession(user);

                                    replaceFragment(new ProfileTerminalFragment(), false, R.id.fragment_place);
                                }
                            });
                            AlertDialog alert = builder.create();
                            alert.show();

                        } else {
                            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    progress.setVisibility(View.GONE);
                    ErrorDialog.showSessionError(error, mContext);
                }
            });
            api.callApi("user/updateInfo", Request.Method.POST, map);
        } else {
            Toast.makeText(mContext, getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isInfoValid() {
        Validation v = new Validation();

        if (!v.isNullValue(ed_update_username)) {
            Toast.makeText(mContext, getResources().getString(R.string.update_username_null), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!v.isNullValue(ed_update_contact)) {
            Toast.makeText(mContext, getResources().getString(R.string.update_contact_null), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!v.isNullValue(ed_update_address)) {
            Toast.makeText(mContext, getResources().getString(R.string.update_address_null), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            ed_update_address.setEnabled(true);
            if (resultCode == -1) {
                Place place = PlaceAutocomplete.getPlace(mContext, data);
                ed_update_address.setText(place.getAddress().toString());


            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(mContext, data);
                // TODO: Handle the error.
            }
        }
    }

}
