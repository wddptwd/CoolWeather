package app.coolweather.com.coolweather.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import app.coolweather.com.coolweather.R;
import app.coolweather.com.coolweather.log.MyLog;
import app.coolweather.com.coolweather.service.AutoUpdateService;
import app.coolweather.com.coolweather.util.HttpCallbackListener;
import app.coolweather.com.coolweather.util.HttpUtil;
import app.coolweather.com.coolweather.util.Utility;

public class WeatherActivity extends AppCompatActivity implements View.OnClickListener{

    private LinearLayout weatherInfoLayout;
    /**
     * 用于显示城市名
     */
    private TextView tv_CityName;

    /**
     * 用于显示发布时间
     */
    private TextView tv_Publish;
    /**
     * 用于显示天气描述信息
     */
    private TextView tv_weatherDesp;
    /**
     * 用于显示气温1
     */
    private TextView tv_temp1;
    /**
     * 用于显示气温2
     */
    private TextView tv_temp2;
    /**
     * 用于显示当前日期
     */
    private TextView tv_currentDate;
    /**
     * 切换城市按钮
     */
    private Button bt_switchCity;
    /**
     * 更新天气按钮
     */
    private Button bt_refreshWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);
        //初始化各控件
        weatherInfoLayout =
                (LinearLayout) findViewById(R.id.ll_weather_info);
        tv_CityName = (TextView) findViewById(R.id.tv_city_name);
        tv_Publish = (TextView) findViewById(R.id.tv_publish_text);
        tv_temp1 = (TextView) findViewById(R.id.tv_temp1);
        tv_temp2 = (TextView) findViewById(R.id.tv_temp2);
        tv_currentDate = (TextView) findViewById(R.id.tv_current_date);
        bt_switchCity = (Button) findViewById(R.id.bt_switch_city);
        bt_refreshWeather = (Button) findViewById(R.id.bt_refresh_weather);
        tv_weatherDesp = (TextView) findViewById(R.id.tv_weather_desp);
        String countyCode = getIntent().getStringExtra("county_code");
        if (!TextUtils.isEmpty(countyCode)){
            //有县级代号时就去查询天气
            tv_Publish.setText("同步中。。");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            tv_CityName.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        } else{
            //没有县级代号时就直接显示本地天气
            showWeather();
        }
        bt_switchCity.setOnClickListener(this);
        bt_refreshWeather.setOnClickListener(this);
    }

    /**
     * 从SharedPreferences 文件中读取存储天气的信息，并显示到界面上
     */
    private void showWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        tv_CityName.setText(prefs.getString("city_name",""));
        tv_temp1.setText(prefs.getString("temp1",""));
        tv_temp2.setText(prefs.getString("temp2",""));
        tv_weatherDesp.setText(prefs.getString("weather_desp",""));
        tv_Publish.setText("今天"+prefs.getString("publish_time","")+"发布");
        tv_currentDate.setText(prefs.getString("current_data",""));
        weatherInfoLayout.setVisibility(View.VISIBLE);
        tv_CityName.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    /**
     * 查询县级代号所对应的天气代号
     * @param countyCode
     */
    private void queryWeatherCode(String countyCode) {
        String address = "http://www.weather.com.cn/data/list3/city" +
                countyCode+".xml";
        MyLog.d(TAG,"countyCode = " + countyCode);
        queryFromServer(address, "countyCode");
    }

    /**
     * 根据传入的地址和类型去向服务器查询天气代号或者天气信息
     * @param address
     * @param type
     */
    private void queryFromServer(String address, final String type) {
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if ("countyCode".equals(type)) {
                    if (!TextUtils.isEmpty(response)) {
                        String[] array = response.split("\\|");
                        if (array != null && array.length == 2) {
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                }else if ("weatherCode".equals(type)){
                    //处理服务器返回的天气信息
                    Utility.handleWeatherResponse(WeatherActivity.this
                            ,response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                MyLog.d(TAG,"error = "+ e.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_Publish.setText("同步失败");
                    }
                });
            }
        });
    }

    public static final String TAG = "WeatherActivity";

    /**
     * 查询天气代号对应的天气
     * @param weatherCode
     */
    private void queryWeatherInfo(String weatherCode) {
        String address = "http://www.weather.com.cn/data/cityinfo/" +
                weatherCode+".html";
        MyLog.d(TAG,"address = " + address);
        queryFromServer(address,"weatherCode");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_switch_city:
                Intent insent = new Intent(this,ChooseAreaActivity.class);
                insent.putExtra("from_weather_activity", true);
                startActivity(insent);
                finish();
                break;
            case R.id.bt_refresh_weather:
                tv_Publish.setText("同步中..");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String weatherCode = prefs.getString("weather_code","");
                if (!TextUtils.isEmpty(weatherCode)){
                    queryWeatherInfo(weatherCode);
                }
                break;
            default:break;
        }
    }
}
