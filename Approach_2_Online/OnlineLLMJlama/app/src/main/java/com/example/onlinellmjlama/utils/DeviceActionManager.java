package com.example.onlinellmjlama.utils;


import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ActivityNotFoundException;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
public class DeviceActionManager {
    private static final int REQUEST_BLUETOOTH_CONNECT = 1;
    private final Context context;
    private final BluetoothAdapter bluetoothAdapter;
    private final WifiManager wifiManager;
    public static final int REQUEST_CAMERA = 1001;


    public DeviceActionManager(Context context) {
        this.context = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    // ========================
    // BLUETOOTH CONTROLS
    // ========================

    public void toggleBluetooth(boolean enable) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager != null ? bluetoothManager.getAdapter() : null;

        if (bluetoothAdapter == null) {
            showToast("Bluetooth not supported on this device");
            return;
        }

        // Check for BLUETOOTH_CONNECT permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // Request the BLUETOOTH_CONNECT permission
                ActivityCompat.requestPermissions(
                        (Activity) context,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        REQUEST_BLUETOOTH_CONNECT
                );
                return;
            }
        }

        // Enable or disable Bluetooth
        if (enable && !bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
            showToast("Bluetooth enabled");
        } else if (!enable && bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
            showToast("Bluetooth disabled");
        } else {
            showToast(enable ? "Bluetooth is already enabled" : "Bluetooth is already disabled");
        }
    }


    // ========================
    // WIFI CONTROLS
    // ========================

    public void toggleWifi(boolean enable) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiManager == null) {
            showToast("Wi-Fi not supported on this device");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent panelIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            context.startActivity(panelIntent);
            showToast("Wi-Fi toggling is restricted on Android 10 and above.");
        } else {
            wifiManager.setWifiEnabled(enable);
            showToast(enable ? "Wi-Fi enabled" : "Wi-Fi disabled");
        }
    }

    // ========================
    // BRIGHTNESS CONTROLS
    // ========================

    public void setBrightness(int level) {
        if (Settings.System.canWrite(context)) {
            int brightness = Math.max(0, Math.min(level, 255));
            Settings.System.putInt(
                    context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS,
                    brightness
            );
            showToast("Brightness set to " + brightness);
        } else {
            showToast("Permission needed to change brightness");
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            context.startActivity(intent);
            showToast("Grant WRITE_SETTINGS Permission to change permission");

        }
    }

    // ========================
    // CALENDAR EVENT
    // ========================
    public void addCalendarEvent(String title, long startTimeMillis) {
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, title)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTimeMillis);
        context.startActivity(intent);
        showToast("Opening calendar to add event.");
    }

    // ========================
    // CAMERA CAPTURE
    // ========================

    public void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Check if the context is an Activity for startActivityForResult
        if (context instanceof Activity) {
            if (cameraIntent.resolveActivity(context.getPackageManager()) != null) {
                ((Activity) context).startActivityForResult(cameraIntent, REQUEST_CAMERA);
            } else {
                showToast("No camera app found");
            }
        } else {
            // Fallback for non-Activity contexts
            cameraIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(cameraIntent);
            } catch (ActivityNotFoundException e) {
                showToast("No camera app available");
            }
        }
    }


    // ========================
    // SEND SMS
    // ========================
    public void sendSms(String phoneNumber, String message) {
        try {
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
            if (phoneNumber.length()==10){
                phoneNumber = "+91" + phoneNumber;
            }
            smsIntent.setData(Uri.parse("smsto:" + phoneNumber));
            smsIntent.putExtra("sms_body", message);
            context.startActivity(smsIntent);
            showToast("Opening SMS app.");
        } catch (Exception e) {
            showToast("Failed to send SMS.");
        }
    }

    // ========================
    // ADD CONTACT
    // ========================
    public void addContact(String name, String phone) {
        try {
            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
            intent.putExtra(ContactsContract.Intents.Insert.NAME, name);
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, phone);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "No contacts app installed", Toast.LENGTH_SHORT).show();
        }
    }

    // ========================
    // SET ALARM
    // ========================
    public void setAlarm(int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Adjust for past time
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            // Best practice for modern Android versions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }
            showToast("Alarm set for " + hour + ":" + String.format("%02d", minute));
        } else {
            showToast("Failed to set alarm.");
        }
    }


    // ========================
    // ADJUST VOLUME
    // ========================
    public void adjustVolume(int volumeLevel) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeLevel, AudioManager.FLAG_SHOW_UI);
        showToast("Volume adjusted.");
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    // ========================
    // ACTION EXECUTOR
    // ========================

    public void executeAction(String actionName, String parameters) {
        try {
            if (parameters==""){
                switch (actionName.toLowerCase()) {
                case "open_camera":
                    openCamera();
                    showToast("Camera Opened: ");
            }
            }
            // Check if parameters is a JSON object or array
            else if (parameters.trim().startsWith("{")) {
                // Parse as JSON object
                JSONObject params = new JSONObject(parameters);

                switch (actionName.toLowerCase()) {
                    case "toggle_bluetooth":
                        toggleBluetooth(params.getBoolean("state"));
                        break;

                    case "toggle_wifi":
                        toggleWifi(params.getBoolean("state"));
                        break;

                    case "set_brightness":
                        setBrightness(params.getInt("level"));
                        break;

//                    case "add_calendar_event":
//                        String title = params.getString("title");
//                        long startTimeMillis = params.getLong("start_time");
//                        addCalendarEvent(title, startTimeMillis);
//                        break;

                    case "send_sms":
                        String phoneNumber = params.getString("phone_number");
                        String message = params.getString("message");
                        sendSms(phoneNumber, message);
                        break;

                    case "add_contact":
                        String contactName = params.getString("name");
                        String contactPhoneNumber = params.getString("phone_number");
                        addContact(contactName, contactPhoneNumber);
                        break;

                    case "set_alarm":
                        int hour = params.getInt("hour");
                        int minute = params.getInt("minute");
                        setAlarm(hour, minute);
                        break;

                    case "adjust_volume":
                        int volumeLevel = params.getInt("level");
                        adjustVolume(volumeLevel);
                        break;

                    default:
                        showToast("Unknown action: " + actionName);
                        break;
                }
            } else if (parameters.trim().startsWith("[")) {
                // Parse as JSON array
                JSONArray paramsArray = new JSONArray(parameters);

                switch (actionName.toLowerCase()) {
                    case "toggle_bluetooth":
                    case "toggle_wifi":
                    case "set_brightness":
                    case "adjust_volume": // Handle single-value array for alarms
                    case "send_sms":
                        if (paramsArray.length() > 0) {
                            switch (actionName.toLowerCase()) {
                                case "toggle_bluetooth":
                                    toggleBluetooth(paramsArray.getBoolean(0));
                                    break;
                                case "toggle_wifi":
                                    toggleWifi(paramsArray.getBoolean(0));
                                    break;
                                case "set_brightness":
                                    setBrightness(paramsArray.getInt(0));
                                    break;
                                case "adjust_volume":
                                    adjustVolume(paramsArray.getInt(0));
                                    break;
                                case "send_sms":
                                    String phoneNumber = paramsArray.getString(0);
                                    String message = paramsArray.getString(1);
                                    sendSms(phoneNumber, message);
                                    break;
                                case "add_contact":
                                    String contactName = paramsArray.getString(0);
                                    String contactPhoneNumber =paramsArray.getString(1);
                                    addContact(contactName, contactPhoneNumber);
                                    break;
                                case "set_alarm":
                                    int hour = paramsArray.getInt(0);
                                    int minute = paramsArray.getInt(1);
                                    setAlarm(hour, minute);
                                    break;

                            }
                        }
                        break;
                    default:
                        showToast("Unsupported action for array parameters: " + actionName);
                        break;
                }
            } else {
                showToast("Invalid parameter format");
            }
        } catch (Exception e) {
            showToast("Error executing action: " + e.getMessage());
            Log.e("ACTION_ERROR", "Parameter parsing failed", e);
        }
    }

}
