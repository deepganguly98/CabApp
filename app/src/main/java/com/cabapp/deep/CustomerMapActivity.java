package com.cabapp.deep;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.renderscript.Sampler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
/*import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;*/
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import Modules.DirectionFinder;
import Modules.DirectionFinderListener;
import Modules.Route;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback,/*RoutingListener,*/DirectionFinderListener, OnItemSelectedListener {

    private GoogleMap mMap;

    Location mLastLocation;
    LocationRequest mLocationRequest;

    private Boolean requestBol = false;
    private  LatLng pickupLocation;

    private FusedLocationProviderClient mFusedLocationClient;

    private Marker pickupMarker = null, destinationMarker = null;

    private Button mLogout, mRequest, mSettings, mHistory,mCancel, mClosePredictbar, mBookRide;
    SupportMapFragment mapFragment;

    private String destination, requestService, paymentMethod="";

    private LatLng destinationLatLng;

    private FloatingActionButton mFloatingButton;

    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};
    private LinearLayout mDriverInfo, mPredictFare;
    private ImageView mDriverProfileImage, mCabPredict, mBikePredict, mAutoPredict;

    private TextView mDriverName, mDriverPhone, mDriverCar, mDriverLicense, mVehicleTitle1,mVehicleTitle2, mVehicleTitle3, mJourneyTime, mPFareC, mPFareB , mPFareA;

    private RadioGroup mRadioGroup;

    private RatingBar mRatingBar;

    private int radius = 1;
    private Boolean driverFound = false;
    private String driverFoundID;

    GeoQuery geoQuery;

    private List<Polyline> polylines;

    private Spinner mPayment;

    double mPredictedDistance=0.0, mPredictedDuration=0.0;

    private PlaceAutocompleteFragment autocompleteFragment, pickupcompleteFragment;

    DatabaseReference mCabFare,mBikeFare, mAutoFare;
    ValueEventListener mCabFareChange, mBikeFareChange, mAutoFareChange;
    private ProgressDialog Dialog ;

    private ProgressDialog progressDialog;

    String ori = "",end="";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);

        Dialog = new ProgressDialog(CustomerMapActivity.this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        /* setUpMapIfNeeded(); */
        mapFragment.getMapAsync(this);

        //statusCheck();
        mCabFare = FirebaseDatabase.getInstance().getReference().child("Fare").child("Cab");
        mBikeFare = FirebaseDatabase.getInstance().getReference().child("Fare").child("Bike");
        mAutoFare = FirebaseDatabase.getInstance().getReference().child("Fare").child("Auto");


        polylines = new ArrayList<>();



        destinationLatLng = new LatLng(0.0,0.0);

        mLogout = (Button) findViewById(R.id.logout);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(CustomerMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        pickupLocation = null;
        pickupcompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.location_autocomplete);

        pickupcompleteFragment.setHint("Set pickup location");

        //if(mLastLocation != null)
            pickupcompleteFragment.setBoundsBias(box);

        pickupcompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                pickupLocation = place.getLatLng();
                /*
                if(destinationLatLng != null)
                    sendRequest(pickupLocation,destinationLatLng);
                else
                    ;*/
                    //getRouteToMarker(pickupLocation,destinationLatLng);
                pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup Here!").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));
                destinationMarker = mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("Pickup Here!").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_drop)));
                //mPFareA.setText(getAutoPredictedFare(mPredictedDistance,mPredictedDuration));
                //mPFareB.setText(getBikePredictedFare(mPredictedDistance,mPredictedDuration));
                //mPFareC.setText(getCabPredictedFare(mPredictedDistance,mPredictedDuration));

            }

            @Override
            public void onError(Status status) {

            }
        });
        pickupcompleteFragment.getView().findViewById(R.id.place_autocomplete_clear_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        erasePolyLines();
                        pickupcompleteFragment.setText("");
                        pickupMarker.remove();
                        destinationMarker.remove();

                    }
                });


        mCancel = (Button) findViewById(R.id.cancel);
        mRequest = (Button) findViewById(R.id.request);
        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                    if(requestBol) {
                        //endRide();
                    }
                    else{
                        /*
                        int selectId = mRadioGroup.getCheckedRadioButtonId();

                        final RadioButton radioButton = (RadioButton) findViewById(selectId);

                        if(radioButton.getText() == null)   {

                            return;
                        }

                        requestService = radioButton.getText().toString();*/
                        if(destinationLatLng.longitude==0.0 && destinationLatLng.latitude==0.0){
                            Toast.makeText(getApplicationContext(), "Please select a destination",Toast.LENGTH_LONG).show();

                        }
                        else {
                            requestBol = true;
                            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");

                            //if(pickupcompleteFragment.toString().equals(null)) {
                            GeoFire geoFire = new GeoFire(ref);
                            geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                            if (pickupLocation == null) {
                                pickupLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                            }
                            pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup Here!").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));
                            destinationMarker = mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("Pickup Here!").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_drop)));
                            //mRequest.setText("Looking for drivers...");



                            //getDistanceInfo(pickupLocation,destinationLatLng);

                            //getRouteToMarker(pickupLocation, destinationLatLng);
                            sendRequest(pickupLocation,destinationLatLng);

                            //getAutoPredictedFare(mPredictedDistance,mPredictedDuration);
                            //getBikePredictedFare(mPredictedDistance,mPredictedDuration);
                            //getCabPredictedFare(mPredictedDistance,mPredictedDuration);
                            mPredictFare.setVisibility(View.VISIBLE);
                            mBookRide.setVisibility(View.VISIBLE);
                            mPayment.setVisibility(View.VISIBLE);
                            //mRadioGroup.setVisibility(View.VISIBLE);
                            mRequest.setVisibility(View.GONE);

                            mBookRide.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    getClosestDriver();
                                    mBookRide.setVisibility(View.GONE);
                                    mCancel.setVisibility(View.VISIBLE);
                                    mPredictFare.setVisibility(View.GONE);
                                    mPayment.setVisibility(View.GONE);
                                    mRadioGroup.setVisibility(View.GONE);
                                }
                            });

                        }





                    }

            }
        });
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCancel.setVisibility(View.GONE);
                endRide();
                mRequest.setVisibility(View.VISIBLE);
                destinationMarker.remove();
                mMap.clear();

            }
        });
        mHistory = (Button) findViewById(R.id.history);
        mSettings = (Button) findViewById(R.id.settings);
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CustomerMapActivity.this, CustomerSettingsActivity.class);
                startActivity(intent);
                return;
            }
        });

        mHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CustomerMapActivity.this, HistoryActivity.class);
                intent.putExtra("customerOrDriver","Customers");
                startActivity(intent);
                return;
            }
        });

        mFloatingButton = (FloatingActionButton) findViewById(R.id.fab);
        mFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mLastLocation != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                }
                else{

                    ;
                }
            }
        });
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setHint("Where to ?");
        //if(mLastLocation != null)
            autocompleteFragment.setBoundsBias(box);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                if(pickupLocation == null)
                    pickupLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                destination = place.getName().toString();
                destinationLatLng = place.getLatLng();
                //getRouteToMarker(pickupLocation,destinationLatLng);

                pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup Here!").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));
                destinationMarker = mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("Pickup Here!").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_drop)));
                sendRequest(pickupLocation,destinationLatLng);
                //mPFareA.setText(getAutoPredictedFare(mPredictedDistance,mPredictedDuration));
                //mPFareB.setText(getBikePredictedFare(mPredictedDistance,mPredictedDuration));
                //mPFareC.setText(getCabPredictedFare(mPredictedDistance,mPredictedDuration));
            }

            @Override
            public void onError(Status status) {

            }


        });
        autocompleteFragment.getView().findViewById(R.id.place_autocomplete_clear_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        erasePolyLines();
                        autocompleteFragment.setText("");
                        pickupMarker.remove();
                        destinationMarker.remove();
                    }
                });




        mDriverInfo = (LinearLayout) findViewById(R.id.driverInfo);
        mDriverProfileImage = (ImageView) findViewById(R.id.driverProfileImage);
        mDriverName = (TextView) findViewById(R.id.driverName);
        mDriverPhone = (TextView) findViewById(R.id.driverPhone);
        mDriverCar = (TextView) findViewById(R.id.driverCar);
        mDriverLicense = (TextView) findViewById(R.id.driverLicense);
        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        mRadioGroup.check(R.id.cab);
        mRatingBar = (RatingBar) findViewById(R.id.ratingBar);


        mVehicleTitle1 = (TextView) findViewById(R.id.title1);
        mVehicleTitle1.setTextColor(Color.rgb(66,134,244));
        mVehicleTitle2 = (TextView) findViewById(R.id.title2);
        mVehicleTitle3 = (TextView) findViewById(R.id.title3);
        mJourneyTime = (TextView) findViewById(R.id.expDuration);
        mPFareC = (TextView) findViewById(R.id.pFareC);
        mPFareB = (TextView) findViewById(R.id.pFareB);
        mPFareA = (TextView) findViewById(R.id.pFareA);
        mPFareA.setVisibility(View.GONE);
        mPFareB.setVisibility(View.GONE);
        mPFareC.setVisibility(View.GONE);

        mBookRide = (Button) findViewById(R.id.bookRide);
        mPredictFare = (LinearLayout) findViewById(R.id.predictFare);
        mCabPredict = (ImageView) findViewById(R.id.cabPredict);
        mCabPredict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestService="Cab";
                mRadioGroup.check(R.id.cab);
                mPFareC.setVisibility(View.VISIBLE);
                //Dialog.show();
                mCabPredict.setImageResource(R.mipmap.ic_cab_selected);
                mBikePredict.setImageResource(R.mipmap.ic_bike);
                mAutoPredict.setImageResource(R.mipmap.ic_auto);
                mVehicleTitle1.setTextColor(Color.rgb(66,134,244));
                mVehicleTitle2.setTextColor(Color.rgb(0,0,0));
                mVehicleTitle3.setTextColor(Color.rgb(0,0,0));
                getCabPredictedFare(mPredictedDuration,mPredictedDistance);
                //Dialog.show();
                mPFareB.setVisibility(View.GONE);
                mPFareA.setVisibility(View.GONE);


            }
        });
        mBikePredict = (ImageView) findViewById(R.id.bikePredict);
        mBikePredict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestService="Bike";
                mRadioGroup.check(R.id.bike);
                mPFareB.setVisibility(View.VISIBLE);
                //Dialog.show();
                mCabPredict.setImageResource(R.mipmap.ic_cab);
                mBikePredict.setImageResource(R.mipmap.ic_bike_selected);
                mAutoPredict.setImageResource(R.mipmap.ic_auto);
                mVehicleTitle2.setTextColor(Color.rgb(66,134,244));
                mVehicleTitle1.setTextColor(Color.rgb(0,0,0));
                mVehicleTitle3.setTextColor(Color.rgb(0,0,0));
                getBikePredictedFare(mPredictedDuration,mPredictedDistance);
                //Dialog.show();
                mPFareA.setVisibility(View.GONE);
                mPFareC.setVisibility(View.GONE);

            }
        });
        mAutoPredict = (ImageView) findViewById(R.id.autoPredict);
        mAutoPredict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestService="Auto";
                mRadioGroup.check(R.id.auto);
                mPFareA.setVisibility(View.VISIBLE);
                //Dialog.show();
                mCabPredict.setImageResource(R.mipmap.ic_cab);
                mBikePredict.setImageResource(R.mipmap.ic_bike);
                mAutoPredict.setImageResource(R.mipmap.ic_auto_selected);
                mVehicleTitle3.setTextColor(Color.rgb(66,134,244));
                mVehicleTitle1.setTextColor(Color.rgb(0,0,0));
                mVehicleTitle2.setTextColor(Color.rgb(0,0,0));
                getAutoPredictedFare(mPredictedDuration,mPredictedDistance);

                mPFareB.setVisibility(View.GONE);
                mPFareC.setVisibility(View.GONE);


            }
        });
        mClosePredictbar = (Button) findViewById(R.id.closePredictBar);
        mClosePredictbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();
                mPFareB.setText("");
                mPFareA.setText("");
                mPFareC.setText("");
                autocompleteFragment.setText("");
                pickupcompleteFragment.setText("");
                destinationLatLng = new LatLng(0.0,0.0);
                pickupLocation = null;
                mBookRide.setVisibility(View.GONE);
                mPayment.setVisibility(View.GONE);
                mRequest.setVisibility(View.VISIBLE);
                mPredictFare.setVisibility(View.GONE);
                mRadioGroup.setVisibility(View.GONE);
                erasePolyLines();
                requestBol = false;
                //geoQuery.removeAllListeners();
                if(driverLocationRef != null) {
                    driverLocationRef.removeEventListener(driverLocationRefListener);
                }
                if(driveHasEndedRef !=null) {
                    driveHasEndedRef.removeEventListener(driverLocationRefListener);
                }

                if(driverFoundID != null){

                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Riders").child(driverFoundID).child("customerRequest");
                    driverRef.removeValue();
                    driverFoundID = null;
                }


                driverFound = false;
                radius = 1;
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("customerRequest");
                GeoFire geoFire = new GeoFire(ref);
                geoFire.removeLocation(userId);

                //if(pickupMarker != null && destinationMarker != null){

                    pickupMarker.remove();
                    destinationMarker.remove();
                //}
                if (mDriverMarker != null ){
                    mDriverMarker.remove();

                }
                mRequest.setText("BOOK A CAB");
                mDriverInfo.setVisibility(View.GONE);
                mDriverPhone.setText("");
                mDriverName.setText("");
                mDriverCar.setText("");
                mDriverLicense.setText("");
                mDriverProfileImage.setImageResource(R.mipmap.ic_default_user);

                if(mCabFare != null)
                    mCabFare.removeEventListener(mCabFareChange);
                mVehicleTitle1.setText("CAB");

            }
        });

        mPayment = (Spinner) findViewById(R.id.paymentMethod);
        mPayment.setOnItemSelectedListener(this);
        List<String> categories = new ArrayList<String>();
        categories.add("Cash");
        categories.add("PayTM");


        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        mPayment.setAdapter(dataAdapter);


    }

    private void sendRequest(LatLng pickupLocation,LatLng destinationLatLng) {
        String origin = pickupLocation.latitude+","+pickupLocation.longitude;
        String destinationx = destinationLatLng.latitude+","+destinationLatLng.longitude;

        if (origin.isEmpty()) {
            Toast.makeText(this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            new DirectionFinder(this, origin, destinationx).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private double base, dfare, tfare, freed, freet, cancel, buff, finalFare=0.0;
    private void getCabPredictedFare(double distance, double time){
        //base = dfare = tfare = freed = freet = cancel = buff =0.0;

        //finalFare = 0.0 ;
        Dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        Dialog.setIndeterminate(true);
        Dialog.setCancelable(false);
        Dialog.setMessage("Fetching Fare...");
        //Dialog.show();

        mCabFareChange = mCabFare.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Dialog.show();
                    Map<String,Object> cabFare = (Map<String,Object>)dataSnapshot.getValue();
                    if(cabFare.get("Base") != null){
                        //Toast.makeText(getApplicationContext(),"Base fare = "+cabFare.get("Base").toString(),Toast.LENGTH_LONG).show();
                        //mVehicleTitle1.append("\nBase Fare = "+cabFare.get("Base").toString());
                        base = Double.parseDouble(cabFare.get("Base").toString());
                    }
                    if(cabFare.get("DFare") != null){

                        dfare = Double.parseDouble(cabFare.get("DFare").toString());
                    }
                    if(cabFare.get("TFare") != null){

                        tfare = Double.parseDouble(cabFare.get("TFare").toString());
                    }
                    if(cabFare.get("FreeD") != null){

                        freed = Double.parseDouble(cabFare.get("FreeD").toString());
                    }
                    if(cabFare.get("FreeT") != null){

                        freet = Double.parseDouble(cabFare.get("FreeT").toString());
                    }
                    if(cabFare.get("Cancel") != null){

                        cancel = Double.parseDouble(cabFare.get("Cancel").toString());
                    }
                    if(cabFare.get("Buff") != null){

                        buff = 100+(Double.parseDouble(cabFare.get("Buff").toString()));
                        double distancevalue = (mPredictedDistance - freed)*dfare;
                        double timevalue = (mPredictedDuration - freet)*tfare;

                        //Toast.makeText(getApplicationContext(),"Distance - "+mPredictedDistance+"km\nTime - "+mPredictedDuration+"s",Toast.LENGTH_LONG).show();

                        finalFare = Math.ceil((base+distancevalue+timevalue)*buff)/100;
                        mPFareC.setText("\u20B9 "+ finalFare+"/-");

                        Dialog.hide();

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Dialog.hide();

            }
        });

        finalFare=0.0;
        //mPFareB.setText("");
        //mPFareA.setText("");

        //return  Double.valueOf(finalFare).toString();



    }
    private void getBikePredictedFare(double distance, double time){
        //base = dfare = tfare = freed = freet = cancel = buff =0.0;

        //finalFare = 0.0 ;
        Dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        Dialog.setIndeterminate(true);
        Dialog.setCancelable(false);
        Dialog.setMessage("Fetching Fare...");
        //Dialog.show();


        mBikeFareChange = mBikeFare.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Dialog.show();
                    Map<String,Object> bikeFare = (Map<String,Object>)dataSnapshot.getValue();
                    if(bikeFare.get("Base") != null){
                        //Toast.makeText(getApplicationContext(),"Base fare = "+cabFare.get("Base").toString(),Toast.LENGTH_LONG).show();
                        //mVehicleTitle1.append("\nBase Fare = "+cabFare.get("Base").toString());
                        base = Double.parseDouble(bikeFare.get("Base").toString());
                    }
                    if(bikeFare.get("DFare") != null){

                        dfare = Double.parseDouble(bikeFare.get("DFare").toString());
                    }
                    if(bikeFare.get("TFare") != null){

                        tfare = Double.parseDouble(bikeFare.get("TFare").toString());
                    }
                    if(bikeFare.get("FreeD") != null){

                        freed = Double.parseDouble(bikeFare.get("FreeD").toString());
                    }
                    if(bikeFare.get("FreeT") != null){

                        freet = Double.parseDouble(bikeFare.get("FreeT").toString());
                    }
                    if(bikeFare.get("Cancel") != null){

                        cancel = Double.parseDouble(bikeFare.get("Cancel").toString());
                    }
                    if(bikeFare.get("Buff") != null){

                        buff = 100+(Double.parseDouble(bikeFare.get("Buff").toString()));
                        double distancevalue = (mPredictedDistance - freed)*dfare;
                        double timevalue = (mPredictedDuration - freet)*tfare;
                        //Toast.makeText(getApplicationContext(),"Distance - "+mPredictedDistance+"km\nTime - "+mPredictedDuration+"s",Toast.LENGTH_LONG).show();

                        finalFare = Math.ceil((base+distancevalue+timevalue)*buff)/100;
                        mPFareB.setText("\u20B9"+ finalFare+"/-");
                        Dialog.hide();
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Dialog.hide();

            }
        });

        finalFare=0.0;
        //mPFareC.setText("");
        //mPFareA.setText("");

        //return  Double.valueOf(finalFare).toString();
    }
    private void getAutoPredictedFare(double distance, double time){
        //base = dfare = tfare = freed = freet = cancel = buff =0.0;
        //finalFare = 0.0 ;
        Dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        Dialog.setIndeterminate(true);
        Dialog.setCancelable(false);
        Dialog.setMessage("Fetching Fare...");

        mAutoFareChange = mAutoFare.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Dialog.show();
                    Map<String,Object> autoFare = (Map<String,Object>)dataSnapshot.getValue();
                    if(autoFare.get("Base") != null){
                        //Toast.makeText(getApplicationContext(),"Base fare = "+cabFare.get("Base").toString(),Toast.LENGTH_LONG).show();
                        //mVehicleTitle1.append("\nBase Fare = "+cabFare.get("Base").toString());
                        base = Double.parseDouble(autoFare.get("Base").toString());
                    }
                    if(autoFare.get("DFare") != null){

                        dfare = Double.parseDouble(autoFare.get("DFare").toString());
                    }
                    if(autoFare.get("TFare") != null){

                        tfare = Double.parseDouble(autoFare.get("TFare").toString());
                    }
                    if(autoFare.get("FreeD") != null){

                        freed = Double.parseDouble(autoFare.get("FreeD").toString());
                    }
                    if(autoFare.get("FreeT") != null){

                        freet = Double.parseDouble(autoFare.get("FreeT").toString());
                    }
                    if(autoFare.get("Cancel") != null){

                        cancel = Double.parseDouble(autoFare.get("Cancel").toString());
                    }
                    if(autoFare.get("Buff") != null){

                        buff = 100+(Double.parseDouble(autoFare.get("Buff").toString()));
                        double distancevalue = (mPredictedDistance - freed)*dfare;
                        double timevalue = (mPredictedDuration - freet)*tfare;
                        //Toast.makeText(getApplicationContext(),"Distance - "+mPredictedDistance+"km\nTime - "+mPredictedDuration+"s",Toast.LENGTH_LONG).show();

                        finalFare = Math.ceil((base+distancevalue+timevalue)*buff)/100;
                        mPFareA.setText("\u20B9"+ finalFare+"/-");
                        Dialog.hide();
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Dialog.hide();

            }
        });

        finalFare=0.0;
        //mPFareC.setText("");
        //mPFareB.setText("");

        //return  Double.valueOf(finalFare).toString();
    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

