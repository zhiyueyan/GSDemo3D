package com.dji.GSDemo.GaodeMap;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;

import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.VisionDetectionState;
import dji.common.flightcontroller.imu.IMUState;
import dji.common.flightcontroller.imu.SensorState;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionDownloadEvent;
import dji.common.mission.waypoint.WaypointMissionExecutionEvent;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.common.mission.waypoint.WaypointMissionUploadEvent;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.Compass;
import dji.sdk.flightcontroller.FlightAssistant;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.useraccount.UserAccountManager;
import dji.ui.widget.PreFlightStatusWidget;

import static com.dji.GSDemo.GaodeMap.PositionUtil.checkGpsCoordination;
import static com.dji.GSDemo.GaodeMap.PositionUtil.coordinateTransform;

public class MainActivity extends FragmentActivity implements View.OnClickListener, AMap.OnMapClickListener,DrawerLayout.DrawerListener{

    protected static final String TAG = "MainActivity";
    private static final int CLEAR = 1;
    private static final int LOCATE = 2;
    private static final int CURRENT_INFORMATION = 3;

    private MapView mapView;
    private AMap aMap;

    private Button locate, add, clear,revoke;
    private Button config, upload, start, stop;
    private TextView infrmationTV;
    private DrawerLayout drawerLayout;
    private ScrollView scrollView;
    private PreFlightStatusWidget flightStatusWidget;

    private boolean isAdd = false;
    private boolean isFlying = false;
    private boolean isDrawerOpen = false;
    private boolean isSensorUsed,isUltrasonicUsed;

    private int IMUCount;
    private int flightTime;
    private double droneLocationLat = 181, droneLocationLng = 181;
    private double droneLocationHeight;


    private float heading,droneVelocityX,droneVelocityY,droneVelocityZ;
    private float ultrasonicHeight;
    private String information;
    private String info;

    private final Map<Integer, Marker> mMarkers = new ConcurrentHashMap<>();
    private Marker droneMarker,markerDot= null;
    private WindowManager wmManager;
    private View view;

    private float altitude = 100.0f;
    private float mSpeed = 10.0f;
    private float mRadius = 5f;

    private List<Waypoint> waypointList = new ArrayList<>();
    private List<Float> Altitude = new ArrayList<>();

    public static WaypointMission.Builder waypointMissionBuilder;
    private FlightController mFlightController;
    private Waypoint mWaypoint;
    private Compass compass;
    private FlightAssistant flightAssistant;
    private WaypointMissionOperator instance;
    private WaypointMissionFinishedAction mFinishedAction = WaypointMissionFinishedAction.NO_ACTION;
    private WaypointMissionHeadingMode mHeadingMode = WaypointMissionHeadingMode.AUTO;
    private WaypointMissionFlightPathMode mFlightPathMode = WaypointMissionFlightPathMode.NORMAL;
    private Handler handler;

    private SensorState GyroscopeState;
    private SensorState AccelerometerState;

