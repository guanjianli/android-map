package com.amap.map2d.demo.overlay;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMap.InfoWindowAdapter;
import com.amap.api.maps2d.AMap.OnInfoWindowClickListener;
import com.amap.api.maps2d.AMap.OnMapLoadedListener;
import com.amap.api.maps2d.AMap.OnMarkerClickListener;
import com.amap.api.maps2d.AMap.OnMarkerDragListener;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.Projection;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.LatLngBounds;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.maps2d.model.Text;
import com.amap.api.maps2d.model.TextOptions;
import com.amap.map2d.demo.PicHandle;
import com.amap.map2d.demo.R;
import com.amap.map2d.demo.util.Constants;
import com.amap.map2d.demo.util.ToastUtil;

import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.image.QualityInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * AMapV1地图中简单介绍一些Marker的用法.
 */
public class CustomMarkerActivity extends Activity implements OnMarkerClickListener,
		OnInfoWindowClickListener, OnMarkerDragListener, OnMapLoadedListener,
		OnClickListener, InfoWindowAdapter, LocationSource, AMapLocationListener {
	private MarkerOptions markerOption;
	private TextView markerText;
	private Button markerButton;// 获取屏幕内所有marker的button
	private RadioGroup radioOption;
	private AMap aMap;
	private MapView mapView;
	private Marker marker2;// 有跳动效果的marker对象
	private LatLng latlng = new LatLng(36.061, 103.834);

	private AMapLocationClient mlocationClient;
	private OnLocationChangedListener mListener;
	private AMapLocationClientOption mLocationOption;
	private Marker myLocationMarker;
	private final int REQ_LOCATION=0x12;
	private LatLng myLocaltion;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.custommarker_activity);
		mapView = (MapView) findViewById(R.id.map);
		mapView.onCreate(savedInstanceState); // 此方法必须重写
		init();
		requestLocationPermission();

	}



	/**
	 * 初始化AMap对象
	 */
	private void init() {
		markerText = (TextView) findViewById(R.id.mark_listenter_text);
		markerButton = (Button) findViewById(R.id.marker_button);
		//markerButton.setOnClickListener(this);

		if (aMap == null) {
			aMap = mapView.getMap();
			aMap.moveCamera(CameraUpdateFactory.zoomBy(16));
			setUpMap();
		}
	}

	public void requestLocationPermission(){
		ActivityCompat.requestPermissions((Activity) this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},REQ_LOCATION);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,  @NonNull String[] permissions, @NonNull int[] grantResults) {
		if(requestCode==REQ_LOCATION){
			if(grantResults!=null&&grantResults.length>0){
				if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
					mlocationClient.startLocation();
				}else{
					Toast.makeText(CustomMarkerActivity.this,"缺少定位权限，无法完成定位~",Toast.LENGTH_LONG).show();
				}
			}
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	private void setUpMap() {

		aMap.setLocationSource(this);// 设置定位监听
		aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
		// 自定义系统定位蓝点
		MyLocationStyle myLocationStyle = new MyLocationStyle();
		// 自定义定位蓝点图标
		myLocationStyle.myLocationIcon(
				BitmapDescriptorFactory.fromResource(R.drawable.gps_point));
		// 自定义精度范围的圆形边框颜色
		myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));
		// 自定义精度范围的圆形边框宽度
		myLocationStyle.strokeWidth(0);
		// 设置圆形的填充颜色
		myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));
		// 将自定义的 myLocationStyle 对象添加到地图上
		aMap.setMyLocationStyle(myLocationStyle);
		aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false

		// 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
//		aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);

		aMap.setOnMarkerDragListener(this);// 设置marker可拖拽事件监听器
		aMap.setOnMapLoadedListener(this);// 设置amap加载成功事件监听器
		aMap.setOnMarkerClickListener(this);// 设置点击marker事件监听器
