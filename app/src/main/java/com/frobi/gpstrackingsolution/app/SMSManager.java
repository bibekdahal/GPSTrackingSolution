package com.frobi.gpstrackingsolution.app;

import android.content.Context;
import android.telephony.SmsManager;
import android.widget.Toast;

public class SMSManager {
    public static void SendSMS(String destination, String message, Context context) {
        // Uncomment the lines below to actually send sms
        /*SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(destination, null, message, null, null);*/
        Toast.makeText(context, "Sending Message to: " + destination + "\nwith:\n" + message, Toast.LENGTH_LONG).show();
    }

    public static void Panic(Context context, GPSData gpsData) {
        String numbers[] = new String[] {
                SettingsActivity.GetStringSetting("emergencyno1", "", context),
                SettingsActivity.GetStringSetting("emergencyno2", "", context),
                SettingsActivity.GetStringSetting("emergencyno3", "", context),
                SettingsActivity.GetStringSetting("emergencyno4", "", context),
        };
        String message = SettingsActivity.GetStringSetting("panicMessage", "", context);
        message += "\n" + "Sent From: " +
                Double.toString(gpsData.GetLatitude()) + ", " +
                Double.toString(gpsData.GetLongitude()) + " at " + gpsData.GetTime()
                + "\nSpeed: " + Double.toString(gpsData.GetSpeed())
                + " Dir: " + Double.toString(gpsData.GetDirection());
        for (int i=0; i<4; ++i)
            if (!numbers[i].equals(""))
                SendSMS(numbers[i], message, context);
    }
}
