package com.nareshit.controlhome;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private Button adminBtn;
    private Button userBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        adminBtn = (Button) findViewById(R.id.adminBtn);
        userBtn = (Button) findViewById(R.id.userBtn);

        adminBtn.setOnClickListener(this);
        userBtn.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        if (view == adminBtn) {
            startActivity(new Intent(this, AdminLoginActivity.class));
        } else if (view == userBtn) {
            //
        }
    }
}