//		aMap.setOnInfoWindowClickListener(this);// 设置点击infoWindow事件监听器
		aMap.setInfoWindowAdapter(this);// 设置自定义InfoWindow样式
		addMarkersToMap();// 往地图上添加marker

	}



	/**
	 * 方法必须重写
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}



	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
	}

	public void  tsss(View view){
		Log.e("wwwwwwwwwww","tttttttttt");
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	/**
	 * 在地图上添加marker
	 */
	private void addMarkersToMap() {

/*		//文字显示标注，可以设置显示内容，位置，字体大小颜色，背景色旋转角度,Z值等
		TextOptions textOptions = new TextOptions().position(Constants.GONGSI)
				.text("TextMe").fontColor(Color.WHITE)
				.backgroundColor(Color.BLUE).fontSize(30).rotate(20).align(Text.ALIGN_CENTER_HORIZONTAL, Text.ALIGN_CENTER_VERTICAL)
				.zIndex(1.f).typeface(Typeface.DEFAULT_BOLD)
				;
		aMap.addText(textOptions);*/

	/*	aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
				.position(Constants.TIANHE1).title("龙胜苑")
				.snippet("龙胜苑玩家").draggable(true));*/

/*		markerOption = new MarkerOptions();
		markerOption.position(Constants.TIANHE2);
		markerOption.title("外国语学院东门").snippet("外国语学院东门");
		markerOption.draggable(true);
		markerOption.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.arrow));
		marker2 = aMap.addMarker(markerOption);
		marker2.showInfoWindow();
		// marker旋转90度
		marker2.setRotateAngle(90);*/

/*		// 动画效果
		ArrayList<BitmapDescriptor> giflist = new ArrayList<BitmapDescriptor>();
		giflist.add(BitmapDescriptorFactory
				.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
		giflist.add(BitmapDescriptorFactory
				.defaultMarker(BitmapDescriptorFactory.HUE_RED));
		giflist.add(BitmapDescriptorFactory
				.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
		aMap.addMarker(new MarkerOptions().anchor(0.5f, 0.5f)
				.position(Constants.TIANHE3).title("？？？").icons(giflist)
				.draggable(true).period(10));*/

		drawMarkers();// 添加10个带有系统默认icon的marker
	}

	/**
	 * 绘制系统默认的1种marker背景图片
	 */
	public void drawMarkers() {
		this.getMarker("玩家1", R.drawable.arena_1, R.drawable.head_1, Constants.TIANHE1);
		this.getMarker("玩家2", R.drawable.arena_2, R.drawable.head_2, Constants.TIANHE2);
		this.getMarker("玩家3", R.drawable.arena_3, R.drawable.head_3, Constants.TIANHE3);
	}

	public Marker getMarker(String name, int frame, int head, LatLng loc){
		View view = getLayoutInflater().inflate(R.layout.custom_info_avatar, null);
		ImageView imageView = (ImageView) view.findViewById(R.id.ava_frame);
		imageView.setImageResource(frame);
		ImageView imageView2 = (ImageView) view.findViewById(R.id.avatar);
		imageView2.setImageResource(head);
		TextView titleUi = ((TextView) view.findViewById(R.id.title));
		titleUi.setText(name);
		titleUi.setTextSize(16);

		Bitmap img = this.convertViewToBitmap(view);

		Marker marker = aMap.addMarker(new MarkerOptions()
				.position(loc)
				.title(name)
				.icon(BitmapDescriptorFactory.fromBitmap(img))
				.draggable(true)
		);
		return marker;
	}


	public void setMyAvatar(Bitmap bitmap){
		View view = getLayoutInflater().inflate(R.layout.custom_info_avatar, null);
		view.setBackgroundDrawable(null);
		if(myLocationMarker == null){
			view.setBackgroundDrawable(null);
			//draweeView.setVisibility(View.VISIBLE);
			ImageView imageView = (ImageView) view.findViewById(R.id.avatar);
			imageView.setImageBitmap(bitmap);

            ImageView imageView2 = (ImageView) view.findViewById(R.id.ava_frame);
            imageView2.setImageResource(R.drawable.arena_3);
            TextView titleUi = ((TextView) view.findViewById(R.id.title));
            titleUi.setText("我的名字");

			Bitmap img = convertViewToBitmap(view);
			Marker marker = aMap.addMarker(new MarkerOptions()
					.position(myLocaltion)
					.title("我的位置")
					.icon(BitmapDescriptorFactory.fromBitmap(img))
					.draggable(true)
			);

			myLocationMarker = marker;
		}else {
			myLocationMarker.setPosition(myLocaltion);
		}
	}

	/**
	 * 对marker标注点点击响应事件
	 */
	@Override
	public boolean onMarkerClick(final Marker marker) {
		if (marker.equals(marker2)) {
			if (aMap != null) {
				jumpPoint(marker);
			}
		}
		double la = marker.getPosition().latitude;
		double lo = marker.getPosition().longitude;
		markerText.setText("你点击的是" + marker.getTitle() + "位置是" + la + '-' +  lo);
		return false;
	}

	/**
	 * marker点击时跳动一下
	 */
	public void jumpPoint(final Marker marker) {
		final Handler handler = new Handler();
		final long start = SystemClock.uptimeMillis();
		Projection proj = aMap.getProjection();
		Point startPoint = proj.toScreenLocation(Constants.TIANHE2);
		startPoint.offset(0, -100);
		final LatLng startLatLng = proj.fromScreenLocation(startPoint);
		final long duration = 1500;

		final Interpolator interpolator = new BounceInterpolator();
		handler.post(new Runnable() {
			@Override
			public void run() {
				long elapsed = SystemClock.uptimeMillis() - start;
				float t = interpolator.getInterpolation((float) elapsed
						/ duration);
				double lng = t * Constants.TIANHE2.longitude + (1 - t)
						* startLatLng.longitude;
				double lat = t * Constants.TIANHE2.latitude + (1 - t)
						* startLatLng.latitude;
				marker.setPosition(new LatLng(lat, lng));
				aMap.invalidate();// 刷新地图
				if (t < 1.0) {
					handler.postDelayed(this, 16);
				}
			}
		});

	}

	/**
	 * 监听点击infowindow窗口事件回调
	 */
	@Override
	public void onInfoWindowClick(Marker marker) {
		ToastUtil.show(this, "你点击了infoWindow窗口" + marker.getTitle());
	}

	//view 转bitmap
	public Bitmap convertViewToBitmap(View view) {
		view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		view.buildDrawingCache();
		Bitmap bitmap = view.getDrawingCache();
		return bitmap;
	}

	/**
	 * 监听拖动marker时事件回调
	 */
	@Override
	public void onMarkerDrag(Marker marker) {
		String curDes = marker.getTitle() + "拖动时当前位置:(lat,lng)\n("
				+ marker.getPosition().latitude + ","
				+ marker.getPosition().longitude + ")";
		markerText.setText(curDes);
	}

	/**
	 * 监听拖动marker结束事件回调
	 */
	@Override
	public void onMarkerDragEnd(Marker marker) {
		markerText.setText(marker.getTitle() + "停止拖动");
	}

	/**
	 * 监听开始拖动marker事件回调
	 */
	@Override
	public void onMarkerDragStart(Marker marker) {
		markerText.setText(marker.getTitle() + "开始拖动");
	}

	/**
	 * 监听amap地图加载成功事件回调
	 */
	@Override
	public void onMapLoaded() {
		// 设置所有maker显示在当前可视区域地图中
//		LatLngBounds bounds = new LatLngBounds.Builder()
//				.include(Constants.XIAN).include(Constants.CHENGDU)
//				.include(latlng).include(Constants.ZHENGZHOU).include(Constants.BEIJING).build();
//		aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10));
	}

	/**
	 * 监听自定义infowindow窗口的infocontents事件回调
	 */
	@Override
	public View getInfoContents(Marker marker) {

		View infoContent = getLayoutInflater().inflate(
				R.layout.custom_info_contents, null);
		return infoContent;
	}

	/**
	 * 监听自定义infowindow窗口的infowindow事件回调
	 */
	@Override
	public View getInfoWindow(Marker marker) {

		View infoWindow = getLayoutInflater().inflate(
				R.layout.custom_info_none, null);
		return infoWindow;
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		/**
		 * 清空地图上所有已经标注的marker
		 */
		case R.id.clearMap:
			if (aMap != null) {
				aMap.clear();
			}
			break;
		/**
		 * 重新标注所有的marker
		 */
		case R.id.resetMap:
			if (aMap != null) {
				aMap.clear();
				addMarkersToMap();

			}
			break;
		// 获取屏幕所有marker
		case R.id.marker_button:
			if (aMap != null) {
				List<Marker> markers = aMap.getMapScreenMarkers();
				if (markers == null || markers.size() == 0) {
					ToastUtil.show(this, "当前屏幕内没有Marker");
					return;
				}
				String tile = "屏幕内有：";
				for (Marker marker : markers) {
					tile = tile + " " + marker.getTitle();

				}
				ToastUtil.show(this, tile);

			}
			break;
		default:
			break;
		}
	}


	/**
	 * 定位成功后回调函数
	 */
	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		if (mListener != null && amapLocation != null) {
			if (amapLocation != null && amapLocation.getErrorCode() == 0) {
//				tvResult.setVisibility(View.GONE);
				System.out.print("我的位置， 经度，纬度" + amapLocation.getLongitude() + "-" + amapLocation.getLatitude());
				mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
				aMap.moveCamera(CameraUpdateFactory.zoomTo(18));
				//设置我的头像
				myLocaltion = new LatLng(amapLocation.getLatitude()	, amapLocation.getLongitude());
				Uri uri = Uri.parse("https://ss0.bdstatic.com/5aV1bjqh_Q23odCf/static/superman/img/logo/logo_white_fe6da1ec.png");
				new PicHandle().setDataSubscriber(this, uri, 80, 80);
			} else {
				String errText = "定位失败," + amapLocation.getErrorCode() + ": "
						+ amapLocation.getErrorInfo();
				Log.e("AmapErr", errText);
				ToastUtil.show(this, errText);
//				tvResult.setVisibility(View.VISIBLE);
//				tvResult.setText(errText);
			}
		}
	}

	/**
	 * 激活定位
	 */
	@Override
	public void activate(OnLocationChangedListener listener) {
		mListener = listener;
		if (mlocationClient == null) {
			mlocationClient = new AMapLocationClient(this);
			mLocationOption = new AMapLocationClientOption();
			// 设置定位监听
			mlocationClient.setLocationListener(this);
			// 设置为高精度定位模式
			mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
			// 只是为了获取当前位置，所以设置为单次定位
			mLocationOption.setOnceLocation(true);
			// 设置定位参数
			mlocationClient.setLocationOption(mLocationOption);
			mlocationClient.startLocation();
		}
	}

	/**
	 * 停止定位
	 */
	@Override
	public void deactivate() {
		mListener = null;
		if (mlocationClient != null) {
			mlocationClient.stopLocation();
			mlocationClient.onDestroy();
		}
		mlocationClient = null;
	}


}
