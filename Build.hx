import haxe.Json;
import sys.FileSystem;
import sys.io.File;
import Sys;

class Build
{
    static var config:Dynamic;
    static var projectName:String = "JackEngine";
    static var projectVersion:String = "0.1.0";

    static function main()
    {
        loadConfig();

        var args = Sys.args();
        var target = args.length > 0 ? args[0].toLowerCase() : "desktop";

        checkJava();
        compileBuildScript();

        switch (target)
        {
            case "desktop": runBuild("desktop");
            case "android": checkAndroid(); runBuild("android");
            case "all":     runBuild("desktop"); checkAndroid(); runBuild("android");
            case "clean":   runBuild("clean");
            default:        runBuild("desktop");
        }
    }

    static function loadConfig()
    {
        if (!FileSystem.exists("config.json"))
            return;

        try
        {
            config = Json.parse(File.getContent("config.json"));

            if (config.name != null)
                projectName = config.name;

            if (config.version != null)
                projectVersion = config.version;
        }
        catch (e:Dynamic) {}
    }

    static function checkJava()
    {
        if (Sys.command("java", ["--version"]) != 0)
            abort("Java not found. Install JDK 21 or later.");

        if (Sys.command("javac", ["--version"]) != 0)
            abort("javac not found. Make sure JDK is installed and in PATH.");
    }

    static function checkAndroid()
    {
        var androidHome = Sys.getEnv("ANDROID_HOME");

        if (androidHome == null || !FileSystem.exists(androidHome))
            abort("ANDROID_HOME is not set or does not exist.");

        var sdkManager = androidHome + "/cmdline-tools/latest/bin/sdkmanager";

        if (!FileSystem.exists(sdkManager) && !FileSystem.exists(sdkManager + ".bat"))
            abort("sdkmanager not found at: " + sdkManager);

        if (Sys.command("gradle", ["--version"]) != 0)
            abort("Gradle not found. Install Gradle 8.6 or later.");
    }

    static function compileBuildScript()
    {
        if (!FileSystem.exists("Build.java"))
            abort("Build.java not found in project root.");

        if (!FileSystem.exists("Project.java"))
            abort("Project.java not found in project root.");

        var needsCompile = !FileSystem.exists("Build.class")
            || isOutdated("Build.java", "Build.class")
            || isOutdated("Project.java", "Build.class");

        if (needsCompile)
        {
            var sources = collectSources(".");

            File.saveContent("build_sources.txt", sources.join("\n"));

            var result = Sys.command("javac", ["@build_sources.txt"]);

            FileSystem.deleteFile("build_sources.txt");

            if (result != 0)
                abort("Failed to compile build scripts.");
        }
    }

    static function collectSources(dir:String):Array<String>
    {
        var result:Array<String> = [];

        for (entry in FileSystem.readDirectory(dir))
        {
            var path = dir + "/" + entry;

            if (FileSystem.isDirectory(path))
            {
                if (entry != "android" && entry != "build" && entry != "output" && entry != "dist")
                    result = result.concat(collectSources(path));
            }
            else if (entry.endsWith(".java"))
            {
                result.push(path);
            }
        }

        return result;
    }

    static function runBuild(target:String)
    {
        var result = Sys.command("java", ["Build", target]);

        if (result != 0)
            abort("Build failed for target: " + target);
    }

    static function isOutdated(source:String, output:String):Bool
    {
        if (!FileSystem.exists(output))
            return true;

        return FileSystem.stat(source).mtime.getTime() > FileSystem.stat(output).mtime.getTime();
    }

    static function abort(message:String)
    {
        Sys.stderr().writeString("[Build.hx] ERROR: " + message + "\n");
        Sys.exit(1);
    }
}