/*
    private void getRouteToMarker(LatLng pickupLocation, LatLng destinationLatLng) {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(pickupLocation,destinationLatLng)
                .build();
        routing.execute();
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));



    }*/

    private void getClosestDriver(){
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driversAvailable");

        GeoFire geoFire = new GeoFire(driverLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound && requestBol){
                    DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Riders").child(key);
                    mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                                Map<String, Object> driverMap = (Map<String, Object>) dataSnapshot.getValue();
                                if(driverFound){

                                    return;
                                }

                                if(driverMap.get("service").equals(requestService)){

                                    driverFound = true;
                                    driverFoundID = dataSnapshot.getKey();

                                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Riders").child(driverFoundID).child("customerRequest");
                                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    HashMap map = new HashMap();
                                    map.put("customerRideId", customerId);
                                    map.put("destination", destination);
                                    map.put("destinationLat", destinationLatLng.latitude);
                                    map.put("destinationLng", destinationLatLng.longitude);
                                    map.put("paymentMethod", paymentMethod);
                                    driverRef.updateChildren(map);

                                    getDriverLocation();
                                    getDriverInfo();
                                    getHasRideEnded();
                                    //mRequest.setText("Looking for Driver Location....");
                                }

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });




                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound)
                {
                    radius++;
                    getClosestDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
    private Marker mDriverMarker = null;
    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListener;
    private void getDriverLocation(){
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("driversWorking").child(driverFoundID).child("l");
        driverLocationRefListener =  driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    mRequest.setText("Driver Found");
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLng = new LatLng(locationLat,locationLng);
                    if(mDriverMarker != null){
                        mDriverMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(pickupLocation.latitude);
                    loc1.setLongitude(pickupLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(driverLatLng.latitude);
                    loc2.setLongitude(driverLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);
                    String d = String.valueOf(distance);
                    if(distance<100){
                        mRequest.setText("Driver is arriving");


                    }
                    else{

                        //mRequest.setText("Cancel Ride");
                        Toast.makeText(getApplicationContext(), "Your driver is "+d+"m away",Toast.LENGTH_LONG).show();
                    }


                    mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("your driver").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car)));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }


    private void getDriverInfo(){
        mDriverInfo.setVisibility(View.VISIBLE);
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Riders").child(driverFoundID);

        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name") != null){

                        mDriverName.setText(map.get("name").toString());

                    }
                    if(map.get("phone") != null){

                        mDriverPhone.setText(map.get("phone").toString());

                    }
                    if(map.get("car") != null){

                        mDriverCar.setText(map.get("car").toString());

                    }
                    if(map.get("license") != null){

                        mDriverLicense.setText(map.get("license").toString());

                    }


                    if(map.get("profileImageUrl") != null){

                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(mDriverProfileImage);


                    }

                    int ratingSum = 0, ratingTotal = 0;
                    float ratingAvg = 0.0f;
                    for(DataSnapshot child : dataSnapshot.child("rating").getChildren()){

                        ratingSum += Integer.valueOf(child.getValue().toString());
                        ratingTotal++;
                    }

                    if(ratingTotal!=0){

                        ratingAvg =  ratingSum/(float)ratingTotal;
                        mRatingBar.setRating(ratingAvg);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    private DatabaseReference driveHasEndedRef;
    private ValueEventListener driveHasEndedRefListener;

    private void getHasRideEnded(){

        driveHasEndedRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Riders").child(driverFoundID).child("customerRequest").child("customerRideId");
        driveHasEndedRefListener = driveHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                }else{
                        endRide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void endRide(){
        erasePolyLines();
        requestBol = false;
        geoQuery.removeAllListeners();
        if(driverLocationRef != null) {
            driverLocationRef.removeEventListener(driverLocationRefListener);
        }
        if(driveHasEndedRef !=null) {
            driveHasEndedRef.removeEventListener(driverLocationRefListener);
        }

        if(driverFoundID != null){

            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Riders").child(driverFoundID).child("customerRequest");
            driverRef.removeValue();
            driverFoundID = null;
        }


        driverFound = false;
        radius = 1;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);

        if(pickupMarker != null){

            pickupMarker.remove();
        }
        if (mDriverMarker != null && destinationMarker != null){
            mDriverMarker.remove();
            destinationMarker.remove();
        }
        mRequest.setText("BOOK A CAB");
        mDriverInfo.setVisibility(View.GONE);
        mDriverPhone.setText("");
        mDriverName.setText("");
        mDriverCar.setText("");
        mDriverLicense.setText("");
        mDriverProfileImage.setImageResource(R.mipmap.ic_default_user);
        mCancel.setVisibility(View.GONE);
        mRequest.setVisibility(View.VISIBLE);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            }else{
                checkLocationPermission();
            }

        }



        Date currentTime = Calendar.getInstance().getTime();
        try{

            Date d = new SimpleDateFormat("HH:mm:ss").parse("17:00:00");
            Calendar c = Calendar.getInstance();
            c.setTime(d);
            c.add(Calendar.DATE, 1);

            Date d1 = new SimpleDateFormat("HH:mm:ss").parse("4:00:00");
            Calendar c1 = Calendar.getInstance();
            c1.setTime(d1);
            c1.add(Calendar.DATE, 1);

            if (currentTime.after(c1.getTime()) && currentTime.before(c.getTime())){
                try {
                    // Customise the styling of the base map using a JSON object defined
                    // in a raw resource file.
                    boolean success = googleMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                    this, R.raw.style_default));

                    if (!success) {
                        System.out.println("Style parsing failed.");
                    }
                } catch (Resources.NotFoundException e) {
                    System.out.println("Can't find style. Error: "+ e);
                }

            }

            else{
                try {
                    // Customise the styling of the base map using a JSON object defined
                    // in a raw resource file.
                    boolean success = googleMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                    this, R.raw.style_json));

                    if (!success) {
                        System.out.println("Style parsing failed.");
                    }
                } catch (Resources.NotFoundException e) {
                    System.out.println("Can't find style. Error: "+ e);
                }
            }
        }catch (ParseException e) {
            e.printStackTrace();
        }

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);




    }

    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    private LatLngBounds box;


    public LatLngBounds getBoxBounds(){
        LatLng sw = new LatLng(0,0);
        LatLng ne = new LatLng(0,0);


        double offset = 1.0 / 1000.0;
        double latMax = mLastLocation.getLatitude() + offset;
        double latMin = mLastLocation.getLatitude() - offset;

        // With longitude, things are a bit more complex.
        // 1 degree of longitude = 111km only at equator (gradually shrinks to zero at the poles)
        // So need to take into account latitude too, using cos(lat).

        double lngOffset = offset * Math.cos(mLastLocation.getLatitude() * (Math.PI / 180.0));
        double lngMax = mLastLocation.getLongitude() + lngOffset;
        double lngMin = mLastLocation.getLongitude() - lngOffset;

        sw = new LatLng(latMin,lngMin);
        ne = new LatLng(latMax,lngMax);

        LatLngBounds box = new LatLngBounds(sw, ne);

        return box;



    }


    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult){
            for(Location location: locationResult.getLocations()){
                if(getApplicationContext()!=null){
                    mLastLocation = location;

                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                    box=getBoxBounds();
                }
            }
        }

    };



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:{
                if(grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                }else{

                    Toast.makeText(getApplicationContext(),"Please provide permission",Toast.LENGTH_LONG).show();
                }
                break;
            }

        }
    }
    @Override
    protected void onStart() {
       /* if(mGoogleApiClient!=null){

            mGoogleApiClient.connect();
        }
       checkLocationPermission();
       mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback,Looper.myLooper());
       mMap.setMyLocationEnabled(true);*/

       super.onStart();
    }

    @Override
    protected void onStop() {
       /* if(mGoogleApiClient!=null){

            if(mGoogleApiClient.isConnected()){

                mGoogleApiClient.disconnect();
            }

        }


       if(mFusedLocationClient != null){

            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
       }*/


        super.onStop();


    }
