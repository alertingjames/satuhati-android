package com.app.satuhati.main;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.app.satuhati.R;
import com.app.satuhati.base.BaseActivity;
import com.app.satuhati.commons.Commons;
import com.app.satuhati.commons.ReqConst;
import com.app.satuhati.models.User;
import com.chaos.view.PinView;
import com.google.android.gms.maps.model.LatLng;
import com.iamhabib.easy_preference.EasyPreference;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends BaseActivity {

    EditText emailBox, passwordBox;
    TextView loginButton, signupButton, forgotpasswordButton;
    ImageButton showButton;
    boolean pwShow = false;
    AVLoadingIndicatorView progressBar;

    private static final int RC_SIGN_IN = 9001;

    private static final String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.VIBRATE,
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.CALL_PHONE,
            android.Manifest.permission.CALL_PRIVILEGED,
            android.Manifest.permission.MEDIA_CONTENT_CONTROL,
            android.Manifest.permission.SYSTEM_ALERT_WINDOW,};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        checkAllPermission();

        progressBar = (AVLoadingIndicatorView)findViewById(R.id.loading_bar);

        emailBox = (EditText)findViewById(R.id.emailBox);
        passwordBox = (EditText)findViewById(R.id.passwordBox);

        loginButton = (TextView) findViewById(R.id.btn_login);
        signupButton = (TextView) findViewById(R.id.btn_signup);
        forgotpasswordButton = (TextView) findViewById(R.id.btn_forgot_pwd);
        showButton = (ImageButton)findViewById(R.id.showButton);
        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!pwShow){
                    pwShow = true;
                    showButton.setImageResource(R.drawable.eyelock);
                    if(passwordBox.getText().length() > 0){
                        passwordBox.setInputType(InputType.TYPE_CLASS_TEXT);
                    }
                }else {
                    pwShow = false;
                    showButton.setImageResource(R.drawable.eyeunlock);
                    if(passwordBox.getText().length() > 0){
                        passwordBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    }
                }
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
            }
        });

        forgotpasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        setupUI(findViewById(R.id.activity), this);

    }

    public void checkAllPermission() {

        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        if (hasPermissions(this, PERMISSIONS)){

        }else {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 101);
        }
    }
    public static boolean hasPermissions(Context context, String... permissions) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {

            for (String permission : permissions) {

                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void login(){

        if(emailBox.getText().length() == 0){
            showToast(getString(R.string.enter_email));
            return;
        }

        if(!isValidEmail(emailBox.getText().toString().trim())){
            showToast(getString(R.string.invalid_email));
            return;
        }

        if(passwordBox.getText().length() == 0){
            showToast(getString(R.string.enter_password));
            return;
        }

        login(emailBox.getText().toString().trim(), passwordBox.getText().toString().trim());

    }

    private void login(String email, String password){

        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "signin")
                .addBodyParameter("email", email)
                .addBodyParameter("password", password)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("RESPONSE!!!", response.toString());
                        progressBar.setVisibility(View.GONE);
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                JSONObject object = response.getJSONObject("data");
                                User user = new User();
                                user.set_idx(object.getInt("id"));
                                user.set_name(object.getString("name"));
                                user.set_email(object.getString("email"));
                                user.set_password(object.getString("password"));
                                user.set_photoUrl(object.getString("picture_url"));
                                user.set_registered_time(object.getString("registered_time"));
                                double lat = 0.0d, lng = 0.0d;
                                if(object.getString("latitude").length() > 0){
                                    lat = Double.parseDouble(object.getString("latitude"));
                                    lng = Double.parseDouble(object.getString("longitude"));
                                }
                                user.setLatLng(new LatLng(lat, lng));
                                user.set_status(object.getString("status"));

                                if(!object.getString("auth_status").equals("verified")){
                                    String msg = getString(R.string.code_sent_to_email) + " " + user.get_email() + ". " + getString(R.string.enter_code_continue);
                                    showAlertDialogForVerification(getString(R.string.otp_auth), msg, user, LoginActivity.this);
                                }else {
                                    Commons.thisUser = user;
                                    showToast(getString(R.string.login_success));

                                    EasyPreference.with(getApplicationContext(), "user_info")
                                            .addString("email", Commons.thisUser.get_email())
                                            .addString("password", Commons.thisUser.get_password())
                                            .save();

                                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }

                            }else if(result.equals("1")){
                                Commons.thisUser = null;
                                showToast(getString(R.string.unknown_user));
                                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                                startActivity(intent);
                            }else if(result.equals("2")){
                                Commons.thisUser = null;
                                showToast(getString(R.string.incorrect_password));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        progressBar.setVisibility(View.GONE);
                        showToast(error.getErrorDetail());
                    }
                });

    }

    public void showAlertDialogForVerification(String title, String message, User user, Activity activity){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.layout_alert_authentication, null);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();
        TextView titleBox = (TextView)view.findViewById(R.id.title);
        titleBox.setText(title);
        TextView messageBox = (TextView) view.findViewById(R.id.messageBox);
        messageBox.setText(message);
        TextView resendButton = (TextView) view.findViewById(R.id.resendButton);
        resendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resendCode(String.valueOf(user.get_idx()));
            }
        });
        PinView codeBox = (PinView) view.findViewById(R.id.codeBox);
        TextView submitButton = (TextView)view.findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(codeBox.getText().length() == 0){
                    showToast(getString(R.string.enter_code));
                    return;
                }
                submitCode(String.valueOf(user.get_idx()), codeBox.getText().toString(), user);

                dialog.dismiss();
            }
        });

        ImageView cancelButton = (ImageView)view.findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // Get screen width and height in pixels
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // The absolute width of the available display size in pixels.
        int displayWidth = displayMetrics.widthPixels;
        // The absolute height of the available display size in pixels.
        int displayHeight = displayMetrics.heightPixels;

        // Initialize a new window manager layout parameters
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

        // Copy the alert dialog window attributes to new layout parameter instance
        layoutParams.copyFrom(dialog.getWindow().getAttributes());

        // Set alert dialog width equal to screen width 80%
        int dialogWindowWidth = (int) (displayWidth * 0.8f);
        // Set alert dialog height equal to screen height 80%
        //    int dialogWindowHeight = (int) (displayHeight * 0.8f);

        // Set the width and height for the layout parameters
        // This will bet the width and height of alert dialog
        layoutParams.width = dialogWindowWidth;
        //      layoutParams.height = dialogWindowHeight;

        // Apply the newly created layout parameters to the alert dialog window
        dialog.getWindow().setAttributes(layoutParams);
        dialog.setCancelable(false);
    }

    public void submitCode(String user_id, String code, User user) {
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "codesubmit")
                .addBodyParameter("member_id", user_id)
                .addBodyParameter("code", code)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("RESPONSE!!!", response.toString());
                        progressBar.setVisibility(View.GONE);
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                Commons.thisUser = user;
                                showToast(getString(R.string.login_success));
                                EasyPreference.with(getApplicationContext(), "user_info")
                                        .addString("email", Commons.thisUser.get_email())
                                        .addString("password", Commons.thisUser.get_password())
                                        .save();
                                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }else if(result.equals("1")){
                                showToast(getString(R.string.incorrect_code));
                            }else if(result.equals("2")){
                                showToast(getString(R.string.unexisting_user));
                            }else {
                                showToast(getString(R.string.server_issue));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        progressBar.setVisibility(View.GONE);
                        showToast(error.getErrorDetail());
                    }
                });
    }

    public void resendCode(String user_id) {
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "resendcode")
                .addBodyParameter("member_id", user_id)
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("RESPONSE!!!", response.toString());
                        progressBar.setVisibility(View.GONE);
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                showToast(getString(R.string.another_code_sent));
                            }else if(result.equals("1")){
                                showToast(getString(R.string.unexisting_user));
                            }else {
                                showToast(getString(R.string.server_issue));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        progressBar.setVisibility(View.GONE);
                        showToast(error.getErrorDetail());
                    }
                });
    }

}



















