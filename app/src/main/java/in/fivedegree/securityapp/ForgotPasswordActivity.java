package in.fivedegree.securityapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.List;
import java.util.regex.Pattern;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText EmailEtv;
    Button SubmitBtn;
    FrameLayout ProgressCont;
    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        EmailEtv = findViewById(R.id.RegEmailEtv);
        SubmitBtn = findViewById(R.id.submitBtn);
        ProgressCont = findViewById(R.id.progressCont);

        SubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeybaord(v);
                String email = EmailEtv.getText().toString();
                if(TextUtils.isEmpty(email)){
                    EmailEtv.setError("Please enter your registered email address");
                    EmailEtv.requestFocus();
                }
                else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    EmailEtv.setError("Please enter a valid email address");
                    EmailEtv.requestFocus();
                }
                else {
                    ProgressCont.setVisibility(View.VISIBLE);
                    verifyEmail(email);
                }
            }
        });
    }

    private void verifyEmail(String email) {
        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(Task<SignInMethodQueryResult> task) {
                if (task.isSuccessful()){
                    SignInMethodQueryResult result = task.getResult();
                    if (result.getSignInMethods().size() > 0) {
                        resetpassword(email);
                    }
                    else {
                        ProgressCont.setVisibility(View.GONE);
                        Toast.makeText(ForgotPasswordActivity.this, "Email Address is not registered.", Toast.LENGTH_SHORT).show();
                        EmailEtv.setError("Enter a registered Email Address.");
                    }
                }
                else {
                    ProgressCont.setVisibility(View.GONE);
                    Toast.makeText(ForgotPasswordActivity.this, "Error Occurred.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void resetpassword(String email) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                if(task.isSuccessful()){
                    ProgressCont.setVisibility(View.GONE);
                    Toast.makeText(ForgotPasswordActivity.this, "An email containing a link to reset your password has been sent. Please check your Inbox.", Toast.LENGTH_SHORT).show();
                    openAppAlertDialog();
                }
                else {
                    ProgressCont.setVisibility(View.GONE);
                    Toast.makeText(ForgotPasswordActivity.this, "An Error Occurred.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openAppAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPasswordActivity.this);
        builder.setTitle("Open Email App.");
        builder.setIcon(R.drawable.ic_baseline_open_email_app_24);
        builder.setMessage("Do you want to open Email App now?.");

        builder.setPositiveButton("Open", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent verifyIntent = new Intent(Intent.ACTION_MAIN);
                verifyIntent.addCategory(Intent.CATEGORY_APP_EMAIL);
                verifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PackageManager packageManager = getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(verifyIntent, 0);
                if (activities != null && activities.size() > 0) {
                    startActivity(verifyIntent);
                    finish();
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Couldn't Open an Email App.", Toast.LENGTH_SHORT).show();
                }

            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void hideKeybaord(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(),0);
    }
}