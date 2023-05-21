# Notlar

## Debug

- Debug etmeye çalışıldığında *Debug processes ... are not found. Aboritng session.* şeklinde alınan
hatanın giderilmesi için yüklü olan sdk>platform-tools klasörüne gidilir ve `./adb kill-server && ./adb start-server`
komutu çalıştırılır.

## Network

- Emilator'de çalışan cihazın internet erişimi olması için **AndroidManifest.xml** dosyası şu şekilde 
güncellenmesi gerekmektedir:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    ...
</manifest>
```
