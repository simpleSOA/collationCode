package com.ly.http;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * Jersey客户端初始化单例
 * @author: Administrator
 * Date: 2016/3/1 Time: 14:00
 */
public class JerseyClientInit {
    private static JerseyClientInit ourInstance = new JerseyClientInit();

    public static JerseyClientInit getInstance() {
        return ourInstance;
    }

    private Client client;

    private JerseyClientInit() {
        ClientConfig cc = new DefaultClientConfig();
        cc.getProperties().put(ClientConfig.PROPERTY_CONNECT_TIMEOUT,1000*10);
        cc.getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT,1000*10);

        SSLContext sslcontext;
        try {
            sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, null, null);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
        cc.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties(
                new HostnameVerifier() {
                    @Override
                    public boolean verify( String s, SSLSession sslSession ) {
                        return true;
                    }
                },sslcontext
        ));

        client =  Client.create(cc);
    }
}
