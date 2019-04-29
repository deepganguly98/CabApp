package com.cabapp.deep;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class CollectCashActivity extends AppCompatActivity {

    private Button mAcceptCash;
    private TextView mFare, mBase, mDist, mTime;
    private String rideID,serviceType;
    private Long rideStart, time;
    DatabaseReference getRideInfo, getFareBreakUp;
    private Double distance, base, dfare, tfare, freed, freet, total,distanceValue, timeValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_cash);

        Bundle extras = getIntent().getExtras();
        rideID = extras.getString("fare");
        serviceType = extras.getString("serviceType");
        rideStart = Long.parseLong(extras.getString("rideStartTime"));



        getFareBreakUp = FirebaseDatabase.getInstance().getReference().child("Fare").child(serviceType);
        getFareBreakUp.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("Base") != null){
                        base = Double.parseDouble(map.get("Base").toString());

                    }
                    if(map.get("DFare") != null){
                        dfare = Double.parseDouble(map.get("DFare").toString());

                    }
                    if(map.get("TFare") != null){
                        tfare = Double.parseDouble(map.get("TFare").toString());

                    }
                    if(map.get("FreeD") != null){
                        freed = Double.parseDouble(map.get("FreeD").toString());

                    }
                    if(map.get("FreeT") != null){
                        freet = Double.parseDouble(map.get("FreeT").toString());

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mAcceptCash = (Button) findViewById(R.id.collect);
        mFare = (TextView) findViewById(R.id.amount);
        mBase = (TextView) findViewById(R.id.baseFare);
        mDist = (TextView) findViewById(R.id.distFare);
        mTime = (TextView) findViewById(R.id.timeFare);


        getRideInfo = FirebaseDatabase.getInstance().getReference().child("History").child(rideID);
        getRideInfo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("distance") != null){

                        distance = Double.parseDouble(map.get("distance").toString());

                        if((distance - freed) < 0.0)
                            distanceValue = 0.0;
                        else
                            distanceValue = (distance - freed);


                    }
                    if(map.get("timestamp") != null){

                        time = Long.parseLong(map.get("timestamp").toString());
                        timeValue = (double)((time - rideStart)/60)%60;

                        if((timeValue - freet) < 0)
                            timeValue = 0.0;
                        else
                            timeValue = timeValue - freet;

                        total = Math.ceil(base+(distanceValue*dfare)+(timeValue*tfare));

                        mFare.setText("\u20B9"+total+"/-");
                        mBase.setText("Base Fare - \u20B9"+base);
                        mDist.setText("Distance  - "+distanceValue+"km");
                        mTime.setText("Time - "+timeValue+"min");
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        mAcceptCash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(CollectCashActivity.this, DriverMapActivity.class);
                startActivity(intent);
                finish();
                mFare.setText("PAY - \u20B9");
                mBase.setText("");
                mDist.setText("");
                mTime.setText("");
                DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("History").child(rideID);
                HashMap map = new HashMap();
                map.put("fare",total);
                historyRef.updateChildren(map);


            }
        });
    }
}
