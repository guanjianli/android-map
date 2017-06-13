package com.amap.map2d.demo;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.amap.api.maps2d.MapsInitializer;
import com.amap.map2d.demo.overlay.CustomMarkerActivity;
import com.amap.map2d.demo.overlay.InfoWindowActivity;
import com.amap.map2d.demo.overlay.MarkerActivity;
import com.amap.map2d.demo.overlay.MarkerClickActivity;
import com.amap.map2d.demo.view.FeatureView;

import java.util.Date;
import java.util.HashMap;

/**
 * AMapV1地图demo总汇
 */
public final class MainActivity extends ListActivity {
	private static class DemoDetails {
		private final int titleId;
		private final int descriptionId;
		private final Class<? extends android.app.Activity> activityClass;

		public DemoDetails(int titleId, int descriptionId,
				Class<? extends android.app.Activity> activityClass) {
			super();
			this.titleId = titleId;
			this.descriptionId = descriptionId;
			this.activityClass = activityClass;
		}
	}

	private static class CustomArrayAdapter extends ArrayAdapter<DemoDetails> {
		public CustomArrayAdapter(Context context, DemoDetails[] demos) {
			super(context, R.layout.feature, R.id.title, demos);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			FeatureView featureView;
			if (convertView instanceof FeatureView) {
				featureView = (FeatureView) convertView;
			} else {
				featureView = new FeatureView(getContext());
			}
			DemoDetails demo = getItem(position);
			featureView.setTitleId(demo.titleId, demo.activityClass!=null);
//			featureView.setDescriptionId(demo.descriptionId);
			return featureView;
		}
	}

	private static final DemoDetails[] demos = {
		//在地图上绘制
		new DemoDetails(R.string.map_overlay, R.string.blank, null),
		//绘制点
		new DemoDetails(R.string.marker_demo, R.string.marker_description,
				MarkerActivity.class),
		//marker点击回调
		new DemoDetails(R.string.marker_click, R.string.marker_click,
				MarkerClickActivity.class),
		//绘制地图上的信息窗口
		new DemoDetails(R.string.infowindow_demo, R.string.infowindow_demo, InfoWindowActivity.class),
		//绘制自定义点
		new DemoDetails(R.string.custommarker_demo, R.string.blank,
				CustomMarkerActivity.class)
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		setTitle("2D地图Demo" + MapsInitializer.getVersion());
		ListAdapter adapter = new CustomArrayAdapter(
				this.getApplicationContext(), demos);
		setListAdapter(adapter);

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		System.exit(0);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		DemoDetails demo = (DemoDetails) getListAdapter().getItem(position);
		if (demo.activityClass != null) {
			startActivity(new Intent(this.getApplicationContext(),
					demo.activityClass));
		}
	}
}
