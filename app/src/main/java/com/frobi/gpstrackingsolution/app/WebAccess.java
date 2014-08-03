package com.frobi.gpstrackingsolution.app;

import android.content.Context;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

class WebAccess
{
    public final String HOST = "http://192.168.0.4/gps-tracking-app/public/";

    private HttpClient m_httpclient;
    private HttpPost m_request;
    private HttpResponse m_response;
    private Context m_context;

    public WebAccess(Context context)
    {
        m_context = context;
    }

    public void Connect(String addr,  List<NameValuePair> nameValuePairs) {
        String url = HOST + addr;
        try {
            m_httpclient = new DefaultHttpClient();
            m_request = new HttpPost(url);
            if (nameValuePairs!=null)
                m_request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            m_response = m_httpclient.execute(m_request);
        } catch(Exception ignored){
            Toast.makeText(m_context, "Error Connecting: " + url, Toast.LENGTH_SHORT).show();
        }
    }
    public void Connect(String addr) { Connect(addr, null); }

    public void Connect(String addr, String email, String password, String imei) {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("email", email));
        nameValuePairs.add(new BasicNameValuePair("password", password));
        nameValuePairs.add(new BasicNameValuePair("imei", imei));
        Connect(addr, nameValuePairs);
    }

    public String GetResponse() {
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(m_response.getEntity().getContent()));
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = rd.readLine()) != null)
                stringBuilder.append(line).append("\n");
            return stringBuilder.toString();
        } catch (Exception ignored) {}
        return "";
    }
}