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
			// �жϷ������汾�� �Ϳͻ��˵İ汾�� �Ƿ���ͬ
			if (isNeedUpdate(versiontext)) {
				Log.i(TAG, "�����������Ի���");
				showUpdataDialog();
			}
		}
		
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ȡ��������
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.splash);
		pd = new ProgressDialog(this);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMessage("��������...");
		ll_splash_main = (LinearLayout) this.findViewById(R.id.ll_splash_main);
		tv_splash_version = (TextView) this
				.findViewById(R.id.tv_splash_version);
		versiontext = getVersion();
		// �õ�ǰ��activity��ʱ������ ������
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

		// ��ɴ����ȫ����ʾ // ȡ����״̬��
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

	}

	/**
	 * �����ĶԻ���
	 */
	private void showUpdataDialog() {
		AlertDialog.Builder buider = new Builder(this);
		buider.setIcon(R.drawable.icon5);
		buider.setTitle("��������");
		buider.setMessage(info.getDescription());
		buider.setCancelable(false); // ���û�����ȡ���Ի���
		buider.setPositiveButton("ȷ��", new OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {

				Log.i(TAG, "����apk�ļ�" + info.getApkurl());
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
					DownLoadFileThreadTask task = new DownLoadFileThreadTask(info.getApkurl(), "/sdcard/new.apk");
					pd.show();
					new Thread(task).start();
				 
				}else{
					Toast.makeText(getApplicationContext(), "sd��������", 1).show();
					loadMainUI();
				}
				
				
			}
		});
		buider.setNegativeButton("ȡ��", new OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				Log.i(TAG, "�û�ȡ���������������");
				loadMainUI();
			}
		});

		buider.create().show();

	}

	private class DownLoadFileThreadTask implements Runnable {
		private String path; // ������·��
		private String filepath; // �����ļ�·��

		public DownLoadFileThreadTask(String path, String filepath) {
			this.path = path;
			this.filepath = filepath;
		}

		public void run() {
			try {
				File file = DownLoadFileTask.getFile(path, filepath,pd);
				Log.i(TAG,"���سɹ�");
				pd.dismiss();
				install(file);
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(getApplicationContext(), "�����ļ�ʧ��", 0).show();
				pd.dismiss();
				loadMainUI();
				
			}

		}

	}

	/**
	 * 
	 * @param versiontext
	 *            ��ǰ�ͻ��˵İ汾����Ϣ
	 * @return �Ƿ���Ҫ����
	 */
	private boolean isNeedUpdate(String versiontext) {
		UpdataInfoService service = new UpdataInfoService(this);
		try {
			info = service.getUpdataInfo(R.string.updataurl);
			String version = info.getVersion();
			if (versiontext.equals(version)) {
				Log.i(TAG, "�汾��ͬ,��������, ����������");
				loadMainUI();
				return false;
			} else {
				Log.i(TAG, "�汾��ͬ,��Ҫ����");
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "��ȡ������Ϣ�쳣", 0).show();
			Log.i(TAG, "��ȡ������Ϣ�쳣, ����������");
			loadMainUI();
			return false;
		}

	}

	/**
	 * ��ȡ��ǰӦ�ó���İ汾��
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
			return "�汾��δ֪";
		}
	}

	private void loadMainUI() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish(); // �ѵ�ǰactivity������ջ�����Ƴ�

	}
	
	/**
	 * ��װapk
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
