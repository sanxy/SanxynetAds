package com.sanxynet_ads;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.parse.ParseException;
import com.parse.ParseUser;
import java.util.Objects;


public class RegisterActivity extends AppCompatActivity {
    @BindView(R.id.edit_email)
    EditText mEmail;
    @BindView(R.id.edit_password)
    EditText mPassword;
    @BindView(R.id.btn_register)
    Button register;

    @BindView(R.id.txt_login)
    TextView login;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        mProgress = new ProgressDialog(this);
    }

    @OnClick(R.id.btn_register)
    public void register(View view) {
        // Process register logic here
        doRegister();
    }

    private void doRegister() {
        mEmail.setError(null);
        mPassword.setError(null);

        final String emailTrue = Objects.requireNonNull(mEmail.getText()).toString();
        String passwordTrue = Objects.requireNonNull(mPassword.getText()).toString();

        boolean cancel = false;
        View focusView = null;


        if (!TextUtils.isEmpty(passwordTrue) && !isPasswordValid(passwordTrue)) {
            mPassword.setError(getString(R.string.error_invalid_password));
            focusView = mPassword;
            cancel = true;
        }

        if (TextUtils.isEmpty(emailTrue)) {
            mEmail.setError(getString(R.string.error_field_required));
            focusView = mEmail;
            cancel = true;
        } else if (!isEmailValid(emailTrue)) {
            mEmail.setError(getString(R.string.error_invalid_email));
            focusView = mEmail;
            cancel = true;
        }

        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        if (email.equals("") || password.equals("")) {
            final String title = getString(R.string.oops);
            final String message = getString(R.string.fill_all_fields);
            alertDisplayer(title, message);
            return;
        }else  if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }

        mProgress.setMessage(getString(R.string.register_wait));
        mProgress.setCancelable(false);
        mProgress.show();
        final ParseUser user = new ParseUser();
        user.setUsername(email);
        user.setPassword(password);

        user.signUpInBackground(e -> {
            mProgress.dismiss();
            if (e == null) {

                ParseUser.getCurrentUser();
                final String title = getString(R.string.title_register_alert);
                final String message = getString(R.string.message_register_alert_login_success);
                alertDisplayer(title, message);

            } else {
                switch (e.getCode()) {
                    case ParseException.USERNAME_TAKEN: {
                        final String title = getString(R.string.message_register_alert_account_error);
                        final String message = getString(R.string.message_register_alert_username_taken);
                        alertDisplayer(title, message + " :" + e.getMessage());
                        break;
                    }
                    case ParseException.EMAIL_TAKEN: {
                        final String title = getString(R.string.message_register_alert_account_error);
                        final String message = getString(R.string.message_register_alert_email_taken);
                        alertDisplayer(title, message + " :" + e.getMessage());
                        break;
                    }
                    default: {
                        final String title = getString(R.string.message_register_alert_account_error);
                        final String message = getString(R.string.message_register_alert_account_error);
                        alertDisplayer(title, message + " :" + e.getMessage());
                    }
                }

            }
        });
    }

    @OnClick(R.id.txt_login)
    public void login(View view) {
        // Process login logic here
        doLogin();
    }

    private void doLogin() {
        Intent register = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(register);
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    void alertDisplayer(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok), (dialog, which) -> dialog.cancel());
        AlertDialog ok = builder.create();
        ok.show();
    }
}
