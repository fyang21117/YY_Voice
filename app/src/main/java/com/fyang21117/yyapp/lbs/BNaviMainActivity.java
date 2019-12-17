package com.fyang21117.yyapp.lbs;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.mapapi.bikenavi.BikeNavigateHelper;
import com.baidu.mapapi.bikenavi.adapter.IBEngineInitListener;
import com.baidu.mapapi.bikenavi.adapter.IBRoutePlanListener;
import com.baidu.mapapi.bikenavi.model.BikeRoutePlanError;
import com.baidu.mapapi.bikenavi.params.BikeNaviLaunchParam;
import com.baidu.mapapi.bikenavi.params.BikeRouteNodeInfo;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.walknavi.WalkNavigateHelper;
import com.baidu.mapapi.walknavi.adapter.IWEngineInitListener;
import com.baidu.mapapi.walknavi.adapter.IWRoutePlanListener;
import com.baidu.mapapi.walknavi.model.WalkRoutePlanError;
import com.baidu.mapapi.walknavi.params.WalkNaviLaunchParam;
import com.baidu.mapapi.walknavi.params.WalkRouteNodeInfo;
import com.fyang21117.yyapp.R;

import java.util.ArrayList;

import static com.fyang21117.yyapp.MainActivity.showTip;

public class BNaviMainActivity extends Activity implements OnGetGeoCoderResultListener {

    private final static String TAG = BNaviMainActivity.class.getSimpleName();

    private MapView mMapView;
    private BaiduMap mBaiduMap;

    /*导航起终点Marker，可拖动改变起终点的坐标*/
    private Marker mStartMarker;
    private Marker mEndMarker;

    private LatLng startPt;
    private LatLng endPt;
    private double startPtLat;
    private double startPtLoc;
    private double endPtLat;
    private double endPtLoc;
    private GeoCoder mCoder;
    private BikeNaviLaunchParam bikeParam;
    private WalkNaviLaunchParam walkParam;

    private static boolean isPermissionRequested = false;

    private BitmapDescriptor bdStart = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_start);
    private BitmapDescriptor bdEnd = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_end);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_main);
        requestPermission();
        mMapView = (MapView) findViewById(R.id.mapview);
        initMapStatus();

        /*骑行导航入口*/
        Button bikeBtn = (Button) findViewById(R.id.btn_bikenavi);
        bikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBikeNavi();
            }
        });

        /*普通步行导航入口*/
        Button walkBtn = (Button) findViewById(R.id.btn_walknavi_normal);
        walkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                walkParam.extraNaviMode(0);
                startWalkNavi();
            }
        });

        /*AR步行导航入口*/
        Button arWalkBtn = (Button) findViewById(R.id.btn_walknavi_ar);
        arWalkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                walkParam.extraNaviMode(1);
                startWalkNavi();
            }
        });

        //顺德瑞德经纬度(22.853001,113.220444);
        startPt = new LatLng(22.853001,113.220444);
        endPt = new LatLng(22.843011,113.220000);

        startPtLat = 22.853001;
        startPtLoc = 113.220444;


//        //北京西二旗地铁站
//        startPt = new LatLng(40.057038,116.307899);
//        endPt = new LatLng(40.035916, 116.340722);

//        Bundle bundle=this.getIntent().getExtras();
//        startPtLat = bundle.getDouble("startLat");
//        startPtLoc = bundle.getDouble("startLoc");
//        Log.e("BNaviMainActivity","startPtlat:"+startPtLat+"\nstartPtloc:"+startPtLoc);
//        String endpt = bundle.getString("endPt");


//        //2019年12月16日10:17:56添加地理编码
//        mCoder = GeoCoder.newInstance();
//        mCoder.setOnGetGeoCodeResultListener(listener);
//        mCoder.geocode(new GeoCodeOption()
//                .city("广州"));
//                //.address(endpt));
//        mCoder.reverseGeoCode(new ReverseGeoCodeOption().location(endPt));

        //语音导航传递经纬度参数
