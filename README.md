# Cordova-Plugin-WristCoin 

This plugins is intended for __USB__ connection only.

Wrapper around [https://github.com/WristCoin/POS-Integration-SDK-Android/tree/master](POS-Integration-SDK-Android) v1.2.0
and [https://github.com/TapTrack/TCMPTappy-Android](TCMPTappy-Android) v2.4.1

Tested with:
- Android 21 (4.4)
- Java  8
- Gradle 7.6


### Setup:
Allow your cordova project to interact with USB interfaces by adding:

xml```
<widget id="your-project-id" .... >
  ...
  <platform name="android">
    <uses-feature android:name="android.hardware.usb.host" />
    ...
  </platform>
</widget>
```

### Actions:

- initializeDevice:
  js```cordova.plugins.WristCoin.initializeDevice(deviceInfo => console.log("Success!"), errorMessage => console.error("Error: " + errorMessage )```
Search for Tappy compatible devices, and request permission for the first match. Once the permission is granted, start `connect` process and await for the connection status READY.

-  readWristBand:
js```cordova.plugins.WristCoin.initializeDevice(wristBandData => console.log("Success!"), errorMessage => console.error("Error: " + errorMessage )```
Call if after `initializeDevice`. Sends a command to the Device requesting read. Then you should tap your wristband to be read. If the reading was successfull, `wristBandData` will have the info. Otherwise, en error callback will be fired.

