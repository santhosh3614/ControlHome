package com.nareshit.controlhome;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AdminLoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button adminLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        findViewById(R.id.adminPwdET);
        findViewById(R.id.adminPwdET);

        adminLoginButton =(Button)findViewById(R.id.adminLoginBtn);
        adminLoginButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view==adminLoginButton){
            
        }
    }

}
