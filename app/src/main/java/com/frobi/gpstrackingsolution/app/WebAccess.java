package com.frobi.gpstrackingsolution.app;

import android.content.Context;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

class WebAccess
{
    public final String HOST = "http://192.168.0.4/gps-tracking-app/public/";

    private HttpClient m_httpclient;
    private HttpPost m_request;
    private HttpResponse m_response;

    public void Connect(String addr,  List<NameValuePair> nameValuePairs) throws IOException {
        String url = HOST + addr;
        m_httpclient = new DefaultHttpClient();
        m_request = new HttpPost(url);
        if (nameValuePairs!=null)
            m_request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        m_response = m_httpclient.execute(m_request);
    }
    public void Connect(String addr) throws IOException { Connect(addr, null); }

    public void Connect(String addr, String email, String password, String imei) throws IOException {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("email", email));
        nameValuePairs.add(new BasicNameValuePair("password", password));
        nameValuePairs.add(new BasicNameValuePair("imei", imei));
        Connect(addr, nameValuePairs);
    }

    public void ConnectWithJSON(String addr, String json) throws IOException {
        String url = HOST + addr;
        m_httpclient = new DefaultHttpClient();
        m_request = new HttpPost(url);
        StringEntity se = new StringEntity(json);
        se.setContentType("application/json;charset=UTF-8");
        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
        m_request.setEntity(se);
        m_response = m_httpclient.execute(m_request);
    }

    public String GetResponse() {
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(m_response.getEntity().getContent()));
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = rd.readLine()) != null)
                stringBuilder.append(line).append("\n");
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}