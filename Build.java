import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.util.ArrayList;
import java.util.List;

public class Build
{
    private static Project project;

    private static final String BUILD_DIR    = "build";
    private static final String OUTPUT_DIR   = "output";
    private static final String ANDROID_DIR  = "android";
    private static final String SOURCES_FILE = "sources.txt";

    public static void main(String[] args)
    {
        project = new Project();

        String target = args.length > 0 ? args[0].toLowerCase() : "desktop";

        switch (target)
        {
            case "desktop": buildDesktop(); break;
            case "android": buildAndroid(); break;
            case "all":     buildDesktop(); buildAndroid(); break;
            case "clean":   clean(); break;
            default:        buildDesktop(); break;
        }
    }

    private static void buildDesktop()
    {
        createDir(BUILD_DIR);
        createDir(OUTPUT_DIR);

        List<String> sources = collectSources(".", new ArrayList<>(), false);

        writeSourcesList(sources);

        if (!compile(BUILD_DIR, SOURCES_FILE))
        {
            System.exit(1);
        }

        copyAssets(BUILD_DIR);
        copyArts(BUILD_DIR);
        copyConfig(BUILD_DIR);

        if (!packageJar(project.name.replace(" ", "") + ".jar", BUILD_DIR, "Main"))
        {
            System.exit(1);
        }
    }

    private static void buildAndroid()
    {
        createDir(ANDROID_DIR + "/app/src/main/java");
        createDir(ANDROID_DIR + "/app/src/main/res/drawable");
        createDir(ANDROID_DIR + "/app/src/main/assets");

        copyFile("Project.java", ANDROID_DIR + "/app/src/main/java/Project.java");

        File sourceDir = new File("source");

        if (sourceDir.exists())
        {
            List<String> sourceSources = collectSources("source", new ArrayList<>(), true);

            for (String src : sourceSources)
            {
                File f = new File(src);
                copyFile(src, ANDROID_DIR + "/app/src/main/java/" + f.getName());
            }
        }

        copyAssetsToAndroid();

        generateAndroidGradle();
        generateAndroidSettings();
        generateRootGradle();
        generateAndroidManifest();
        generateGradleWrapper();

        run(new String[]{"chmod", "+x", ANDROID_DIR + "/gradlew"});

        boolean success = run(new String[]{"./" + ANDROID_DIR + "/gradlew", "assembleDebug", "-p", ANDROID_DIR});

        if (!success)
        {
            System.exit(1);
        }

        String apkSrc  = ANDROID_DIR + "/app/build/outputs/apk/debug/app-debug.apk";
        String apkDest = OUTPUT_DIR + "/" + project.name.replace(" ", "") + ".apk";

        createDir(OUTPUT_DIR);
        copyFile(apkSrc, apkDest);
    }

    private static void clean()
    {
        deleteDir(new File(BUILD_DIR));
        deleteDir(new File(OUTPUT_DIR));
        deleteDir(new File(ANDROID_DIR));

        new File(SOURCES_FILE).delete();
    }

    private static List<String> collectSources(String dir, List<String> result, boolean skipMain)
    {
        File root = new File(dir);

        if (!root.exists())
            return result;

        for (File f : root.listFiles())
        {
            if (f.isDirectory())
            {
                collectSources(f.getPath(), result, skipMain);
            }
            else if (f.getName().endsWith(".java"))
            {
                if (skipMain && f.getName().equals("Main.java"))
                    continue;

                result.add(f.getPath());
            }
        }

        return result;
    }

