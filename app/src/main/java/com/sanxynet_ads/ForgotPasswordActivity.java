package com.sanxynet_ads;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.parse.ParseUser;


public class ForgotPasswordActivity extends AppCompatActivity {

    @BindView(R.id.editEmail)
    EditText emailField;
    @BindView(R.id.bForgotPassword)
    Button resetPassword;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        ButterKnife.bind(this);
        mProgress = new ProgressDialog(this);
    }

    void alertDisplayer(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPasswordActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.cancel());
        AlertDialog ok = builder.create();
        ok.show();
    }

    @OnClick(R.id.bForgotPassword)
    public void resetPassword(View view) {
        // Process reset password logic here
       ResetPassword();
    }

    public void ResetPassword() {
        String email = emailField.getText().toString().trim();
        mProgress.setMessage("Please wait...");
        mProgress.show();

        ParseUser.requestPasswordResetInBackground(String.valueOf(email), e -> {
            mProgress.dismiss();

            if (e == null) {
                // An email was successfully sent with reset instructions
                final String title = "Password Reset Email Sent!";
                final String message = "Check Your Email To Change Your Password";
                alertDisplayer(title, message);

            } else {
                // Something went wrong. Look at the ParseException
                final String title = "Password Reset Failed";
                final String message = "Password cannot be changed";
                alertDisplayer(title, message + " :" + e.getMessage());
            }
        });
    }
}
