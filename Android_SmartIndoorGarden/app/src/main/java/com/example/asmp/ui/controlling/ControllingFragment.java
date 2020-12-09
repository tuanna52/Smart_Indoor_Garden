package com.example.asmp.ui.controlling;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.asmp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ControllingFragment extends Fragment {

    private CheckBox mSoilAuto, mLightAuto, mPump, mLed;

    private TextView mIndex;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference soilRef = database.getReference("pump").child("status");
    DatabaseReference lightRef = database.getReference("led").child("status");
    DatabaseReference pumpRef = database.getReference("pump").child("activation");
    DatabaseReference ledRef = database.getReference("led").child("activation");

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_controlling, container, false);

        mSoilAuto = root.findViewById(R.id.soil_auto);
        mLightAuto = root.findViewById(R.id.light_auto);
        mPump = root.findViewById(R.id.pump);
        mLed = root.findViewById(R.id.led);

        mIndex = getActivity().findViewById(R.id.Index);
        mIndex.setVisibility(View.INVISIBLE);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Read from the database
        soilRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer soilStatus = dataSnapshot.getValue(Integer.class);
                if(soilStatus == 0) {
                    mSoilAuto.setChecked(true);
                    mPump.setChecked(false);
                }
                else {
                    mSoilAuto.setChecked(false);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Fail_To_Read_Database: ", "Failed to read value.", error.toException());
            }
        });
        lightRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer lightStatus = dataSnapshot.getValue(Integer.class);
                if(lightStatus == 0) {
                    mLightAuto.setChecked(true);
                    mLed.setChecked(false);
                }
                else {
                    mLightAuto.setChecked(false);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Fail_To_Read_Database: ", "Failed to read value.", error.toException());
            }
        });
        pumpRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer pumpActivation = dataSnapshot.getValue(Integer.class);
                mPump.setChecked(pumpActivation == 1);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Fail_To_Read_Database: ", "Failed to read value.", error.toException());
            }
        });
        ledRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer ledActivation = dataSnapshot.getValue(Integer.class);
                mLed.setChecked(ledActivation == 1);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Fail_To_Read_Database: ", "Failed to read value.", error.toException());
            }
        });

        mSoilAuto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    soilRef.setValue(0);
                    pumpRef.setValue(0);
                }
                else {
                    soilRef.setValue(1);
                }
            }
        });

        mPump.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    pumpRef.setValue(1);
                    mSoilAuto.setChecked(false);
                }
                else {
                    pumpRef.setValue(0);
                }
            }
        });

        mLightAuto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    lightRef.setValue(0);
                    ledRef.setValue(0);
                }
                else {
                    lightRef.setValue(1);
                }
            }
        });

        mLed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    ledRef.setValue(1);
                    mLightAuto.setChecked(false);
                }
                else {
                    ledRef.setValue(0);
                }
            }
        });

    }

}
