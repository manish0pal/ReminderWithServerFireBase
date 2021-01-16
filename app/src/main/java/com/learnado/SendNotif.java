package com.learnado;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.learnado.SendNotificationPack.APIService;
import com.learnado.SendNotificationPack.Client;
import com.learnado.SendNotificationPack.Data;
import com.learnado.SendNotificationPack.MyResponse;
import com.learnado.SendNotificationPack.NotificationSender;
import com.learnado.SendNotificationPack.Token;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendNotif extends AppCompatActivity {
    EditText UserTB,Title,Message;
    Button send;
    private APIService apiService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_notif);
        UserTB=findViewById(R.id.UserID);
        Title=findViewById(R.id.Title);
        Message=findViewById(R.id.Message);
        send=findViewById(R.id.button);
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance("https://testcode-22ae5-default-rtdb.firebaseio.com/").getReference("Tokens").child(UserTB.getText().toString().trim()).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String usertoken=dataSnapshot.getValue(String.class);
                        Toast.makeText(SendNotif.this, "usertoken: "+usertoken, Toast.LENGTH_SHORT).show();
                        Log.d("Manihs",usertoken+" usertoken");
                        sendNotifications(usertoken, Title.getText().toString().trim(),Message.getText().toString().trim());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(SendNotif.this, ""+databaseError, Toast.LENGTH_SHORT).show();
                        Log.d("Manihs",databaseError+"");
                    }
                });
            }
        });
        UpdateToken();
    }
    private void UpdateToken(){
      FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();




        String refreshToken= FirebaseInstanceId.getInstance().getToken();
        Token token= new Token(refreshToken);
        Toast.makeText(this, "Token: "+token, Toast.LENGTH_SHORT).show();
        FirebaseDatabase.getInstance("https://testcode-22ae5-default-rtdb.firebaseio.com/").getReference("Tokens").child(firebaseUser.getUid()).setValue(token);
    }

    public void sendNotifications(String usertoken, String title, String message) {
        Data data = new Data(title, message);
        NotificationSender sender = new NotificationSender(data, usertoken);
        apiService.sendNotifcation(sender).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                if (response.code() == 200) {
                    if (response.body().success != 1) {
                        Toast.makeText(SendNotif.this, "Failed ", Toast.LENGTH_LONG);
                    }
                    else {
                        Toast.makeText(SendNotif.this, "Sucess 1: "+response.body().success, Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(SendNotif.this, "Sucess 2", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {
                Toast.makeText(SendNotif.this, "throw :"+t, Toast.LENGTH_SHORT).show();

            }
        });
    }
    public void scheduleJob(View v) {
        ComponentName componentName = new ComponentName(this, TestJobService.class);
        JobInfo info = new JobInfo.Builder(123, componentName)
                .setRequiresCharging(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setPersisted(true)
                .setPeriodic(15 * 60 * 1000)
                .build();
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(info);
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d("Manish", "Job scheduled");
        } else {
            Log.d("Manish", "Job scheduling failed");
        }
    }

}
