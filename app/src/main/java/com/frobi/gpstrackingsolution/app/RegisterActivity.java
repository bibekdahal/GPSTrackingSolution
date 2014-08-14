package com.frobi.gpstrackingsolution.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;


public class RegisterActivity extends Activity{

    public static final String PREFS_NAME = "GPSTrackerEmailsPreferences";

    //Keep track of the login task to ensure we can cancel it if requested.
    private UserLoginTask m_authTask = null;

    private EditText m_emailView;
    private EditText m_passwordView;
    private View m_progressView;
    private View m_loginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        if (!settings.getString("Email", "").equals("")) {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_register);

        // Set up the login form.
        m_emailView = (EditText) findViewById(R.id.email);
        m_passwordView = (EditText) findViewById(R.id.password);
        m_passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        m_loginFormView = findViewById(R.id.login_form);
        m_progressView = findViewById(R.id.login_progress);
    }

    public void attemptLogin() {
        if (m_authTask != null) {
            return;
        }

        // Reset errors.
        m_emailView.setError(null);
        m_passwordView.setError(null);

        // Store values at the time of the login attempt.
        String email = m_emailView.getText().toString();
        String password = m_passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            m_passwordView.setError(getString(R.string.error_invalid_password));
            focusView = m_passwordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            m_emailView.setError(getString(R.string.error_field_required));
            focusView = m_emailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            m_emailView.setError(getString(R.string.error_invalid_email));
            focusView = m_emailView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            m_authTask = new UserLoginTask(email, password);
            m_authTask.execute((Void) null);
        }
    }
    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            m_loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            m_loginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    m_loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            m_progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            m_progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    m_progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            m_progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            m_loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    public class UserLoginTask extends AsyncTask<Void, Void, Integer> {

        private final String m_email;
        private final String m_password;

        UserLoginTask(String email, String password) {
            m_email = email;
            m_password = password;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                WebAccess access = new WebAccess();
                access.Connect("check-email-password", m_email, m_password, "000");
                String response = access.GetResponse();
                if (response.contains("UNREGISTERED")) return 1;
                else if (response.contains("REGISTERED BUT INVALID")) return 2;
                else if (response.contains("REGISTERED AND VALID")) return 0;
                else return -1;
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }

        @Override
        protected void onPostExecute(final Integer retCode) {
            m_authTask = null;
            if (retCode!=0) showProgress(false);

            if (retCode==0) {
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("Email", m_email);
                editor.putString("Password", m_password);
                editor.commit();

                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else if (retCode==1) {
                m_emailView.setError(getString(R.string.error_incorrect_email));
                m_emailView.requestFocus();
            } else if (retCode==2) {
                m_passwordView.setError(getString(R.string.error_incorrect_password));
                m_passwordView.requestFocus();
            } else if (retCode==-1) {
                Toast.makeText(RegisterActivity.this, getString(R.string.error_connection), Toast.LENGTH_SHORT).show();

                //////// REMOVE THIS ///////////
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("Email", "NULLEMAIL");
                editor.putString("Password", "NULLPASSWORD");
                editor.commit();
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                ////////////////////////////////
            }
        }

        @Override
        protected void onCancelled() {
            m_authTask = null;
            showProgress(false);
        }
    }
}



