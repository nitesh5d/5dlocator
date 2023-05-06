package in.fivedegree.securityapp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MyService extends Service {

    private int interval;
    private final Handler handler = new Handler();
    private boolean taskRunning = false;
    LocationManager locationManager;
    DatabaseReference reference;
    Double latitude, longitude;
    String fulladdress, monitorFrequency, flash, playSound, ringMode;
    Integer batteryLevel;
    private static final String CHANNEL_ID = "Main";
    private int NOTIFICATION_ID = 1;
    private CameraManager cameraManager;
    private String cameraId;

    private BroadcastReceiver shutdownReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
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
    };

    private BroadcastReceiver batteyLevelReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final Runnable runnable = new Runnable() {
        public void run() {
            if (taskRunning) {
                getInterval();
                getLocationUpdate();
                if (fulladdress != null && latitude != null && longitude != null) {
                    storelatestLocation();
                }
                handler.postDelayed(this, interval);
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerReceiver(shutdownReceiver, new IntentFilter(Intent.ACTION_SHUTDOWN));
        registerReceiver(batteyLevelReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("Keeping this device safe")
                .setContentText("App is now monitoring device location..")
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "My Service", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(NOTIFICATION_ID, builder.build());

        startForeground(NOTIFICATION_ID, builder.build());

        taskRunning = true;
        handler.post(runnable);
        return START_STICKY;
    }

    private void getInterval() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();
        assert currentUser != null;
        reference.child("Users").child(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(Task<DataSnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().exists()){
                        DataSnapshot snapshot = task.getResult();
                        monitorFrequency = String.valueOf(snapshot.child("monitorFrequency").getValue());
                        if (monitorFrequency.equals("10s")){
                            interval = 10000;
                        }
                        else if (monitorFrequency.equals("5s")){
                            interval = 5000;
                        }
                        else if (monitorFrequency.equals("2s")){
                            interval = 2000;
                        }
                        else if (monitorFrequency.equals("1s")){
                            interval = 1000;
                        }
                        else if (monitorFrequency.equals("1m")){
                            interval = 60000;
                        }
                        else {
                            interval = 10000;
                        }
                        flash = String.valueOf(snapshot.child("flash").getValue());
                        if(flash.equals("true")){
                            flashOn();
                        }

                        playSound = String.valueOf(snapshot.child("playSound").getValue());
                        if (playSound.equals("true")){
                            playSoundPhone();
                        }
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "Failed to get Monitor Frequency.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void storelatestLocation() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();
            assert currentUser != null;
            reference.child("Users").child(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(Task<DataSnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().exists()){

                        Map<String, Object> map = new HashMap<>();
                        map.put("address", fulladdress);
                        map.put("latitude", latitude);
                        map.put("longitude", longitude);
                        map.put("isMonitoring", "true");
                        map.put("batteryLevel", batteryLevel);
                        map.put("ringMode", ringMode);

                        FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid()).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(getApplicationContext(), "Location update failed: "+e ,Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "Failed to store latest location.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getLocationUpdate() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (!addresses.isEmpty()) {
                        fulladdress = addresses.get(0).getAddressLine(0);
                    } else {
                        fulladdress = "Unknown location";
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        switch (am.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                ringMode = "silent";
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                ringMode = "vibrate";
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                ringMode = "ring";
                break;
            default: ringMode = "ring";
        }
    }

    private void flashOn() {
        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
            cameraManager =  (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            try {
                cameraId = cameraManager.getCameraIdList()[0];
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < 50; i++) {
                try {
                    cameraManager.setTorchMode(cameraId, true);
                } catch (CameraAccessException | IllegalArgumentException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    cameraManager.setTorchMode(cameraId, false);
                } catch (CameraAccessException | IllegalArgumentException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            reference = FirebaseDatabase.getInstance().getReference();
            assert currentUser != null;
            reference.child("Users").child(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(Task<DataSnapshot> task) {
                    if (task.isSuccessful()){
                        if (task.getResult().exists()){

                            Map<String, Object> map = new HashMap<>();
                            map.put("flash", "false");

                            FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid()).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(getApplicationContext(), "Location update failed: "+e ,Toast.LENGTH_SHORT).show();

                                }
                            });
                        }
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Failed to update flashlight status.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void playSoundPhone() {
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.alert_sound);
        mp.start();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();
        assert currentUser != null;
        reference.child("Users").child(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(Task<DataSnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().exists()){

                        Map<String, Object> map = new HashMap<>();
                        map.put("playSound", "false");

                        FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid()).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(getApplicationContext(), "sound update failed: "+e ,Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "Failed to update sound status.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        taskRunning = false;
        unregisterReceiver(shutdownReceiver);
        unregisterReceiver(batteyLevelReceiver);
        Toast.makeText(getApplicationContext(),"Location monitoring turned off.",Toast.LENGTH_SHORT).show();
    }
}
