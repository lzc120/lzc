package cn.itcast.mobilesafe.test;

import cn.itcast.mobilesafe.engine.AppInfoProvider;
import android.test.AndroidTestCase;

public class TestGetAppInfo extends AndroidTestCase {
	public void getApps() throws Exception{
		AppInfoProvider provider = new AppInfoProvider(getContext());
		provider.getAllApps();
	}
}
