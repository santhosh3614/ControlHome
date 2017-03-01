package com.nareshit.controlhome;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AdminLoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button adminLoginButton;
    private EditText adminEditText;
    private EditText pwdEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);
        adminEditText = (EditText) findViewById(R.id.user_id);
        pwdEditText = (EditText) findViewById(R.id.password);

        adminLoginButton = (Button) findViewById(R.id.adminLoginBtn);
        adminLoginButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == adminLoginButton) {
            String adminId = adminEditText.getText().toString();
            String pwd = pwdEditText.getText().toString();
            if (adminId.equals("admin") && pwd.equals("abc123")) {
                startActivity(new Intent(this, AddDeviceActivity.class));
            } else {
                Toast.makeText(this, getString(R.string.invalid_login_credentials), Toast.LENGTH_LONG).show();
            }
        }
    }

}
