package com.example.onlinellmjlama.utils;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Alarm triggered!", Toast.LENGTH_SHORT).show();
        // Add additional alarm handling logic here
    }
}
