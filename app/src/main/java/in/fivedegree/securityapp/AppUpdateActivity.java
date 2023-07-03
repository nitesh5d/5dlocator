package in.fivedegree.securityapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class AppUpdateActivity extends AppCompatActivity {

    ImageView closeBtn, ss1, ss2, ss3, ss4;
    TextView listItem1, listItem2, listItem3, description;
    CardView updateBtn;
    DatabaseReference reference;
    String  list1, list2, list3, desc, type, ss1url, ss2url, ss3url, ss4url, updateUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_update);

        closeBtn = findViewById(R.id.close_btn);
        ss1 = findViewById(R.id.ss1);
        ss2 = findViewById(R.id.ss2);
        ss3 = findViewById(R.id.ss3);
        ss4 = findViewById(R.id.ss4);
        listItem1 = findViewById(R.id.list_item_1);
        listItem2 = findViewById(R.id.list_item_2);
        listItem3 = findViewById(R.id.list_item_3);
        description = findViewById(R.id.decsription);
        updateBtn = findViewById(R.id.update_btn);

        reference = FirebaseDatabase.getInstance().getReference();
        try {
            reference.child("AppUpdate").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>(){
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()){
                        if (task.getResult().exists()){
                            DataSnapshot snapshot = task.getResult();
                            list1 = snapshot.child("listItem1").getValue().toString();
                            list2 = snapshot.child("listItem2").getValue().toString();
                            list3 = snapshot.child("listItem3").getValue().toString();
                            desc = snapshot.child("description").getValue().toString();
                            ss1url = Objects.requireNonNull(snapshot.child("ss1").getValue()).toString();
                            ss2url = snapshot.child("ss2").getValue().toString();
                            ss3url = snapshot.child("ss3").getValue().toString();
                            ss4url = snapshot.child("ss4").getValue().toString();
                            type = Objects.requireNonNull(snapshot.child("type").getValue()).toString();
                            updateUrl = snapshot.child("url").getValue().toString();

                            if (type.equals("imm")){
                                closeBtn.setVisibility(View.GONE);
                            }
                            else {
                                closeBtn.setVisibility(View.VISIBLE);
                                closeBtn.setOnClickListener(v ->{
                                    finish();
                                });
                            }

                            listItem1.setText("✦ " + list1);
                            listItem2.setText("✦ " + list2);
                            listItem3.setText("✦ " + list3);
                            description.setText(desc);

                            Picasso.get().load(ss1url).into(ss1);
                            Picasso.get().load(ss2url).into(ss2);
                            Picasso.get().load(ss3url).into(ss3);
                            Picasso.get().load(ss4url).into(ss4);

                            updateBtn.setOnClickListener(v ->{
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(updateUrl));
                                startActivity(intent);
                            });
                        }
                    }
                    else {
                        Toast.makeText(AppUpdateActivity.this, "Failed to get App Update Details.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }
    }
}