    private static void writeSourcesList(List<String> sources)
    {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SOURCES_FILE)))
        {
            for (String src : sources)
                writer.println(src);
        }
        catch (IOException e)
        {
            System.exit(1);
        }
    }

    private static boolean compile(String outputDir, String sourcesFile)
    {
        return run(new String[]{
            "javac",
            "-d", outputDir,
            "@" + sourcesFile
        });
    }

    private static boolean packageJar(String jarName, String buildDir, String mainClass)
    {
        String manifestPath = buildDir + "/MANIFEST.MF";

        try (PrintWriter writer = new PrintWriter(new FileWriter(manifestPath)))
        {
            writer.println("Main-Class: " + mainClass);
            writer.println();
        }
        catch (IOException e)
        {
            return false;
        }

        boolean result = run(new String[]{
            "jar", "cfm",
            OUTPUT_DIR + "/" + jarName,
            manifestPath,
            "-C", buildDir, "."
        });

        new File(manifestPath).delete();

        return result;
    }

    private static boolean run(String[] command)
    {
        try
        {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.inheritIO();

            Process process = pb.start();

            return process.waitFor() == 0;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private static void createDir(String path)
    {
        new File(path).mkdirs();
    }

    private static void copyFile(String src, String dest)
    {
        try
        {
            Path source      = Paths.get(src);
            Path destination = Paths.get(dest);

            if (!Files.exists(source))
                return;

            Files.createDirectories(destination.getParent());
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e) {}
    }

    private static void copyDir(String src, String dest)
    {
        File srcDir = new File(src);

        if (!srcDir.exists())
            return;

        createDir(dest);

        for (File f : srcDir.listFiles())
        {
            if (f.isDirectory())
                copyDir(f.getPath(), dest + "/" + f.getName());
            else
                copyFile(f.getPath(), dest + "/" + f.getName());
        }
    }

    private static void copyAssets(String dest)
    {
        copyDir("assets", dest + "/assets");
    }

    private static void copyArts(String dest)
    {
        copyDir("arts", dest + "/arts");
    }

    private static void copyConfig(String dest)
    {
        copyFile("config.json", dest + "/config.json");
    }

    private static void copyAssetsToAndroid()
    {
        copyDir("assets", ANDROID_DIR + "/app/src/main/assets");
        copyDir("arts", ANDROID_DIR + "/app/src/main/res/drawable");
    }

    private static void deleteDir(File dir)
    {
        if (!dir.exists())
            return;

        if (dir.isDirectory())
            for (File f : dir.listFiles())
                deleteDir(f);

        dir.delete();
    }

    private static void generateAndroidManifest()
    {
        String orientation = project.landscapeMode ? "landscape" : "portrait";

        String content =
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
            "    package=\"" + project.mobilePackage + "\">\n\n" +
            "    <uses-permission android:name=\"android.permission.INTERNET\"/>\n" +
            "    <uses-permission android:name=\"android.permission.WAKE_LOCK\"/>\n\n" +
            "    <application\n" +
            "        android:label=\"" + project.name + "\"\n" +
            "        android:hardwareAccelerated=\"true\"\n" +
            "        android:resizeableActivity=\"true\"\n" +
            "        android:theme=\"@android:style/Theme.DeviceDefault.NoActionBar.Fullscreen\">\n\n" +
            "        <activity\n" +
            "            android:name=\".MainActivity\"\n" +
            "            android:exported=\"true\"\n" +
            "            android:configChanges=\"orientation|screenSize|keyboardHidden\"\n" +
            "            android:screenOrientation=\"" + orientation + "\">\n\n" +
            "            <intent-filter>\n" +
            "                <action android:name=\"android.intent.action.MAIN\"/>\n" +
            "                <category android:name=\"android.intent.category.LAUNCHER\"/>\n" +
            "            </intent-filter>\n\n" +
            "        </activity>\n\n" +
            "    </application>\n\n" +
            "</manifest>\n";

        writeFile(ANDROID_DIR + "/app/src/main/AndroidManifest.xml", content);
    }

    private static void generateAndroidGradle()
    {
        String content =
            "plugins {\n" +
            "    id 'com.android.application'\n" +
            "}\n\n" +
            "android {\n" +
            "    namespace '" + project.mobilePackage + "'\n" +
            "    compileSdk 34\n\n" +
            "    defaultConfig {\n" +
            "        applicationId \"" + project.mobilePackage + "\"\n" +
            "        minSdk 24\n" +
            "        targetSdk 34\n" +
            "        versionCode 1\n" +
            "        versionName \"" + project.version + "\"\n" +
            "    }\n\n" +
            "    buildTypes {\n" +
            "        debug {\n" +
            "            debuggable true\n" +
            "        }\n" +
            "        release {\n" +
            "            minifyEnabled false\n" +
            "        }\n" +
            "    }\n\n" +
            "    compileOptions {\n" +
            "        sourceCompatibility JavaVersion.VERSION_11\n" +
            "        targetCompatibility JavaVersion.VERSION_11\n" +
            "    }\n" +
            "}\n";

        writeFile(ANDROID_DIR + "/app/build.gradle", content);
    }

    private static void generateRootGradle()
    {
        String content =
            "buildscript {\n" +
            "    repositories {\n" +
            "        google()\n" +
            "        mavenCentral()\n" +
            "    }\n" +
            "    dependencies {\n" +
            "        classpath 'com.android.tools.build:gradle:8.4.0'\n" +
            "    }\n" +
            "}\n\n" +
            "allprojects {\n" +
            "    repositories {\n" +
            "        google()\n" +
            "        mavenCentral()\n" +
            "    }\n" +
            "}\n";

        writeFile(ANDROID_DIR + "/build.gradle", content);
    }

    private static void generateAndroidSettings()
    {
        String content =
            "pluginManagement {\n" +
            "    repositories {\n" +
            "        google()\n" +
            "        mavenCentral()\n" +
            "        gradlePluginPortal()\n" +
            "    }\n" +
            "}\n\n" +
            "rootProject.name = '" + project.name.replace(" ", "") + "'\n" +
            "include ':app'\n";

        writeFile(ANDROID_DIR + "/settings.gradle", content);
    }

    private static void generateGradleWrapper()
    {
        run(new String[]{"gradle", "wrapper", "--gradle-version", "8.6", "-p", ANDROID_DIR});
        run(new String[]{"chmod", "+x", ANDROID_DIR + "/gradlew"});
    }

    private static void writeFile(String path, String content)
    {
        try
        {
            File file = new File(path);
            file.getParentFile().mkdirs();

            try (PrintWriter writer = new PrintWriter(new FileWriter(file)))
            {
                writer.print(content);
            }
        }
        catch (IOException e) {}
    }
}