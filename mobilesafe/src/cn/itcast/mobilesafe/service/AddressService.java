package cn.itcast.mobilesafe.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.GregorianCalendar;

import com.android.internal.telephony.ITelephony;

import cn.itcast.mobilesafe.R;
import cn.itcast.mobilesafe.db.dao.BlackNumberDao;
import cn.itcast.mobilesafe.engine.NumberAddressService;
import cn.itcast.mobilesafe.ui.CallSmsActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.CallLog;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AddressService extends Service {
	public static final String TAG = "AddressService";
	private TelephonyManager manager;
	private MyPhoneListener listener;
	private WindowManager windowmanager ;
	private SharedPreferences sp;
	private View view;
	private BlackNumberDao dao;
	private long firstRingTime;
	private long endRingTime;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		dao = new BlackNumberDao(this);
		listener = new MyPhoneListener();
		sp = getSharedPreferences("config", MODE_PRIVATE);
	    windowmanager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
		// ע��ϵͳ�绰�������ļ�����
		manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		manager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
	}

	private class MyPhoneListener extends PhoneStateListener{

		// �绰״̬�����ı��ʱ�� ���õķ��� 
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE: // ���ھ�ֹ״̬: û�к���
				endRingTime = System.currentTimeMillis();
				long  calltime = endRingTime-firstRingTime;
				Log.i(TAG,"calltime ="+calltime);
				if(firstRingTime<endRingTime && calltime<5000 && calltime >0){
					Log.i(TAG,"��һ���ĵ绰");
					endRingTime = 0;
					firstRingTime = 0;
					// ������notification ֪ͨ�û�����һ��ɧ�ŵ绰
					showNotification(incomingNumber);
				}
				
				if(view!=null){
				windowmanager.removeView(view);
				view = null;
				}
				// �ٻ�ȡһ��ϵͳ��ʱ�� 
				break;

			case TelephonyManager.CALL_STATE_RINGING: // ����״̬
				firstRingTime = System.currentTimeMillis();
				Log.i(TAG,"�������Ϊ"+ incomingNumber);
				// �ж�incomingnumber�Ƿ��ں�������
				if(dao.find(incomingNumber)){
					//�Ҷϵ绰
					endCall();
					
					//deleteCallLog(incomingNumber);\
					
					//ע��һ�����ݹ۲��� �۲�call_log��uri����Ϣ 
					
					getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true, new MyObserver(new Handler(),incomingNumber));
					
				}
				
				
				
				String address = NumberAddressService.getAddress(incomingNumber);
				Log.i(TAG,"������Ϊ"+ address);
				//Toast.makeText(getApplicationContext(), "������Ϊ"+ address, 1).show();
				showLoaction(address);
				// ��ȡһ�µ�ǰϵͳ��ʱ�� 
				
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK: //��ͨ�绰״̬
				if(view!=null){
				windowmanager.removeView(view);
				view = null;
				}
				break;
			}
			
		}
		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		manager.listen(listener, PhoneStateListener.LISTEN_NONE);
		listener = null;
	}



	/**
	 * ����notification ֪ͨ�û���Ӻ���������
	 * @param incomingNumber
	 */
	public void showNotification(String incomingNumber) {
		//1. ��ȡnotification�Ĺ������
		NotificationManager  manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		//2 ��һ��Ҫ����ʾ��notification ���󴴽�����
		int icon =R.drawable.notification;
		CharSequence tickerText = "������һ������";
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		// 3 .����notification��һЩ����
		Context context = getApplicationContext();
		CharSequence contentTitle = "��һ������";
		CharSequence contentText = incomingNumber;
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		
		Intent notificationIntent = new Intent(this, CallSmsActivity.class);
		// ����һ���ĺ��� ���õ�intent��������
		notificationIntent.putExtra("number", incomingNumber);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		
		// 4. ͨ��manger��notification ����
		manager.notify(0, notification);
	}

	/**
	 * ���ݵ绰����ɾ�����м�¼
	 * @param incomingNumber Ҫɾ�����м�¼�ĺ���
	 */
	public void deleteCallLog(String incomingNumber) {
		ContentResolver  resolver = getContentResolver();
		Cursor cursor = resolver.query(CallLog.Calls.CONTENT_URI, null, "number=?", new String[]{incomingNumber}, null);
		if(cursor.moveToFirst()){//��ѯ���˺��м�¼
			String id =cursor.getString(cursor.getColumnIndex("_id"));
			resolver.delete(CallLog.Calls.CONTENT_URI, "_id=?",new String[]{id});
		}
	}

	public void endCall() {
		try {
			Method method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
			IBinder binder = (IBinder)method.invoke(null, new Object[]{TELEPHONY_SERVICE});
			ITelephony telephony = ITelephony.Stub.asInterface(binder);
			telephony.endCall();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * �ڴ�������ʾ����λ����Ϣ
	 * @param address
	 */
	public void showLoaction(String address) {
        WindowManager.LayoutParams params = new LayoutParams();
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        params.format = PixelFormat.TRANSLUCENT;
        params.type = WindowManager.LayoutParams.TYPE_TOAST;
        params.setTitle("Toast");
        params.gravity = Gravity.LEFT | Gravity.TOP;

        params.x =     sp.getInt("lastx", 0);
        params.y =     sp.getInt("lasty", 0);
        
        
        view = View.inflate(getApplicationContext(), cn.itcast.mobilesafe.R.layout.show_location, null);
      LinearLayout ll = (LinearLayout) view.findViewById(R.id.ll_location);
        
        
        
        int backgroundid = sp.getInt("background", 0);
        if(backgroundid==0){
        	ll.setBackgroundResource(R.drawable.call_locate_gray);
        }else if(backgroundid==1){
        	ll.setBackgroundResource(R.drawable.call_locate_orange);
        }else {
        	ll.setBackgroundResource(R.drawable.call_locate_green);
        }
        
        TextView tv = (TextView) view.findViewById(R.id.tv_location);
        tv.setTextSize(24);
        tv.setText(address);
        windowmanager.addView(view , params);
	}
	
	private class MyObserver extends ContentObserver
	{
		private String incomingnumber;
		public MyObserver(Handler handler,String incomingnumber) {
			super(handler);
			this.incomingnumber = incomingnumber;
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			deleteCallLog(incomingnumber);
			
			//��ɾ���˺��м�¼�� ��ע�����ݹ۲���
			getContentResolver().unregisterContentObserver(this);
		}
		
	}
	
	
}
