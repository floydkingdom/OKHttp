package com.example.administrator.okhttp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.okhttp.util.CountingRequestBody;
import com.example.administrator.okhttp.util.L;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private ImageView mIvResult;
    private TextView mTvResult;
    OkHttpClient mOkHttpClient = new OkHttpClient.Builder()
            .cookieJar(new CookieJar() {                                            //设置Cookie
                private Map<String,List<Cookie>> cookieStore = new HashMap<>();

                @Override
                public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                    cookieStore.put(url.host(),cookies);
                }

                @Override
                public List<Cookie> loadForRequest(HttpUrl url) {
                    List<Cookie> cookies = cookieStore.get(url.host());
                    return cookies != null ? cookies:new ArrayList<Cookie>();
                }
            }).build();
    private String mBaseUrl = "http://192.168.13.21:8080/123/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvResult = (TextView) findViewById(R.id.tv_result);
        mIvResult = (ImageView) findViewById(R.id.iv_result);
    }

    //Get提交参数，直接添加到URL后
    public void doGet(View view) {

        //1.拿到okHttpClient对象
        //2.构造request
        Request.Builder builder = new Request.Builder();
        Request request = builder
                .get()
                .url(mBaseUrl + "login?username=hyman&password=123456")
                .build();

        executeRequest(request);
    }

    private void executeRequest(Request request) {
        Call call = mOkHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                L.e("OnFailure:" + e);
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                L.e("onResponse:");
                final String str = response.body().string();
                L.e(str);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTvResult.setText(str);
                    }
                });
            }
        });
    }

    /*
    * //1.拿到OKHttpClient对象
    *2.构造Request
    *2.1构造RequestBody
    *2.2包装RequestBody
    *3.Call  →  execute
    * */

    //Post提交参数，键值对
    public void doPost(View view) {

        FormBody.Builder requestBodyBuilder = new FormBody.Builder();
        RequestBody requestBody = requestBodyBuilder
                .add("username", "haha")
                .add("password", "123")
                .build();

        Request.Builder builder = new Request.Builder();
        Request request = builder
                .url(mBaseUrl + "login")
                .post(requestBody)
                .build();
        executeRequest(request);
    }

    //Post提交String
    public void doPostString(View view) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain;charset=utf-8"), "{username:qqq,password:111}");
        Request request = new Request.Builder()
                .url(mBaseUrl + "postString")
                .post(requestBody)
                .build();
        executeRequest(request);
    }

    //Post提交文件
    public void doPostFile(View view) {
        File file = new File(Environment.getExternalStorageDirectory(), "masami.jpg");
        if (!file.exists()) {
            L.e(file.getAbsolutePath() + " not exists");
            return;
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        Request request = new Request.Builder()
                .url(mBaseUrl + "postFile")
                .post(requestBody)
                .build();
        executeRequest(request);
    }

    //上传文件
    public void doUpload(View view) {
        File file = new File(Environment.getExternalStorageDirectory(), "masami.jpg");
        if (!file.exists()) {
            L.e(file.getAbsolutePath() + " not exists");
            return;
        }
        //构造RequestBody
        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("username", "wu")
                .addFormDataPart("password", "haha")
                .addFormDataPart("mPhoto", "wu.jpg", RequestBody.create(MediaType.parse("application/octet-stream"), file))
                .build();

        CountingRequestBody countingRequestBody = new CountingRequestBody(multipartBody, new CountingRequestBody.Listener() {
            @Override
            public void onRequestProgress(long bytesWritten, long contentLength) {
                L.e("onRequestProgress: " + bytesWritten + "/" + contentLength);
            }
        });

        Request.Builder builder = new Request.Builder();
        Request request = builder.url(mBaseUrl + "uploadInfo")
                .post(countingRequestBody)
                .build();
        executeRequest(request);
    }

    //下载文件
    public void doDownload(View view) {
        Request request = new Request.Builder()
                .get()
                .url(mBaseUrl + "files/wu.jpg")
                .build();

        Call call = mOkHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                L.e("onFailure: " + e);
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                L.e("onResponse:");

                final long total = response.body().contentLength();     //文件总长
                long sum = 0L;

                InputStream inputStream = response.body().byteStream();
                int len = 0;
                byte[] buf = new byte[255];
                File file = new File(Environment.getExternalStorageDirectory(), "tushengzhi.jpg");
                FileOutputStream fos = null;

                try {
                    fos = new FileOutputStream(file);
                    while ((len = inputStream.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        L.e(sum + "/" + total );

                        final long finalSum = sum;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTvResult.setText(finalSum + "/" + total);
                            }
                        });
                    }
                    fos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    if (fos != null) fos.close();
                    if (inputStream != null) inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                L.e("Download Success!");

            }
        });

    }

    public void doDownloadImage(View view){
        Request request = new Request.Builder()
                .get()
                .url(mBaseUrl + "files/wu.jpg")
                .build();
        Call call = mOkHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                L.e("onFailure: " + e);
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                L.e("onResponse: ");
                InputStream inputStream = response.body().byteStream();

                final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mIvResult.setImageBitmap(bitmap);
                    }
                });
            }
        });
    }
}
