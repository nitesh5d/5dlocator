package in.fivedegree.securityapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.Keep;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

@Keep
public class BootCompletedReceiver extends BroadcastReceiver {

    DatabaseReference reference;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null){
                reference = FirebaseDatabase.getInstance().getReference();
                try{
                    reference.child("Users").child(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(Task<DataSnapshot> task) {
                            if (task.isSuccessful()){
                                if (task.getResult().exists()){
                                    DataSnapshot snapshot = task.getResult();

                                    String isMonitoring = String.valueOf(snapshot.child("isMonitoring").getValue());
                                    if (isMonitoring.equals("true")){
                                        Intent serviceIntent = new Intent(context, LocationService.class);
                                        context.startForegroundService(serviceIntent);
                                    }
                                }
                            }
                        }
                    });
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
