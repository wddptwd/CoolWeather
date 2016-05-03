package app.coolweather.com.coolweather.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;

import app.coolweather.com.coolweather.log.MyLog;

/**
 * Created by Administrator on 2016/4/11.
 */
public class HttpUtil {
    public static final String TAG= "HttpUtil";
//    public static void sendHttpRequest(final String address, //2016-04-23 jiangning HttpURLConnection不好用,报错eof
//                                       final HttpCallbackListener listener){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                HttpURLConnection connection = null;
//                try {
//                    URL url = new URL(address);
//                    connection = (HttpURLConnection)url.openConnection();
//                    connection.setRequestMethod("GET");
//                    connection.setConnectTimeout(8000);
//                    connection.setReadTimeout(8000);
//                    connection.setDoInput(true);
//                    connection.connect();
//                    InputStream in = connection.getInputStream();
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//                    StringBuilder response =
//                            new StringBuilder();
//                    String line;
//                    while ((line = reader.readLine()) !=null){
//                        response.append(line);
//                    }
//                    MyLog.d(TAG,"response = " + response);
//                    if (listener !=null){
//                        //回调onFinish()方法
//                        listener.onFinish(response.toString());
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    MyLog.d(TAG,"error = " +e.toString());
//                    if (listener != null){
//                        //回调onError()方法
//                        listener.onError(e);
//                    }
//                }finally {
//                    if (connection != null){
//                        connection.disconnect();
//                    }
//                }
//
//            }
//        }).start();
//    }
    public static void sendHttpRequest(final String address,
                                       final HttpCallbackListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpEntity entity = send(METHOD_GET,address,null);
                    String resJson= EntityUtils.toString(entity,"UTF-8");
                    MyLog.d(TAG,"response = " + resJson);
                    listener.onFinish(resJson);
                } catch (IOException e) {
                    if (listener != null){
                        //回调onError()方法
                        listener.onError(e);
                    }
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static final int METHOD_GET=0;
    public static final int METHOD_POST=1;

    /**
     * 发送http请求的工具类
     * @param method	请求方式  METHOD_GET  METHOD_POST
     * @param uri  请求资源路径
     * @param pairs	请求参数
     * @return	响应实体 HttpEntity
     * @throws IOException	.....
     */
    public static HttpEntity send (int method, String uri, List<NameValuePair> pairs) throws IOException {
        HttpClient client=new DefaultHttpClient();
        HttpResponse resp=null;
        switch (method) {
            case METHOD_GET:
                HttpGet get=new HttpGet(uri);
                resp=client.execute(get);
                break;
            case METHOD_POST:
                HttpPost post=new HttpPost(uri);
                HttpEntity entity=new UrlEncodedFormEntity(pairs, "utf-8");
                post.setEntity(entity);
                post.setHeader("Content-Type", "application/x-www-form-urlencoded");
                resp=client.execute(post);
                break;
        }
        return resp.getEntity();
    }
}
