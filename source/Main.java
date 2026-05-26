import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import java.io.File;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;

public class Main extends JPanel implements Runnable
{
    public static JFrame window;
    public static Project project;
    public static Main instance;

    public static int width;
    public static int height;
    public static int framerate;

    public static boolean running;
    public static boolean mobile;
    public static boolean focused;

    public static long currentFrame;
    public static long engineStartTime;

    public static Scene currentScene;

    public static final Input input = new Input();
    public static final Platform platform = new Platform();

    private Thread gameThread;
    private BufferedImage backBuffer;
    private Graphics2D backGraphics;

    static
    {
        project = new Project();
    }

    public Main()
    {
        instance = this;

        width = project.width;
        height = project.height;
        framerate = project.framerate;
        mobile = platform.isMobile() && project.mobileSupport;
        running = true;
        focused = true;
        engineStartTime = System.currentTimeMillis();

        setPreferredSize(new Dimension(width, height));
        setFocusable(true);
        setBackground(Color.BLACK);

        backBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        backGraphics = backBuffer.createGraphics();

        addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                input.onKeyPressed(e.getKeyCode());

                if (currentScene != null)
                    currentScene.keyPressed(e);
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
                input.onKeyReleased(e.getKeyCode());

                if (currentScene != null)
                    currentScene.keyReleased(e);
            }
        });

        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                input.onMousePressed(e.getButton());

                if (currentScene != null)
                    currentScene.mousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                input.onMouseReleased(e.getButton());

                if (currentScene != null)
                    currentScene.mouseReleased(e);
            }
        });

        addMouseMotionListener(new MouseAdapter()
        {
            @Override
            public void mouseMoved(MouseEvent e)
            {
                input.mouseX = e.getX();
                input.mouseY = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e)
            {
                input.mouseX = e.getX();
                input.mouseY = e.getY();
            }
        });

        addMouseWheelListener((MouseWheelEvent e) ->
        {
            input.mouseScroll = e.getWheelRotation();

            if (currentScene != null)
                currentScene.mouseScrolled(e);
        });

        initialize();
    }

    private void initialize()
    {
        currentScene = new Scene();
    }

    public void startLoop()
    {
        gameThread = new Thread(this);
        gameThread.setDaemon(true);
        gameThread.start();
    }

    @Override
    public void run()
    {
        long targetFrameNanos = 1_000_000_000L / framerate;
        long lastTime = System.nanoTime();
        double delta = 0;

        while (running)
        {
            long now = System.nanoTime();
            delta += (now - lastTime) / (double) targetFrameNanos;
            lastTime = now;

            if (delta >= 1)
            {
                delta--;

                input.update();
                update();
                renderToBuffer();
                repaint();

                currentFrame++;
            }

            try
            {
                Thread.sleep(1);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void update()
    {
        if (currentScene != null)
            currentScene.update();
    }

    private void renderToBuffer()
    {
        backGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        backGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        backGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        backGraphics.setColor(Color.BLACK);
        backGraphics.fillRect(0, 0, width, height);

        if (currentScene != null)
            currentScene.render(backGraphics);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        if (backBuffer != null)
            g.drawImage(backBuffer, 0, 0, null);
    }

    public static void switchScene(Scene scene)
    {
        if (currentScene != null)
            currentScene.destroy();

        currentScene = scene;
    }

    public static long getEngineUptime()
    {
        return System.currentTimeMillis() - engineStartTime;
    }

    public static void hideCursor()
    {
        BufferedImage blank = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Cursor invisible = Toolkit.getDefaultToolkit().createCustomCursor(blank, new java.awt.Point(0, 0), "invisible");
        window.setCursor(invisible);
    }

    public static void showCursor()
    {
        window.setCursor(Cursor.getDefaultCursor());
    }

    public static void setFullscreen(boolean enabled)
    {
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        if (enabled && device.isFullScreenSupported())
        {
            window.dispose();
            window.setUndecorated(true);
            device.setFullScreenWindow(window);
        }
        else
        {
            device.setFullScreenWindow(null);
            window.setUndecorated(false);
            window.setVisible(true);
        }
    }

    public static void shutdown()
    {
        running = false;
        window.dispose();
        System.exit(0);
    }

    public static void main(String[] args)
    {
        platform.applySystemProperties();

        SwingUtilities.invokeLater(() ->
        {
            window = new JFrame(project.windowTitle);

            Main runtime = new Main();

            window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            window.setResizable(false);
            window.add(runtime);
            window.pack();
            window.setLocationRelativeTo(null);

            window.addWindowListener(new WindowAdapter()
            {
                @Override
                public void windowClosing(WindowEvent e)
                {
                    if (currentScene != null)
                        currentScene.onWindowClose();

                    shutdown();
                }

                @Override
                public void windowIconified(WindowEvent e)
                {
                    focused = false;
                }

                @Override
                public void windowDeiconified(WindowEvent e)
                {
                    focused = true;
                }

                @Override
                public void windowGainedFocus(WindowEvent e)
                {
                    focused = true;
                }

                @Override
                public void windowLostFocus(WindowEvent e)
                {
                    focused = false;
                    input.clear();
                }
            });

            loadWindowIcon();

            if (project.fullscreen)
                setFullscreen(true);

            window.setVisible(true);

            runtime.requestFocusInWindow();
            runtime.startLoop();
        });
    }

    private static void loadWindowIcon()
    {
        try
        {
            File iconFile = new File(project.iconPath);

            if (!iconFile.exists())
                return;

            Image icon = ImageIO.read(iconFile);

            List<Image> icons = new ArrayList<>();
            icons.add(icon);

            window.setIconImages(icons);
        }
        catch (Exception e) {}
    }
}

class Scene
{
    public void update() {}

    public void render(Graphics2D g) {}

    public void keyPressed(KeyEvent e) {}

    public void keyReleased(KeyEvent e) {}

    public void mousePressed(MouseEvent e) {}

    public void mouseReleased(MouseEvent e) {}

    public void mouseScrolled(MouseWheelEvent e) {}

    public void onWindowClose() {}

    public void destroy() {}
}

class Input
{
    public int mouseX;
    public int mouseY;
    public int mouseScroll;

    private final Set<Integer> keysPressed = new HashSet<>();
    private final Set<Integer> keysJustPressed = new HashSet<>();
    private final Set<Integer> keysJustReleased = new HashSet<>();

    private final Set<Integer> mousePressed = new HashSet<>();
    private final Set<Integer> mouseJustPressed = new HashSet<>();
    private final Set<Integer> mouseJustReleased = new HashSet<>();

    public void update()
    {
        keysJustPressed.clear();
        keysJustReleased.clear();
        mouseJustPressed.clear();
        mouseJustReleased.clear();
        mouseScroll = 0;
    }

    public void onKeyPressed(int keyCode)
    {
        if (!keysPressed.contains(keyCode))
            keysJustPressed.add(keyCode);

        keysPressed.add(keyCode);
    }

    public void onKeyReleased(int keyCode)
    {
        keysPressed.remove(keyCode);
        keysJustReleased.add(keyCode);
    }

    public void onMousePressed(int button)
    {
        if (!mousePressed.contains(button))
            mouseJustPressed.add(button);

        mousePressed.add(button);
    }

    public void onMouseReleased(int button)
    {
        mousePressed.remove(button);
        mouseJustReleased.add(button);
    }

    public boolean isKeyDown(int keyCode)
    {
        return keysPressed.contains(keyCode);
    }

    public boolean isKeyJustPressed(int keyCode)
    {
        return keysJustPressed.contains(keyCode);
    }

    public boolean isKeyJustReleased(int keyCode)
    {
        return keysJustReleased.contains(keyCode);
    }

    public boolean isMouseDown(int button)
    {
        return mousePressed.contains(button);
    }

    public boolean isMouseJustPressed(int button)
    {
        return mouseJustPressed.contains(button);
    }

    public boolean isMouseJustReleased(int button)
    {
        return mouseJustReleased.contains(button);
    }

    public void clear()
    {
        keysPressed.clear();
        keysJustPressed.clear();
        keysJustReleased.clear();
        mousePressed.clear();
        mouseJustPressed.clear();
        mouseJustReleased.clear();
        mouseScroll = 0;
    }
}

class Platform
{
    public enum OS { WINDOWS, LINUX, MAC, ANDROID, IOS, UNKNOWN }

    public final OS os;

    public Platform()
    {
        String name = System.getProperty("os.name", "").toLowerCase();
        String vendor = System.getProperty("java.vendor", "").toLowerCase();

        if (name.contains("android") || vendor.contains("android"))
            os = OS.ANDROID;
        else if (name.contains("win"))
            os = OS.WINDOWS;
        else if (name.contains("mac"))
            os = OS.MAC;
        else if (name.contains("ios"))
            os = OS.IOS;
        else if (name.contains("nix") || name.contains("nux"))
            os = OS.LINUX;
        else
            os = OS.UNKNOWN;
    }

    public boolean isWindows() { return os == OS.WINDOWS; }
    public boolean isMac()     { return os == OS.MAC; }
    public boolean isLinux()   { return os == OS.LINUX; }
    public boolean isMobile()  { return os == OS.ANDROID || os == OS.IOS; }

    public int getScreenWidth()
    {
        return GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getDefaultScreenDevice()
            .getDisplayMode()
            .getWidth();
    }

    public int getScreenHeight()
    {
        return GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getDefaultScreenDevice()
            .getDisplayMode()
            .getHeight();
    }

    public int getRefreshRate()
    {
        DisplayMode mode = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getDefaultScreenDevice()
            .getDisplayMode();

        return mode.getRefreshRate() == DisplayMode.REFRESH_RATE_UNKNOWN ? 60 : mode.getRefreshRate();
    }

    public void applySystemProperties()
    {
        System.setProperty("sun.java2d.opengl", "true");

        if (isWindows())
            System.setProperty("sun.java2d.d3d", "true");

        if (isMac())
            System.setProperty("apple.laf.useScreenMenuBar", "true");
    }
}