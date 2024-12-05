package com.example.functions;

import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private static final int BLUETOOTH_PERMISSION_REQUEST = 1;
    private TextView outputText;
    private SeekBar volumeSeekBar;
    private SeekBar brightnessSeekBar;
    private Button bluetoothButton;
    private Button wifiButton;
    private Button powerSavingButton;
    private Button gpsButton;
    private Button dndButton;
    private Button soundButton;
    private LocationManager locationManager;
    private NotificationManager notificationManager;
    private Button airplaneModeButton;
// In initializeUI():


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeUI();
        setupListeners();
    }

    private void initializeUI() {
        outputText = findViewById(R.id.outputText);
        volumeSeekBar = findViewById(R.id.volumeSeekBar);
        brightnessSeekBar = findViewById(R.id.brightnessSeekBar);
        bluetoothButton = findViewById(R.id.bluetoothButton);
        wifiButton = findViewById(R.id.wifiButton);
        powerSavingButton = findViewById(R.id.powerSavingButton);
        gpsButton = findViewById(R.id.gpsButton);
        dndButton = findViewById(R.id.dndButton);
        soundButton = findViewById(R.id.soundButton);
        airplaneModeButton = findViewById(R.id.airplaneModeButton);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        volumeSeekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volumeSeekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

        try {
            int brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            brightnessSeekBar.setProgress(brightness);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setupListeners() {
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    String result = adjustVolume(progress);
                    updateOutput(result);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    String result = adjustBrightness(progress);
                    updateOutput(result);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        bluetoothButton.setOnClickListener(v -> {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            boolean enable = bluetoothAdapter != null && !bluetoothAdapter.isEnabled();
            String result = toggleBluetooth(enable);
            updateOutput(result);
        });

        wifiButton.setOnClickListener(v -> {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            boolean enable = !wifiManager.isWifiEnabled();
            String result = toggleWifi(enable);
            updateOutput(result);
        });

        powerSavingButton.setOnClickListener(v -> {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            boolean enable = !powerManager.isPowerSaveMode();
            String result = togglePowerSaving(enable);
            updateOutput(result);
        });

        gpsButton.setOnClickListener(v -> {
            String result = checkGPSStatus();
            updateOutput(result);
        });

        dndButton.setOnClickListener(v -> {
            String result = toggleDND();
            updateOutput(result);
        });

        soundButton.setOnClickListener(v -> {
            openSoundSettings();
        });

        airplaneModeButton.setOnClickListener(v -> {
            String result = toggleAirplaneMode();
            updateOutput(result);
        });
    }

    private String adjustVolume(int volumeLevel) {
        try {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeLevel, 0);
            JSONObject jsonOutput = new JSONObject();
            jsonOutput.put("function", "adjustVolume");
            jsonOutput.put("status", "success");
            jsonOutput.put("volumeLevel", volumeLevel);
            return jsonOutput.toString();
        } catch (Exception e) {
            return createErrorJson("adjustVolume", e.getMessage());
        }
    }

    private String adjustBrightness(int brightnessValue) {
        try {
            if (!Settings.System.canWrite(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                return createErrorJson("adjustBrightness", "Requesting permission");
            }

            // Ensure brightness value is within valid range (0-255)
            brightnessValue = Math.max(0, Math.min(brightnessValue, 255));

            // Set screen brightness
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS,
                    brightnessValue);

            // Also update brightness mode to manual
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

            JSONObject jsonOutput = new JSONObject();
            jsonOutput.put("function", "adjustBrightness");
            jsonOutput.put("status", "success");
            jsonOutput.put("brightnessValue", brightnessValue);
            return jsonOutput.toString();
        } catch (Exception e) {
            return createErrorJson("adjustBrightness", e.getMessage());
        }
    }

    private String toggleBluetooth(boolean enable) {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            // Check for Bluetooth permissions
            if (checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.BLUETOOTH_CONNECT},
                        BLUETOOTH_PERMISSION_REQUEST);
                return createErrorJson("toggleBluetooth", "Bluetooth permission required");
            }

            if (enable) {
                bluetoothAdapter.enable();
            } else {
                bluetoothAdapter.disable();
            }

            JSONObject jsonOutput = new JSONObject();
            jsonOutput.put("function", "toggleBluetooth");
            jsonOutput.put("status", "success");
            jsonOutput.put("bluetoothState", enable ? "enabled" : "disabled");
            return jsonOutput.toString();
        } catch (SecurityException se) {
            return createErrorJson("toggleBluetooth", "Permission denied: " + se.getMessage());
        } catch (Exception e) {
            return createErrorJson("toggleBluetooth", e.getMessage());
        }
    }
    private String toggleWifi(boolean enable) {
        try {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wifiManager.setWifiEnabled(enable);
            JSONObject jsonOutput = new JSONObject();
            jsonOutput.put("function", "toggleWifi");
            jsonOutput.put("status", "success");
            jsonOutput.put("wifiState", enable ? "enabled" : "disabled");
            return jsonOutput.toString();
        } catch (Exception e) {
            return createErrorJson("toggleWifi", e.getMessage());
        }
    }

    private String togglePowerSaving(boolean enable) {
        try {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            boolean isPowerSaveMode = powerManager.isPowerSaveMode();
            JSONObject jsonOutput = new JSONObject();
            jsonOutput.put("function", "togglePowerSaving");
            jsonOutput.put("status", "info");
            jsonOutput.put("powerSavingState", isPowerSaveMode ? "enabled" : "disabled");
            jsonOutput.put("message", "Power saving mode cannot be programmatically toggled");
            return jsonOutput.toString();
        } catch (Exception e) {
            return createErrorJson("togglePowerSaving", e.getMessage());
        }
    }

    private String checkGPSStatus() {
        try {
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            JSONObject jsonOutput = new JSONObject();
            jsonOutput.put("function", "checkGPS");
            jsonOutput.put("status", "success");
            jsonOutput.put("gpsEnabled", isGPSEnabled);
            return jsonOutput.toString();
        } catch (Exception e) {
            return createErrorJson("checkGPS", e.getMessage());
        }
    }

    private String toggleDND() {
        try {
            if (!notificationManager.isNotificationPolicyAccessGranted()) {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                startActivity(intent);
                return createErrorJson("toggleDND", "Permission not granted");
            }

            int currentFilter = notificationManager.getCurrentInterruptionFilter();
            boolean isDNDEnabled = currentFilter != NotificationManager.INTERRUPTION_FILTER_ALL;

            // Toggle DND state
            notificationManager.setInterruptionFilter(
                    isDNDEnabled ?
                            NotificationManager.INTERRUPTION_FILTER_ALL :
                            NotificationManager.INTERRUPTION_FILTER_NONE
            );

            JSONObject jsonOutput = new JSONObject();
            jsonOutput.put("function", "toggleDND");
            jsonOutput.put("status", "success");
            jsonOutput.put("dndState", !isDNDEnabled ? "enabled" : "disabled");
            return jsonOutput.toString();
        } catch (Exception e) {
            return createErrorJson("toggleDND", e.getMessage());
        }
    }private String toggleAirplaneMode() {
        try {
            boolean isAirplaneModeOn = Settings.Global.getInt(
                    getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) == 1;

            // Since Android 4.2, apps cannot directly toggle airplane mode
            // We can only open the settings page
            Intent intent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
            startActivity(intent);

            JSONObject jsonOutput = new JSONObject();
            jsonOutput.put("function", "toggleAirplaneMode");
            jsonOutput.put("status", "info");
            jsonOutput.put("airplaneModeState", isAirplaneModeOn ? "enabled" : "disabled");
            jsonOutput.put("message", "Airplane mode cannot be programmatically toggled");
            return jsonOutput.toString();
        } catch (Exception e) {
            return createErrorJson("toggleAirplaneMode", e.getMessage());
        }
    }
    private void openSoundSettings() {
        startActivity(new Intent(Settings.ACTION_SOUND_SETTINGS));
    }

    private String createErrorJson(String function, String message) {
        try {
            JSONObject jsonOutput = new JSONObject();
            jsonOutput.put("function", function);
            jsonOutput.put("status", "error");
            jsonOutput.put("message", message);
            return jsonOutput.toString();
        } catch (Exception e) {
            return "{\"function\": \"" + function + "\", \"status\": \"error\", \"message\": \"Error creating JSON\"}";
        }
    }

    private void updateOutput(String jsonOutput) {
        outputText.setText(jsonOutput);
        Log.d("FunctionOutput", jsonOutput);
    }
}