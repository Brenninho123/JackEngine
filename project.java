import java.util.HashMap;

public class Project
{
    public String name = "Jack Engine";
    public String version = "0.1.0";
    public String packageName = "com.jackengine.game";

    public int width = 1280;
    public int height = 720;
    public int framerate = 60;

    public boolean fullscreen = false;
    public boolean vsync = true;
    public boolean antialiasing = true;

    public String mainClass = "Main";

    public String assetsPath = "assets/";
    public String sourcePath = "source/";

    public String windowTitle = "Jack Engine";

    public HashMap<String, String> metadata = new HashMap<>();

    public Project()
    {

    }

    public Project setName(String value)
    {
        name = value;
        return this;
    }

    public Project setVersion(String value)
    {
        version = value;
        return this;
    }

    public Project setPackage(String value)
    {
        packageName = value;
        return this;
    }

    public Project setWindow(String title, int w, int h)
    {
        windowTitle = title;
        width = w;
        height = h;
        return this;
    }

    public Project setFramerate(int value)
    {
        framerate = value;
        return this;
    }

    public Project setFullscreen(boolean value)
    {
        fullscreen = value;
        return this;
    }

    public Project setVsync(boolean value)
    {
        vsync = value;
        return this;
    }

    public Project setAntialiasing(boolean value)
    {
        antialiasing = value;
        return this;
    }

    public Project setMainClass(String value)
    {
        mainClass = value;
        return this;
    }

    public Project setAssetsPath(String value)
    {
        assetsPath = value;
        return this;
    }

    public Project setSourcePath(String value)
    {
        sourcePath = value;
        return this;
    }

    public Project setMetadata(String key, String value)
    {
        metadata.put(key, value);
        return this;
    }

    public String getMetadata(String key)
    {
        return metadata.get(key);
    }
}
