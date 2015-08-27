package com.evanhalley.whereip;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashSet;

public class MapActivity extends FragmentActivity implements View.OnClickListener,
        DialogInterface.OnShowListener {

    private static final String EXTRA_PARAM_LOCATIONS = "com.evanhalley.whereip.locations";

    private GoogleMap mMap;
    private AlertDialog mDialog;
    private int mNewLocationsAdded = 0;

    // use the location list to store locations already on the map
    private ArrayList<Location> mLocationList = new ArrayList<>();
    private HashSet<String> mCoordinateSet = new HashSet<>();

    private FloatingActionButton mLocateButton;
    private ProgressDialog mProgressDialog;

    private BroadcastReceiver mNewLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LookupService.EXTRA_PARAM_LOCATION);

            if (location != null) {
                mNewLocationsAdded++;
                mLocationList.add(location);
                addLocationToMap(location);
            }
        }
    };

    private BroadcastReceiver mProcessingStartedReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mProgressDialog = ProgressDialog.show(MapActivity.this,
                    getString(R.string.dialog_processing_title),
                    getString(R.string.dialog_processing_message));
            mProgressDialog.show();

            if (mLocateButton != null) {
                mLocateButton.setEnabled(false);
            }
        }
    };

    private BroadcastReceiver mProcessingFinishedReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mProgressDialog.dismiss();

            if (mNewLocationsAdded > 1) {
                Toast.makeText(MapActivity.this, R.string.status_finished_more_than_one,
                        Toast.LENGTH_SHORT).show();
            }
            mNewLocationsAdded = 0;

            if (mLocateButton != null) {
                mLocateButton.setEnabled(true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        setUpMapIfNeeded();
        findViewById(R.id.locate_ip).setOnClickListener(this);
        mLocateButton = (FloatingActionButton) findViewById(R.id.locate_ip);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.locate_ip:
                mDialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.title_dialog_search_ip)
                        .setView(LayoutInflater.from(this).inflate(R.layout.dialog_locate_ip, null))
                        .setPositiveButton(R.string.button_find, null)
                        .setNegativeButton(R.string.button_cancel, null)
                        .create();
                mDialog.setOnShowListener(this);
                mDialog.show();
                break;
        }
    }

    @Override
    public void onShow(DialogInterface dialog) {
        mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String ipAddressStart = ((TextView) mDialog
                                .findViewById(R.id.ip_address_start))
                                .getText().toString();

                        if (!runIpValidation(ipAddressStart, R.id.ip_address_start_input_layout)) {
                            return;
                        }

                        String ipAddressEnd = ((TextView) mDialog
                                .findViewById(R.id.ip_address_end))
                                .getText().toString();

                        if (TextUtils.isEmpty(ipAddressEnd)) {
                            ipAddressEnd = ipAddressStart;
                        } else {
                            if (!runIpValidation(ipAddressEnd, R.id.ip_address_end_input_layout)) {
                                return;
                            }
                        }
                        LookupService.lookupIpRange(MapActivity.this,
                                ipAddressStart, ipAddressEnd);
                        mDialog.dismiss();
                    }
                });

        mDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();
                    }
                }
        );
    }

    private boolean runIpValidation(String ipAddess, int inputLayoutResId) {

        if (!LookupHelper.validateIpAddress(ipAddess)) {
            TextInputLayout inputLayout = ((TextInputLayout) mDialog.findViewById(inputLayoutResId));
            inputLayout.setErrorEnabled(true);
            inputLayout.setError(MapActivity.this
                    .getString(R.string.error_invalid_ip_address));
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        LocalBroadcastManager.getInstance(this).registerReceiver(mNewLocationReceiver,
                new IntentFilter(LookupService.ACTION_NEW_LOCATION));
        LocalBroadcastManager.getInstance(this).registerReceiver(mProcessingStartedReciever,
                new IntentFilter(LookupService.ACTION_PROCESSING_STARTED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mProcessingFinishedReciever,
                new IntentFilter(LookupService.ACTION_PROCESSING_FINISHED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mNewLocationReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mProcessingFinishedReciever);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mProcessingStartedReciever);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(EXTRA_PARAM_LOCATIONS, mLocationList);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mLocationList = savedInstanceState.getParcelableArrayList(EXTRA_PARAM_LOCATIONS);
        mCoordinateSet = new HashSet<>();

        if (mLocationList == null) {
            mLocationList = new ArrayList<>();
        }

        for (int i = 0; i < mLocationList.size(); i++) {
            addLocationToMap(mLocationList.get(i));
        }
    }

    private void addToCoordinateSet(Location location) {
        String key = String.valueOf(location.getLatitude() + "_" + location.getLongitude());

        if (!mCoordinateSet.contains(key)) {
            mCoordinateSet.add(key);
        }
    }

    private boolean locationAtCoordinatesExists(Location location) {
        String key = String.valueOf(location.getLatitude() + "_" + location.getLongitude());
        return mCoordinateSet.contains(key);
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();

            if (mMap == null) {
                Toast.makeText(this,
                        R.string.error_google_play_services_needed, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mMap.getUiSettings().setMapToolbarEnabled(false);
        }
    }

    private void addLocationToMap(Location location) {

        // does a marker already exist that these coordinates, if not add it
        if (!locationAtCoordinatesExists(location)) {
            addToCoordinateSet(location);
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            MarkerOptions marker = new MarkerOptions()
                    .position(latLng)
                    .title(String.format("%s @ %s, %s", location.getIpAddress(), location.getCity(),
                            location.getCountryName()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker));
            mMap.addMarker(marker);
            mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        }
    }
}