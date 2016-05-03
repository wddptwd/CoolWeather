package app.coolweather.com.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import app.coolweather.com.coolweather.R;
import app.coolweather.com.coolweather.model.City;
import app.coolweather.com.coolweather.model.CooolWeatherDB;
import app.coolweather.com.coolweather.model.County;
import app.coolweather.com.coolweather.model.Province;
import app.coolweather.com.coolweather.util.HttpCallbackListener;
import app.coolweather.com.coolweather.util.HttpUtil;
import app.coolweather.com.coolweather.util.Utility;

public class ChooseAreaActivity extends Activity {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private ProgressDialog progressDialog;
    private TextView tv_title;
    private ListView lv_content;
    private ArrayAdapter<String>adapter;
    private CooolWeatherDB cooolWeatherDB;
    private List<String>dataList = new ArrayList<>();

    /**
     *省列表
     */
    private List<Province>provinceList;
    /**
     * 市列表
     */
    private List<City>cityList;

    /**
     * 县列表
     */
    private List<County>countyList;

    /**
     * 选中的省份
     */
    private Province selectedProvince;
    /**
     * 选中的城市
     */
    private City selectedCity;
    /**
     * 选中的级别
     */
    private int currentLeve;

    /**
     *
     * 是否从WeatherActivity中跳转过来。
     */
    private boolean isFromWeatherActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity",
                false);
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(this);
        //已经选择了城市且不是从WeatherActivity跳转过来，才会直接跳转到WeatherActivity

        if (prefs.getBoolean("city_selected",false) && !isFromWeatherActivity){
            Intent intent = new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        lv_content = (ListView) findViewById(R.id.lv_content);
        tv_title = (TextView) findViewById(R.id.tv_title);
        adapter = new ArrayAdapter<String>(this
                ,android.R.layout.simple_list_item_1
                ,dataList);
        lv_content.setAdapter(adapter);
        cooolWeatherDB = CooolWeatherDB.getInstance(this);
        lv_content.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLeve == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);
                    queryCities();
                }else if (currentLeve == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();
                }else if (currentLeve == LEVEL_COUNTY){
                    String countyCode = countyList.get(position).getCountCode();
                    Intent intent = new Intent(ChooseAreaActivity.this,
                            WeatherActivity.class);
                    intent.putExtra("county_code",countyCode);
                    startActivity(intent);
                    finish();
                }
            }
        });
        queryProvinces();
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上面查询。
     */
    private void queryProvinces(){
        provinceList = cooolWeatherDB.loadProvinces();
        if (provinceList.size()>0){
            dataList.clear();
            for (Province province : provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            tv_title.setText("中国");
            currentLeve = LEVEL_PROVINCE;
        }else {
            qureyFromServer(null,"province");
        }
    }
    public static final String TAG = "ChooseAreaActivity";
    /**
     * 根据传入的代号和服务器上查询省市数据
     * @param code
     * @param type
     */
    private void qureyFromServer(String code, final String type) {
        String address;
        if (!TextUtils.isEmpty(code)){
            address = "http://www.weather.com.cn/data/list3/city"+
            code+".xml";
        }else {
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }

        showProgressDialog();
        Log.d(TAG,"address = "+ address);

        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result = false;
                if ("province".equals(type)){
                    result = Utility.handleProvinceResponse(cooolWeatherDB,response);
                }else if ("city".equals(type)){
                    result = Utility.handleCitiesResponse(cooolWeatherDB
                            ,response
                            ,selectedProvince.getId());
                }else if ("county".equals(type)){
                    result = Utility.handleCountiesResponse(cooolWeatherDB
                            ,response
                            ,selectedCity.getId());
                }
                if (result){
                    //通过runOnUiThread()方法回到主线程
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }else {
                    closProgressDialog();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ChooseAreaActivity.this
                                    , "加载失败,result = false"
                                    , Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                Log.d(TAG,"result = " + result+"// " +"response ="+ response);

            }

            @Override
            public void onError(Exception e) {
                //通过runOnUiThread()方法回到主线程处理逻辑
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ChooseAreaActivity.this
                                ,"加载失败"
                                ,Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 显示进度条
     */
    private void showProgressDialog() {
        if (progressDialog ==null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void queryCounties() {
        countyList = cooolWeatherDB.loadCounties(selectedCity.getId());
        if (countyList.size()>0){
            dataList.clear();
            for (County county : countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            lv_content.setSelection(0);
            tv_title.setText(selectedCity.getCityName());
            currentLeve = LEVEL_COUNTY;
        }else {
            qureyFromServer(selectedCity.getCityCode(),"county");
        }
    }

    /**
     * 查询选中的省内的所有市，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryCities() {
        cityList = cooolWeatherDB.loadCities(selectedProvince.getId());
        if (cityList.size()>0){
            dataList.clear();
            for (City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            lv_content.setSelection(0);
            tv_title.setText(selectedProvince.getProvinceName());
            currentLeve = LEVEL_CITY;
        }else {
            qureyFromServer(selectedProvince.getProvinceCode(),"city");
        }

    }


    /**
     * 关闭进度条对话框
     */
    private void closProgressDialog() {
        if (progressDialog !=null){
            progressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if (currentLeve == LEVEL_COUNTY){
            queryCities();
        }else if (currentLeve == LEVEL_CITY){
            queryProvinces();
        }else {
            if (isFromWeatherActivity){
                Intent intent = new Intent(this,WeatherActivity.class);
                startActivity(intent);
            }
            finish();
        }
    }
}
