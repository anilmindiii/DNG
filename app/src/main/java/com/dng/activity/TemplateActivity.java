package com.dng.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.dng.R;
import com.dng.model.User;
import com.dng.session.Session;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;

public class TemplateActivity extends AppCompatActivity {
    private TextView tv_templete;
    private Session session;
    private TextView btn_signout;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template);
        tv_templete = findViewById(R.id.tv_templete);
        btn_signout = findViewById(R.id.btn_signout);
        session = new Session(this);
        user = new User();
        //String token = FirebaseInstanceId.getInstance().getToken();
        //  Log.d("token",token);


        user = session.getUser();

        if (user.data.approval.equals("1")) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();

        } else if (user.data.isVerify != null) {

            if (user.data.isVerify.equals("0")) {

                tv_templete.setText(R.string.non_verify);

            } else if (user.data.isVerify.equals("1")) {
                tv_templete.setText(R.string.not_approved);

            }
        }

        btn_signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                session.logout();
            }
        });
    }
}
