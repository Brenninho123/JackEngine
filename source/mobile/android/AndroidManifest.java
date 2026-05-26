package source.mobile.android;

public class AndroidManifest
{
    public String packageName = "com.jackengine.game";

    public String appName = "Jack Engine";

    public String versionName = "0.1.0";

    public int versionCode = 1;

    public int minSdk = 24;

    public int targetSdk = 34;

    public int compileSdk = 34;

    public boolean fullscreen = true;

    public boolean hardwareAcceleration = true;

    public boolean internetPermission = true;

    public boolean vibrationPermission = true;

    public boolean storagePermission = true;

    public boolean wakeLockPermission = true;

    public boolean landscapeMode = true;

    public boolean portraitMode = false;

    public boolean resizeableActivity = true;

    public boolean allowBackup = false;

    public boolean supportsMouse = true;

    public boolean supportsKeyboard = true;

    public boolean mobileControls = true;

    public boolean immersiveMode = true;

    public String icon = "arts/icon.png";

    public String activityName = "MainActivity";

    public String theme = "@android:style/Theme.DeviceDefault.NoActionBar.Fullscreen";

    public AndroidManifest()
    {

    }

    public String generate()
    {
        String orientation = "landscape";

        if(portraitMode)
        {
            orientation = "portrait";
        }

        String permissions = "";

        if(internetPermission)
        {
            permissions +=
                "<uses-permission android:name=\"android.permission.INTERNET\"/>\n";
        }

        if(vibrationPermission)
        {
            permissions +=
                "<uses-permission android:name=\"android.permission.VIBRATE\"/>\n";
        }

        if(storagePermission)
        {
            permissions +=
                "<uses-permission android:name=\"android.permission.READ_EXTERNAL_STORAGE\"/>\n" +
                "<uses-permission android:name=\"android.permission.WRITE_EXTERNAL_STORAGE\"/>\n";
        }

        if(wakeLockPermission)
        {
            permissions +=
                "<uses-permission android:name=\"android.permission.WAKE_LOCK\"/>\n";
        }

        return
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +

            "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
            "    package=\"" + packageName + "\">\n\n" +

            permissions + "\n" +

            "    <application\n" +
            "        android:label=\"" + appName + "\"\n" +
            "        android:hardwareAccelerated=\"" + hardwareAcceleration + "\"\n" +
            "        android:allowBackup=\"" + allowBackup + "\"\n" +
            "        android:resizeableActivity=\"" + resizeableActivity + "\"\n" +
            "        android:theme=\"" + theme + "\">\n\n" +

            "        <activity\n" +
            "            android:name=\"." + activityName + "\"\n" +
            "            android:exported=\"true\"\n" +
            "            android:screenOrientation=\"" + orientation + "\"\n" +
            "            android:resizeableActivity=\"" + resizeableActivity + "\">\n\n" +

            "            <intent-filter>\n" +
            "                <action android:name=\"android.intent.action.MAIN\"/>\n" +
            "                <category android:name=\"android.intent.category.LAUNCHER\"/>\n" +
            "            </intent-filter>\n\n" +

            "        </activity>\n\n" +

            "    </application>\n\n" +

            "</manifest>";
    }
}