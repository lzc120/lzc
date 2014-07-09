package cn.itcast.mobilesafe.receiver;

import cn.itcast.mobilesafe.R;
import cn.itcast.mobilesafe.db.dao.BlackNumberDao;
import cn.itcast.mobilesafe.engine.GPSInfoProvider;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver {

	private static final String TAG = "SMSReceiver";
	private BlackNumberDao dao;
	@Override
	public void onReceive(Context context, Intent intent) {
		dao = new BlackNumberDao(context);
		// 获取短信的内容
		// #*location*#123456
		Object[] pdus = (Object[]) intent.getExtras().get("pdus");
		for (Object pdu : pdus) {
			SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
			String content = sms.getMessageBody();
			Log.i(TAG, "短信内容" + content);
			String sender = sms.getOriginatingAddress();
			if ("#*location*#".equals(content)) {
				// 终止广播
				abortBroadcast();
				GPSInfoProvider provider = GPSInfoProvider.getInstance(context);
				String location = provider.getLocation();
				SmsManager smsmanager = SmsManager.getDefault();
				if ("".equals(location)) {

				} else {
					smsmanager.sendTextMessage(sender, null, location, null,
							null);
				}
			}else if("#*locknow*#".equals(content)){
				DevicePolicyManager manager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
				manager.resetPassword("123", 0);
				manager.lockNow();
				abortBroadcast();
			}else if("#*wipedata*#".equals(content)){
				DevicePolicyManager manager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
				manager.wipeData(0);
				abortBroadcast();
			}else if("#*alarm*#".equals(content)){
				MediaPlayer player = MediaPlayer.create(context, R.raw.ylzs);
				player.setVolume(1.0f, 1.0f);
				player.start();
				abortBroadcast();
			}
			
			if(dao.find(sender)){
				// 黑名单的短信
				abortBroadcast();
				//todo: 把短信内容存放到自己的数据库里面
			}
			
			//建立短信内容的匹配库 (关键字: 发票,卖房,哥,学生....办证...)
			if(content.contains("fapiao")){
				Log.i(TAG,"垃圾短信 发票");
				abortBroadcast();
			}
			
		}

	}

}
