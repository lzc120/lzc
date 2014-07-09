package cn.itcast.mobilesafe.ui;

import java.util.List;

import cn.itcast.mobilesafe.R;
import cn.itcast.mobilesafe.db.dao.AppLockDao;
import cn.itcast.mobilesafe.domain.AppInfo;
import cn.itcast.mobilesafe.engine.AppInfoProvider;
import android.app.Activity;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class AppLockActivity extends Activity {
	private ListView lv;
	private List<AppInfo> appinfos;
	private AppInfoProvider provider;
	private AppLockAdapter adapter;
	private AppLockDao dao;
	private LinearLayout ll_app_manager_loading;
	
	private List<String> lockappinfos ;
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			ll_app_manager_loading.setVisibility(View.INVISIBLE);
			adapter = new AppLockAdapter();
			lv.setAdapter(adapter);
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_lock);
		ll_app_manager_loading = (LinearLayout) this.findViewById(R.id.ll_app_manager_loading);
		dao = new AppLockDao(this);
		lockappinfos = dao.getAllApps();
		provider = new AppInfoProvider(this);
		lv = (ListView) this.findViewById(R.id.lv_app_lock);

		initUI();

		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				TranslateAnimation ta = new TranslateAnimation(
						Animation.RELATIVE_TO_SELF, 0.0f,
						Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.0f,
						Animation.RELATIVE_TO_SELF, 0.0f);
				ta.setDuration(500);
				view.startAnimation(ta);
				ImageView iv = (ImageView) view.findViewById(R.id.iv_app_lock_status);
				
				// 传递当前要锁定程序的包名
				AppInfo info = (AppInfo) lv.getItemAtPosition(position);
				String packname = info.getPackname();
				if(dao.find(packname)){
					// 移除这个条目
					//dao.delete(packname);
					getContentResolver().delete(Uri.parse("content://cn.itcast.applockprovider/delete"), null, new String[]{packname});
					lockappinfos.remove(packname);
					iv.setImageResource(R.drawable.unlock);
				}else{
					//dao.add(packname);
					lockappinfos.add(packname);
					ContentValues values = new ContentValues();
					values.put("packname", packname);
					getContentResolver().insert(Uri.parse("content://cn.itcast.applockprovider/insert"), values);
					iv.setImageResource(R.drawable.lock);
				}
		
			}
		});
	}

	private void initUI() {
		ll_app_manager_loading.setVisibility(View.VISIBLE);
		new Thread() {

			@Override
			public void run() {
				appinfos = provider.getAllApps();
				handler.sendEmptyMessage(0);
			}

		}.start();

	}

	private class AppLockAdapter extends BaseAdapter {

		public int getCount() {

			return appinfos.size();
		}

		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return appinfos.get(position);
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			if (convertView == null) {
				view = View.inflate(getApplicationContext(),
						R.layout.lock_app_item, null);
			} else {
				view = convertView;
			}
			//更改view对象的状态
			AppInfo info = appinfos.get(position);
			ImageView iv = (ImageView) view.findViewById(R.id.iv_app_icon);
			TextView tv = (TextView) view.findViewById(R.id.tv_app_name);
			ImageView iv_lock_status = (ImageView) view.findViewById(R.id.iv_app_lock_status);
			TextView tv_pack_name =  (TextView) view.findViewById(R.id.tv_app_packname);
			tv_pack_name.setText(info.getPackname());
			//			if(dao.find(info.getPackname())){
//				iv_lock_status.setImageResource(R.drawable.lock);
//			}else{
//				iv_lock_status.setImageResource(R.drawable.unlock);
//			}
			if(lockappinfos.contains(info.getPackname())){
				iv_lock_status.setImageResource(R.drawable.lock);
			}else{
				iv_lock_status.setImageResource(R.drawable.unlock);
			}
			iv.setImageDrawable(info.getIcon());
			tv.setText(info.getAppname());
			return view;
		}

	}
}
