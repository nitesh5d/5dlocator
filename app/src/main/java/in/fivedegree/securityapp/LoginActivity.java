package in.fivedegree.securityapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    TextView CreateAccLink, ForgotPw;
    Button loginBtn;
    EditText EmailEtv, PwEtv;
    FrameLayout ProgressCont;
    FirebaseAuth auth;
    private long pressedTime;
    String Brand = Build.BRAND;
    String Model = Build.MODEL;
    String deviceName = Brand+" " +Model;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        CreateAccLink = findViewById(R.id.createAccLink);
        loginBtn = findViewById(R.id.loginBtn);
        EmailEtv = findViewById(R.id.emailEtv);
        PwEtv = findViewById(R.id.pwEtv);
        ForgotPw = findViewById(R.id.forgotPw);
        ProgressCont = findViewById(R.id.progressCont);

        auth = FirebaseAuth.getInstance();


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hideKeybaord(v);

                String textemail = EmailEtv.getText().toString();
                String textpw = PwEtv.getText().toString();

                if (TextUtils.isEmpty(textemail)){
                    EmailEtv.setError("Please enter a valid Email Address");
                    EmailEtv.requestFocus();
                }
                else if(!Patterns.EMAIL_ADDRESS.matcher(textemail).matches()){
                    EmailEtv.setError("Please enter a valid Email Address");
                    EmailEtv.requestFocus();
                }
                else if(TextUtils.isEmpty(textpw)){
                    PwEtv.setError("Please enter your Password");
                    PwEtv.requestFocus();
                }
                else if(textpw.length() < 8){
                    PwEtv.setError("Weak Password!");
                    PwEtv.requestFocus();
                }

                else{
                    ProgressCont.setVisibility(View.VISIBLE);
                    auth.signInWithEmailAndPassword(textemail, textpw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(Task<AuthResult> task) {
                            ProgressCont.setVisibility(View.GONE);

                            if (task.isSuccessful()){
                                String loginDate =  getCurrentDate();
                                String loginTime = getCurrentTime();

                                FirebaseUser firebaseUser = auth.getCurrentUser();

                                if(firebaseUser.isEmailVerified()){
                                    updateLoginDetails(loginDate, loginTime);
                                    Toast.makeText(LoginActivity.this, "Logged In.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                                else {
                                    firebaseUser.sendEmailVerification();
                                    auth.signOut();
                                    verifyAlertDialog();
                                }
                            }
                            else {
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthWeakPasswordException e) {
                                    PwEtv.setError("Weak Password");
                                    PwEtv.requestFocus();
                                } catch (FirebaseAuthInvalidUserException e){
                                    EmailEtv.setError("User does not exist.");
                                    EmailEtv.requestFocus();
                                } catch (FirebaseAuthInvalidCredentialsException e){
                                    PwEtv.setError("Invalid Credentials.");
                                    PwEtv.requestFocus();
                                } catch (Exception e){
                                    Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            }
        });

        CreateAccLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createAccLinkInt = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(createAccLinkInt);
                overridePendingTransition(0,0);
                finish();
            }
        });

        ForgotPw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent forgotPwIntent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(forgotPwIntent);
            }
        });
    }

    private void verifyAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Email Not Verified.");
        builder.setIcon(R.drawable.ic_baseline_error_24);
        builder.setMessage("Please verify your email.");

        builder.setPositiveButton("Verify Now", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent verifyIntent = new Intent(Intent.ACTION_MAIN);
                verifyIntent.addCategory(Intent.CATEGORY_APP_EMAIL);
                verifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PackageManager packageManager = getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(verifyIntent, 0);
                if (activities != null && activities.size() > 0) {
                    startActivity(verifyIntent);
                } else {
                    Toast.makeText(LoginActivity.this, "Couldn't Open an Email App.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void updateLoginDetails(String loginDate, String loginTime) {
        HashMap UserLoginDetails = new  HashMap();
        UserLoginDetails.put("loginDate",loginDate);
        UserLoginDetails.put("loginTime",loginTime);
        UserLoginDetails.put("deviceName", deviceName);

        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(auth.getCurrentUser().getUid()).updateChildren(UserLoginDetails).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(Task task) {
                if (task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "Login Details Updated.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(LoginActivity.this, "Login Details Update Failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String getCurrentTime(){
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
    }

    private String getCurrentDate(){
        return new SimpleDateFormat("dd/LLL/yyyy", Locale.getDefault()).format(new Date());
    }

    private void hideKeybaord(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(),0);
    }

    @Override
    public void onBackPressed() {

        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            finish();
        } else {
            Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        pressedTime = System.currentTimeMillis();
    }

    @Override
    protected void onStart() {
        super.onStart();
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            Intent badConnection = new Intent(LoginActivity.this, SlowConActivity.class);
            startActivity(badConnection);
            finish();
        }

        if (auth.getCurrentUser() != null && auth.getCurrentUser().isEmailVerified()) {
            // User is authenticated and email is verified
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

    }
}
