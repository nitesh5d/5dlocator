package in.fivedegree.securityapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.transition.Slide;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Integer.parseInt;

public class MainActivity extends AppCompatActivity {

    TextView DeviceModel, RegisterDate, RegisterTime, LoginDate, LoginTime, currLocation, LongitudeTv, LattitudeTv, isMonitoringTv;
    String loginDate, loginTime, registerDate, registerTime, deviceName, isMonitoring, latitude, longitude, fulladdress;
    Button mainStartBtn, mainStopBtn;
    ImageView isMonitoringIcon, SlideUpIcon, LogoFull;
    LinearLayout PermissionWarning, RefreshCont, LogoutBtn, SlideUp, openWeb;
    CardView BottomCont;
    FirebaseAuth auth;
    DatabaseReference reference;
    ConstraintLayout homeActivity;
    private final static int REQUEST_CODE = 100;
    Handler handler = new Handler();
    Runnable runnable;
    Intent serviceIntent;
    Boolean isServiceRunning = false;
    Boolean isCardVisible = false;
    Boolean isGetDetailsRunning = false;


    @SuppressLint("ServiceCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DeviceModel = findViewById(R.id.device_model);
        RegisterDate = findViewById(R.id.register_date);
        RegisterTime = findViewById(R.id.register_time);
        LoginDate = findViewById(R.id.login_date);
        LoginTime = findViewById(R.id.login_time);
        LogoutBtn = findViewById(R.id.logoutBtn);
        currLocation = findViewById(R.id.currLocation);
        LongitudeTv = findViewById(R.id.longitudeTv);
        LattitudeTv = findViewById(R.id.lattitudeTv);
        isMonitoringTv = findViewById(R.id.isMonitoringTv);
        isMonitoringIcon = findViewById(R.id.isMonitoringIcon);
        mainStartBtn = findViewById(R.id.mainStartBtn);
        mainStopBtn = findViewById(R.id.mainStopBtn);
        PermissionWarning = findViewById(R.id.permission_warning);
        RefreshCont = findViewById(R.id.refresh_cont);
        homeActivity = findViewById(R.id.homeActivity);
        LogoFull = findViewById(R.id.logo_full);
        openWeb = findViewById(R.id.open_web);
        BottomCont = findViewById(R.id.bottom_cont);
        SlideUp = findViewById(R.id.slide_up);
        SlideUpIcon = findViewById(R.id.slide_up_icon);
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null){
            getDetails();
        }

        isGetDetailsRunning = true;
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, 2000);
                try {
                    getDetails();
                }
                catch (NullPointerException e){
                    e.printStackTrace();
                }
                if(isServiceRunning){
                    mainStartBtn.setVisibility(View.GONE);
                    mainStopBtn.setVisibility(View.VISIBLE);
                }
                else {
                    mainStartBtn.setVisibility(View.VISIBLE);
                    mainStopBtn.setVisibility(View.GONE);
                }
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    PermissionWarning.setVisibility(View.VISIBLE);
                }
                else {
                    PermissionWarning.setVisibility(View.GONE);
                }
            }
        }, 2000);

        serviceIntent = new Intent(MainActivity.this, MyService.class);

        mainStartBtn.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.Q)
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, getApplicationInfo().uid);
                    Toast.makeText(MainActivity.this, "Go to Permission >> Location >> Select 'Allow all the time'", Toast.LENGTH_LONG).show();
                    startActivity(intent);
                }
                else if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, getApplicationInfo().uid);
                    Toast.makeText(MainActivity.this, "Go to Permission >> Camera >> Select 'Allow'", Toast.LENGTH_LONG).show();
                    startActivity(intent);
                }
                else {
                    startForegroundService(serviceIntent);
                    isServiceRunning = !isServiceRunning;
                    RefreshCont.setVisibility(View.VISIBLE);
                    BottomCont.setVisibility(View.GONE);
                }
            }
        });
        mainStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(serviceIntent);
                isServiceRunning = !isServiceRunning;
                switchUnchecked();
            }
        });

        LattitudeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager cm = (ClipboardManager)MainActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setText(LattitudeTv.getText());
                Toast.makeText(MainActivity.this, "Latitude Copied", Toast.LENGTH_SHORT).show();
            }
        });
        LongitudeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager cm = (ClipboardManager)MainActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setText(LongitudeTv.getText());
                Toast.makeText(MainActivity.this, "Longitude Copied", Toast.LENGTH_SHORT).show();
            }
        });
        currLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager cm = (ClipboardManager)MainActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setText(currLocation.getText());
                Toast.makeText(MainActivity.this, "Address Copied", Toast.LENGTH_SHORT).show();
            }
        });

        SlideUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                final float screenHeight = getResources().getDisplayMetrics().heightPixels;
                final int screenHeight = getResources().getDimensionPixelOffset(R.dimen.cardview_translation_y);
