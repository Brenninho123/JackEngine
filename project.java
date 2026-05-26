import java.nio.file.Files;
import java.nio.file.Paths;

public class project
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

    public Project()
    {
        loadConfig();
    }

    public void loadConfig()
    {
        try
        {
            String json = new String(
                Files.readAllBytes(
                    Paths.get("config.json")
                )
            );

            name = getString(json, "name");

            version = getString(json, "version");

            windowTitle = getString(json, "title");

            width = getInt(json, "width");
            height = getInt(json, "height");

            fullscreen = getBoolean(json, "fullscreen");

            framerate = getInt(json, "framerate");

            mobileSupport = getBoolean(json, "mobileSupport");

            touchControls = getBoolean(json, "touchControls");

            autoMobileScaling = getBoolean(json, "autoMobileScaling");

            mobileShaders = getBoolean(json, "mobileShaders");

            landscapeMode = getBoolean(json, "landscapeMode");

            mobilePackage = getString(json, "mobilePackage");

            assetsPath = getString(json, "assets");

            sourcePath = getString(json, "source");

            iconPath = getString(json, "icon");

            mainClass = getString(json, "mainClass");
        }
        catch(Exception e)
        {

        }
    }

    public String getString(String json, String key)
    {
        try
        {
            String search = "\"" + key + "\":";

            int start = json.indexOf(search);

            if(start == -1)
            {
                return "";
            }

            start = json.indexOf("\"", start + search.length()) + 1;

            int end = json.indexOf("\"", start);

            return json.substring(start, end);
        }
        catch(Exception e)
        {
            return "";
        }
    }

    public int getInt(String json, String key)
    {
        try
        {
            String search = "\"" + key + "\":";

            int start = json.indexOf(search);

            if(start == -1)
            {
                return 0;
            }

            start += search.length();

            while(json.charAt(start) == ' ' || json.charAt(start) == '\n')
            {
                start++;
            }

            int end = start;

            while(Character.isDigit(json.charAt(end)))
            {
                end++;
            }

            return Integer.parseInt(
                json.substring(start, end)
            );
        }
        catch(Exception e)
        {
            return 0;
        }
    }

    public boolean getBoolean(String json, String key)
    {
        try
        {
            String search = "\"" + key + "\":";

            int start = json.indexOf(search);

            if(start == -1)
            {
                return false;
            }

            start += search.length();

            while(json.charAt(start) == ' ' || json.charAt(start) == '\n')
            {
                start++;
            }

            return json.startsWith("true", start);
        }
        catch(Exception e)
        {
            return false;
        }
    }
}