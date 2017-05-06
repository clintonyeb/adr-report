package com.clinton.adrreport;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;

import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

import net.gotev.uploadservice.Logger;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.okhttp.OkHttpStack;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

import static com.clinton.adrreport.utils.Helpers.SETTINGS_FILE;


public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // For FileExposure error
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        //DBFlow initialize
        FlowManager.init(new FlowConfig.Builder(this).build());

        //Upload service initializer
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
        Logger.setLogLevel(Logger.LogLevel.DEBUG);
        UploadService.UPLOAD_POOL_SIZE = 1;
        UploadService.HTTP_STACK = new OkHttpStack(getOkHttpClient());
    }

    private OkHttpClient getOkHttpClient() {

        SharedPreferences preferences = this.getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE);
        final String access_token = preferences.getString("access_token", null);

        return new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .cache(null)
                .build();
    }
}