    @Override
    protected void onResume(){
        super.onResume();
        initFlightController();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onDestroy(){
        unregisterReceiver(mReceiver);
        removeListener();
        wmManager.removeView(view);
        super.onDestroy();
    }

    /**
     * @Description : RETURN Button RESPONSE FUNCTION
     */
    public void onReturn(View view){
        Log.d(TAG, "onReturn");
        this.finish();
    }

    private void setResultToToast(final String string){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, string, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initUI() {

        locate = (Button) findViewById(R.id.locate);
        //设置透明度
        locate.getBackground().setAlpha(150);
        add = (Button) findViewById(R.id.add);
        add.getBackground().setAlpha(150);
        clear = (Button) findViewById(R.id.clear);
        clear.getBackground().setAlpha(150);
        revoke = (Button) findViewById(R.id.revoke);
        revoke.getBackground().setAlpha(150);
        config = (Button) findViewById(R.id.config);
        config.getBackground().setAlpha(150);
        upload = (Button) findViewById(R.id.upload);
        upload.getBackground().setAlpha(150);
        start = (Button) findViewById(R.id.start);
        start.getBackground().setAlpha(150);
        stop = (Button) findViewById(R.id.stop);
        stop.getBackground().setAlpha(150);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        infrmationTV = (TextView) findViewById(R.id.infrmation);
        flightStatusWidget = (PreFlightStatusWidget) findViewById(R.id.flightStatus);
        scrollView = (ScrollView) findViewById(R.id.menu_btn);

        locate.setOnClickListener(this);
        add.setOnClickListener(this);
        clear.setOnClickListener(this);
        revoke.setOnClickListener(this);
        config.setOnClickListener(this);
        upload.setOnClickListener(this);
        start.setOnClickListener(this);
        stop.setOnClickListener(this);
        flightStatusWidget.setOnClickListener(this);
        drawerLayout.setDrawerListener(this);
    }

    private void initWindow(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                wmManager=(WindowManager) getSystemService(Context.WINDOW_SERVICE);
                //设置窗口属性
                WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
                wmParams.type = WindowManager.LayoutParams.TYPE_BASE_APPLICATION;
                wmParams.gravity = Gravity.END| Gravity.TOP;
                wmParams.x = 0;// 以屏幕右上角为原点，设置x、y初始值
                wmParams.y = 50;
                wmParams.width = 200;//WindowManager.LayoutParams.WRAP_CONTENT;// 设置悬浮窗口长宽数据
                wmParams.height = 140;//WindowManager.LayoutParams.WRAP_CONTENT;
                wmParams.flags= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                wmParams.alpha = 1;
                view = View.inflate(getApplicationContext(), R.layout.fpv_layout,null);
                wmManager.addView(view,wmParams);
            }
        });
    }

    private void initMapView() {

        if (aMap == null) {
            aMap = mapView.getMap();
            aMap.setOnMapClickListener(this);// add the listener for click for amap object
        }

        LatLng nanjing = new LatLng(32.04, 118.78);
        //aMap.addMarker(new MarkerOptions().position(nanjing).title("Marker in nanjing"));
        aMap.moveCamera(CameraUpdateFactory.newLatLng(nanjing));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // When the compile and target version is higher than 22, please request the
        // following permissions at runtime to ensure the
        // SDK work well.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.VIBRATE,
                            Manifest.permission.INTERNET, Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.WAKE_LOCK, Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW,
                            Manifest.permission.READ_PHONE_STATE,
                    }
                    , 1);
        }
        //取消状态栏，必须写在setContentView之前
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter();
        filter.addAction(DJIDemoApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);

        //初始化地图
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);


        initMapView();
        initUI();
        addListener();
        initWindow();
        handler = new Handler(){
            public void handleMessage(Message msg){
                switch(msg.what){
                    case CLEAR:
                        if (mMarkers.size()>0) {
                            aMap.clear();
                            waypointList.clear();
                            mMarkers.clear();
                            waypointMissionBuilder.waypointList(waypointList);
                            if (Altitude.size()>0){
                                Altitude.clear();
                            }
                            //updateDroneLocation();
                            setResultToToast("清除成功");
                        }else {
                            setResultToToast("没有航点，无法删除");
                        }
                    case LOCATE:
                        updateDroneLocation();
                        cameraUpdate(); // Locate the drone's place

                    case CURRENT_INFORMATION:
                        information = getInformation();
                        infrmationTV.setText(information);
                    default:
                        break;
                }
            }
        };
    }

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            onProductConnectionChange();
        }
    };

    private void onProductConnectionChange() {
        initFlightController();
        loginAccount();
    }

    private void loginAccount(){

        UserAccountManager.getInstance().logIntoDJIUserAccount(this,
                new CommonCallbacks.CompletionCallbackWith<UserAccountState>() {
                    @Override
                    public void onSuccess(final UserAccountState userAccountState) {
                        Log.e(TAG, "Login Success");
                    }
                    @Override
                    public void onFailure(DJIError error) {
                        setResultToToast("Login Error:"
                                + error.getDescription());
                    }
                });
    }

    private void initFlightController() {

        BaseProduct product = DJIDemoApplication.getProductInstance();
        if (product != null && product.isConnected()) {
            if (product instanceof Aircraft) {
                mFlightController = ((Aircraft) product).getFlightController();
            }
        }

        if (mFlightController != null) {
            compass = mFlightController.getCompass();
            flightAssistant = mFlightController.getFlightAssistant();
            IMUCount = mFlightController.getIMUCount();

            mFlightController.setStateCallback(new FlightControllerState.Callback() {
                @Override
                public void onUpdate(@NonNull FlightControllerState currentState) {
                    //实时获取无人机传感器数据
                    droneLocationLat = currentState.getAircraftLocation().getLatitude();
                    droneLocationLng = currentState.getAircraftLocation().getLongitude();
                    droneLocationHeight = currentState.getAircraftLocation().getAltitude();
                    droneVelocityX = currentState.getVelocityX();
                    droneVelocityY = currentState.getVelocityY();
                    droneVelocityZ = currentState.getVelocityZ();
                    heading = compass.getHeading();
                    isUltrasonicUsed = currentState.isUltrasonicBeingUsed();
                    ultrasonicHeight = currentState.getUltrasonicHeightInMeters();
                    updateDroneLocation();
                }
            });
            mFlightController.setIMUStateCallback(new IMUState.Callback() {
                @Override
                public void onUpdate(@NonNull IMUState imuState) {
                    AccelerometerState = imuState.getAccelerometerState();
                    GyroscopeState = imuState.getGyroscopeState();
                }
            });


            flightAssistant.setVisionDetectionStateUpdatedCallback(new VisionDetectionState.Callback() {
                @Override
                public void onUpdate(@NonNull VisionDetectionState visionDetectionState) {
                    isSensorUsed = visionDetectionState.isSensorBeingUsed();
                }
            });
        }
    }

    //Add Listener for WaypointMissionOperator
    private void addListener() {
        if (getWaypointMissionOperator() != null) {
            getWaypointMissionOperator().addListener(eventNotificationListener);
        }
    }

    private void removeListener() {
        if (getWaypointMissionOperator() != null) {
            getWaypointMissionOperator().removeListener(eventNotificationListener);
        }
    }

    private WaypointMissionOperatorListener eventNotificationListener = new WaypointMissionOperatorListener() {
        @Override
        public void onDownloadUpdate(@NonNull WaypointMissionDownloadEvent downloadEvent) {

        }

        @Override
        public void onUploadUpdate(@NonNull WaypointMissionUploadEvent uploadEvent) {

        }

        @Override
        public void onExecutionUpdate(@NonNull WaypointMissionExecutionEvent executionEvent) {

        }

        @Override
        public void onExecutionStart() {

        }

        @Override
        public void onExecutionFinish(@Nullable final DJIError error) {
            //setResultToToast("Execution finished: " + (error == null ? "Success!" : error.getDescription()));
            if (error == null){
                setResultToToast("Execution finished: Success!");
                isFlying = false;
            }else {
                setResultToToast("Execution finished: " +error.getDescription());
            }
        }
    };

    public WaypointMissionOperator getWaypointMissionOperator() {
        if (instance == null) {
            instance = DJISDKManager.getInstance().getMissionControl().getWaypointMissionOperator();
        }
        return instance;
    }

    @Override
    public void onMapClick(LatLng point) {
        //地图点击事件
        if (isAdd){
            markWaypoint(point);
            LatLng pointAfter = PositionUtil.gcj_To_Gps84(point.latitude,point.longitude);
            mWaypoint = new Waypoint(pointAfter.latitude, pointAfter.longitude, altitude);
            //Add Waypoints to Waypoint arraylist;
            missionBuild(mWaypoint);
        }else{
            setResultToToast("Cannot Add Waypoint");
        }
    }

    // Update the drone location based on states from MCU.
    private void updateDroneLocation(){
        LatLng posBefore = new LatLng(droneLocationLat, droneLocationLng);
        LatLng posAfter = coordinateTransform(posBefore,this);
        LatLng posDotAfter = coordinateTransform(posBefore,this);
        //Create MarkerOptions object
        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(posAfter);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.aircraft));
        final MarkerOptions markerOptionsDot = new MarkerOptions();
        markerOptionsDot.position(posDotAfter);
        markerOptionsDot.icon(BitmapDescriptorFactory.fromResource(R.drawable.bluedot));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (droneMarker != null) {
                    droneMarker.remove();
                }

                if (checkGpsCoordination(droneLocationLat, droneLocationLng)) {
                    droneMarker = aMap.addMarker(markerOptions);
                    if (isFlying) {
                        markerDot = aMap.addMarker(markerOptionsDot);
                    }
                }
            }
        });
    }

    private void markWaypoint(LatLng point){
        //Create MarkerOptions object
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        Marker marker = aMap.addMarker(markerOptions);
        //marker.setDraggable(true);
        mMarkers.put(mMarkers.size(), marker);
        //如果第一个不设置高度，则认为后面的点都不单独设置高度，在config里统一设置
        if ((mMarkers.size()>1 && !Altitude.isEmpty()) || mMarkers.size() == 1) {
            //这里不能用Alititude.get(0)来判断
            //否则会出现空指针异常
            setAltitude();
        }
    }

    private void markCurrentLocation(){
        LatLng currentLocation = new LatLng(droneLocationLat,droneLocationLng);
        LatLng currentLocationAfter = coordinateTransform(currentLocation,this);
        markWaypoint(currentLocationAfter);
        //setAltitude();
        Waypoint point = new Waypoint(currentLocation.latitude,currentLocation.longitude,altitude);
        missionBuild(point);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.locate:{
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message message = new Message();
                        message.what = LOCATE;
                        handler.sendMessage(message);
                    }
                }).start();
                break;
            }
            case R.id.add:{
                enableDisableAdd();
                break;
            }
            case R.id.clear: {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message message = new Message();
                        message.what = CLEAR;
                        handler.sendMessage(message);
                    }
                }).start();
                break;
            }
            case R.id.revoke:{
                onRevokeClick();
                break;
            }
            case R.id.config:{
                showSettingDialog();
                break;
            }
            case R.id.upload:{
                uploadWayPointMission();
                break;
            }
            case R.id.start:{
                startWaypointMission();
                break;
            }
            case R.id.stop:{
                stopWaypointMission();
                break;
            }
            case R.id.flightStatus:{
                if (scrollView.getVisibility() == View.VISIBLE){
                    scrollView.setVisibility(View.INVISIBLE);
                }else {
                    scrollView.setVisibility(View.VISIBLE);
                }
            }
            default:
                break;
        }
    }

    private void showSettingDialog(){
        //设置航点任务
        LinearLayout wayPointSettings = (LinearLayout)getLayoutInflater().inflate(R.layout.dialog_waypointsetting, null);
        LinearLayout altitude_Layout = (LinearLayout) wayPointSettings.findViewById(R.id.altitude_layout);
        //如果每个点都设置了高度，则这个控件不需要显示出来
        if (!Altitude.isEmpty()){
            altitude_Layout.setVisibility(View.GONE);
        }
        final EditText wpAltitude_TV = (EditText) wayPointSettings.findViewById(R.id.altitude);
        RadioGroup speed_RG = (RadioGroup) wayPointSettings.findViewById(R.id.speed);
        RadioGroup actionAfterFinished_RG = (RadioGroup) wayPointSettings.findViewById(R.id.actionAfterFinished);
        RadioGroup heading_RG = (RadioGroup) wayPointSettings.findViewById(R.id.heading);
        RadioGroup curved_RG = (RadioGroup) wayPointSettings.findViewById(R.id.curved_or_normal);
        final TextView radius_tv = (TextView) wayPointSettings.findViewById(R.id.radius_tv);
        final EditText radius = (EditText) wayPointSettings.findViewById(R.id.radius);

        curved_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkId) {
                if (checkId == R.id.curved){
                    //如果是弯曲路径，则设置半径的文本框需要显示出来
                    radius_tv.setVisibility(View.VISIBLE);
                    radius.setVisibility(View.VISIBLE);
                    mFlightPathMode = WaypointMissionFlightPathMode.CURVED;
                }else {
                    radius_tv.setVisibility(View.INVISIBLE);
                    radius.setVisibility(View.INVISIBLE);
                    mFlightPathMode = WaypointMissionFlightPathMode.NORMAL;
                }
            }
        });

        speed_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.lowSpeed){
                    mSpeed = 3.0f;
                } else if (checkedId == R.id.MidSpeed){
                    mSpeed = 5.0f;
                } else if (checkedId == R.id.HighSpeed){
                    mSpeed = 10.0f;
                }
            }
        });

        actionAfterFinished_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d(TAG, "Select finish action");
                if (checkedId == R.id.finishNone){
                    mFinishedAction = WaypointMissionFinishedAction.NO_ACTION;
                } else if (checkedId == R.id.finishGoHome){
                    mFinishedAction = WaypointMissionFinishedAction.GO_HOME;
                } else if (checkedId == R.id.finishAutoLanding){
                    mFinishedAction = WaypointMissionFinishedAction.AUTO_LAND;
                } else if (checkedId == R.id.finishToFirst){
                    mFinishedAction = WaypointMissionFinishedAction.GO_FIRST_WAYPOINT;
                }
            }
        });

        heading_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d(TAG, "Select heading");

                if (checkedId == R.id.headingNext) {
                    mHeadingMode = WaypointMissionHeadingMode.AUTO;
                } else if (checkedId == R.id.headingInitDirec) {
                    mHeadingMode = WaypointMissionHeadingMode.USING_INITIAL_DIRECTION;
                } else if (checkedId == R.id.headingRC) {
                    mHeadingMode = WaypointMissionHeadingMode.CONTROL_BY_REMOTE_CONTROLLER;
                } else if (checkedId == R.id.headingWP) {
                    mHeadingMode = WaypointMissionHeadingMode.USING_WAYPOINT_HEADING;
                }
            }
        });

        new AlertDialog.Builder(this)
                .setTitle("")
                .setView(wayPointSettings)
                .setPositiveButton("Finish",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id) {

                        String altitudeString = wpAltitude_TV.getText().toString();
                        altitude = Integer.parseInt(Util.nulltoIntegerDefalt(altitudeString));
                        if (radius.getText()!=null && !radius.getText().toString().equals("")) {
                            mRadius = Float.parseFloat(radius.getText().toString());
                        }
                        Log.e(TAG,"altitude "+altitude);
                        Log.e(TAG,"speed "+mSpeed);
                        Log.e(TAG, "mFinishedAction "+mFinishedAction);
                        Log.e(TAG, "mHeadingMode "+mHeadingMode);
                        configWayPointMission();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }

                })
                .create()
                .show();
    }

    private void configWayPointMission(){

        if (waypointMissionBuilder == null){

            waypointMissionBuilder = new WaypointMission.Builder().finishedAction(mFinishedAction)
                    .headingMode(mHeadingMode)
                    .autoFlightSpeed(mSpeed)
                    .maxFlightSpeed(mSpeed)
                    .flightPathMode(mFlightPathMode);

        }else {
            waypointMissionBuilder.finishedAction(mFinishedAction)
                    .headingMode(mHeadingMode)
                    .autoFlightSpeed(mSpeed)
                    .maxFlightSpeed(mSpeed)
                    .flightPathMode(mFlightPathMode);

        }

        if (waypointMissionBuilder.getWaypointList().size() > 0){

            //判断高度是否已经设置
            if (Altitude.size()>0){
                for (int i=0; i< waypointMissionBuilder.getWaypointList().size(); i++){
                    waypointMissionBuilder.getWaypointList().get(i).altitude = Altitude.get(i);
                    if (waypointMissionBuilder.getFlightPathMode() == WaypointMissionFlightPathMode.CURVED) {
                        waypointMissionBuilder.getWaypointList().get(i).cornerRadiusInMeters = mRadius;
                    }
                }
            }else {
                for (int i=0; i< waypointMissionBuilder.getWaypointList().size(); i++){
                    waypointMissionBuilder.getWaypointList().get(i).altitude = altitude;
                    if (waypointMissionBuilder.getFlightPathMode() == WaypointMissionFlightPathMode.CURVED) {
                        waypointMissionBuilder.getWaypointList().get(i).cornerRadiusInMeters = mRadius;
                    }
                }
            }
            setResultToToast("Set Waypoint attitude successfully");
        }

        DJIError error = getWaypointMissionOperator().loadMission(waypointMissionBuilder.build());
        if (error == null) {
            setResultToToast("loadWaypoint succeeded");
        } else {
            setResultToToast("loadWaypoint failed " + error.getDescription());
        }

    }

    private void uploadWayPointMission(){

        getWaypointMissionOperator().uploadMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                if (error == null) {
                    setResultToToast("Mission upload successfully!");
                } else {
                    setResultToToast("Mission upload failed, error: " + error.getDescription() + " retrying...");
                    getWaypointMissionOperator().retryUploadMission(null);
                }
            }
        });
    }

    private void startWaypointMission(){
        getWaypointMissionOperator().startMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                //setResultToToast("Mission Start: " + (error == null ? "Successfully" : error.getDescription()));
                if (error == null){
                    setResultToToast("Mission Start: Success" );
                    isFlying = true;
                    flightTime = 0;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while(isFlying){
                                info = getInformation();
                                Util.saveInfo(info);
                                flightTime++;
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();
                }else {
                    setResultToToast("Mission Start: "+error.getDescription());
                }
            }
        });
    }

    private void stopWaypointMission(){
        getWaypointMissionOperator().stopMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                //setResultToToast("Mission Stop: " + (error == null ? "Successfully" : error.getDescription()));
                if (error == null){
                    setResultToToast("Mission Stop: Successfully!");
                    isFlying = false;
                }else {
                    setResultToToast("Mission Stop" + error.getDescription());
                }
            }
        });

    }

    private void enableDisableAdd(){
        if (!isAdd) {
            isAdd = true;
            add.setText("Exit");
            markCurrentLocation();
        }else{
            isAdd = false;
            add.setText("Add");
        }
    }

    private void cameraUpdate(){
        LatLng pos = new LatLng(droneLocationLat, droneLocationLng);
        LatLng posAfter = coordinateTransform(pos,this);
        float zoomLevel = (float) 18.0;
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(posAfter, zoomLevel);
        aMap.moveCamera(cu);
    }

    private void onRevokeClick(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mMarkers.size()>0) {
                    mMarkers.get(mMarkers.size() - 1).destroy();
                    mMarkers.remove(mMarkers.size() - 1);
                    //如果没有设置高度，则Altitude的list中不需要删除
                    if (!Altitude.isEmpty()) {
                        Altitude.remove(Altitude.size() - 1);
                    }
                    waypointList.remove(waypointList.size()-1);
                    waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
                    updateDroneLocation();
                    setResultToToast("撤销成功");
                }else {
                    setResultToToast("没有航点，无法删除");
                }
            }
        });
    }

    private void missionBuild(Waypoint point){
        if (waypointMissionBuilder != null) {
            waypointList.add(point);
            waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
        }else {
            waypointMissionBuilder = new WaypointMission.Builder();
            waypointList.add(point);
            waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
        }
    }

    private String getInformation(){
        StringBuffer sb = new StringBuffer();
        sb.append("Time :").append(Util.getCurrentTime()).append("\n");
        sb.append("Latitude :").append(Util.format7f(droneLocationLat)).append("\n");
        sb.append("Longitude :").append(Util.format7f(droneLocationLng)).append("\n");
        sb.append("Altitude :").append(Util.format2f(droneLocationHeight)).append("m").append("\n");
        sb.append("Heading :").append(Util.getDirection(heading)).append("\n");
        sb.append("VelocityX :").append(Util.format2f(droneVelocityX)).append("m/s").append("\n");
        sb.append("VelocityY :").append(Util.format2f(droneVelocityY)).append("m/s").append("\n");
        sb.append("VelocityZ :").append(Util.format2f(droneVelocityZ)).append("m/s").append("\n");
        sb.append("Velocity :").append(Util.format2f(Math.sqrt(
                droneVelocityX*droneVelocityX
                        +droneVelocityY*droneVelocityY
                        +droneVelocityZ*droneVelocityZ))).append("m/s").append("\n");
        sb.append("FlightTime :").append(flightTime).append("s").append("\n");
        sb.append("IMUCount :").append(IMUCount).append("\n");
        sb.append("UltrasonicHeight :").append(Util.format2f(ultrasonicHeight)).append("(5米)").append("\n");
        sb.append("isSensorUsed :").append(isSensorUsed).append("\n");
        sb.append("isUltrasonicUsed :").append(isUltrasonicUsed).append("\n");
        sb.append("GyroscopeState :").append(GyroscopeState).append("\n");
        sb.append("AccelerometerState :").append(AccelerometerState).append("\n");
        sb.append("\n");
        return sb.toString();
    }

    private void setAltitude(){
        RelativeLayout relativeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.altitude_layout,null);
        final EditText altitudeET = (EditText)relativeLayout.findViewById(R.id.altitudeET);
        new AlertDialog.Builder(this)
                .setTitle("Set Altitude")
                .setView(relativeLayout)
                .setPositiveButton("Finish",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id) {
                        if (altitudeET.getText() != null && !altitudeET.getText().toString().equals("")) {
                            float mAltitude = Float.parseFloat(altitudeET.getText().toString());
                            Altitude.add(mAltitude);
                            setResultToToast("Set Successfully!");
                        }else if (altitudeET.getText().toString().equals("")){
                            if (mMarkers.size() == 1) {//第一个点的高度
                                setResultToToast("Set without altitude!");
                                Altitude.clear();
                            }else {
                                float a1 = Altitude.get(mMarkers.size()-2);
                                Altitude.add(a1);
                                //后面的点如果不设置自动认为和前一个点高度相同
                                setResultToToast("Altitude equals the former one");
                            }
                        }else {
                            setResultToToast("输入有误，请重新取点");
                            mMarkers.get(mMarkers.size() - 1).destroy();
                            mMarkers.remove(mMarkers.size() - 1);
                            waypointList.remove(waypointList.size()-1);
                            waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        mMarkers.get(mMarkers.size() - 1).destroy();
                        mMarkers.remove(mMarkers.size() - 1);
                        waypointList.remove(waypointList.size()-1);
                        waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
                    }
                })
                .create()
                .show();
    }


    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(View drawerView) {
        isDrawerOpen = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isDrawerOpen) {
                    Message message = new Message();
                    message.what = CURRENT_INFORMATION;
                    handler.sendMessage(message);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void onDrawerClosed(View drawerView) {
        isDrawerOpen = false;
    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }
}
