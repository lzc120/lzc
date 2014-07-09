package cn.itcast.mobilesafe.ui;

import java.io.File;

import cn.itcast.mobilesafe.R;
import cn.itcast.mobilesafe.R.layout;
import cn.itcast.mobilesafe.domain.UpdataInfo;
import cn.itcast.mobilesafe.engine.DownLoadFileTask;
import cn.itcast.mobilesafe.engine.UpdataInfoService;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SplashActivity extends Activity {
	private static final String TAG = "SplashActivity";
	private TextView tv_splash_version;
	private LinearLayout ll_splash_main;
	private UpdataInfo info;
	private ProgressDialog pd ;
	private String versiontext;
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			// 判断服务器版本号 和客户端的版本号 是否相同
			if (isNeedUpdate(versiontext)) {
				Log.i(TAG, "弹出来升级对话框");
				showUpdataDialog();
			}
		}
		
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 取消标题栏
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.splash);
		pd = new ProgressDialog(this);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMessage("正在下载...");
		ll_splash_main = (LinearLayout) this.findViewById(R.id.ll_splash_main);
		tv_splash_version = (TextView) this
				.findViewById(R.id.tv_splash_version);
		versiontext = getVersion();
		// 让当前的activity延时两秒钟 检查更新
		new Thread(){

			@Override
			public void run() {
				super.run();
				try {
					sleep(2000);
					handler.sendEmptyMessage(0);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}.start();
		
		


		tv_splash_version.setText(versiontext);
		AlphaAnimation aa = new AlphaAnimation(0.0f, 1.0f);
		aa.setDuration(2000);
		ll_splash_main.startAnimation(aa);

		// 完成窗体的全屏显示 // 取消掉状态栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

	}

	/**
	 * 升级的对话框
	 */
	private void showUpdataDialog() {
		AlertDialog.Builder buider = new Builder(this);
		buider.setIcon(R.drawable.icon5);
		buider.setTitle("升级提醒");
		buider.setMessage(info.getDescription());
		buider.setCancelable(false); // 让用户不能取消对话框
		buider.setPositiveButton("确定", new OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {

				Log.i(TAG, "下载apk文件" + info.getApkurl());
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
					DownLoadFileThreadTask task = new DownLoadFileThreadTask(info.getApkurl(), "/sdcard/new.apk");
					pd.show();
					new Thread(task).start();
				 
				}else{
					Toast.makeText(getApplicationContext(), "sd卡不可用", 1).show();
					loadMainUI();
				}
				
				
			}
		});
		buider.setNegativeButton("取消", new OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				Log.i(TAG, "用户取消进入程序主界面");
				loadMainUI();
			}
		});

		buider.create().show();

	}

	private class DownLoadFileThreadTask implements Runnable {
		private String path; // 服务器路径
		private String filepath; // 本地文件路径

		public DownLoadFileThreadTask(String path, String filepath) {
			this.path = path;
			this.filepath = filepath;
		}

		public void run() {
			try {
				File file = DownLoadFileTask.getFile(path, filepath,pd);
				Log.i(TAG,"下载成功");
				pd.dismiss();
				install(file);
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(getApplicationContext(), "下载文件失败", 0).show();
				pd.dismiss();
				loadMainUI();
				
			}

		}

	}

	/**
	 * 
	 * @param versiontext
	 *            当前客户端的版本号信息
	 * @return 是否需要更新
	 */
	private boolean isNeedUpdate(String versiontext) {
		UpdataInfoService service = new UpdataInfoService(this);
		try {
			info = service.getUpdataInfo(R.string.updataurl);
			String version = info.getVersion();
			if (versiontext.equals(version)) {
				Log.i(TAG, "版本相同,无需升级, 进入主界面");
				loadMainUI();
				return false;
			} else {
				Log.i(TAG, "版本不同,需要升级");
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "获取更新信息异常", 0).show();
			Log.i(TAG, "获取更新信息异常, 进入主界面");
			loadMainUI();
			return false;
		}

	}

	/**
	 * 获取当前应用程序的版本号
	 * 
	 * @return
	 */
	private String getVersion() {
		try {
			PackageManager manager = getPackageManager();
			PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
			return info.versionName;
		} catch (Exception e) {

			e.printStackTrace();
			return "版本号未知";
		}
	}

	private void loadMainUI() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish(); // 把当前activity从任务栈里面移除

	}
	
	/**
	 * 安装apk
	 * @param file
	 */
	private void install(File file){
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
		finish();
		startActivity(intent);
	}
	
}
