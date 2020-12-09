package com.example.asmp.ui.monitoring;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.example.asmp.MainActivity;
import com.example.asmp.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MonitoringFragment extends Fragment {
    String CHANNEL_ID = "garden_channel";

    int notificationId0 = 0;

    private TextView mTemp, mHumid, mMoisture, mLight;
    private TextView mIndex;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference dbRef = database.getReference("data");

    //final ObservableDouble obsTemp = new ObservableDouble();

    //double tempObs = 0;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_monitoring, container, false);
        createNotificationChannel();

        mTemp = root.findViewById(R.id.temperature_value);
        mHumid = root.findViewById(R.id.humidity_value);
        mMoisture = root.findViewById(R.id.moisture_value);
        mLight = root.findViewById(R.id.light_value);

        mIndex = getActivity().findViewById(R.id.Index);
        mIndex.setVisibility(View.VISIBLE);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Read from the database
        dbRef.limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot ds, String previousChildName) {
                Double Humid = ds.child("humidity").getValue(Double.class);
                Double Temp = ds.child("temperature").getValue(Double.class);
                Integer Light = ds.child("light").getValue(Integer.class);
                String Moisture = ds.child("moisture").getValue(String.class);

                mHumid.setText(String.format("%.2f", Humid));
                mTemp.setText(String.format("%.2f", Temp));
                mLight.setText(Integer.toString(Light));
                mMoisture.setText(Moisture);

                try {
                    if(Moisture.equals("Dry")) addNotification();
                }
                catch(Exception e) {
                    System.out.println("Something went wrong.");
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Fail_Test: ", "Failed to read value.", error.toException());
            }
        });

    }

    // Create the channel for notification
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Create a notification when the co2 concentration exceed 2000 ppm or below 800ppm
    private void addNotification() {
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getActivity(), CHANNEL_ID)
                .setSmallIcon(R.drawable.warning)
                .setContentTitle("Warning: ")
                .setContentText("The soil is too dry now!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity());

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId0, mBuilder.build());
    }
}

