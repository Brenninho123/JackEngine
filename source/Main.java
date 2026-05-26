import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Main extends JPanel implements KeyListener
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

    public static String engineName = "JackEngine";
    public static String engineVersion = "1.0.0";

    public static Scene currentScene;

    private Timer gameLoop;

    public Main()
    {
        instance = this;

        project = new Project();

        width = project.width;
        height = project.height;

        framerate = project.framerate;

        mobile = detectMobile();

        running = true;

        engineStartTime = System.currentTimeMillis();

        setPreferredSize(new Dimension(width, height));

        setFocusable(true);

        addKeyListener(this);

        initialize();

        int delay = 1000 / framerate;

        gameLoop = new Timer(delay, e ->
        {
            if(running)
            {
                update();
                repaint();
            }
        });

        gameLoop.start();
    }

    public void initialize()
    {
        currentScene = new Scene();
    }

    public boolean detectMobile()
    {
        String os = System.getProperty("os.name").toLowerCase();

        return os.contains("android")
            || os.contains("ios");
    }

    public void update()
    {
        currentFrame++;

        if(currentScene != null)
        {
            currentScene.update();
        }
    }

    @Override
    protected void paintComponent(Graphics graphics)
    {
        super.paintComponent(graphics);

        Graphics2D g = (Graphics2D)graphics;

        g.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        g.setColor(Color.black);
        g.fillRect(0, 0, width, height);

        if(currentScene != null)
        {
            currentScene.render(g);
        }
    }

    public static void switchScene(Scene newScene)
    {
        currentScene = newScene;
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

    @Override
    public void keyTyped(KeyEvent e)
    {

    }

    @Override
    public void keyPressed(KeyEvent e)
    {

    }

    @Override
    public void keyReleased(KeyEvent e)
    {

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
        });
    }
}

class Scene
{
    public void update()
    {

    }

    public void render(Graphics2D g)
    {

    }
}
