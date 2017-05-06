package com.clinton.adrreport.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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

public class RecoverActivity extends AppCompatActivity {

    // UI references.
    @BindView(R.id.emailView)
    TextInputEditText mEmailView;
    @BindView(R.id.progressView)
    View mProgressView;
    @BindView(R.id.login_form)
    View mLoginFormView;
    @BindView(R.id.sign_in_button)
    Button mSignInButton;
    @BindView(R.id.messageView)
    TextView mMessageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.sign_in_button)
    void attemptLogin() {

        mEmailView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!Validators.isNotEmpty(email)) {
            mEmailView.setError("Cannot be empty");
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            new PasswordRecoverTask(email).postLogin();
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

    private static class EmailForm {
        String username;

        public EmailForm(String username) {
            this.username = username;
        }

    }

    public class PasswordRecoverTask {

        private final String email;
        
        PasswordRecoverTask(String email) {
            this.email = email;
        }

       void postLogin() {

            OkHttpClient client = new OkHttpClient();

            RequestBody data = new FormBody.Builder()
                    .add("username", email)
                    .build();

            Request request = new Request.Builder()
                    .url(BASE_URL + "users/recover")
                    .post(data)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, final IOException e) {
                    RecoverActivity.this.runOnUiThread(new Runnable() {
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
                    if(preResponse(RecoverActivity.this, response)) {
                        LoginActivity.mMessage = "New password sent to email";
                        finish();
                    }
                }
            });
        }
    }

    private boolean preResponse(Activity context, final Response response){
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgress(false);
            }
        });

        System.out.println(response);

        if (response.code() != 200){
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMessageView.setVisibility(View.VISIBLE);
                    mMessageView.setText(Helpers.getMessage(response.code()));
                }
            });
            return false;
        }
        return true;
    }

}
