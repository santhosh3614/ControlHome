package com.nareshit.controlhome.utils;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

/**
 * Created by Santhosh on 3/2/2017.
 */

public class GoogleClient {

    private static GoogleApiClient mGoogleApiClient;

    public static GoogleApiClient getInstance(Context context) {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .build();
        }
        return mGoogleApiClient;
    }

}
