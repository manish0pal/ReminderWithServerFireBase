package com.learnado;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;

public class TestJobService extends JobService {
    private boolean jobCancelled = false;
    String TAG = "Manish";
    @Override
    public boolean onStartJob(JobParameters params) {
        doBackground(params);
        return true;
    }
    private void doBackground(JobParameters params){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    Log.d(TAG, "run: " + i);
                    if (jobCancelled) {
                        return;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "Job finished");
                Log.d("Manish", "Charging");
                FirebaseDatabase.getInstance("https://testcode-22ae5-default-rtdb.firebaseio.com/").getReference("Tokens").child("manish").setValue("job Service");

            }
        }).start();
             jobFinished(params,false);  //job finished
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        jobCancelled = true;
        return true;
    }
}
