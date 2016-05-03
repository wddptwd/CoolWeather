package app.coolweather.com.coolweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import app.coolweather.com.coolweather.log.MyLog;
import app.coolweather.com.coolweather.model.City;
import app.coolweather.com.coolweather.model.CooolWeatherDB;
import app.coolweather.com.coolweather.model.County;
import app.coolweather.com.coolweather.model.Province;

/**
 * Created by Administrator on 2016/4/11.
 */
public class Utility {
    /**
     * 解释和出来服务器返回的省级数据
     */
    public synchronized static boolean handleProvinceResponse(
            CooolWeatherDB cooolWeatherDB,
            String response
    ){
        if (!TextUtils.isEmpty(response)){
            String[] allProvices = response.split(",");
            if (allProvices != null && allProvices.length>0){
                for (String p :allProvices){
                    String[] array = p.split("\\|");
                    Province province = new Province();
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);
                    //将解释出来的数据存储到Province表
                    cooolWeatherDB.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }
    public static final String TAG = "Utility";
    /**
     * 解释和处理服务器返回的市级数据
     */
    public static boolean handleCitiesResponse(CooolWeatherDB cooolWeatherDB,
                                               String response,int provinceId){
        if (!TextUtils.isEmpty(response)){
            String[] allCities = response.split(",");
            if (allCities !=null && allCities.length>0){
                for (String c :allCities){
                    String[] array = c.split("\\|");
                    for (String s :array){
                        Log.d(TAG,"array = " + s);
                    }
                    City city = new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);
                    Log.d(TAG, "cooolWeatherDB = " + cooolWeatherDB);
                    //将解释出来是数据存储到City表
                    if (null != cooolWeatherDB) {
                        cooolWeatherDB.saveCity(city);
                    }else {
                        Log.d(TAG,"cooolWeatherDB = " +null);
                    }
                }
                return true;
            }
        }
        return false;
    }
    /**
     * 解释和处理服务器返回的县级数据
     */
    public static boolean handleCountiesResponse(CooolWeatherDB cooolWeatherDB
    ,String response
    ,int cityId){
        if (!TextUtils.isEmpty(response)){
            String[] allCounties = response.split(",");
            if (allCounties != null && allCounties.length>0){
                for (String c:allCounties){
                    String[] array = c.split("\\|");
                    County county = new County();
                    county.setCountCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(cityId);
                    //将解释出来的数据存储到County表
                    cooolWeatherDB.saveCounty(county);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析服务器返回的json数据，并将解析出的数据存储到本地
     * @param context
     * @param response
     */
    public static void handleWeatherResponse(Context context
            ,String response){
        try {
            MyLog.d(TAG,"response = " +response);
            JSONObject jsonObject = new JSONObject(response);
            JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
            String cityName = weatherInfo.getString("city");
            String weatherCode = weatherInfo.getString("cityid");
            String temp1 = weatherInfo.getString("temp1");
            String temp2 = weatherInfo.getString("temp2");
            String weatherDesp = weatherInfo.getString("weather");
            String publishTime = weatherInfo.getString("ptime");
            saveWeatherInfo(context,cityName,weatherCode,temp1,temp2,weatherDesp,publishTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将服务器返回的所有天气信息存储到sharedPreferences文件中
     * @param context
     * @param cityName
     * @param weatherCode
     * @param temp1
     * @param temp2
     * @param weatherDesp
     * @param publishTime
     */
    private static void saveWeatherInfo(Context context, String cityName, String weatherCode, String temp1, String temp2, String weatherDesp, String publishTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        SharedPreferences.Editor editor =
                PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected",true);
        editor.putString("city_name", cityName);
        editor.putString("weather_code", weatherCode);
        editor.putString("temp1", temp1);
        editor.putString("temp2", temp2);
        editor.putString("weather_desp", weatherDesp);
        editor.putString("publish_time",publishTime);
        editor.putString("current_date",sdf.format(new Date()));
        editor.commit();
    }
}
