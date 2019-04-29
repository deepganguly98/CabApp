package com.cabapp.deep;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistorySingleActivity extends AppCompatActivity implements OnMapReadyCallback, RoutingListener{
    private String rideId, currentUserId, customerId, driverId, userDriverOrCustomer;

    private TextView locationRide;
    private TextView distanceRide;
    private TextView dateRide;
    private TextView nameUser;
    private TextView phoneUser;
    private TextView priceRide;

    private RatingBar mRatingBar;

    private ImageView imageUser;


    private String distance;
    private Double ridePrice,baseFare=60D,distanceUnit=5D;

    private DatabaseReference historyRideInfoDb;

    private LatLng destinationLatLng, pickupLatLng;

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_single);
        polylines = new ArrayList<>();
        rideId = getIntent().getExtras().getString("rideId");

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mMapFragment.getMapAsync(this);

        locationRide = (TextView) findViewById(R.id.rideLocation);
        distanceRide = (TextView) findViewById(R.id.rideDistance);
        dateRide = (TextView) findViewById(R.id.rideDate);
        nameUser = (TextView) findViewById(R.id.userName);
        phoneUser = (TextView) findViewById(R.id.userPhone);
        priceRide = (TextView) findViewById(R.id.ridePrice);

        imageUser = (ImageView) findViewById(R.id.userImage);

        mRatingBar = (RatingBar) findViewById(R.id.ratingBar);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        historyRideInfoDb = FirebaseDatabase.getInstance().getReference().child("History").child(rideId);

        getRideInfo();
    }

    private void getRideInfo() {

        historyRideInfoDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot child:dataSnapshot.getChildren()){

                        if(child.getKey().equals("customer")){

                            customerId = child.getValue().toString();

                            if(!customerId.equals(currentUserId)){

                                userDriverOrCustomer = "Riders";

                                getUserInformation("Customers",customerId);
                            }
                        }

                        if(child.getKey().equals("driver")){

                            driverId = child.getValue().toString();

                            if(!driverId.equals(currentUserId)){

                                userDriverOrCustomer = "Customers";
                                getUserInformation("Riders",driverId);
                                displayCustomerRelatedObjects();
                            }
                        }
                        if(child.getKey().equals("timestamp")){

                            dateRide.setText(getDate(Long.valueOf(child.getValue().toString())));
                        }
                        if(child.getKey().equals("rating")){

                            mRatingBar.setRating(Integer.valueOf(child.getValue().toString()));
                        }
                        if(child.getKey().equals("distance")){
                            distance = child.getValue().toString();
                            distanceRide.setText(distance.substring(0,5)+" km");
                            //ridePrice = baseFare + distanceUnit*(Double.valueOf(distance));
                           // priceRide.setText("Rs."+Math.round(ridePrice)+"");
                        }
                        if(child.getKey().equals("fare")){
                            ridePrice = Double.parseDouble(child.getValue().toString());
                            priceRide.setText("\u20B9"+ridePrice+"/-");

                        }
                        if(child.getKey().equals("destination")){

                            locationRide.setText(child.getValue().toString());
                        }
                        if(child.getKey().equals("location")) {
                            pickupLatLng = new LatLng(Double.valueOf(child.child("from").child("lat").getValue().toString()),Double.valueOf(child.child("from").child("lng").getValue().toString()));
                            destinationLatLng = new LatLng(Double.valueOf(child.child("to").child("lat").getValue().toString()),Double.valueOf(child.child("to").child("lng").getValue().toString()));
                            if(destinationLatLng!=new LatLng(0,0)){

                                getRouteToMarker();
                            }
                        }
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void displayCustomerRelatedObjects() {
        mRatingBar.setVisibility(View.VISIBLE);
        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean b) {
                historyRideInfoDb.child("rating").setValue(rating);
                DatabaseReference mDriverRatingDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Riders").child(driverId).child("rating");
                mDriverRatingDb.child(rideId).setValue(rating);
            }
        });
    }

    private void getUserInformation(String otherUserDriverOrCustomer, String otherUserId) {

        DatabaseReference mOtherUserDB = FirebaseDatabase.getInstance().getReference().child("Users").child(otherUserDriverOrCustomer).child(otherUserId);
        mOtherUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Map<String,Object> map = (Map<String,Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){

                        nameUser.setText(map.get("name").toString());
                    }
                    if(map.get("phone")!=null){

                        phoneUser.setText(map.get("phone").toString());
                    }
                    if(map.get("profileImageUrl")!=null){

                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(imageUser);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String getDate(Long time) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(time*1000);
        String date = DateFormat.format("MM-dd-yyyy hh:mm", cal).toString();
        return date;
    }

    private void getRouteToMarker() {

        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(pickupLatLng,destinationLatLng)
                .build();
        routing.execute();
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;

    }

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};
    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(pickupLatLng);
        builder.include(destinationLatLng);
        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int padding = (int) (width*0.2);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,padding);

        mMap.animateCamera(cameraUpdate);

        mMap.addMarker(new MarkerOptions().position(pickupLatLng).title("pickup location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));
        mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("destination location"));


        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingCancelled() {

    }
    private void erasePolyLines(){
        for(Polyline line : polylines){
            line.remove();

        }
        polylines.clear();
    }
}
