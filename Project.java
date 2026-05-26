import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Consumer;

import source.mobile.android.AndroidManifest;

public class Project
{
    public String name = "Jack Engine";
    public String version = "0.1.0";
    public String windowTitle = "Jack Engine";
    public int width = 1280;
    public int height = 720;
    public boolean fullscreen = false;
    public int framerate = 60;
    public boolean mobileSupport = true;
    public boolean touchControls = true;
    public boolean autoMobileScaling = true;
    public boolean mobileShaders = false;
    public boolean landscapeMode = true;
    public String mobilePackage = "com.jackengine.game";
    public String assetsPath = "assets/";
    public String sourcePath = "source/";
    public String iconPath = "arts/icon.png";
    public String mainClass = "Main";
    public AndroidManifest androidManifest;

    public Project()
    {
        loadConfig();
        initializeMobile();
    }

    private void initializeMobile()
    {
        if (!mobileSupport)
            return;

        androidManifest = new AndroidManifest();
        androidManifest.packageName = mobilePackage;
        androidManifest.appName = name;
        androidManifest.versionName = version;
        androidManifest.landscapeMode = landscapeMode;
        androidManifest.icon = iconPath;
    }

    private void loadConfig()
    {
        try
        {
            String json = new String(Files.readAllBytes(Paths.get("config.json")));

            applyString(json, "name", v -> name = v);
            applyString(json, "version", v -> version = v);
            applyString(json, "title", v -> windowTitle = v);
            applyString(json, "mainClass", v -> mainClass = v);

            String window = getSection(json, "window");
            applyInt(window, "width", v -> width = v);
            applyInt(window, "height", v -> height = v);
            applyBoolean(window, "fullscreen", v -> fullscreen = v);
            applyString(window, "icon", v -> iconPath = v);

            String runtime = getSection(json, "runtime");
            applyInt(runtime, "framerate", v -> framerate = v);
            applyBoolean(runtime, "mobileSupport", v -> mobileSupport = v);
            applyBoolean(runtime, "touchControls", v -> touchControls = v);
            applyBoolean(runtime, "autoMobileScaling", v -> autoMobileScaling = v);
            applyBoolean(runtime, "mobileShaders", v -> mobileShaders = v);
            applyBoolean(runtime, "landscapeMode", v -> landscapeMode = v);
            applyString(runtime, "mobilePackage", v -> mobilePackage = v);

            String paths = getSection(json, "paths");
            applyString(paths, "assets", v -> assetsPath = v);
            applyString(paths, "source", v -> sourcePath = v);
        }
        catch (Exception e) {}
    }

    private void applyString(String json, String key, Consumer<String> setter)
    {
        if (!hasKey(json, key))
            return;

        String value = getString(json, key);

        if (!value.isEmpty())
            setter.accept(value);
    }

    private void applyInt(String json, String key, Consumer<Integer> setter)
    {
        if (!hasKey(json, key))
            return;

        int value = getInt(json, key);

        if (value > 0)
            setter.accept(value);
    }

    private void applyBoolean(String json, String key, Consumer<Boolean> setter)
    {
        if (!hasKey(json, key))
            return;

        setter.accept(getBoolean(json, key));
    }

    private boolean hasKey(String json, String key)
    {
        return json != null && json.contains("\"" + key + "\":");
    }

    private String getSection(String json, String key)
    {
        try
        {
            String search = "\"" + key + "\":";

            int start = json.indexOf(search);

            if (start == -1)
                return "";

            start = json.indexOf("{", start + search.length());

            if (start == -1)
                return "";

            int depth = 0;
            int end = start;

            while (end < json.length())
            {
                char c = json.charAt(end);

                if (c == '{')
                    depth++;
                else if (c == '}')
                {
                    depth--;

                    if (depth == 0)
                        break;
                }

                end++;
            }

            return json.substring(start, end + 1);
        }
        catch (Exception e)
        {
            return "";
        }
    }

    private String getString(String json, String key)
    {
        try
        {
            String search = "\"" + key + "\":";

            int start = json.indexOf(search);

            if (start == -1)
                return "";

            start = json.indexOf("\"", start + search.length()) + 1;

            int end = json.indexOf("\"", start);

            return json.substring(start, end);
        }
        catch (Exception e)
        {
            return "";
        }
    }

    private int getInt(String json, String key)
    {
        try
        {
            String search = "\"" + key + "\":";

            int start = json.indexOf(search);

            if (start == -1)
                return 0;

            start += search.length();

            while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '\n' || json.charAt(start) == '\r' || json.charAt(start) == '\t'))
                start++;

            int end = start;

            while (end < json.length() && Character.isDigit(json.charAt(end)))
                end++;

            if (start == end)
                return 0;

            return Integer.parseInt(json.substring(start, end));
        }
        catch (Exception e)
        {
            return 0;
        }
    }

    private boolean getBoolean(String json, String key)
    {
        try
        {
            String search = "\"" + key + "\":";

            int start = json.indexOf(search);

            if (start == -1)
                return false;

            start += search.length();

            while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '\n' || json.charAt(start) == '\r' || json.charAt(start) == '\t'))
                start++;

            return json.startsWith("true", start);
        }
        catch (Exception e)
        {
            return false;
        }
    }
}