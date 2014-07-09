package cn.itcast.mobilesafe.test;

import cn.itcast.mobilesafe.R;
import cn.itcast.mobilesafe.domain.UpdataInfo;
import cn.itcast.mobilesafe.engine.UpdataInfoService;
import android.test.AndroidTestCase;

public class TestGetUpdataInfo extends AndroidTestCase {

	
	public void testGetInfo() throws Exception{
		UpdataInfoService service = new UpdataInfoService(getContext());
		UpdataInfo  info = service.getUpdataInfo(R.string.updataurl);
		assertEquals("2.0", info.getVersion());
	}
}
