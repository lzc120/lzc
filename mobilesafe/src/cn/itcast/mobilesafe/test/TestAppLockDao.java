package cn.itcast.mobilesafe.test;

import java.util.List;

import cn.itcast.mobilesafe.db.dao.AppLockDao;
import android.test.AndroidTestCase;

public class TestAppLockDao extends AndroidTestCase {
	public void testAdd() throws Exception{
		AppLockDao dao = new AppLockDao(getContext());
		dao.add("cn.itcast.xxx");
		dao.add("cn.itcast.xxx1");
		dao.add("cn.itcast.xxx2");
	}
	public void testdelete() throws Exception{
		AppLockDao dao = new AppLockDao(getContext());
		dao.delete("cn.itcast.xxx");
	}
	public void testfindall() throws Exception{
		
		AppLockDao dao = new AppLockDao(getContext());
		List<String> apps = dao.getAllApps();
		System.out.println(apps.size());
		
	}
	
}
