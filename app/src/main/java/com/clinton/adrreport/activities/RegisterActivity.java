package com.clinton.adrreport.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import com.clinton.adrreport.R;
import com.clinton.adrreport.utils.Helpers;
import com.clinton.adrreport.utils.Validators;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.clinton.adrreport.utils.Helpers.BASE_URL;

public class RegisterActivity extends AppCompatActivity {

    // UI references.
    @BindView(R.id.progressView)
    View mProgressView;
    @BindView(R.id.fullName)
    TextInputEditText mFullNameView;
    @BindView(R.id.email)
    TextInputEditText mEmailView;
    @BindView(R.id.phoneView)
    TextInputEditText mPhoneView;
    @BindView(R.id.designation)
    TextInputEditText mDesignationView;
    @BindView(R.id.org_name)
    TextInputEditText mOrgNameView;
    @BindView(R.id.email_login_form)
    View mLoginFormView;
    @BindView(R.id.register_button)
    Button mRegisterButtonView;
    @BindView(R.id.messageView)
    TextView mMessageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        setupActionBar();

        mOrgNameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }


    @OnClick(R.id.register_button)
    void attemptRegister() {

        removeErrors();

        // Store values at the time of the login attempt.
        String fullName = mFullNameView.getText().toString();
        String email = mEmailView.getText().toString();
        String phone = mPhoneView.getText().toString();
        String orgName = mOrgNameView.getText().toString();
        String des = mDesignationView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(orgName)) {
            mOrgNameView.setError("This is required");
            focusView = mOrgNameView;
            cancel = true;
        }

        if (TextUtils.isEmpty(phone)) {
            mPhoneView.setError("Phone number required");
            focusView = mPhoneView;
            cancel = true;
        } else if (!Validators.isValidPhoneNumber(phone)) {
            mPhoneView.setError("Phone number invalid");
            focusView = mPhoneView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!Validators.isValidEmail(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }


        if (TextUtils.isEmpty(fullName)) {
            mFullNameView.setError("Name is required");
            focusView = mFullNameView;
            cancel = true;
        } else if (!Validators.isNotEmpty(fullName)) {
            mFullNameView.setError("Invalid Name");
            focusView = mFullNameView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            sendToServer(fullName, email, phone, orgName, des);
        }
    }

    private void removeErrors() {
        mEmailView.setError(null);
        mFullNameView.setError(null);
        mPhoneView.setError(null);
        mOrgNameView.setError(null);
        mDesignationView.setError(null);
        mMessageView.setVisibility(View.GONE);
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void sendToServer(String fullName, String email, String phone, String orgName, String des) {

        OkHttpClient client = new OkHttpClient();

        RequestBody data = new FormBody.Builder()
                .add("fullName", fullName)
                .add("username", email)
                .add("mobile", phone)
                .add("organization", orgName)
                .add("designation", des)
                .add("enabled", "false")
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL + "users")
                .post(data)
                .build();

        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                RegisterActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showProgress(false);
                        mMessageView.setVisibility(View.VISIBLE);
                        mMessageView.setText(Helpers.exceptionMessage(e));
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                if(preResponse(RegisterActivity.this, response)) {
                    LoginActivity.mMessage = "Account registration success, we will email you after verification";
                    finish();
                }
            }
        });


    }

    private boolean preResponse(Activity context, final Response response){
        if (response.code() != 200){
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showProgress(false);
                    mMessageView.setVisibility(View.VISIBLE);
                    mMessageView.setText(Helpers.getMessage(response.code()));
                }
            });
            return false;
        }
        return true;
    }
}