//        startPt = new LatLng(startPtLat,startPtLoc);
//        endPt = new LatLng(endPtLat, endPtLoc);


        /*构造导航起终点参数对象*/
        BikeRouteNodeInfo bikeStartNode = new BikeRouteNodeInfo();
        bikeStartNode.setLocation(startPt);
        BikeRouteNodeInfo bikeEndNode = new BikeRouteNodeInfo();
        bikeEndNode.setLocation(endPt);
        bikeParam = new BikeNaviLaunchParam().startNodeInfo(bikeStartNode).endNodeInfo(bikeEndNode);

        WalkRouteNodeInfo walkStartNode = new WalkRouteNodeInfo();
        walkStartNode.setLocation(startPt);
        WalkRouteNodeInfo walkEndNode = new WalkRouteNodeInfo();
        walkEndNode.setLocation(endPt);
        walkParam = new WalkNaviLaunchParam().startNodeInfo(walkStartNode).endNodeInfo(walkEndNode);

        /* 初始化起终点Marker */
        initOverlay();
    }


    //2019年12月16日10:17:56添加
    OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
        @Override
        public  void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
            if (null != geoCodeResult && null != geoCodeResult.getLocation()) {
                if (geoCodeResult == null || geoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(BNaviMainActivity.this,"没有检索到结果，请返回重试",Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    endPtLat = geoCodeResult.getLocation().latitude;
                    endPtLoc = geoCodeResult.getLocation().longitude;
                }
            }
        }
        @Override
        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        }
    };


    @Override
    public  void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
        if (null != geoCodeResult && null != geoCodeResult.getLocation()) {
        if (geoCodeResult == null || geoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
            showTip("没有检索到结果，请返回重试");
            return;
        } else {
            endPtLat = geoCodeResult.getLocation().latitude;
            endPtLoc = geoCodeResult.getLocation().longitude;
        }
    } }
    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {    }

    /**
     * 初始化地图状态
     */
    private void initMapStatus(){
        mBaiduMap = mMapView.getMap();
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(new LatLng(22.853001,113.220444)).zoom(15);
//        builder.target(new LatLng(40.048424, 116.313513)).zoom(15);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }

    /**
     * 初始化导航起终点Marker
     */
    public void initOverlay() {
        MarkerOptions ooA = new MarkerOptions().position(startPt).icon(bdStart).zIndex(9).draggable(true);
        mStartMarker = (Marker) (mBaiduMap.addOverlay(ooA));
        mStartMarker.setDraggable(true);

        MarkerOptions ooB = new MarkerOptions().position(endPt).icon(bdEnd).zIndex(5);
        mEndMarker = (Marker) (mBaiduMap.addOverlay(ooB));
        mEndMarker.setDraggable(true);


        //拖动起终点，获取经纬度
        mBaiduMap.setOnMarkerDragListener(new BaiduMap.OnMarkerDragListener() {
            public void onMarkerDrag(Marker marker) {            }

            public void onMarkerDragEnd(Marker marker) {
                if(marker == mStartMarker){
                    startPt = marker.getPosition();
                }else if(marker == mEndMarker){
                    endPt = marker.getPosition();
                }

                BikeRouteNodeInfo bikeStartNode = new BikeRouteNodeInfo();
                bikeStartNode.setLocation(startPt);
                BikeRouteNodeInfo bikeEndNode = new BikeRouteNodeInfo();
                bikeEndNode.setLocation(endPt);
                bikeParam = new BikeNaviLaunchParam().startNodeInfo(bikeStartNode).endNodeInfo(bikeEndNode);

                WalkRouteNodeInfo walkStartNode = new WalkRouteNodeInfo();
                walkStartNode.setLocation(startPt);
                WalkRouteNodeInfo walkEndNode = new WalkRouteNodeInfo();
                walkEndNode.setLocation(endPt);
                walkParam = new WalkNaviLaunchParam().startNodeInfo(walkStartNode).endNodeInfo(walkEndNode);
            }

            public void onMarkerDragStart(Marker marker) {            }
        });
    }

    /**
     * 开始骑行导航
     */
    private void startBikeNavi() {
        try {
            BikeNavigateHelper.getInstance().initNaviEngine(this, new IBEngineInitListener() {
                @Override
                public void engineInitSuccess() {
                    showTip("BikeNavi engineInitSuccess");
                    routePlanWithBikeParam();
                }
                @Override
                public void engineInitFail() {
                    showTip("BikeNavi engineInitFail");
                    BikeNavigateHelper.getInstance().unInitNaviEngine();
                }
            });
        } catch (Exception e) {
            showTip("startBikeNavi Exception");
            e.printStackTrace();
        }
    }

    /**
     * 开始步行导航
     */
    private void startWalkNavi() {
        try {
            WalkNavigateHelper.getInstance().initNaviEngine(this, new IWEngineInitListener() {
                @Override
                public void engineInitSuccess() {
                    showTip("WalkNavi engineInitSuccess");
                    routePlanWithWalkParam();
                }
                @Override
                public void engineInitFail() {
                    showTip("WalkNavi engineInitFail");
                    WalkNavigateHelper.getInstance().unInitNaviEngine();
                }
            });
        } catch (Exception e) {
            showTip("startWalkNavi Exception");
            e.printStackTrace();
        }
    }

    /**
     * 发起骑行导航算路
     */
    private void routePlanWithBikeParam() {
        BikeNavigateHelper.getInstance().routePlanWithRouteNode(bikeParam, new IBRoutePlanListener() {
            @Override
            public void onRoutePlanStart() {
                showTip("BikeNavi onRoutePlanStart");

            }
            @Override
            public void onRoutePlanSuccess() {
                showTip("BikeNavi onRoutePlanSuccess");
                Intent intent = new Intent(BNaviMainActivity.this, BNaviGuideActivity.class);
                startActivity(intent);
            }
            @Override
            public void onRoutePlanFail(BikeRoutePlanError error) {
                showTip("BikeNavi onRoutePlanFail");
            }
        });
    }

    /**
     * 发起步行导航算路
     */
    private void routePlanWithWalkParam() {
        WalkNavigateHelper.getInstance().routePlanWithRouteNode(walkParam, new IWRoutePlanListener() {
            @Override
            public void onRoutePlanStart() {
                showTip("WalkNavi onRoutePlanStart");
            }
            @Override
            public void onRoutePlanSuccess() {
                showTip("WalkNavi onRoutePlanSuccess");
                Intent intent = new Intent(BNaviMainActivity.this, WNaviGuideActivity.class);
                startActivity(intent);
            }
            @Override
            public void onRoutePlanFail(WalkRoutePlanError error) {
                showTip("WalkNavi onRoutePlanFail");
            }
        });
    }

    /**
     * Android6.0之后需要动态申请权限
     */
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= 23 && !isPermissionRequested) {
            isPermissionRequested = true;
            ArrayList<String> permissionsList = new ArrayList<>();
            String[] permissions = {
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS,
                    Manifest.permission.WRITE_SETTINGS,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_MULTICAST_STATE
            };

            for (String perm : permissions) {
                if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(perm)) {
                    permissionsList.add(perm);
                    // 进入到这里代表没有权限.
                }
            }
            if (permissionsList.isEmpty()) {
                return;
            } else {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), 0);
            }
        }
    }

    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        bdStart.recycle();
        bdEnd.recycle();
        //mCoder.destroy();
    }
}
