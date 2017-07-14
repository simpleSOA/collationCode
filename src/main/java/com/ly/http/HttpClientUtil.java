package com.ly.http;

import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.*;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpClientUtil {

    public static final Logger LOGGER = LoggerFactory.getLogger(HttpClientUtil.class);

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
        X509TrustManager tm = new X509TrustManager() {

            public void checkClientTrusted(X509Certificate[] xcs,
                                           String string) throws CertificateException {

            }
            public void checkServerTrusted(X509Certificate[] xcs,
                                           String string) throws CertificateException {
            }
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        SSLContext sslcontext;
        try {
            sslcontext = SSLContext.getInstance("SSL");
            sslcontext.init(null,new TrustManager[]{tm},null);
        } catch (Exception e) {
            throw new RuntimeException("init SSLContext error",e);
        }

        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(sslcontext))
                .build();

        connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
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
        /**
         * 此处解释下MaxtTotal和DefaultMaxPerRoute的区别：
            1、MaxtTotal是整个池子的大小；
            2、DefaultMaxPerRoute是根据连接到的主机对MaxTotal的一个细分；比如：
            MaxtTotal=400 DefaultMaxPerRoute=200
            而我只连接到http://sishuok.com时，到这个主机的并发最多只有200；而不是400；
            而我连接到http://sishuok.com 和 http://qq.com时，到每个主机的并发最多只有200；即加起来是400（但不能超过400）；所以起作用的设置是DefaultMaxPerRoute。
         */
        connManager.setMaxTotal(100);
        connManager.setDefaultMaxPerRoute(10);


        httpClient = HttpClients.custom().setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
            @Override
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                return -1;
            }
        }).setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connManager).build();
    }


    /**
     * 发送 post请求，默认编码是UTF-8
     * @param httpUrl 地址
     * @param maps 参数
     */
    public static String sendHttpPost(String httpUrl, Map<String, String> maps, Map<String, String> headers) throws IOException{
        return sendHttpPost(httpUrl, maps, StandardCharsets.UTF_8, headers);
    }


    /**
     * 发送 post请求
     * @param httpUrl 地址
     * @param maps 参数
     * @param charset 按给定的字符集给参数编码
     */
    public static String sendHttpPost(String httpUrl, Map<String, String> maps, Charset charset,Map<String, String> headers) throws IOException{
        HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost
        // 创建参数队列
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> m : maps.entrySet()) {
            nameValuePairs.add(new BasicNameValuePair(m.getKey(), m.getValue()));
        }
        if(headers != null){
            for (Map.Entry<String, String> m : headers.entrySet()) {
                httpPost.addHeader(m.getKey(), m.getValue());
            }
        }else{
            addDefaultHeader(httpPost);
        }
        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, charset));
        return sendHttpPost(httpPost, charset);
    }

    /**
     * 发送doGet请求，默认编码是UTF-8
     * @param url 地址
     * @param maps 参数
     */
    public static String sendHttpGet(String url,Map<String, String> maps,Map<String, String> headers) throws Exception{
        return sendHttpGet(url,maps,StandardCharsets.UTF_8,headers);
    }

    public static String sendHttpGet(String url,Map<String, String> maps, Charset charset,Map<String, String> headers) throws Exception{
        HttpGet httpGet = new HttpGet(url);// 创建httpPost
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        for (Map.Entry<String, String> m : maps.entrySet()) {
            nameValuePairs.add(new BasicNameValuePair(m.getKey(), m.getValue()));
        }
        // 设置参数
        String str = EntityUtils.toString(new UrlEncodedFormEntity(nameValuePairs));
        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("get request url [{}], query string [{}]",url,str);
        }
        httpGet.setURI(new URI(httpGet.getURI().toString() + "?" + str));
        if(headers != null){
            for (Map.Entry<String, String> m : headers.entrySet()) {
                httpGet.addHeader(m.getKey(), m.getValue());
            }
        }else{
            addDefaultHeader(httpGet);
        }
        try(CloseableHttpResponse response = getHttpClient().execute(httpGet)) {
            return EntityUtils.toString(response.getEntity(), charset);
        }
    }


    public static CloseableHttpClient getHttpClient(){
        return httpClient;
    }

    private static void addDefaultHeader(HttpRequestBase request){
        request.addHeader(HTTP.CONN_DIRECTIVE,HTTP.CONN_CLOSE);
    }

    /**
     * 发送Post请求
     * @param httpPost
     * @return
     */
    private static String sendHttpPost(HttpPost httpPost, Charset charset) throws IOException {
        try (CloseableHttpResponse response = httpClient.execute(httpPost)){
            return EntityUtils.toString(response.getEntity(), charset);
        }
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
