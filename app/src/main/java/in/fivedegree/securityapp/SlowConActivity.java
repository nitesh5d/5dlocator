package in.fivedegree.securityapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SlowConActivity extends AppCompatActivity {

    TextView btn;
    LinearLayout retry, refreshCont;

    Handler handler = new Handler();
    Runnable runnable;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slow_con);

        btn = findViewById(R.id.btn);
        retry = findViewById(R.id.retrybtn);
        refreshCont = findViewById(R.id.refresh_cont);


        btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                btn.setTextColor(Color.parseColor("#919191"));
                return false;
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                startActivity(intent);
            }
        });

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshCont.setVisibility(View.VISIBLE);
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
                if (isConnected) {
                    Intent goodConnection = new Intent(SlowConActivity.this, MainActivity.class);
                    refreshCont.setVisibility(View.GONE);
                    startActivity(goodConnection);
                    finish();
                }
                else {
                    Toast.makeText(SlowConActivity.this, "No Connection", Toast.LENGTH_SHORT).show();
                    refreshCont.setVisibility(View.GONE);
                }
            }
        });


    }

}