package in.fivedegree.securityapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

    TextView LoginAccLink;
    EditText EmailEtv, PwEtv;
    Button SignUpBtn;
    FrameLayout ProgressCont;
    LinearLayout TnCLink;
    private long pressedTime;
    private static final String TAG = "RegisterActivity";

    String Brand = Build.BRAND;
    String Model = Build.MODEL;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        LoginAccLink = findViewById(R.id.loginAccLink);
        SignUpBtn = findViewById(R.id.signUpBtn);
        EmailEtv = findViewById(R.id.emailEtv);
        PwEtv = findViewById(R.id.pwEtv);
        ProgressCont = findViewById(R.id.progressCont);
        TnCLink = findViewById(R.id.tncLink);

        SignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeybaord(v);
                String textemail = EmailEtv.getText().toString();
                String textpw = PwEtv.getText().toString();

                if (TextUtils.isEmpty(textemail)){
                    EmailEtv.setError("Email is Required.");
                    EmailEtv.requestFocus();
                }
                else if(!Patterns.EMAIL_ADDRESS.matcher(textemail).matches()){
                    EmailEtv.setError("Please enter a valid Email Address.");
                    EmailEtv.requestFocus();
                }
                else if(TextUtils.isEmpty(textpw)){
                    PwEtv.setError("Please Create a Password.");
                    PwEtv.requestFocus();
                }
                else if(textpw.length() < 8){
                    PwEtv.setError("Weak Password!");
                    PwEtv.requestFocus();
                }
                else{
                    ProgressCont.setVisibility(View.VISIBLE);
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    auth.createUserWithEmailAndPassword(textemail, textpw).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                String registerDate =  getCurrentDate();
                                String registerTime = getCurrentTime();
                                String loginDate =  getCurrentDate();
                                String loginTime = getCurrentTime();
                                String deviceName = Brand + " " + Model;
                                String latitude = "Latitude will be displayed here.";
                                String longitude = "Longitude will be displayed here.";
                                String address = "Full Address goes here.";
                                String frontPhoto = "Front Photo url";
                                String rearPhoto = "Rear Photo url";
                                String monitorFrequency = "10s";
                                String isMonitoring = "false";
                                String ringMode = "ring";
                                String playSound = "false";
                                String flash = "false";
                                Integer batteryLevel = 0;


                                FirebaseUser firebaseUser = auth.getCurrentUser();

                                ReadWriteUserDetails writeuserdetails = new ReadWriteUserDetails(textemail, registerDate, registerTime, loginDate, loginTime, deviceName, latitude, longitude, address, frontPhoto, rearPhoto, monitorFrequency, isMonitoring, ringMode, flash, playSound, batteryLevel);

                                firebaseDatabase = FirebaseDatabase.getInstance();

                                reference = firebaseDatabase.getReference("Users");

                                reference.child(firebaseUser.getUid()).setValue(writeuserdetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(Task<Void> task) {
                                        ProgressCont.setVisibility(View.GONE);
                                        if (task.isSuccessful()){
                                            firebaseUser.sendEmailVerification();
                                            LayoutInflater inflater = getLayoutInflater();
                                            View layout = inflater.inflate(R.layout.login_success_toast, (ViewGroup) findViewById(R.id.toast_layout_root));
                                            TextView toastTextView = (TextView) layout.findViewById(R.id.toastTextView);
                                            ImageView toastImageView = (ImageView) layout.findViewById(R.id.toastImageView);
                                            toastTextView.setText("Signup Successful. Please Verify Email Address and Login.");
                                            toastImageView.setImageResource(R.drawable.baseline_done_all_24);
                                            Toast toast = new Toast(getApplicationContext());
                                            toast.setDuration(Toast.LENGTH_LONG);
                                            toast.setView(layout);
                                            toast.show();
                                            auth.signOut();
                                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        }
                                        else {
                                            Exception ew = task.getException();
                                            Toast.makeText(RegisterActivity.this, "Sign Up Failed" + ew,Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                });
                            }
                            else{
                                ProgressCont.setVisibility(View.GONE);
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthWeakPasswordException e) {
                                    PwEtv.setError("Weak Password");
                                    PwEtv.requestFocus();
                                } catch (FirebaseAuthInvalidCredentialsException e){
                                    EmailEtv.setError("Invalid Credentials");
                                    EmailEtv.requestFocus();
                                } catch (FirebaseAuthUserCollisionException e ){
                                    EmailEtv.setError("Email already in use.");
                                    EmailEtv.requestFocus();
                                } catch (Exception e){
                                    Log.e(TAG, e.getMessage());
                                    Toast.makeText(RegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            }
        });

        LoginAccLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginAccLinkInt = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(loginAccLinkInt);
                overridePendingTransition(0,0);
                finish();
            }
        });

        TnCLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://www.5degree.in/5dlocator/tnc";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
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

    public void onBackPressed() {

        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            finish();
        } else {
            Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        pressedTime = System.currentTimeMillis();
    }

}