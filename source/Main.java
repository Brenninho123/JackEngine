import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Main extends JPanel
{
    public static JFrame window;
    public static Project project;
    public static Main instance;

    public static int width;
    public static int height;
    public static int framerate;

    public static boolean running;
    public static boolean mobile;

    public static long currentFrame;
    public static long engineStartTime;

    public static Scene currentScene;

    private Timer gameLoop;

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
        mobile = detectMobile();
        running = true;
        engineStartTime = System.currentTimeMillis();

        setPreferredSize(new Dimension(width, height));
        setFocusable(true);

        addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (currentScene != null)
                    currentScene.keyPressed(e);
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
                if (currentScene != null)
                    currentScene.keyReleased(e);
            }
        });

        initialize();

        gameLoop = new Timer(1000 / framerate, e ->
        {
            if (running)
            {
                update();
                repaint();
            }
        });

        gameLoop.setCoalesce(true);
        gameLoop.start();
    }

    private void initialize()
    {
        currentScene = new Scene();
    }

    private static boolean detectMobile()
    {
        if (!project.mobileSupport)
            return false;

        String os = System.getProperty("os.name", "").toLowerCase();

        return os.contains("android") || os.contains("ios");
    }

    private void update()
    {
        currentFrame++;

        if (currentScene != null)
            currentScene.update();
    }

    @Override
    protected void paintComponent(Graphics graphics)
    {
        super.paintComponent(graphics);

        Graphics2D g = (Graphics2D) graphics;

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);

        if (currentScene != null)
            currentScene.render(g);
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

    public static void shutdown()
    {
        running = false;
        window.dispose();
        System.exit(0);
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() ->
        {
            window = new JFrame(project.windowTitle);

            Main runtime = new Main();

            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setResizable(false);
            window.add(runtime);
            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);

            runtime.requestFocusInWindow();
        });
    }
}

class Scene
{
    public void update() {}

    public void render(Graphics2D g) {}

    public void keyPressed(KeyEvent e) {}

    public void keyReleased(KeyEvent e) {}

    public void destroy() {}
}