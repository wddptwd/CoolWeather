package app.coolweather.com.coolweather.log;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class MyLog {
	private static final String LOG ="log";
	private static final String isShowTag ="isshow";
	private static String TAG =MyLog.class.getSimpleName();
	
	private  static boolean isShow = true ;
	
	public static void initShow(Context ctx){
		SharedPreferences sharedPreferences = ctx.getSharedPreferences(LOG, Context.MODE_PRIVATE);
		isShow = sharedPreferences.getBoolean(isShowTag, false);
		isShow = true ;
	};
	
	public static void  setDefaultTAG(String tag){
		TAG = tag;
	}
	public static void e(String msg){
		e(TAG, msg);
	}
	public static void w(String msg){
		w(TAG, msg);
	}		
	public static void d(String msg){
		d(TAG, msg);
	}		
	public static void i(String msg){
		i(TAG, msg);
	}	
	public static void e(String tag,String msg){
		if(!isShow){
			return ;
		}
		Log.e(tag, msg);
	}
	public static void w(String tag,String msg){
		if(!isShow){
			return ;
		}
		Log.w(tag, msg);
	}		
	public static void d(String tag,String msg){
		if(!isShow){
			return ;
		}
		Log.d(tag, msg);
	}		
	public static void i(String tag,String msg){
		if(!isShow){
			return ;
		}
		Log.i(tag, msg);
	}
}
