package com.clinton.adrreport.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import com.clinton.adrreport.R;
import com.clinton.adrreport.utils.Helpers;
import com.clinton.adrreport.utils.User;
import com.clinton.adrreport.utils.Validators;
import com.google.gson.Gson;

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
import static com.clinton.adrreport.utils.Helpers.SETTINGS_FILE;

public class LoginActivity extends AppCompatActivity {

    // UI references.
    @BindView(R.id.emailView)
    TextInputEditText mEmailView;
    @BindView(R.id.password)
    TextInputEditText mPasswordView;
    @BindView(R.id.progressView)
    View mProgressView;
    @BindView(R.id.login_form)
    View mLoginFormView;
    @BindView(R.id.sign_in_button)
    Button mSignInButton;
    @BindView(R.id.reg_text)
    TextView mRegTextView;
    @BindView(R.id.messageView)
    TextView mMessageView;

    public static String mMessage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
    }

    @OnClick(R.id.sign_in_button)
    void attemptLogin() {

        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!Validators.isNotEmpty(password)) {
            mPasswordView.setError("Password cannot be empty");
            focusView = mPasswordView;
            cancel = true;
        } else if (!Validators.isValidPassword(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (!Validators.isNotEmpty(email)) {
            mEmailView.setError("Cannot be empty");
            focusView = mEmailView;
            cancel = true;
        }else if (!Validators.isValidEmail(email)){
            mEmailView.setError("Invalid email address");
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            new UserLoginTask(email, password).postLogin();
        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {

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

    @Override
    protected void onStart() {
        System.out.println("start login");
        super.onStart();
        if (mMessage != null){
            mMessageView.setText(mMessage);
            mMessageView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        mMessage = null;
        mMessageView.setVisibility(View.VISIBLE);
        super.onPause();
    }

    @OnClick(R.id.forget_pass)
    void actionForgotPassword() {
       startActivity(new Intent(LoginActivity.this, RecoverActivity.class));
    }

    @OnClick(R.id.reg_text)
    public void registerLinkClicked(View view) {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }

    private class UserLoginTask {

        private final String email;
        private final String password;

        UserLoginTask(String email, String password) {
            this.email = email;
            this.password = password;
        }

        void postLogin() {

            OkHttpClient client = new OkHttpClient();

            RequestBody data = new FormBody.Builder()
                    .add("username", email)
                    .add("password", password)
                    .build();

            Request request = new Request.Builder()
                    .url(BASE_URL + "login")
                    .post(data)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    LoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showProgress(false);
                            mMessageView.setVisibility(View.VISIBLE);
                            mMessageView.setText(Helpers.exceptionMessage(e));
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    if (preResponse(LoginActivity.this, response) && parseLoginResponse(response)) {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
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

        private boolean parseLoginResponse(Response response) throws IOException {

            User user = new Gson().fromJson(response.body().string(), User.class);
            System.out.println(user.toString());
            SharedPreferences preferences = LoginActivity.this.getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("token", user.token);
            editor.putString("_id", user._id);
            editor.putString("username", user.username);
            editor.putString("fullName", user.fullName);
            editor.putString("role", user.role);
            editor.apply();
            return true;

        }
    }
}

