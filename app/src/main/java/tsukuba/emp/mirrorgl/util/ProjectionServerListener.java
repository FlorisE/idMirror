package tsukuba.emp.mirrorgl.util;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

/**
 * Created by utail on 06/10/2015.
 */
public class ProjectionServerListener implements NsdManager.DiscoveryListener {

    private static String tag = "DiscoveryListener";

    @Override
    public void onStartDiscoveryFailed(String serviceType, int errorCode) {
        Log.d(tag, "Discovery failed, error code " + errorCode);
    }

    @Override
    public void onStopDiscoveryFailed(String serviceType, int errorCode) {
        Log.d(tag, "Discovery failed, error code " + errorCode);
    }

    @Override
    public void onDiscoveryStarted(String serviceType) {
        Log.d(tag, "Discovery started, service type: " + serviceType);
    }

    @Override
    public void onDiscoveryStopped(String serviceType) {
        Log.d(tag, "Discovery stopped, service type: " + serviceType);
    }

    @Override
    public void onServiceFound(NsdServiceInfo serviceInfo) {
        Log.d(tag, "Service found, service name: " + serviceInfo.getServiceName());
    }

    @Override
    public void onServiceLost(NsdServiceInfo serviceInfo) {
        Log.d(tag, "Service lost, service name: " + serviceInfo.getServiceName());
    }
}
