package com.runfoodapp.CordovaPluginWristCoin;

import static com.taptrack.tcmptappy2.Tappy.STATUS_CLOSED;
import static com.taptrack.tcmptappy2.Tappy.STATUS_DISCONNECTED;
import static com.taptrack.tcmptappy2.Tappy.STATUS_CONNECTING;
import static com.taptrack.tcmptappy2.Tappy.STATUS_DISCONNECTING;
import static com.taptrack.tcmptappy2.Tappy.STATUS_READY;

import com.mywristcoin.wristcoinpos.AppWristbandState;
import com.mywristcoin.wristcoinpos.commands.*;
import com.mywristcoin.wristcoinpos.responses.*;
import com.taptrack.tcmptappy2.MalformedPayloadException;
import com.taptrack.tcmptappy2.TCMPMessage;
import com.taptrack.tcmptappy2.MessageResolver;
import com.taptrack.tcmptappy2.MessageResolverMux;
import com.taptrack.tcmptappy2.Tappy;
import com.taptrack.tcmptappy2.commandfamilies.systemfamily.SystemCommandResolver;
import com.mywristcoin.wristcoinpos.WristCoinPOSCommandResolver;
import com.taptrack.tcmptappy2.usb.TappyUsb;

import android.util.Log;

import androidx.annotation.NonNull;

public class Device {
private final String TAG = "WristCoin.Device";
    public final TappyUsb tappy;
    private Callback global_callback;
    private String COMMAND_SENT = "";
    public Device(TappyUsb tappyDevice){
        this.tappy = tappyDevice;
        this.tappy.registerResponseListener(responseListener);
    }
    public  String description(){
        return this.tappy.getDeviceDescription();
    }
    public void Read(ReaderCallback cb) {
        Connect(new Callback() {
            @Override
            public void success(TCMPMessage value) {
                SendMessage(new Callback() {
                    @Override
                    public void success(TCMPMessage value) {
                        Log.d(TAG, "Read.Success");
                        cb.success(((GetWristbandStatusResponse) value).getWristbandState());
                    }

                    @Override
                    public void failed(String errorMessage) {
                        cb.failed(errorMessage);
                    }
                }, new GetWristbandStatusCommand());
            }


            @Override
            public void failed(String errorMessage) {
                cb.failed(errorMessage);
            }
        });

    }
    private void Connect (Callback cb) {
        if (this.tappy.getLatestStatus() ==  STATUS_CLOSED ) {
            cb.failed("DEVICE_CLOSED");
            this.flush();
            return;
        }
        if (this.tappy.getLatestStatus() == STATUS_READY) {
            cb.success(null);
            return;
        }
        final Tappy.StatusListener statusListener = new Tappy.StatusListener() {
            @Override
            public void statusReceived(int status) {
                switch (status) {
                    case STATUS_DISCONNECTED:
                    case STATUS_DISCONNECTING:
                        cb.failed("DEVICE_DISCONNECTED");
                        tappy.unregisterStatusListener(this);
                        return;
                    case STATUS_CLOSED:
                        cb.failed("DEVICE_CLOSED");
                        tappy.unregisterStatusListener(this);
                        flush();
                        return;
                    case STATUS_CONNECTING: // Esperando que se conete
                        return;
                    default:
                        break;
                }
                cb.success(null);
                tappy.unregisterStatusListener(this);
            }
        };
        this.tappy.registerStatusListener(statusListener);
        this.tappy.connect();
    }

    public int Status() {
        return this.tappy.getLatestStatus();
    }

    public void flush(){
        this.tappy.removeAllListeners();
    }

    private void SendMessage(Callback cb, TCMPMessage command) {
        COMMAND_SENT = "READ";
        this.global_callback = cb;
        final boolean readerToSend = this.tappy.sendMessage(new GetWristbandStatusCommand());
        if (!readerToSend){
            this.global_callback = null;
            COMMAND_SENT = "";
            cb.failed("DEVICE_NOT_READY_TO_TRANSMIT_DATA");
        }
    }
;

    public final Tappy.ResponseListener responseListener = new Tappy.ResponseListener() {
        @Override
        public void responseReceived(@NonNull TCMPMessage message) {
            if (global_callback == null) return;
            MessageResolver resolver = new MessageResolverMux(
                    new SystemCommandResolver(),
                    new WristCoinPOSCommandResolver()
            );

            TCMPMessage resolvedResponse = null;
            try {
                resolvedResponse = resolver.resolveResponse(message);
                if (resolvedResponse == null) {
                    Log.e(TAG, COMMAND_SENT = ".received");
                    throw new MalformedPayloadException(String.format("Response for command '%s' is invalid", COMMAND_SENT));
                }
            } catch (MalformedPayloadException e) {
                global_callback.failed(e.getMessage());
                return;
            }

            if (resolvedResponse instanceof WristCoinPOSApplicationErrorMessage) {
                final WristCoinPOSApplicationErrorMessage err = ((WristCoinPOSApplicationErrorMessage) resolvedResponse);
                Log.v(TAG, String.format("Error code: %s\nError descriptor: %s\n Error internal: %s", err.getAppErrorCode(), err.getErrorDescription(), err.getInternalErrorCode() ));
                global_callback.failed( err.getErrorDescription() );
                return;
            }

            if (COMMAND_SENT.equals("READ")) {
                try {
                    if (!(resolvedResponse instanceof GetWristbandStatusResponse)){
                        throw new MalformedPayloadException("Se recibio una respuesta inesperada");
                    }
                    global_callback.success(resolvedResponse);
                } catch (Exception e) {
                    Log.e(TAG, COMMAND_SENT + ".received", e);
                    global_callback.failed(e.getMessage());
                }
                return;
            }
            Log.e(TAG, COMMAND_SENT = ".received (Not implemented)");
            global_callback.failed(String.format("Command '%s' not implemented", COMMAND_SENT));
        }
    };
    public interface ReaderCallback {
        void success(AppWristbandState value);
        void failed(String errorMessage);
    }

    private interface Callback {
        void success(TCMPMessage value);
        void failed(String errorMessage);
    }
}
