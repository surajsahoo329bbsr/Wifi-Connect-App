package com.example.wificonnectapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private EditText etWifiName, etWifiPassword;
    private WifiManager wifiManager;
    private List<WifiNetworkSuggestion> suggestionsList;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etWifiName = findViewById(R.id.et_wifi_name);
        etWifiPassword = findViewById(R.id.et_wifi_password);
        Button btnConnect = findViewById(R.id.btn_connect);
        Button btnDisconnect = findViewById(R.id.btn_disconnect);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        suggestionsList = new ArrayList<>();
        /*networkRequest =  new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                .build();*/

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etWifiName.getText().toString().trim().isEmpty() || etWifiPassword.getText().toString().trim().isEmpty())
                    Toast.makeText(getApplicationContext(), "Please enter fields", Toast.LENGTH_LONG).show();
                else
                    connectWifi();
            }
        });

        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (wifiManager.isWifiEnabled()) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            connectivityManager.unregisterNetworkCallback(networkCallback);
                            Toast.makeText(getApplicationContext(), "Disconnected Android 10+", Toast.LENGTH_LONG).show();
                            /*final int status = wifiManager.removeNetworkSuggestions(new ArrayList<WifiNetworkSuggestion>());
                            if (status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
                                // do error handling here…
                                if (networkCallback != null && connectivityManager != null) {
                                    connectivityManager.unregisterNetworkCallback(networkCallback);
                                    networkCallback = null;
                                    Toast.makeText(getApplicationContext(), "Disconnected Android 10+", Toast.LENGTH_LONG).show();
                                }
                                else{
                                    Toast.makeText(getApplicationContext(), "NetworkCallback Or ConnectivityMgr Null", Toast.LENGTH_LONG).show();
                                }
                            }
                            else
                                Toast.makeText(getApplicationContext(), "Error Disconnecting Android 10+", Toast.LENGTH_LONG).show();*/
                        } else {
                            if (wifiManager.getConnectionInfo().getNetworkId() == -1) {
                                Toast.makeText(getApplicationContext(), "Connect to a network first", Toast.LENGTH_LONG).show();
                            } else {
                                int networkId = wifiManager.getConnectionInfo().getNetworkId();
                                wifiManager.removeNetwork(networkId);
                                wifiManager.saveConfiguration();
                                wifiManager.disconnect();
                                Toast.makeText(getApplicationContext(), "Disconnected Android 10-", Toast.LENGTH_LONG).show();
                            }
                        }
                    } else
                        Toast.makeText(getApplicationContext(), "Wifi Turned Off", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Disconnect Exception : " + e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void connectWifi() {

        try {
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                //Connects to network with internet connection but cannot be removed
                /*WifiNetworkSuggestion wifiNetworkSuggestionWpa2 = new WifiNetworkSuggestion.Builder()
                        .setSsid("MyAirtel")
                        .setWpa2Passphrase("@dmin#329")
                        .build();

                WifiNetworkSuggestion wifiNetworkSuggestionWpa3 = new WifiNetworkSuggestion.Builder()
                        .setSsid("MyAirtel")
                        .setWpa3Passphrase("@dmin#329")
                        .build();

                suggestionsList = new ArrayList<>();
                suggestionsList.add(wifiNetworkSuggestionWpa2);
                suggestionsList.add(wifiNetworkSuggestionWpa3);

                int status = wifiManager.addNetworkSuggestions(suggestionsList);

                if (status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
                    Toast.makeText(getApplicationContext(), "Oh yeah", Toast.LENGTH_LONG).show();
                }*/

                //Connects to network without internet connection but can be removed
                WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder();
                builder.setSsid(etWifiName.getText().toString().trim());
                builder.setWpa2Passphrase(etWifiPassword.getText().toString().trim());

                WifiNetworkSpecifier wifiNetworkSpecifier = builder.build();

                final NetworkRequest.Builder networkRequestBuilder1 = new NetworkRequest.Builder();
                networkRequestBuilder1.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
                networkRequestBuilder1.setNetworkSpecifier(wifiNetworkSpecifier);

                NetworkRequest networkRequest = networkRequestBuilder1.build();

                /*final NetworkRequest networkRequest = new NetworkRequest.Builder()
                                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                                        .build();*/

                // Optional (Wait for post connection broadcast to one of your suggestions)
                /*final IntentFilter intentFilter = new IntentFilter(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION);

                final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (!intent.getAction().equals(
                                WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION)) {
                            Toast.makeText(getApplicationContext(), "Error Connecting Android 10+ 2", Toast.LENGTH_LONG).show();
                            return;
                        }
                        // do post connect processing here...

                    }
                };*/

                networkCallback = new
                        ConnectivityManager.NetworkCallback() {
                            @Override
                            public void onAvailable(@NonNull Network network) {
                                super.onAvailable(network);
                                connectivityManager.bindProcessToNetwork(network);
                                Toast.makeText(getApplicationContext(), "Connected Android 10+", Toast.LENGTH_LONG).show();
                            }
                        };

                connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
                connectivityManager.requestNetwork(networkRequest, networkCallback);
            } else {
                WifiConfiguration wifiConfiguration = new WifiConfiguration();
                wifiConfiguration.SSID = String.format("\"%s\"", etWifiName.getText().toString().trim());
                wifiConfiguration.preSharedKey = String.format("\"%s\"", etWifiPassword.getText().toString().trim());
                int wifiId = wifiManager.addNetwork(wifiConfiguration);
                wifiManager.enableNetwork(wifiId, true);
                Toast.makeText(getApplicationContext(), "Connected Android 10-", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Connect Exception : " + e.toString(), Toast.LENGTH_LONG).show();
        }
    }
}