package com.nareshit.controlhome;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.nareshit.controlhome.utils.GoogleClient;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = HomeActivity.class.getSimpleName();
    private static final int RESOLVE_CONNECTION_REQUEST_CODE = 1;
    private Button adminBtn;
    private Button userBtn;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mGoogleApiClient = GoogleClient.getInstance(this);
        adminBtn = (Button) findViewById(R.id.adminBtn);
        userBtn = (Button) findViewById(R.id.userBtn);

        adminBtn.setOnClickListener(this);
        userBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == adminBtn) {
            Intent intent = new Intent(this, AdminLoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        } else if (view == userBtn) {
            mGoogleApiClient.registerConnectionCallbacks(this);
            mGoogleApiClient.registerConnectionFailedListener(this);
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.unregisterConnectionCallbacks(this);
                mGoogleApiClient.unregisterConnectionFailedListener(this);
                startActivity(new Intent(this, MainActivity.class));
            } else if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected");
        mGoogleApiClient.unregisterConnectionCallbacks(this);
        mGoogleApiClient.unregisterConnectionFailedListener(this);
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended......i" + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed......connectionResult.hasResolution()" + connectionResult.toString());
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, RESOLVE_CONNECTION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                Log.d(TAG, "onConnectionFailed......SendIntentException" + e.getMessage());
                // Unable to resolve, message user appropriately
                e.printStackTrace();
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult......Request" + requestCode + " ResultCode:" + resultCode + " data:" + data);
        switch (requestCode) {
            case RESOLVE_CONNECTION_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    GoogleApiClient mGoogleApiClient = GoogleClient.getInstance(this);
                    mGoogleApiClient.connect();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }
}
