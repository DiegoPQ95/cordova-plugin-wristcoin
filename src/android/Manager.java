package com.runfoodapp.CordovaPluginWristCoin;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.util.Log;

import com.taptrack.tcmptappy2.usb.TappyUsb;

import java.util.List;

public class Manager {

    private static final String TAG = "WristCoinDevice";
    private final Context context;
    private final UsbDelegate permissionDelegate;

    private Callback callback;
    private final UsbDelegate.PermissionListener usbPermissionListener;

    public Manager(Context ctx) {
        this.context = ctx;

        this.usbPermissionListener = new UsbDelegate.PermissionListener() {
            @Override
            public void permissionDenied(UsbDevice device) {
                Log.i(TAG, "Permission denied");
                // Hay un error actualmente en los permisos
                // aunque si sea apruebe, el permiso sale que esta denegado...
                if (device == null && callback != null) {
                    FindDevice(callback);
                    return;
                }
                permissionDelegate.unregister();
                if (callback == null) return;
                callback.failed("PERMISSION_DENIED");
            }

            @Override
            public void permissionGranted(UsbDevice device) {
                Log.i(TAG, "Permission granted");
                permissionDelegate.unregister();
                if (callback == null) return;
                callback.success( new Device(TappyUsb.getTappyUsb(context, device)) );
            }
        };

        // Inicializar el delegado de permisos
        permissionDelegate = new UsbDelegate(ctx, usbPermissionListener);
    }

    public void FindDevice(Callback cb) {
        try {
            this.callback = cb;
            List<UsbDevice> usbDevices = TappyUsb.getPotentialTappies(context);
            assert usbDevices != null;
            for (UsbDevice device : usbDevices) {
                    permissionDelegate.register();
                    if (permissionDelegate.hasPermission(device)) {
                        usbPermissionListener.permissionGranted(device);
                        return;
                    }
                    permissionDelegate.requestPermission(device);
                    return;
            }
            cb.failed("DEVICE_NOT_PLUGGED_IN");
        } catch (Exception ex) {
            Log.e("SCAN", "Error al escanear", ex);
            callback.failed(ex.getMessage());
        }
    }

    public interface Callback {
        void success(Device device);
        void failed(String error_message);

    }

}