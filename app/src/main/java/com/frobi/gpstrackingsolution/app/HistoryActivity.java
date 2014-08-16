package com.frobi.gpstrackingsolution.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;


public class HistoryActivity extends ActionBarActivity implements GPSListener {

    GPSHistory m_history;
    TableLayout m_tableLayout;
    private static GPSTracker m_tracker;
    public static void SetTracker(GPSTracker tracker) { m_tracker = tracker; }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        m_history = new GPSHistory(this);
        m_tableLayout = (TableLayout) findViewById(R.id.historyTable);
        ShowData();

        findViewById(R.id.refreshHistory).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowData();
            }
        });

        findViewById(R.id.syncHistory).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SyncData(HistoryActivity.this);
                ShowData();
            }
        });

        if (m_tracker != null && GPSTracker.IsRunning())
            m_tracker.SetListener2(this);
    }

    public static void SyncData(Context context) {
        new SyncAll(context).execute();
    }

    private static class SyncAll extends AsyncTask<Void, Void, Void> {
        private String response;
        private GPSHistory m_history;
        private Context m_context;

        public SyncAll(Context context) {
            m_context = context;
            m_history = new GPSHistory(m_context);
        }
        @Override
        protected Void doInBackground(Void... voids) {
            WebAccess access = new WebAccess();
            response = "ERROR";
            try {
                SharedPreferences settings = m_context.getSharedPreferences(RegisterActivity.PREFS_NAME, 0);
                TelephonyManager telephonyManager = (TelephonyManager)m_context.getSystemService(Context.TELEPHONY_SERVICE);
                access.ConnectWithJSON("json", m_history.GetJSON());
                response = access.GetResponse();
                if (!response.contains("SUCCESS 101")) return null;

                List<ImageData> images = m_history.GetNewImages();
                for (ImageData data: images) {
                    File file = new File(data.filepath);
                    if (!file.exists()) continue;
                    access.UploadFile("upload-image", file.getAbsolutePath(),
                            data.history_id, settings.getString("Email", ""), settings.getString("Password", ""), telephonyManager.getDeviceId());
                    response = access.GetResponse();
                    if (!response.contains("SUCCESS 101")) return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void v) {
            //Toast.makeText(HistoryActivity.this, response, Toast.LENGTH_LONG).show();
            //((TextView)findViewById(R.id.testWebView)).setText(response);
            if (response.contains("SUCCESS 101")) {
                m_history.SetUpdateAll();
                m_history.SetUpdateAllImages();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_clearHistory) {
            m_history.DeleteAll();
            PictureManager.DeleteAllImages();
            ShowData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void ShowData() {
        List<GPSData> dataList = m_history.GetAllData();
        Collections.reverse(dataList);

        m_tableLayout.removeAllViews();
        for (GPSData data : dataList) {
            TableRow row = new TableRow(this);
            row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            TextView tv = new TextView(this);
            String txt = "Lat:" + data.GetDataString(0)
                    + "\nLong:" + data.GetDataString(1)
                    + "\nSpeed: " + data.GetDataString(2)
                    + "\nDir: " + data.GetDataString(3);
            tv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            tv.setTextSize(14);
            tv.setPadding(5, 5, 0, 5);
            tv.setText(txt);
            row.addView(tv);

            tv = new TextView(this);
            txt = data.GetDataString(4).replace(' ', '\n');
            tv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(14);
            tv.setText(txt);
            row.addView(tv);

            row.setBackgroundResource(R.drawable.cellshape);
            m_tableLayout.addView(row);
        }
    }

    @Override
    public void LocationChanged() {
        ShowData();
    }
}