/*
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
            mPredictedDistance = route.get(i).getDistanceValue();
            mPredictedDuration = route.get(i).getDurationValue();
            //Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ mPredictedDistance +": duration - "+ mPredictedDuration,Toast.LENGTH_SHORT).show();
            mPredictedDistance = (double)mPredictedDistance/1000;
            mPredictedDuration = Math.ceil(mPredictedDuration/60);
            mJourneyTime.setText("Expected Journey Time : "+(int)mPredictedDuration+"min");
            //getCabPredictedFare(mPredictedDuration,mPredictedDistance);
            //getBikePredictedFare(mPredictedDuration,mPredictedDistance);
            //getAutoPredictedFare(mPredictedDuration,mPredictedDistance);
            //mPFare.setText("Estimated Fare by Bike : Rs."+ getCabPredictedFare(mPredictedDuration,mPredictedDistance)+"/-");
        }

    }

    @Override
    public void onRoutingCancelled() {

    }
    */
    private void erasePolyLines(){
        for(Polyline line : polylines){
            line.remove();

        }
        polylines.clear();
        mJourneyTime.setText("");
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
        String item = parent.getItemAtPosition(position).toString();
        paymentMethod = item;
        // Showing selected spinner item
        if(position==1)
            //Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
            Toast.makeText(parent.getContext(), "Paytm is not available" , Toast.LENGTH_LONG).show();


    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);


        if (polylines != null) {
            for (Polyline polyline:polylines ) {
                polyline.remove();
            }
        }
    }
/*
    @Override
    public void onDirectionFinderSuccess(List<Modules.Route> route) {

    }*/


    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {

        progressDialog.dismiss();
        polylines = new ArrayList<>();
        //originMarkers = new ArrayList<>();
        //destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));

            mPredictedDistance = route.distance.value;
            mPredictedDuration = route.duration.value;

            mPredictedDistance = (double)mPredictedDistance/1000;
            mPredictedDuration = Math.ceil(mPredictedDuration/60);

            mJourneyTime.setText("Expected Journey Time : "+(int)mPredictedDuration+"min");
            //((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
            //((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);
/*
            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green))
                    .title(route.endAddress)
                    .position(route.endLocation)));*/

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylines.add(mMap.addPolyline(polylineOptions));
        }
    }


}
