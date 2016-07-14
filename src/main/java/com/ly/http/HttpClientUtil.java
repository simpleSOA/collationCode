package com.ly.http;

import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.*;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpClientUtil {

    public static final Logger log = LoggerFactory.getLogger(HttpClientUtil.class);

    final static int CONNECTION_TIMEOUT = 2 * 1000; //设置连接超时时间，单位毫秒
    final static int SO_TIMEOUT = 2 * 1000; //请求获取数据的超时时间，单位毫秒
    final static int CONNECTION_REQUEST_TIMEOUT = 2 * 1000; //设置从connect Manager获取Connection 超时时间，单位毫秒

    private static RequestConfig requestConfig = RequestConfig.custom()
            .setSocketTimeout(SO_TIMEOUT)
            .setConnectTimeout(CONNECTION_TIMEOUT).setCookieSpec(CookieSpecs.IGNORE_COOKIES)
            .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
            .build();

    private static PoolingHttpClientConnectionManager connManager;
    private static CloseableHttpClient httpClient;

    static {
        SSLContext sslcontext = SSLContexts.createSystemDefault();

        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(sslcontext))
                .build();

        connManager = new PoolingHttpClientConnectionManager(
                socketFactoryRegistry);

        SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).build();
        connManager.setDefaultSocketConfig(socketConfig);
        connManager.setValidateAfterInactivity(1000);

        // Create message constraints
        MessageConstraints messageConstraints = MessageConstraints.custom()
                .setMaxHeaderCount(200)
                .setMaxLineLength(2000)
                .build();
        // Create connection configuration
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setMalformedInputAction(CodingErrorAction.IGNORE)
                .setUnmappableInputAction(CodingErrorAction.IGNORE)
                .setCharset(Consts.UTF_8)
                .setMessageConstraints(messageConstraints)
                .build();
        connManager.setDefaultConnectionConfig(connectionConfig);
        // Configure total max or per route limits for persistent connections
        // that can be kept in the pool or leased by the connection manager.
        connManager.setMaxTotal(100);
        connManager.setDefaultMaxPerRoute(10);

        httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connManager).build();
    }


    /**
     * 发送 post请求
     * @param httpUrl 地址
     * @param maps 参数
     */
    public static String sendHttpPost(String httpUrl, Map<String, String> maps) throws IOException{
        return sendHttpPost(httpUrl,maps,Consts.UTF_8);
    }

    /**
     * 发送 post请求
     * @param httpUrl 地址
     * @param maps 参数
     * @param charset 按给定的字符集给参数编码
     */
    public static String sendHttpPost(String httpUrl, Map<String, String> maps, Charset charset) throws IOException{
        HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost
        // 创建参数队列
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        for (String key : maps.keySet()) {
            nameValuePairs.add(new BasicNameValuePair(key, maps.get(key)));
        }
        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, charset));
        return sendHttpPost(httpPost);
    }

    public static String sendHttpGet(String url,Map<String, String> maps) throws Exception{
        HttpGet httpGet = new HttpGet(url);// 创建httpPost
        // 创建参数队列
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        for (String key : maps.keySet()) {
            nameValuePairs.add(new BasicNameValuePair(key, maps.get(key)));
        }
        // 设置参数
        String str = EntityUtils.toString(new UrlEncodedFormEntity(nameValuePairs));
        httpGet.setURI(new URI(httpGet.getURI().toString() + "?" + str));
        CloseableHttpResponse response = null;
        String responseContent = null;
        try {
            // 执行请求
            response = getHttpClient().execute(httpGet);
            HttpEntity entity = response.getEntity();
            responseContent = EntityUtils.toString(entity, "UTF-8");
        } finally {
            try {
                // 关闭连接,释放资源
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                log.warn("close the http response error",e);
            }
        }
        return responseContent;
    }


    public static CloseableHttpClient getHttpClient(){
        return httpClient;
    }

    /**
     * 发送Post请求
     * @param httpPost
     * @return
     */
    private static String sendHttpPost(HttpPost httpPost) throws IOException {
        CloseableHttpResponse response = null;
        String responseContent = null;
        try {
            // 执行请求
            response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            responseContent = EntityUtils.toString(entity, "UTF-8");
        } finally {
            try {
                // 关闭连接,释放资源
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                log.warn("close the http response error",e);
            }
        }
        return responseContent;
    }

    /**
     * 将服务端返回的cookie存放起来
     * @param cookieStore 存放cookie的对象
     * @param response
     */
    public static void parseRespCookie(CookieStore cookieStore, CloseableHttpResponse response){
        Header[] headers = response.getHeaders("Set-Cookie");
        for(Header header : headers){
            HeaderElement[] elements = header.getElements();
            for (HeaderElement element : elements){
                BasicClientCookie cookie = new BasicClientCookie(element.getName(),element.getValue());
                NameValuePair[] params = element.getParameters();
                for (int i = 0; i < params.length; i++) {
                    if ("path".equalsIgnoreCase(params[i].getName())){
                        cookie.setPath(params[i].getValue());
                    }else {
                        cookie.setDomain(params[i].getValue());
                    }
                }
                cookieStore.addCookie(cookie);
            }
        }
    }

    /**
     * 将cookie拼装成http请求中格式的字符串
     */
    public static String getRequestCookie(CookieStore cookieStore){
        String cookieValue = "";
        for(Cookie cookie :cookieStore.getCookies()){
            cookieValue+=cookie.getName()+"="+cookie.getValue()+"; ";
        }
        if(cookieValue.length() > 0){
            cookieValue = cookieValue.substring(0,cookieValue.length()-2);
        }
        return cookieValue;
    }

}
