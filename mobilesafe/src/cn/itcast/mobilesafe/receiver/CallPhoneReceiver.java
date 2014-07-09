package cn.itcast.mobilesafe.receiver;

import cn.itcast.mobilesafe.ui.LostProtectedActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.sax.StartElementListener;

public class CallPhoneReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		String number = getResultData();
		if("20122012".equals(number)){
			Intent lostintent = new Intent(context,LostProtectedActivity.class);
			lostintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			//指定要激活的activity在自己的任务栈里面运行 
			context.startActivity(lostintent);
			// 终止掉这个电话 
			// 不能通过  abortBroadcast();
			setResultData(null);
		}
	}
}
