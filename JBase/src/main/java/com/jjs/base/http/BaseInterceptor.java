package com.jjs.base.http;

import android.text.TextUtils;

import com.blankj.utilcode.util.LogUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSource;

/**
 * 说明：
 * Created by aa on 2017/12/25.
 */

public abstract class BaseInterceptor implements Interceptor {
    private Map<String, String> fieldParamsMap = new HashMap<>();
    private Map<String, String> headerParamsMap = new HashMap<>();


    protected abstract void _intercept(Request request) throws IOException;

    public void addFieldParam(String key, String value) {
        fieldParamsMap.put(key, value);
    }

    public void addHeaderParam(String key, String value) {
        headerParamsMap.put(key, value);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder requestBuilder = request.newBuilder();
        _intercept(request);
        //注入header参数
        if (headerParamsMap != null && headerParamsMap.size() > 0) {
            Set<String> keys = headerParamsMap.keySet();
            for (String headerKey : keys) {
                requestBuilder.addHeader(headerKey, headerParamsMap.get(headerKey)).build();
            }
        }
        //注入params参数
        if (fieldParamsMap.size() > 0) {
            if (canInjectIntoBody(request)) {
                FormBody.Builder formBodyBuilder = new FormBody.Builder();
                for (Map.Entry<String, String> entry : fieldParamsMap.entrySet()) {
                    formBodyBuilder.add(entry.getKey(), entry.getValue());
                }

                RequestBody formBody = formBodyBuilder.build();
                String postBodyString = bodyToString(request.body());
                postBodyString += ((postBodyString.length() > 0) ? "&" : "") + bodyToString(formBody);
                requestBuilder.post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8"), postBodyString));
            } else {
                Iterator iterator = fieldParamsMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    request.url().newBuilder().addQueryParameter((String) entry.getKey(), (String) entry.getValue());
                }
                requestBuilder.url(request.url().newBuilder().build());
            }
        }

        request = requestBuilder.build();

        //打印发送参数
        sendLogRequest(request);
        Response response = chain.proceed(request);
        //打印接收参数
        sendLogResponse(response);
        return response;
    }

    /**
     * 开始解析发送参数
     */
    private void sendLogRequest(Request request) throws IOException {
        if (request != null) {
            String body = "";
            if (request.body() != null) {
                Buffer buffer = new Buffer();
                request.body().writeTo(buffer);
                //编码设为UTF-8
                Charset charset = Charset.forName("UTF-8");
                MediaType contentType = request.body().contentType();
                if (contentType != null) {
                    charset = contentType.charset(Charset.forName("UTF-8"));
                }
                body = buffer.readString(charset);
                //如果你出现中文参数乱码情况，请进行URL解码处理！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
                body = URLEncoder.encode(body, "UTF-8");
            }
            LogUtils.i("发送----" + "method:" + request.method() + "  url:" + request.url() + "  body:" + body);
        }
    }

    /**
     * 开始解析服务器返回参数
     */
    private void sendLogResponse(Response response) throws IOException {
        String rBody = "";
        if (response != null && response.body() != null) {
            BufferedSource source = response.body().source();
            source.request(Long.MAX_VALUE); // Buffer the entire body.
            Buffer buffer = source.buffer();

            Charset charset = Charset.forName("UTF-8");
            MediaType contentType = response.body().contentType();
            if (contentType != null) {
                try {
                    charset = contentType.charset(Charset.forName("UTF-8"));
                } catch (UnsupportedCharsetException e) {
                    e.printStackTrace();
                }
            }
            rBody = buffer.clone().readString(charset);
        }
        LogUtils.i("接收：" + rBody.toString());
    }

    protected boolean canInjectIntoBody(Request request) {
        if (request == null) {
            return false;
        }
        if (!TextUtils.equals(request.method(), "POST")) {
            return false;
        }
        RequestBody body = request.body();
        if (body == null) {
            return false;
        }
        MediaType mediaType = body.contentType();
        if (mediaType == null) {
            return false;
        }
        if (!TextUtils.equals(mediaType.subtype(), "x-www-form-urlencoded")) {
            return false;
        }
        return true;
    }

    private static String bodyToString(final RequestBody request) {
        try {
            final RequestBody copy = request;
            final Buffer buffer = new Buffer();
            if (copy != null)
                copy.writeTo(buffer);
            else
                return "";
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }


}
