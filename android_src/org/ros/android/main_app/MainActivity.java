/*
 * Copyright (C) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.android.main_app;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.ros.android.BitmapFromCompressedImage;
import org.ros.android.MessageCallable;
import org.ros.android.RosActivity;
import org.ros.android.view.RosImageView;
import org.ros.android.view.RosTextView;
import org.ros.message.Time;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends RosActivity implements View.OnClickListener  {

    // ROS
    // Main Executor and nodeConfig for this App
    private NodeMainExecutor nodeMainExecutor;
    private NodeConfiguration nodeConfiguration;

    // RosTextView Listeners
    private RosTextView<std_msgs.Header> robotJobStatusListener;                    // jobStatus Listener
    private RosTextView<sensor_msgs.NavSatFix> gpsListener;
    private RosTextView<std_msgs.Header> rsListener;
    private RosTextView<smart_battery_msgs.SmartBatteryStatus> batteryInfoView;     // battery listener
    private RosTextView<nav_msgs.Odometry> speedInfoView;                           // speed listener
    private RosImageView<sensor_msgs.CompressedImage> liveCamView;                  // LiveView Listener

    // Publishers (so far, only one publisher is needed) //TODO
    private rcPublisher rcp;
    private pathPublisher pp;

    // android
    // variables

    private int  time_diff; // initial job status = 0 = idle
    private double robotLat, robotLong;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private String pointsText;
    private String command_name;
    private int command_id, robotJob_status_id = 0;     // initial job status = 0 = idle
    private double robotLatitude, robotLongitude;       // for gps mapping
    public Time command_time,status_time;
    private int count=1;

    // flags
    private boolean isControls = false;
    private boolean isLive = false;
    private boolean isShowSpeed = true;
    private boolean isShowBattery = true;
    private boolean isMap = false;
    private boolean isPathSeletor = false;
    private boolean satellite = true;
    private boolean isRandom = false;


    // GUI items
    private RelativeLayout menu, mainPage, mapWrapper,menuBlock,pathSelector;
    private LinearLayout speedBar, batteryGroup, ControlGroup;

    private ImageButton speedButton, batteryButton, batteryProg, liveFeedButton, MapButton,
            LivefeedButtonSmall, BatteryButtonSmall, SpeedButtonSmall, MapButtonSmall,
            menuXbutton, menuButton, ControlsButton, ControlsButtonSmall,
            ControlGoHomeButton, ControlStopButton, ControlWorkButton, ControlResumeButton,
            ControlSpeakButton,viewMapButton,selectPathButton,diButton1,diButton2,creatPathButton,satelliteButton;
    private ProgressBar speedProgPs, speedProgNs;
    private TextView speedProgLabel, batteryProgLabel;
    private GoogleMap mMap;

    private double robot_lat = 2.974385, robot_lng = 101.729854;    // initial/default values

    private double[] pointsPath = new double[]{0.5,-0.5, 1.0,0.0, 1.5,0.5, 1.0,1.0, 0.5, 1.5, 0.0,1.0,-0.5,0.5, 0.0,0.0}; // 8 points
    private double[] oneside = new double[]{1.077, -2.765, 3.409, 0.244, 2.041, 1.553, 1.700, 3.577, -1.745, 2.478, 0.161, 0.526};
    private double[] twosides = new double[]{1.264, -1.419, 2.814, -0.185, 2.658, 2.467, 1.481, 3.790, 0.901, 1.813};

    // Voice Recognition

    private SpeechRecognizer sr;
    private TextToSpeech textReader;


    // Shake Detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    //Map
    private Marker marker;
    private List<LatLng> makersTravelPoints;


// ----- App Initiations --------------

    // Construct to pre-populate Notification & Ticker bars
    public MainActivity() {
        super("ROS-android HRI", "ROS-android HRI"); // , URI.create("http://172.17.32.21:11311")
    }

    // Start RosTextView Listeners

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);  // removes title bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setCorrespondingView();

        // order matters here
        startAndroidGUIs();              // start any other GUIs for this app
        initROSListeners();                 // start rosTextView listeners

        //addListenerOnButton();           // on button click listeners
        setOnClickListeners(R.id.ControlsGroup);
        setGUIButtonsListeners();

        //Shake and Voice init
        TextReaderAndVoiceRecInit();
        ShakeDetectorInit();


    }

    public void setCorrespondingView(){
        // check if tablet or not
        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        if (tabletSize) {
            setContentView(R.layout.tablet2); // tablet layout
        } else {
            setContentView(R.layout.main); // phone layout
        }

    }
    // housekeeping, putting all views here (other than RosTextViews)
    public void startAndroidGUIs() {

        // Part I - Define Views --------------

        // speed view
        speedButton = (ImageButton) findViewById(R.id.SpeedButton);
        speedBar = (LinearLayout) findViewById(R.id.speedBar);
        speedProgPs = (ProgressBar) findViewById(R.id.SpeedProgPs); // +ve robot velocity (forward)
        speedProgNs = (ProgressBar) findViewById(R.id.SpeedProgNs); // -ve robot velocity (reverse)
        speedProgLabel = (TextView) findViewById(R.id.SpeedProgLabel);

        // battery view
        batteryButton = (ImageButton) findViewById(R.id.BatteryButton);
        batteryProg = (ImageButton) findViewById(R.id.BatteryProg);
        batteryProgLabel = (TextView) findViewById(R.id.BatteryProgLabel);
        batteryGroup= (LinearLayout) findViewById(R.id.batteryGroup);

        // LiveFeed view
        liveFeedButton = (ImageButton) findViewById(R.id.LiveFeedButton);

        // Commands view
        ControlsButton = (ImageButton) findViewById(R.id.ControlsButton);
        ControlGroup = (LinearLayout) findViewById(R.id.ControlsGroup);

        // map view
        MapButton = (ImageButton) findViewById(R.id.MapButton);
        mapWrapper = (RelativeLayout) findViewById(R.id.mapWrapper);
        menuBlock =  (RelativeLayout) findViewById(R.id.menuBlock);
        pathSelector =  (RelativeLayout) findViewById(R.id.pathSelector);
        viewMapButton = (ImageButton) findViewById(R.id.viewMapButton);
        selectPathButton = (ImageButton) findViewById(R.id.selectPathButton);
        creatPathButton = (ImageButton) findViewById(R.id.creatPathButton);
        diButton1= (ImageButton) findViewById(R.id.diButton1);
        diButton2= (ImageButton) findViewById(R.id.diButton2);
        satelliteButton = (ImageButton) findViewById(R.id.satelliteButton);


        if(servicesOK()){
            if(initMap()){
                gotoLocation(robot_lat,robot_lng,19);
            }else {
                Toast.makeText(this,"Map not connected!", Toast.LENGTH_SHORT).show();
            }
        }

        // menu x button
        menuXbutton = (ImageButton) findViewById(R.id.xbutton);

        // show menu button
        menuButton = (ImageButton) findViewById(R.id.level2menu);

        // small  menu bar icons
        LivefeedButtonSmall = (ImageButton) findViewById(R.id.LiveFeedButtonSmall);
        MapButtonSmall = (ImageButton) findViewById(R.id.MapButtonSmall);
        ControlsButtonSmall = (ImageButton) findViewById(R.id.ControlsButtonSmall);
        BatteryButtonSmall = (ImageButton) findViewById(R.id.BatteryButtonSmall);
        SpeedButtonSmall = (ImageButton) findViewById(R.id.SpeedButtonSmall);

        //menu
        menu = (RelativeLayout) findViewById(R.id.menu);
        mainPage = (RelativeLayout) findViewById(R.id.mainPage);

        // Control buttons
        ControlGoHomeButton = (ImageButton) findViewById(R.id.ControlGoHome);
        ControlStopButton = (ImageButton) findViewById(R.id.ControlStop);
        ControlResumeButton = (ImageButton) findViewById(R.id.ControlResume);
        ControlWorkButton= (ImageButton) findViewById(R.id.ControlWork);
        ControlSpeakButton = (ImageButton) findViewById(R.id.ControlSpeak);



      // Part II - Define OnClick Listeners (non-robot Commands) -------------

        setGUIButtonsListeners();
    }

    private void setGUIButtonsListeners() { // app Nav

        batteryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //

                // fade in and out animation for the battery bar start here..
                if (isShowBattery) {
                    batteryButton.setImageResource(R.drawable.batteryblack);
                    BatteryButtonSmall.setVisibility(View.GONE);
                    batteryGroup.setVisibility(View.GONE);
                } else {
                    batteryButton.setImageResource(R.drawable.batterblue);
                    BatteryButtonSmall.setVisibility(View.VISIBLE);
                    batteryGroup.setVisibility(View.VISIBLE);
                }
                isShowBattery = !isShowBattery; // toggle the flag value
                // fade in and out animation for the battery bar ends here..
            }
        });

        speedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // fade in and out animation for the capacity bar start here..
                if (isShowSpeed) {

                    SpeedButtonSmall.setVisibility(View.GONE);
                    speedButton.setImageResource(R.drawable.speedblack);
                    speedBar.setVisibility(View.GONE);
                    speedProgLabel.setVisibility(View.GONE);
//
                } else {
                    speedButton.setImageResource(R.drawable.speedblue);
                    SpeedButtonSmall.setVisibility(View.VISIBLE);
                    speedBar.setVisibility(View.VISIBLE);
                    speedProgLabel.setVisibility(View.VISIBLE);
                }
                isShowSpeed = !isShowSpeed; // toggle the flag value
                // fade in and out animation for the capacity bar ends here..

            }
        });

        ControlsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  "Go home" ROS prodcastor can be here ...

                // checking if the robot is going home
                if (isControls) {

                    ControlsButton.setImageResource(R.drawable.controlblack);
                    ControlsButtonSmall.setVisibility(View.GONE);
                    ControlGroup.setVisibility(View.GONE);
                } else {

                    ControlsButton.setImageResource(R.drawable.controlblue);
                    ControlsButtonSmall.setVisibility(View.VISIBLE);
                    ControlGroup.setVisibility(View.VISIBLE);
                }

                isControls = !isControls; // toggle the flag value
            }
        });

        liveFeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLive) {

                    liveFeedButton.setImageResource(R.drawable.livefeedblack);
                    LivefeedButtonSmall.setVisibility(View.GONE);
                    ObjectAnimator fadeOUT= ObjectAnimator.ofFloat(liveCamView,"Alpha",1f,0f).setDuration(500);
                    fadeOUT.start();

                } else {
                    // the Cam Ros Image View is NOT live ..
                    liveFeedButton.setImageResource(R.drawable.livefeedblue);
                    LivefeedButtonSmall.setVisibility(View.VISIBLE);
                    ObjectAnimator fadeIN= ObjectAnimator.ofFloat(liveCamView,"Alpha",0f,1f).setDuration(500);
                    fadeIN.start();

                }

                isLive = !isLive;// toggle the flag value
            }
        });


        diButton1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                /* TODO add travel points to the map
                    (name: One Side) =
                        [1.264, -1.419, 2.814, -0.185, 2.658, 2.467, 1.481, 3.790, 0.901, 1.813]
                 */

                diButton1.setImageResource(R.drawable.dibuttonblue);
                diButton2.setImageResource(R.drawable.dibuttongray);
                pathSelector.setVisibility(View.GONE);
                menuBlock.setVisibility(View.VISIBLE);
                menu.setVisibility(View.VISIBLE);
                isPathSeletor = !isPathSeletor;// toggle the flag value
                if(servicesOK()) {
                    if (initMap()) {
                        mMap.clear();
                        gotoLocation(robot_lat,robot_lng,19);
                        if (mMap != null) {
                                for (int i=0; i<oneside.length; i+=2) {
                                    MainActivity.this.drawMaker(oneside[i], oneside[i+1]);
                                }
                        }
                    }
                }

                command_id = 17;    // to disable rcPublisher
                // view created path (cosmatic)
                count = 0;
                for (int i=0; i<oneside.length; i+=2) {
                    pointsText += "Position (" + (count) + "): {" + oneside[i] + "," + oneside[i+1] + "} \n";
                    count++; }

                // publish path to robot
                ppPublisher(pointsPath);

                }

        });

        diButton2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                 /* TODO add travel points to the map

                        path 2 (Name: Two Sides) =
               [1.077, -2.765, 3.409, 0.244, 2.041, 1.553, 1.700, 3.577, -1.745, 2.478, 0.161, 0.526]
                   */

                diButton1.setImageResource(R.drawable.dibuttongray);
                diButton2.setImageResource(R.drawable.dibuttonblue);
                pathSelector.setVisibility(View.GONE);
                menuBlock.setVisibility(View.VISIBLE);
                menu.setVisibility(View.VISIBLE);
                isPathSeletor = !isPathSeletor;// toggle the flag value


                if(servicesOK()) {
                    if (initMap()) {
                        if (mMap != null) {
                            mMap.clear();
                            gotoLocation(robot_lat,robot_lng,19);
                            for (int i=0; i<twosides.length; i+=2) {
                                MainActivity.this.drawMaker(twosides[i], twosides[i+1]);
                            }
                        }
                    }
                }

                command_id = 17;    // to disable rcPublisher
                // view created path (cosmatic)
                count = 0;
                for (int i=0; i<twosides.length; i+=2) {
                    pointsText += "Position (" + (count) + "): {" + twosides[i] + "," + twosides[i+1] + "} \n";
                    count++;
                }

                // publish path to robot
                ppPublisher(pointsPath);

            }


        });

        satelliteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(servicesOK()){
                    if(initMap()){
                        if(mMap != null){
                            if(satellite) {
                                satelliteButton.setImageResource(R.drawable.satellitebuttonblack);
                                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                            }else{
                                satelliteButton.setImageResource(R.drawable.satellitebuttonblue);
                                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                            }

                            satellite = !satellite;
                        }
                    }else {
                        Toast.makeText(MainActivity.this,"Map not connected!", Toast.LENGTH_SHORT).show();
                    }
                }


                pathSelector.setVisibility(View.GONE);
                menuBlock.setVisibility(View.GONE);
                ObjectAnimator mapIn = ObjectAnimator.ofFloat(mapWrapper,"Alpha",0f,1f).setDuration(500);
                ObjectAnimator mOut = ObjectAnimator.ofFloat(menu, "Alpha", 1f, 0f).setDuration(200);
                ObjectAnimator pIn = ObjectAnimator.ofFloat(mainPage, "Alpha", 0f, 1f).setDuration(200);
                AnimatorSet FadeIn = new AnimatorSet();
                FadeIn.playTogether(mOut, pIn,mapIn);
                FadeIn.start();
                menu.setVisibility(View.GONE);
                mainPage.setVisibility(View.VISIBLE);
                MapButtonSmall.setVisibility(View.VISIBLE);
            }
        });

        creatPathButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                MapButtonSmall.setVisibility(View.VISIBLE);
                pathSelector.setVisibility(View.GONE);
                menuBlock.setVisibility(View.GONE);
                ObjectAnimator mapIn = ObjectAnimator.ofFloat(mapWrapper,"Alpha",0f,1f).setDuration(500);
                ObjectAnimator mOut = ObjectAnimator.ofFloat(menu, "Alpha", 1f, 0f).setDuration(200);
                ObjectAnimator pIn = ObjectAnimator.ofFloat(mainPage, "Alpha", 0f, 1f).setDuration(200);
                AnimatorSet FadeIn = new AnimatorSet();
                FadeIn.playTogether(mOut, pIn,mapIn);
                FadeIn.start();
                menu.setVisibility(View.GONE);
                mainPage.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "Please Long press to define new Travel Points", Toast.LENGTH_LONG).show();
                Toast.makeText(MainActivity.this, "Saving travel points disabled for demo reasons", Toast.LENGTH_LONG).show();
                isPathSeletor = !isPathSeletor;// toggle the flag value
            }
        });

        selectPathButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isPathSeletor) {
                    selectPathButton.setImageResource(R.drawable.selectpathblack);
                    pathSelector.setVisibility(View.GONE);
                    menuBlock.setVisibility(View.VISIBLE);
                    menu.setVisibility(View.VISIBLE);
                }else{
                    selectPathButton.setImageResource(R.drawable.selectedpathblue);
                    pathSelector.setVisibility(View.VISIBLE);
                    menuBlock.setVisibility(View.GONE);
                    menu.setVisibility(View.GONE);
                }
                isPathSeletor = !isPathSeletor;// toggle the flag value
            }
        });

        viewMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isMap) {
                    // the Cam Ros Image View is live ..
                    viewMapButton.setImageResource(R.drawable.viewmapblack);
                    MapButton.setImageResource(R.drawable.mapblack);
                    MapButtonSmall.setVisibility(View.GONE);
                    ObjectAnimator fadeOUT= ObjectAnimator.ofFloat(mapWrapper,"Alpha",1f,0f).setDuration(500);
                    fadeOUT.start();
                    batteryProgLabel.setTextColor(Color.parseColor("#FFFFFF"));
                    speedProgLabel.setTextColor(Color.parseColor("#FFFFFF"));

                } else {
                    // the Cam Ros Image View is NOT live ..
                    viewMapButton.setImageResource(R.drawable.viewmapblue);
                    MapButton.setImageResource(R.drawable.mapblue);
                    MapButtonSmall.setVisibility(View.VISIBLE);
                    ObjectAnimator fadeIN= ObjectAnimator.ofFloat(mapWrapper,"Alpha",0f,1f).setDuration(500);
                    fadeIN.start();
                    batteryProgLabel.setTextColor(Color.parseColor("#000000"));
                    speedProgLabel.setTextColor(Color.parseColor("#000000"));
                }
                menuBlock.setVisibility(View.GONE);
                isMap = !isMap;// toggle the flag value
            }
        });

        MapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuBlock.setVisibility(View.VISIBLE);
            }
        });

        // show and hide the menu
        menuXbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ObjectAnimator mOut = ObjectAnimator.ofFloat(menu, "Alpha", 1f, 0f).setDuration(200);
                ObjectAnimator pOut = ObjectAnimator.ofFloat(mainPage, "Alpha", 0f, 1f).setDuration(200);
                AnimatorSet FadeIn = new AnimatorSet();
                FadeIn.playTogether(mOut, pOut);
                FadeIn.start();
                menu.setVisibility(View.GONE);
                mainPage.setVisibility(View.VISIBLE);
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ObjectAnimator mOut = ObjectAnimator.ofFloat(menu, "Alpha", 0f, 1f).setDuration(200);
                ObjectAnimator pOut = ObjectAnimator.ofFloat(mainPage, "Alpha", 1f, 0f).setDuration(200);
                AnimatorSet FadeIn = new AnimatorSet();
                FadeIn.playTogether(mOut, pOut);
                FadeIn.start();
                menu.setVisibility(View.VISIBLE);
                mainPage.setVisibility(View.GONE);

            }
        });
        // SmallButtons
        LivefeedButtonSmall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isLive= false;
                LivefeedButtonSmall.setVisibility(View.GONE);
                liveFeedButton.setImageResource(R.drawable.livefeedblack);
                ObjectAnimator fadeOUT= ObjectAnimator.ofFloat(liveCamView,"Alpha",1f,0f).setDuration(500);
                fadeOUT.start();
            }
        });

        MapButtonSmall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapButtonSmall.setVisibility(View.GONE);
                viewMapButton.setImageResource(R.drawable.viewmapblack);
                MapButton.setImageResource(R.drawable.mapblack);
                ObjectAnimator fadeOUT= ObjectAnimator.ofFloat(mapWrapper,"Alpha",1f,0f).setDuration(500);
                fadeOUT.start();
                batteryProgLabel.setTextColor(Color.parseColor("#FFFFFF"));
                speedProgLabel.setTextColor(Color.parseColor("#FFFFFF"));
                isMap = false;

            }
        });

        ControlsButtonSmall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ControlsButtonSmall.setVisibility(View.GONE);
                ControlsButton.setImageResource(R.drawable.controlblack);
                ControlGroup.setVisibility(View.GONE);
                isControls = false;

            }
        });

        BatteryButtonSmall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BatteryButtonSmall.setVisibility(View.GONE);
                batteryButton.setImageResource(R.drawable.batteryblack);
                batteryGroup.setVisibility(View.GONE);
                isShowBattery = false;

            }
        });

        SpeedButtonSmall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isShowSpeed= false;
                SpeedButtonSmall.setVisibility(View.GONE);
                speedButton.setImageResource(R.drawable.speedblack);
                speedBar.setVisibility(View.GONE);
                speedProgLabel.setVisibility(View.GONE);

            }
        });

    }

    // ----- Robot-to-Android Interactions --------------

    // RosTextView Listeners
    public void initROSListeners() {

      /* ---- For each RosTextView listener -----
        Initiate rosTextView, using given message type
        setTopic              using given topic
        setMessageType        using given message type
        setMessageToStringCallable => define what to do with message once captured
      */

        //  robotJobStatus Listener
        robotJobStatusListener = (RosTextView<std_msgs.Header>) findViewById(R.id.jobStatusMessage);
        robotJobStatusListener.setTopicName("robotJobStatus");
        robotJobStatusListener.setMessageType(std_msgs.Header._TYPE);
        robotJobStatusListener.setMessageToStringCallable(
                new MessageCallable<String, std_msgs.Header>() {
            @Override
            public String call(std_msgs.Header msg) {
                robotJob_status_id = msg.getSeq();  // Header.Seq = int = job_status_id
                return msg.getFrameId();            // Header.FrameID = String = task_name
            }
        });

        // GPS position Listener
        gpsListener = (RosTextView<sensor_msgs.NavSatFix>) findViewById(R.id.gpsMessage);
        gpsListener.setTopicName("fix");
        gpsListener.setMessageType("sensor_msgs/NavSatFix");
        gpsListener.setMessageToStringCallable(
                new MessageCallable<String, sensor_msgs.NavSatFix>() {

            @Override
            public String call(sensor_msgs.NavSatFix message) {

                robotLatitude = message.getLatitude();     // extract coordinates
                robotLongitude = message.getLongitude();
                setMapView(robotLatitude, robotLongitude);  // pass coordinates to MapView

                return "Current Robot Location is: Latitude: " + robotLatitude
                        + ", Longitude: " + robotLongitude; // show message
            }
        });

        // Speed Listener
        speedInfoView = (RosTextView<nav_msgs.Odometry>) findViewById(R.id.speedInfo);
        speedInfoView.setTopicName("/odom");
        speedInfoView.setMessageType(nav_msgs.Odometry._TYPE);
        speedInfoView.setMessageToStringCallable(
                new MessageCallable<String, nav_msgs.Odometry>() {

            @Override
            public String call(nav_msgs.Odometry msg) {
                double speed = msg.getTwist().getTwist().getLinear().getX();
                if (speed != Math.abs(speed)) {// it is -ve
                    speedProgNs.setProgress((int) Math.round(speed * 100) * -1 );// updating the custom bar for the speed
                } else { // it is +ve
                    speedProgPs.setProgress((int) Math.round(speed * 100));// updating the custom bar for the speed
                }
                speedProgLabel.setText(Math.round((speed * 100)) + "cm/s");
                return "";
            }


        });

        // Battery Listener
        batteryInfoView = (RosTextView<smart_battery_msgs.SmartBatteryStatus>) findViewById(R.id.batteryInfo);
        batteryInfoView.setTopicName("/laptop_charge"); // CHANGE TOPIC NAME HERE !!
        batteryInfoView.setMessageType(smart_battery_msgs.SmartBatteryStatus._TYPE);
        batteryInfoView.setMessageToStringCallable(
                new MessageCallable<String, smart_battery_msgs.SmartBatteryStatus>() {
            @Override
            public String call(smart_battery_msgs.SmartBatteryStatus msg) {
                byte ChargeState = msg.getChargeState();

                int percentage = msg.getPercentage();
                batteryProgLabel.setText(percentage + "%");

                if (ChargeState == 0) {
                 if(percentage < 20){
                        batteryProg.setImageResource(R.drawable.battery10pre);
                    }else if(percentage < 60){
                        batteryProg.setImageResource(R.drawable.battery25pre);
                    }else if(percentage < 90){
                        batteryProg.setImageResource(R.drawable.battery50pre);
                    }else if(percentage <= 100){
                        batteryProg.setImageResource(R.drawable.battery100pre);
                    }
                } else {
                    batteryProg.setImageResource(R.drawable.charge);
                }
                return "";
            }

        });

        // LiveFeed Listener (imageListener)
        liveCamView = (RosImageView<sensor_msgs.CompressedImage>) findViewById(R.id.livecamView);
        liveCamView.setTopicName("camera/rgb/image_color/compressed"); // setting the topic name here
        // camera/rgb/image_color/compressed
        liveCamView.setMessageType(sensor_msgs.CompressedImage._TYPE);
        liveCamView.setMessageToBitmapCallable(new BitmapFromCompressedImage());

        // other Listeners ..
    }

    // Mapping methods -----------------------------
    public void setMapView(double robot_latitude, double robot_longitude) {

        robot_lat = robot_latitude;
        robot_lng= robot_longitude;
        gotoLocation(robot_lat,robot_lng,19);
    }

    public boolean servicesOK() {
        //check if the android deivce have google services

        int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable, this, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "Can not connect to mapping service", Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    private boolean initMap(){
        if(mMap == null){
            MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
            mMap = mapFragment.getMap();


                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        String msg = marker.getTitle() + "  ( " +
                                marker.getPosition().latitude + ", " +
                                marker.getPosition().longitude+ " ) ";
                        Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
                mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                    @Override
                    public void onMarkerDragStart(Marker marker) {

                    }

                    @Override
                    public void onMarkerDrag(Marker marker) {

                    }

                    @Override
                    public void onMarkerDragEnd(Marker marker) {
                        Geocoder gc = new Geocoder(MainActivity.this);
                        List<Address> list = null;
                        LatLng ll = marker.getPosition();
                        try {
                            list = gc.getFromLocation(ll.latitude, ll.longitude,1);
                            Log.e("MAP","It works WORKING"); // Error
                        } catch (IOException e) {
                            Log.e("MAP","NOT WORKING"); // Error
                            e.printStackTrace();
                            return;
                        }

                        Address add = list.get(0);
                        marker.setTitle(add.getLocality());
                        marker.setSnippet(add.getCountryName());


                    }
                });
            if(mMap != null){


               mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener(){

                   @Override
                   public void onMapLongClick(LatLng latLng) {
                       Geocoder gc = new Geocoder(MainActivity.this);
                       List<Address> list = null;

                       try {
                           list = gc.getFromLocation(latLng.latitude, latLng.longitude,1);
                           Log.e("MAP","It works WORKING"); // Error
                       } catch (IOException e) {
                           Log.e("MAP","NOT WORKING"); // Error
                           e.printStackTrace();
                           return;
                       }

                       Address add = list.get(0);
                       MainActivity.this.addMarker(add,latLng.latitude,latLng.longitude);
                   }
               });
            }
        }
        return (mMap != null);
    }

    private void drawMaker( double lat, double lng){
        MarkerOptions options = new MarkerOptions()
                .position(new LatLng(lat, lng))
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.flag));
        marker = mMap.addMarker(options);
    }

    private void addMarker(Address add, double lat, double lng) {
        MarkerOptions options = new MarkerOptions()
                .title(add.getLocality())
                .position(new LatLng(lat, lng))
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.flag));

        String country = add.getCountryName();
        if (country.length() > 0) {
            options.snippet(country);
        }

        marker = mMap.addMarker(options);

    }

    private void gotoLocation(double lat, double lng,float zoom) {
        LatLng latlng = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latlng, zoom);
        mMap.animateCamera(update);
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        MarkerOptions options = new MarkerOptions()
                .title("Robot")
                .position(latlng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.robotlocation));
        mMap.addMarker(options);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
    }

    // ----- Android-to-Robot Interactions --------------

    // regardless of input type, the process should follow these two steps:
    // Step 1 - setOnEventListener, onClick, onVoice, onMovement, etc
    // Step 2 - Process user input, based on input, set command_id
    // Step 3 - Call rcPublisher

    // -------------- Buttons input  -----------------------------

    public void setOnClickListeners(int layout_id) {
        LinearLayout viewContainer = (LinearLayout) findViewById(layout_id);  // find layout containing all views (needs an id)

        for (int i = 0; i < viewContainer.getChildCount(); i++) {
            View v = viewContainer.getChildAt(i);

            if (v instanceof ImageButton)   // set onClick for every ImageButton
                v.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {   // responds to clicks on robotCommand Buttons

        switch (view.getId()){

            // robotCommandButtons
            case R.id.ControlGoHome:
                // update UI
                ControlGoHomeButton.setImageResource(R.drawable.gohomeblue);
                ControlStopButton.setImageResource(R.drawable.stopblack);
                ControlResumeButton.setImageResource(R.drawable.resumeblack);
                ControlWorkButton.setImageResource(R.drawable.work);

                // update command
                command_id = 11;   // 1 for button input, 1 for task 1
                command_name = "doGoHome";
                captureTime();
                break;
            case R.id.ControlStop:
                // update UI
                ControlGoHomeButton.setImageResource(R.drawable.gohomeblack);
                ControlStopButton.setImageResource(R.drawable.stopblue);
                ControlResumeButton.setImageResource(R.drawable.resumeblack);
                ControlWorkButton.setImageResource(R.drawable.work);
                // update command
                command_id = 12;   // 1 for button input, 2 for task 2
                command_name = "doStop";
                captureTime();
                break;
            case R.id.ControlResume:
                // update UI
                ControlGoHomeButton.setImageResource(R.drawable.gohomeblack);
                ControlStopButton.setImageResource(R.drawable.stopblack);
                ControlResumeButton.setImageResource(R.drawable.resumeblue);
                ControlWorkButton.setImageResource(R.drawable.work);
                // update command
                command_id = 13;
                command_name = "doResume";
                captureTime();
                break;
            case R.id.ControlWork:
                // update UI
                ControlGoHomeButton.setImageResource(R.drawable.gohomeblack);
                ControlStopButton.setImageResource(R.drawable.stopblack);
                ControlResumeButton.setImageResource(R.drawable.resumeblack);
                ControlWorkButton.setImageResource(R.drawable.workblue);
                // update command
                command_id = 14;
                command_name = "doWork";
                captureTime();
                break;
            case R.id.ControlSpeak: // Activates Speech Recognizer
                // update UI
                ControlGoHomeButton.setImageResource(R.drawable.gohomeblack);
                ControlStopButton.setImageResource(R.drawable.stopblack);
                ControlResumeButton.setImageResource(R.drawable.resumeblack);
                ControlWorkButton.setImageResource(R.drawable.work);
                ControlSpeakButton.setImageResource(R.drawable.micactive);
                // voice recognizer
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE," org.ros.android.main_app");
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);
                sr.startListening(intent); // starting to listen to the speech
                sr.stopListening();       // stopping to listen to the speech
                break;
            default:
                //
                break;
        }

        // update conditions
        if ( (robotJob_status_id != (command_id%10)))   //  command_id%10  = 2nd digit = task id, is this task already running?
            rcPublisher(command_id, command_name);

    }

    // -------------- Voice Input  -----------------------------

    void TextReaderAndVoiceRecInit(){


        // text reader init starts here
        textReader = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                textReader.setLanguage(Locale.US);
                textReader.setSpeechRate(2);

            }
        });

        ControlSpeakButton.setOnClickListener(this);
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new voiceRecognition(textReader,ControlSpeakButton));

    }
    // Step 1 - OnEventListener
    // Step 2 - Process user input
    // Step 3 - Call rcPublisher

    // -------------- Gesture Input  -----------------------------

    void ShakeDetectorInit(){
        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector(textReader);
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
				/*
				 * The following method, "handleShakeEvent(count):" is a stub //
				 * method you would use to setup whatever you want done once the
				 * device has been shook.
				 */
                Log.d("TESTING", "emergency stop! ");
                textReader.speak("emergency stop! " + count, TextToSpeech.QUEUE_FLUSH, null);
            }
        });

    }
    // Step 1 - OnEventListener
    // Step 2 - Process user input
    // Step 3 - Call rcPublisher


    // -------------- Publish Commands to Robot--------------------

    // method to publish robotCommand, using User input
    public void rcPublisher(int id, String name) {
        rcp = new rcPublisher("robotCommands");         // define topic
        rcp.setCommandID(id);                           // set command id (contains input id as well)
        rcp.setCommandName(name);                       // set command name (Work, Stop, resume, GoHome)
        rcp.setPublishOnce(true);                       // supress ROS usual behavior and publish just one message
        rcp.setCommandTime(command_time);               // Set time = time when command was selected
        startNode(rcp, "robotCommandPublisher_android");  // Use same nodeName to avoid concurrent robotCommands
    }

    public void ppPublisher(double[] pointsPath ) {     // initial new ROS Publisher (Java Class)
        pp = new pathPublisher("robotPath");            // define topic
        pp.setPointsArray(pointsPath);                  //
        //    pp.setCommandTime(command_time);              // Set time = time when command was selected
        pp.setPublishOnce(true);                        // suppress ROS usual behavior and publish just one message
        startNode(pp, "pathPublisher_android");         // Use same nodeName to avoid concurrent robotCommands
    }

    // ---- ROS initiation (linking nodes started here to ROSmaster of the robot) ----------

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        // Concurrency issues: for publishers & subscribers to start on demand
        // NodeMaineExecutor ... inherited from RosActivity, must be available throughout app
        // nodeConfiguration ... declared above, but configured here
        // each node must have a unique nodeName (expect for rcPublisher, explained above)

        this.nodeMainExecutor = nodeMainExecutor;
        nodeConfiguration = NodeConfiguration.newPublic(getRosHostname(), getMasterUri());

        // start all ROS listeners (that need to be started on App start)
        // IMPORTANT: Node name must NOT contain spaces
        startNode(robotJobStatusListener, "rjsListener_android");
        startNode(gpsListener, "gpsListener_android");
        startNode(batteryInfoView, "batteryListener_android");
        startNode(speedInfoView, "speedListener_android");
        startNode(liveCamView, "LiveFeedListener_android");
        // Other Listener

    }

    // Utility method: start ROS node (withing ROS domain) .. be it publisher or subscriber
    protected void startNode(NodeMain node, String nodeName) {
        // Assign new name and Start Node
        nodeConfiguration.setNodeName(nodeName);
        nodeMainExecutor.execute(node, nodeConfiguration);
    }

    public void captureTime(){
        command_time = Time.fromMillis(System.currentTimeMillis());
        // Time is a rosjava object, combine java and ROS features and methods
        // fromMillis = capture current time in milli seconds
    }


}
