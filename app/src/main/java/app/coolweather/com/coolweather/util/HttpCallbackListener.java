package app.coolweather.com.coolweather.util;

/**
 * Created by Administrator on 2016/4/11.
 */
public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