//                final float translationY = 0.37f * screenHeight;
                ObjectAnimator animator;
                if (isCardVisible) {
                    animator = ObjectAnimator.ofFloat(BottomCont, "translationY", screenHeight);
                    SlideUpIcon.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24);
                } else {
                    animator = ObjectAnimator.ofFloat(BottomCont, "translationY", 0f);
                    SlideUpIcon.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24);
                }
                animator.setDuration(500); // Set the animation duration to 500ms
                animator.start(); // Start the animation
                isCardVisible = !isCardVisible;
            }
        });

        LogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isServiceRunning){
                    Toast.makeText(MainActivity.this, "Please Turn off Device Monitoring first.", Toast.LENGTH_SHORT).show();
                }
                else {
                    auth.signOut();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });

        LogoFull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://5degree.in/";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        openWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://nitesh5d.github.io/5dlocatorweb/";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });
    }

    private void switchUnchecked() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();
        assert currentUser != null;
        reference.child("Users").child(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(Task<DataSnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().exists()){
                        Map<String, Object> map = new HashMap<>();
                        map.put("isMonitoring", "false");
                        FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid()).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(getApplicationContext(), "Monitor status update failed: "+e ,Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "Failed to change monitoring status location.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void getDetails() {
        if(!isGetDetailsRunning){
            return;
        }
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();
        try {
            reference.child("Users").child(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(Task<DataSnapshot> task) {
                    if (task.isSuccessful()){
                        if (task.getResult().exists()){
                            DataSnapshot snapshot = task.getResult();
                            deviceName = String.valueOf(snapshot.child("deviceName").getValue());
                            DeviceModel.setText(deviceName);
                            registerDate = String.valueOf(snapshot.child("registerDate").getValue());
                            RegisterDate.setText(registerDate);
                            registerTime = String.valueOf(snapshot.child("registerTime").getValue());
                            RegisterTime.setText(registerTime);
                            loginDate = String.valueOf(snapshot.child("loginDate").getValue());
                            LoginDate.setText(loginDate);
                            loginTime = String.valueOf(snapshot.child("loginTime").getValue());
                            LoginTime.setText(loginTime);

                            isMonitoring = String.valueOf(snapshot.child("isMonitoring").getValue());
                            if (isMonitoring.equals("false")){
                                isMonitoringTv.setText("Not Monitoring");
                                isMonitoringIcon.setVisibility(View.VISIBLE);
                                isMonitoringIcon.setImageResource(R.drawable.monitor_notactive);
                                isServiceRunning = false;
                            }
                            else if (isMonitoring.equals("true")){
                                isServiceRunning = true;
                                isMonitoringTv.setText("Monitoring Active");
                                isMonitoringIcon.setVisibility(View.VISIBLE);
                                isMonitoringIcon.setImageResource(R.drawable.monitor_active);
                                RefreshCont.setVisibility(View.GONE);
                                BottomCont.setVisibility(View.VISIBLE);
                            }
                            else {
                                isMonitoringTv.setText("{Error fetching monitoring status}");
                                isMonitoringIcon.setVisibility(View.GONE);
                            }

                            latitude = String.valueOf(snapshot.child("latitude").getValue());
                            LattitudeTv.setText(latitude);
                            longitude = String.valueOf(snapshot.child("longitude").getValue());
                            LongitudeTv.setText(longitude);
                            fulladdress = String.valueOf(snapshot.child("address").getValue());
                            currLocation.setText(fulladdress);
                        }
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Failed to get details.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        catch (NullPointerException e){
            e.printStackTrace();
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(loginIntent);
            finish();
        }

    }

//    @Override
//    protected void onResume() {
//
//        super.onResume();
//    }

    @Override
    protected void onStart() {
        super.onStart();
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            Intent badConnection = new Intent(MainActivity.this, SlowConActivity.class);
            startActivity(badConnection);
            finish();
        }

        if (auth.getCurrentUser() == null) {
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(loginIntent);
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isGetDetailsRunning = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isGetDetailsRunning = false;
    }

}