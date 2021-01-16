package com.learnado.reminder;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.learnado.R;
import com.learnado.SendNotificationPack.Data;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MainPage extends AppCompatActivity {

    private FloatingActionButton add;
    private Dialog dialog;
    private AppDatabase appDatabase;
    private RecyclerView recyclerView;
    private AdapterReminders adapter;
    private List<Reminders> temp;
    private TextView empty;
    TextToSpeech toSpeech1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        appDatabase = AppDatabase.geAppdatabase(MainPage.this);

        add = findViewById(R.id.floatingButton);
        empty = findViewById(R.id.empty);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              //  FirebaseDatabase.getInstance("https://testcode-22ae5-default-rtdb.firebaseio.com/").getReference("Tokens").child("manish").setValue("job Service");

                addReminder();
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainPage.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        setItemsInRecyclerView();
        try{
            saveinDataBase();
        }catch (Exception e){

        }

        //texttospeech
        toSpeech1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    toSpeech1.setLanguage(Locale.UK);
                }
            }
        });

    }

    public void addReminder(){

        dialog = new Dialog(MainPage.this);
        dialog.setContentView(R.layout.floating_popup);

        final TextView textView = dialog.findViewById(R.id.date);
        Button select,add;
        select = dialog.findViewById(R.id.selectDate);
        add = dialog.findViewById(R.id.addButton);
        final EditText message = dialog.findViewById(R.id.message);


        final Calendar newCalender = Calendar.getInstance();
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(MainPage.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, final int year, final int month, final int dayOfMonth) {

                        final Calendar newDate = Calendar.getInstance();
                        Calendar newTime = Calendar.getInstance();
                        TimePickerDialog time = new TimePickerDialog(MainPage.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                                newDate.set(year,month,dayOfMonth,hourOfDay,minute,0);
                                Log.d("Manish","Y "+year+" M "+month+" D "+dayOfMonth+" H "+hourOfDay+" M "+minute);
                                Calendar tem = Calendar.getInstance();
                                Log.w("TIME", System.currentTimeMillis()+"");
                                if(newDate.getTimeInMillis()-tem.getTimeInMillis()>0)
                                    textView.setText(newDate.getTime().toString());
                                else
                                    Toast.makeText(MainPage.this,"Invalid time", Toast.LENGTH_SHORT).show();

                            }
                        },newTime.get(Calendar.HOUR_OF_DAY),newTime.get(Calendar.MINUTE),true);
                        time.show();

                    }
                },newCalender.get(Calendar.YEAR),newCalender.get(Calendar.MONTH),newCalender.get(Calendar.DAY_OF_MONTH));

                dialog.getDatePicker().setMinDate(System.currentTimeMillis());
                dialog.show();

            }
        });


        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RoomDAO roomDAO = appDatabase.getRoomDAO();
                Reminders reminders = new Reminders();
                reminders.setMessage(message.getText().toString().trim());
                Date remind = new Date(textView.getText().toString().trim());

                Log.d("TAG","date: "+remind);


                reminders.setRemindDate(remind);
                roomDAO.Insert(reminders);
                List<Reminders> l = roomDAO.getAll();
                reminders = l.get(l.size()-1);
                Log.e("ID chahiye",reminders.getId()+"");

                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+5:30"));
                calendar.setTime(remind);
                calendar.set(Calendar.SECOND,0);
                Intent intent = new Intent(MainPage.this, NotifierAlarm.class);
                intent.putExtra("Message",reminders.getMessage());
                intent.putExtra("RemindDate",reminders.getRemindDate().toString());
                intent.putExtra("id",reminders.getId());
                PendingIntent intent1 = PendingIntent.getBroadcast(MainPage.this,reminders.getId(),intent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),intent1);

                Toast.makeText(MainPage.this,"Inserted Successfully", Toast.LENGTH_SHORT).show();
                setItemsInRecyclerView();
                AppDatabase.destroyInstance();
                dialog.dismiss();

            }
        });


        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }

    public void setItemsInRecyclerView(){

        RoomDAO dao = appDatabase.getRoomDAO();
        temp = dao.orderThetable();
        if(temp.size()>0) {
            empty.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        adapter = new AdapterReminders(temp);
        recyclerView.setAdapter(adapter);

    }
    public  void saveinDataBase(){
        final String Message;
        final String TimingForReminder;
        Log.d("Manish", "saveinDataBase1");
        DatabaseReference databaseReference =   FirebaseDatabase.getInstance("https://testcode-22ae5-default-rtdb.firebaseio.com/").getReference("Tokens").child("manish");


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String year= ""+snapshot.child("year").getValue();
                String month= ""+snapshot.child("month").getValue();
                String day= ""+snapshot.child("day").getValue();
                String hours= ""+snapshot.child("hours").getValue();
                String min= ""+snapshot.child("min").getValue();

                Log.d("Manish", "time: "+year);
                    Calendar firebaseT = Calendar.getInstance();
                    firebaseT.set(Integer.parseInt(year),Integer.parseInt(month),Integer.parseInt(day),Integer.parseInt(hours),Integer.parseInt(min),0);
                Log.d("Manish", "time: 1 "+firebaseT.getTime());
                Calendar tem1 = Calendar.getInstance();
                //TODO change the formate time and add uid

                if(firebaseT.getTimeInMillis()-tem1.getTimeInMillis()>0) {
                    Toast.makeText(MainPage.this, "Data Updated", Toast.LENGTH_SHORT).show(); //commint it
                    setFireReminder("Firebase Message", firebaseT.getTime().toString());
                }else {
                  //  Toast.makeText(MainPage.this, "Invalid time", Toast.LENGTH_SHORT).show();
                }



            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Manish", String.valueOf(databaseError));
            }
        });

        //2

        // get the details from firebase checking with email
    /*    DatabaseReference databaseReference1 =   FirebaseDatabase.getInstance("https://testcode-22ae5-default-rtdb.firebaseio.com/").getReference("Tokens");

        Query query=databaseReference1.orderByChild("uid").equalTo("1");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    //get data
                    String year= ""+ds.child("year").getValue();
                    String month= ""+ds.child("month").getValue();
                    String day= ""+ds.child("day").getValue();

                Log.d("Manish",year+" "+month+" day "+day);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Manish","q: "+ String.valueOf(databaseError));
            }
        });*/
    }
    public void setFireReminder(String msg,String timeonnotification){
        Log.d("Manish","setFireReminder");
        RoomDAO roomDAO = appDatabase.getRoomDAO();
        Reminders reminders = new Reminders();
        reminders.setMessage(msg);
        Date remind = new Date(timeonnotification);
        reminders.setRemindDate(remind);
        roomDAO.Insert(reminders);
        List<Reminders> l = roomDAO.getAll();
        reminders = l.get(l.size()-1);
        Log.e("ID chahiye",reminders.getId()+"");

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+5:30"));
        calendar.setTime(remind);
        calendar.set(Calendar.SECOND,0);
        Intent intent = new Intent(MainPage.this, NotifierAlarm.class);
        intent.putExtra("Message",reminders.getMessage());
        intent.putExtra("RemindDate",reminders.getRemindDate().toString());
        intent.putExtra("id",reminders.getId());
        PendingIntent intent1 = PendingIntent.getBroadcast(MainPage.this,reminders.getId(),intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),intent1);

        Toast.makeText(MainPage.this,"Inserted Successfully", Toast.LENGTH_SHORT).show();
        setItemsInRecyclerView();
        AppDatabase.destroyInstance();
    }
    public void textToSpeech(View view){



        toSpeech1.speak("Ajay",TextToSpeech.QUEUE_FLUSH,null);
        Toast.makeText(this, "speak", Toast.LENGTH_SHORT).show();
    }

}
