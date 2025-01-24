package com.runfoodapp.CordovaPluginWristCoin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.mywristcoin.wristcoinpos.AppWristbandState;

public class WristCoin extends CordovaPlugin {

    // Este método es llamado desde JavaScript cuando el desarrollador invoca una acción
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        // Aquí se manejan las acciones específicas del plugin
        if (action.equals("initializeDevice")) {
            this.initializeDevice(callbackContext);
            return true;
        } else if (action.equals("readWristBand")) {
            this.readWristBand(callbackContext);
            return true;
        } else {
            callbackContext.error(String.format("El modulo 'WristCoin' no tiene la acción: '%s'", action));
        }
        return false;
    }

    private Device connectedDevice;

    // Función para mostrar un mensaje
    private void initializeDevice(CallbackContext callbackContext) {

        Manager mgr = new Manager(cordova.getActivity());
        Manager.Callback deviceCallback = new Manager.Callback() {
            @Override
            public void success(Device device) {
                // Actualizamos el texto cuando el dispositivo se encuentra
                connectedDevice = device;
                try {
                    JSONObject result = new JSONObject();
                    result.put("deviceId", device.description());
                    result.put("deviceType", "usb");
                    result.put("deviceStatus", "connected");
                    callbackContext.success(result);
                } catch (JSONException e) {
                    callbackContext.error("JSON_CONVERSION_EXCEPTION:" + e.getMessage());
                }
            }

            @Override
            public void failed(String errMessage) {
                callbackContext.error(errMessage);
            }
        };
        mgr.FindDevice(deviceCallback);
    }

    // Función para obtener información del dispositivo
    private void readWristBand(CallbackContext callbackContext) {
        try {
            if (connectedDevice == null) {
                callbackContext.error("INIITALIZE_FIRST");
                return;
            }

            connectedDevice.Read(new Device.ReaderCallback() {
            @Override
            public void success(AppWristbandState wristBand) {
                try {
                    JSONObject info = new JSONObject();
                    info.put("wristBandUID", wristBand.getUid());
                    info.put("isClosedOut", wristBand.isClosedOut());
                    info.put("isDeactivated", wristBand.isDeactivated());
                    info.put("balance", wristBand.getBalance());
                    info.put("refundableOfflineBalance", wristBand.getRefundableOfflineBalance());
                    info.put("debitTotal", wristBand.getDebitTotal());
                    callbackContext.success(info);
                } catch (JSONException e) {
                    callbackContext.error("JSON_CONVERSION_EXCEPTION:" + e.getMessage());
                }
            }

            @Override
            public void failed(String errorMsg) {
                callbackContext.error(errorMsg);
            }
        });
        } catch (Exception e) {
            callbackContext.error("EXCEPTION:" + e.getMessage());
        }
    }
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
