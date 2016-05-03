package app.coolweather.com.coolweather.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import app.coolweather.com.coolweather.db.CoolWeatherOpenHelper;
import app.coolweather.com.coolweather.log.MyLog;

/**
 * Created by Administrator on 2016/4/10.
 */
public class CooolWeatherDB {
    /**
     * 数据库名
     */
    public static final String DB_NAME = "cool_weather";
    /**
     * 数据库版本
     */
    public static final int VERSION = 1;
    private static CooolWeatherDB coolWeatherDB;
    private SQLiteDatabase db;
    /**
     * 将结构方法私有化
     */
    private CooolWeatherDB(Context context){
        CoolWeatherOpenHelper dbHelper = new CoolWeatherOpenHelper(context,
                DB_NAME,null,VERSION);
        db = dbHelper.getWritableDatabase();
    }
    /**
     * 获取CoolWeatherDB的实例
     */
    public synchronized  static CooolWeatherDB getInstance(Context context){
        if (coolWeatherDB == null){
            coolWeatherDB = new CooolWeatherDB(context);
        }
        return coolWeatherDB;
    }
    /**
     * 将province 实例存到数据库
     */
    public void saveProvince(Province province){
        if (province !=null){
            ContentValues values = new ContentValues();
            values.put("province_name",province.getProvinceName());
            values.put("province_code",province.getProvinceCode());
            db.insert("Province",null,values);
        }
    }
    /**
     * 从数据库读取全国所有的省份信息
     */
    public List<Province> loadProvinces(){
        List<Province>list = new ArrayList<>();
        Cursor cursor = db.query("Province",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                Province province = new Province();
                province.setId(cursor.getInt(cursor.getColumnIndex("id")));
                province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
                province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
                list.add(province);
            }while (cursor.moveToNext());
        }
        return list;
    }
    public static final String TAG = "CooolWeatherDB";
    /**
     * 将city实例存储到数据库
     */
    public void saveCity(City city){
        if (city != null){
            ContentValues values = new ContentValues();
            values.put("city_name",city.getCityName());
            values.put("city_code",city.getCityCode());
            values.put("province_id",city.getProvinceId());
            Log.d("TAG", "saveCity(City city)");
            if (db == null){
                Log.d("TAG","db is null");
            }
            if(db.insert("City",null,values)!=-1){
                Log.d("TAG","insert city is seccess");
            }else{
                Log.d("TAG","insert city is failed");
            }
        }
    }
    /**
     * 从数据库读取某省份下所有的城市信息
     */
    public List<City> loadCities(int provinceId) {
        List<City> list = new ArrayList<>();
        Cursor cursor = db.query("City", null, "province_id = ?",
                new String[]{String.valueOf(provinceId)}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                City city = new City();
                city.setId(cursor.getInt(cursor.getColumnIndex("id")));
                city.setCityName(cursor.getString(
                        cursor.getColumnIndex("city_name")));
                city.setCityCode(cursor.getString(
                        cursor.getColumnIndex("city_code")));
                city.setProvinceId(provinceId);
                list.add(city);
            } while (cursor.moveToNext());
        }
        return list;
    }

    /**
     * 将county实例存储到数据库
     */
    public void saveCounty(County county){
        if (county != null){
            ContentValues values
                    = new ContentValues();
            values.put("county_name",county.getCountyName());
            values.put("county_code",county.getCountCode());
            values.put("city_id", county.getCityId());
            if(db.insert("County",null,values)!=-1){
                MyLog.d(TAG,"insert County success");
            }else{
                MyLog.d(TAG,"insert County failed");
            }
        }
    }

    /**
     * 从数据库读取某城市下所有的县城信息
     */
    public List<County>loadCounties(int cityId){
        List<County>list = new ArrayList<>();
        Cursor cursor = db.query("County",null,"city_id = ?",new
                String[]{String.valueOf(cityId)},null,null,null
        );
        if (cursor.moveToNext()){
            do {
                County county = new County();
                county.setId(cursor.getInt(cursor.getColumnIndex("id")));
                county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
                county.setCountCode(cursor.getString(cursor.getColumnIndex("county_code")));
                county.setCityId(cityId);
                list.add(county);
            }while (cursor.moveToNext());
        }
        return list;
    }

}
