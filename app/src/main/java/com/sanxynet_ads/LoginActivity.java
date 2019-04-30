package com.sanxynet_ads;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.parse.ParseUser;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.edit_text_email)
    EditText mEmail;
    @BindView(R.id.edit_text_password)
    EditText mPassword;
    @BindView(R.id.login_button)
    Button login;

    @BindView(R.id.reset_password_button)
    Button resetPassword;
    @BindView(R.id.text_view_signup)
    TextView register;
    private ProgressDialog mProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        mProgress = new ProgressDialog(this);

        final User user = new User(LoginActivity.this);
        if (!user.getUsername().equals("")){
            Intent  intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("Username", user.getUsername());
            startActivity(intent);
            finish();
        }else{

        }
    }

    @OnClick(R.id.login_button)
    public void login(View view) {
        // Process login logic here
        doLogin();
    }

    @OnClick(R.id.reset_password_button)
    public void resetPassword(View view) {
        // Open reset password activity
        Intent resetPassword = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
        startActivity(resetPassword);
    }

    @OnClick(R.id.text_view_signup)
    public void register(View view) {
        // Open register activity
        Intent register = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(register);
    }

    private void doLogin(){
        mEmail.setError(null);
        mPassword.setError(null);


        final String email = Objects.requireNonNull(mEmail.getText()).toString();
        String password = Objects.requireNonNull(mPassword.getText()).toString();

        boolean cancel = false;
        View focusView = null;


        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPassword.setError(getString(R.string.error_invalid_password));
            focusView = mPassword;
            cancel = true;
        }



        if (cancel) {

            focusView.requestFocus();
        } else {

            mProgress.setMessage(getString(R.string.login_wait));
            mProgress.setCancelable(false);
            mProgress.show();
            ParseUser.logInInBackground(email, password, (parseUser, e) -> {
                mProgress.dismiss();

                if (parseUser != null) {

                    ParseUser currentUser = ParseUser.getCurrentUser();
                        if (currentUser != null) {
                            User user = new User(LoginActivity.this);
                            user.setUsername(email);

                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.putExtra("Username", email);
                            startActivity(intent);
                            finish();
                        } else {
                            ParseUser.logOut();

                            alertDisplayer(getString(R.string.login_fail), getString(R.string.verify_email));

                        }

                }else{

                    alertDisplayer(getString(R.string.login_fail), e.getMessage() + getString(R.string.retry_again));

                }
            });
        }}


    private void alertDisplayer(String title,String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.cancel();
                    // don't forget to change the line below with the names of your Activities
//                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
                });
        AlertDialog ok = builder.create();
        ok.show();
    }

    private boolean isPasswordValid(String password) {
        // Password must be more than 4
        return password.length() > 4;
    }

}
