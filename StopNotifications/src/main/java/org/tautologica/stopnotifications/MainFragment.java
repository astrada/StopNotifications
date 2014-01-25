package org.tautologica.stopnotifications;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MainFragment extends Fragment {

    private static final String NOTIFICATION_LIGHT_PULSE = "notification_light_pulse";

    public void onToggleClicked(View view) {
        ToggleButton toggleButton = (ToggleButton)view;
        boolean notificationsEnabled = !toggleButton.isChecked();
        writeState(notificationsEnabled);
        updateInterface(notificationsEnabled);
        toggleNotifications(notificationsEnabled);
    }

    private void updateInterface(boolean notificationsEnabled) {
        ToggleButton toggleButton = (ToggleButton)getView().findViewById(R.id.toggle_button);
        int drawableId = notificationsEnabled ? R.drawable.night_time : R.drawable.day_time;
        // Switch icon
        toggleButton.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(drawableId), null, null, null);
        // Update status
        TextView textView = (TextView)getView().findViewById(R.id.status_text);
        int statusTextId = notificationsEnabled ? R.string.notifications_on : R.string.notifications_off;
        textView.setText(getResources().getString(statusTextId));
    }

    private void toggleNotifications(boolean enabled) {
        // Turn on/off notification LED
        toggleNotificationLightPulse(enabled);
        // Turn on/off WiFi
        toggleWifi(enabled);
        // Turn on/off Mobile Data Connection
        toggleMobileDataConnectivity(enabled);
    }

    private void toggleNotificationLightPulse(boolean enabled) {
        try {
            Settings.System.putInt(getActivity().getContentResolver(),
                    NOTIFICATION_LIGHT_PULSE, enabled ? 1 : 0);
        } catch (Exception e) {
            showMessage(getResources().getString(R.string.led_error));
        }
    }

    private void toggleWifi(boolean enabled) {
        try {
            WifiManager wifiManager = (WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
            wifiManager.setWifiEnabled(enabled);
        } catch (Exception e) {
            showMessage(getResources().getString(R.string.wifi_error));
        }
    }

    private void toggleMobileDataConnectivity(boolean enabled) {
        try {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            Class conmanClass = Class.forName(connectivityManager.getClass().getName());
            Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
            iConnectivityManagerField.setAccessible(true);
            Object iConnectivityManager = iConnectivityManagerField.get(connectivityManager);
            Class iConnectivityManagerClass =  Class.forName(iConnectivityManager.getClass().getName());
            Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
            setMobileDataEnabledMethod.setAccessible(true);
            setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
        } catch (Exception e) {
            showMessage(getResources().getString(R.string.mobile_data_connectivity_error));
        }
    }

    private boolean readState() {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getBoolean(getString(R.string.state_key), true);
    }

    private void writeState(boolean notificationsEnabled) {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.state_key), notificationsEnabled);
        editor.commit();
    }

    private void showMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        boolean notificationsEnabled = readState();
        if (!notificationsEnabled) {
            ToggleButton toggleButton = (ToggleButton)getView().findViewById(R.id.toggle_button);
            toggleButton.setChecked(true);
            updateInterface(false);
        }
    }

}
