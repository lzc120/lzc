package cn.itcast.mobilesafe.test;

import java.util.List;

import cn.itcast.mobilesafe.domain.SmsInfo;
import cn.itcast.mobilesafe.engine.SmsInfoService;
import android.test.AndroidTestCase;

public class TestSmsInfoService extends AndroidTestCase {

	
	public void testGetSmsInfos() throws Exception{
		SmsInfoService service  = new SmsInfoService(getContext());
		List<SmsInfo>  smsinfos = service.getSmsInfos();
		assertEquals(5, smsinfos.size());
	}
}
