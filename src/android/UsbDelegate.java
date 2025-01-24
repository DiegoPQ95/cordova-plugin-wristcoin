package com.runfoodapp.CordovaPluginWristCoin;

import static android.content.Context.RECEIVER_EXPORTED;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;

import androidx.annotation.NonNull;


public class UsbDelegate {

    private static final String ACTION_USB_PERMISSION = "com.taptrack.tappy.usb.USB_PERMISSION";

    public interface PermissionListener {
        void permissionDenied(UsbDevice device);
        void permissionGranted(UsbDevice device);
    }

    @NonNull
    private final Context context;
    @NonNull
    private final PendingIntent permissionIntent;
    @NonNull
    private final IntentFilter permissionFilter;
    @NonNull
    private final UsbManager usbManager;
    @NonNull
    private final PermissionListener listener;


    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(ACTION_USB_PERMISSION.equals(intent.getAction())) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,false)) {
                    if(device != null) {
                        listener.permissionGranted(device);
                    } else {
                        throw new IllegalStateException("Permission granted for no device");
                    }
                } else {
                    listener.permissionDenied(device);
                }
            }
        }
    };

    public UsbDelegate(@NonNull Context context,
                                 @NonNull PermissionListener listener) {
        this.listener = listener;
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        if(usbManager == null) {
            throw new IllegalStateException("Must have a USB Manager");
        }
        this.context = context.getApplicationContext();
        permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION),  PendingIntent.FLAG_IMMUTABLE );
        permissionFilter = new IntentFilter(ACTION_USB_PERMISSION);
        this.usbManager = usbManager;
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    public void register() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(receiver, permissionFilter, RECEIVER_EXPORTED);
        } else {
            // Para versiones anteriores a 8.0, simplemente registra el receptor sin exportarlo
            context.registerReceiver(receiver, permissionFilter);
        }
    }

    public boolean hasPermission(UsbDevice device){
        return usbManager.hasPermission(device);
    }


    public void requestPermission(UsbDevice device) {
         usbManager.requestPermission(device,permissionIntent);
    }

    public void unregister(){
        context.unregisterReceiver(receiver);
    }
}
