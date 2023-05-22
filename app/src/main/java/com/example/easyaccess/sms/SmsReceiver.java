package com.example.easyaccess.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SmsReceiver extends BroadcastReceiver {

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    private static final String SMS_SENT = "android.provider.Telephony.SMS_SENT";
    private static final String TAG = "SmsBroadcastReceiver";

    public boolean received = false;

   // public boolean sent = false;

    private List<Message> messageList = new ArrayList<>();

    public List<Message> getMessageList() {
        return messageList;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Intent received " + intent.getAction());
        if (intent.getAction().equals(SMS_RECEIVED)) {
            Log.d(TAG, "SMS RECEIVED");
            received = true;
        }
    }
}

