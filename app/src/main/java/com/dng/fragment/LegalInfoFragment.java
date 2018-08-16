package com.dng.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
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
import com.dng.helper.AppHelper;
import com.dng.helper.Constant;
import com.dng.helper.ErrorDialog;
import com.dng.helper.Validation;
import com.dng.model.User;
import com.dng.server_task.WebService;
import com.dng.session.Session;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.dng.helper.AppHelper.getEditTextFilterEmoji;

/**
 * Created by mindiii on 9/4/18.
 */

public class LegalInfoFragment extends Fragment implements View.OnClickListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private Context mContext;

    private EditText ed_legal_name, ed_legal_relation, ed_legal_contact;
    private TextView btn_legal_update;
    private ImageView iv_back;
    private ProgressBar progress;
    private Session session;
    private User userInfo;

    public LegalInfoFragment() {
    }

    public static ProfileTerminalFragment newInstance(String param1, String param2) {
        ProfileTerminalFragment fragment = new ProfileTerminalFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_legal_info, container, false);
        session = new Session(mContext);
        userInfo = session.getUser();
        init(view);

        iv_back.setOnClickListener(this);
        btn_legal_update.setOnClickListener(this);
        return view;
    }

    private void init(View view) {
        ed_legal_name = view.findViewById(R.id.ed_legal_name);
        ed_legal_relation = view.findViewById(R.id.ed_legal_relation);
        ed_legal_contact = view.findViewById(R.id.ed_legal_contact);
        progress = view.findViewById(R.id.progress);

        btn_legal_update = view.findViewById(R.id.btn_legal_update);
        iv_back = view.findViewById(R.id.iv_back);

        stopEmojisAndCopyPast(ed_legal_name);
        stopEmojisAndCopyPast(ed_legal_relation);
        stopEmojisAndCopyPast(ed_legal_contact);

        // set data
        if (userInfo.data != null) {
            ed_legal_name.setText(userInfo.data.driverDetail.emergencyName);
            ed_legal_relation.setText(userInfo.data.driverDetail.emergencyRelationship);
            ed_legal_contact.setText(userInfo.data.driverDetail.emergencyPhoneNumber);
        }
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

            case R.id.btn_legal_update:
                Constant.hideSoftKeyboard(getActivity());
                if (isInfoValid()) {
                    String name = ed_legal_name.getText().toString().trim();
                    String relation = ed_legal_relation.getText().toString().trim();
                    String contact = ed_legal_contact.getText().toString().trim();
                    updatePersonalInfo(name, relation, contact);
                }
                break;
        }
    }

    private void updatePersonalInfo(String name, String relation, String contact) {
        if (AppHelper.isConnectingToInternet(mContext)) {
            progress.setVisibility(View.VISIBLE);
            Map<String, String> map = new HashMap<>();
            map.put("emergencyName", name);
            map.put("emergencyRelationship", relation);
            map.put("emergencyPhoneNumber", contact);

            WebService api = new WebService(mContext, "fds", new WebService.ResponceListner() {
                @Override
                public void onResponse(final String response, String apiName) {
                    progress.setVisibility(View.GONE);
                    try {
                        final JSONObject jsonObject = new JSONObject(response);

                        Log.e("LEGAL INFO RESPONSE", response.toString());
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");


                        if (status.equals("success")) {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setTitle("Alert");
                            builder.setCancelable(false);
                            builder.setMessage(getResources().getString(R.string.legal_info_update_success));
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

        if (!v.isNullValue(ed_legal_name)) {
            Toast.makeText(mContext, getResources().getString(R.string.legal_name_null), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!v.isNullValue(ed_legal_relation)) {
            Toast.makeText(mContext, getResources().getString(R.string.legal_relation_null), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!v.isNullValue(ed_legal_contact)) {
            Toast.makeText(mContext, getResources().getString(R.string.legal_contact_null), Toast.LENGTH_SHORT).show();
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
}
