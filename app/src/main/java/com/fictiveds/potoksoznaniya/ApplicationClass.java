package com.fictiveds.potoksoznaniya;
import android.app.Application;

import androidx.annotation.Nullable;
import com.appodeal.ads.initializing.ApdInitializationCallback;
import com.appodeal.ads.initializing.ApdInitializationError;
import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;
import com.onesignal.Continue;
import com.appodeal.ads.Appodeal;

import java.util.List;

public class ApplicationClass extends Application {

    // NOTE: Replace the below with your own ONESIGNAL_APP_ID
    private static final String ONESIGNAL_APP_ID = "dc6b42df-c507-4def-a3a8-112f40a46b4a";
    private static final String APPODEAL_APP_KEY = "3cbff62dec44ef74de8873dce7c3b595b0d59719975826fd";

    @Override
    public void onCreate() {
        super.onCreate();

      //  Appodeal.initialize(this, APPODEAL_APP_KEY, Appodeal.INTERSTITIAL | Appodeal.BANNER);
        Appodeal.initialize(this, APPODEAL_APP_KEY, Appodeal.INTERSTITIAL | Appodeal.BANNER, new ApdInitializationCallback() {
            @Override
            public void onInitializationFinished(@Nullable List<ApdInitializationError> errors) {
                if (errors == null || errors.isEmpty()) {
                    // Appodeal initialization finished successfully
                } else {
                    // Handle initialization errors
                    for (ApdInitializationError error : errors) {
                        // Log or handle each error
                    }
                }
            }
        });


        // Verbose Logging set to help debug issues, remove before releasing your app.
        OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);

        // OneSignal Initialization
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID);

        // requestPermission will show the native Android notification permission prompt.
        // NOTE: It's recommended to use a OneSignal In-App Message to prompt instead.
        OneSignal.getNotifications().requestPermission(true, Continue.with(r -> {
            if (r.isSuccess()) {
                if (r.getData()) {
                    // `requestPermission` completed successfully and the user has accepted permission
                }
                else {
                    // `requestPermission` completed successfully but the user has rejected permission
                }
            }
            else {
                // `requestPermission` completed unsuccessfully, check `r.getThrowable()` for more info on the failure reason
            }
        }));
    }
}
