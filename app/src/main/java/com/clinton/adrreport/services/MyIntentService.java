package com.clinton.adrreport.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.clinton.adrreport.R;
import com.clinton.adrreport.dbflow.PatientReport;
import com.clinton.adrreport.dbflow.PatientReport_Table;
import com.clinton.adrreport.utils.Helpers;
import com.clinton.adrreport.utils.HttpStatus;
import com.google.gson.Gson;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.clinton.adrreport.utils.Helpers.BASE_URL;
import static com.clinton.adrreport.utils.Helpers.SETTINGS_FILE;

public class MyIntentService extends IntentService {

    String mToken = null;
    String mUsername = null;
    String mFullName= null;


    private static final String REPORT_OBJECT = "com.clinton.adrreport.funcs.report.object";

    public MyIntentService() {
        super("MyIntentService");
    }

    public static void createService(Context context, PatientReport report) {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.putExtra(REPORT_OBJECT, report);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && setHeaders()) {
            PatientReport report = intent.getParcelableExtra(REPORT_OBJECT);
            report.save();
            sendReportsFromDatabase();
        }
    }

    private boolean setHeaders() {
        SharedPreferences preferences =  this.getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE);
        mToken = preferences.getString("token", null);
        mUsername = preferences.getString("username", null);
        mFullName= preferences.getString("fullName", null);
        return mToken != null;
    }

    public static List<PatientReport> getPatientReports(){
        return SQLite.select()
                .from(PatientReport.class)
                .orderBy(PatientReport_Table.id, false)
                .limit(20)
                .queryList();

    }



    private void sendReportsFromDatabase() {
        List<PatientReport> reports = getPatientReports();

        for (PatientReport report : reports) {

            if (report.getServerId() != null) {
                sendReport(report);
            }
            else {
                getServerId(report);
            }
        }
    }

    private void getServerId(final PatientReport report) {

        OkHttpClient client = new OkHttpClient();

        RequestBody data = new FormBody.Builder()

                .add("patientName", report.getPatientName())
                .add("patientId", report.getPatientId() != null ? report.getPatientId() : "")
                .add("userId", mUsername)
                .add("userFullName", mFullName)
                .build();


        Request request = new Request.Builder()
                .url(BASE_URL + "reports")
                .addHeader("Authorization", mToken)
                .post(data)
                .build();

        Call call = client.newCall(request);

        try {
            Response response = call.execute();

            ReportToReceive res = parseResponse(response);

            if (res != null) {
                report.setServerId(res.reportId);
                Log.i("SERVICE", res.reportId);
                report.save();
                sendReport(report);
            }else {
                Log.e("SERVICE", "response null");
            }

        }
        catch (IOException e) {
            System.out.println(Helpers.exceptionMessage(e));
        }
    }

    private  class ReportToReceive {
        String reportId;
        String created;
        boolean valid;
    }

    private ReportToReceive parseResponse(Response response) throws IOException {
        String data = response.body().string();
        Log.i("SERVICE", data);
        if (response.code() == HttpStatus.OK){
            return new Gson().fromJson(data, ReportToReceive.class);
        }else if (response.code() == HttpStatus.UNAUTHORIZED || response.code() == HttpStatus.BAD_REQUEST) {
            Log.e("SERVICE", String.valueOf(response.code()));
            SharedPreferences preferences = getApplicationContext().getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove("token");
            editor.remove("username");
            editor.apply();
        }
        return null;
    }

    private void sendReport(PatientReport report) {

        boolean hasImages = false;

        if (report.getImageFile1() != null) {
            sendImagesToServer(report.getImageFile1(), report, 1);
            hasImages = true;
        }
        if (report.getImageFile2() != null) {
            sendImagesToServer(report.getImageFile2(), report, 2);
            hasImages = true;
        }
        if (report.getImageFile3() != null) {
            sendImagesToServer(report.getImageFile3(), report, 3);
            hasImages = true;
        }
        if (report.getImageFile4() != null) {
            sendImagesToServer(report.getImageFile4(), report, 4);
            hasImages = true;
        }

        if(!hasImages) {
            report.delete();
        }
    }


    private void sendImagesToServer(final String imagePath, final PatientReport report, final int id) {

        System.out.println("sending " + imagePath);

        try {
            //String uploadId =
                    new MultipartUploadRequest(getApplicationContext(), BASE_URL + "images")
                            .addFileToUpload(imagePath, "file")
                            .addHeader("Authorization", mToken)
                            .addParameter("username", mUsername)
                            .addParameter("reportId", report.getServerId())
                            .setAutoDeleteFilesAfterSuccessfulUpload(true)
                            .setUsesFixedLengthStreamingMode(true)
                            .setNotificationConfig(getNotificationConfig())
                            .setMaxRetries(3)
                            .setDelegate(new UploadStatusDelegate() {
                                @Override
                                public void onProgress(Context context, UploadInfo uploadInfo) {

                                }

                                @Override
                                public void onError(Context context, UploadInfo uploadInfo, Exception exception) {
                                    Log.e("SERVICE", "Images upload error", exception);
                                }

                                @Override
                                public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {

                                    System.out.println("sent image code " + serverResponse.getHttpCode());
                                    if (serverResponse.getHttpCode() != 200) return;

                                    switch (id) {
                                        case 1:
                                            report.setImageFile1(null);
                                            break;
                                        case 2:
                                            report.setImageFile2(null);
                                            break;
                                        case 3:
                                            report.setImageFile3(null);
                                            break;
                                        case 4:
                                            report.setImageFile4(null);
                                            break;
                                    }

                                     report.save();

                                }

                                @Override
                                public void onCancelled(Context context, UploadInfo uploadInfo) {

                                }
                            })
                            .startUpload();
        } catch (Exception exc) {
            Log.e("AndroidUploadService", exc.getMessage(), exc);
        }
    }

    private UploadNotificationConfig getNotificationConfig() {
        return new UploadNotificationConfig()
                .setIcon(R.mipmap.ic_main)
                .setCompletedIcon(R.mipmap.ic_main)
                .setErrorIcon(R.mipmap.ic_main)
                .setAutoClearOnSuccess(true)
                .setTitle("ADR Report")
                //.setInProgressMessage(getString(R.string.uploading))
                //.setCompletedMessage(getString(R.string.upload_success))
                //.setAutoClearOnCancel(true)
                //.setAutoClearOnSuccess(true)
                //.setClickIntent(new Intent(this, MainActivity.class))
                .setClearOnAction(true);
    }
}
