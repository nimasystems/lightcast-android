package com.nimasystems.lightcast.network;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.support.annotation.RequiresApi;

// TODO: make this shit work!
public class ConnectionStateMonitor {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class ConnectivityManagerNetworkCallback extends ConnectivityManager.NetworkCallback {
        private ConnectionStateMonitor mMonitor;

        public ConnectivityManagerNetworkCallback(ConnectionStateMonitor monitor) {
            super();

            mMonitor = monitor;
        }

        private void updateNetworkState(Network network) {
            boolean connected = false;

            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connectivityManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    connected = connectivityManager.bindProcessToNetwork(network);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    try {
                        connected = ConnectivityManager.setProcessDefaultNetwork(network);
                    } catch (Exception e) {
                        //
                    }
                }

                if (mMonitor != null && mMonitor.hasNetwork != connected) {
                    mMonitor.hasNetwork = connected;

                    if (mListener != null) {
                        mListener.onNetworkStatusChanged(mMonitor.hasNetwork);
                    }
                }
            }
        }

        @Override
        public void onAvailable(Network network) {
            updateNetworkState(network);
        }

        @Override
        public void onLost(Network network) {
            updateNetworkState(network);
        }
    }

    private NetworkRequest networkRequest;

    private boolean isEnabled;
    private boolean hasNetwork;

    private ConnectivityManagerNetworkCallback networkCallback;

    private Context context;
    private ConnectionStateMonitorListener mListener;

    public interface ConnectionStateMonitorListener {
        void onNetworkStatusChanged(boolean connected);
    }

    public ConnectionStateMonitor(Context context) {
        this.context = context;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            networkCallback = new ConnectivityManagerNetworkCallback(this);
            networkRequest = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .build();
        }
    }

    @SuppressLint("MissingPermission")
    public void enable() {
        if (!isEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

                if (connectivityManager != null) {
                    connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
                }
            }

            isEnabled = true;
        }
    }

    public void disable() {
        if (isEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

                if (connectivityManager != null) {
                    connectivityManager.unregisterNetworkCallback(networkCallback);
                }
            }

            isEnabled = false;
        }
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setListener(ConnectionStateMonitorListener listener) {
        this.mListener = listener;
    }

    public boolean hasNetwork() {
        return hasNetwork;
    }
}