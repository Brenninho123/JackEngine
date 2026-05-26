package source.mobile;

import java.util.ArrayList;
import java.util.List;

public class MobileSystem
{
    public static MobileSystem instance;

    public static boolean active;
    public static boolean landscape;
    public static boolean autoScaling;
    public static boolean shadersEnabled;
    public static boolean touchEnabled;

    public static int screenWidth;
    public static int screenHeight;

    public static float scaleX;
    public static float scaleY;
    public static float scale;

    public static float offsetX;
    public static float offsetY;

    public static int targetWidth;
    public static int targetHeight;

    public static Orientation orientation = Orientation.UNKNOWN;
    public static ScreenDensity density = ScreenDensity.MDPI;

    private static final List<TouchPointer> pointers = new ArrayList<>();
    private static final int MAX_POINTERS = 10;

    public enum Orientation
    {
        PORTRAIT,
        LANDSCAPE,
        UNKNOWN
    }

    public enum ScreenDensity
    {
        LDPI,
        MDPI,
        HDPI,
        XHDPI,
        XXHDPI,
        XXXHDPI
    }

    public static class TouchPointer
    {
        public int id;
        public float x;
        public float y;
        public float rawX;
        public float rawY;
        public boolean down;
        public long pressTime;

        public TouchPointer(int id)
        {
            this.id = id;
        }

        public float getScreenX()
        {
            return (rawX - offsetX) / scale;
        }

        public float getScreenY()
        {
            return (rawY - offsetY) / scale;
        }

        public long getHoldDuration()
        {
            return down ? System.currentTimeMillis() - pressTime : 0;
        }

        public boolean isHolding(long millis)
        {
            return down && getHoldDuration() >= millis;
        }
    }

    public static void initialize(int targetW, int targetH, boolean useLandscape, boolean useAutoScaling, boolean useTouch, boolean useShaders)
    {
        instance      = new MobileSystem();
        active        = true;
        targetWidth   = targetW;
        targetHeight  = targetH;
        landscape     = useLandscape;
        autoScaling   = useAutoScaling;
        touchEnabled  = useTouch;
        shadersEnabled = useShaders;

        for (int i = 0; i < MAX_POINTERS; i++)
            pointers.add(new TouchPointer(i));
    }

    public static void resize(int newWidth, int newHeight)
    {
        screenWidth  = newWidth;
        screenHeight = newHeight;

        orientation = (newWidth >= newHeight) ? Orientation.LANDSCAPE : Orientation.PORTRAIT;

        density = resolveDensity(newWidth, newHeight);

        if (autoScaling)
            recalculateScale();
    }

    private static void recalculateScale()
    {
        scaleX = (float) screenWidth / targetWidth;
        scaleY = (float) screenHeight / targetHeight;

        scale = Math.min(scaleX, scaleY);

        offsetX = (screenWidth  - targetWidth  * scale) / 2f;
        offsetY = (screenHeight - targetHeight * scale) / 2f;
    }

    private static ScreenDensity resolveDensity(int w, int h)
    {
        int shortest = Math.min(w, h);

        if (shortest >= 1080) return ScreenDensity.XXXHDPI;
        if (shortest >= 720)  return ScreenDensity.XXHDPI;
        if (shortest >= 540)  return ScreenDensity.XHDPI;
        if (shortest >= 360)  return ScreenDensity.HDPI;
        if (shortest >= 240)  return ScreenDensity.MDPI;

        return ScreenDensity.LDPI;
    }

    public static void onTouchDown(int pointerId, float rawX, float rawY)
    {
        if (!touchEnabled || pointerId >= MAX_POINTERS)
            return;

        TouchPointer pointer = pointers.get(pointerId);
        pointer.rawX      = rawX;
        pointer.rawY      = rawY;
        pointer.x         = pointer.getScreenX();
        pointer.y         = pointer.getScreenY();
        pointer.down      = true;
        pointer.pressTime = System.currentTimeMillis();
    }

    public static void onTouchUp(int pointerId, float rawX, float rawY)
    {
        if (!touchEnabled || pointerId >= MAX_POINTERS)
            return;

        TouchPointer pointer = pointers.get(pointerId);
        pointer.rawX = rawX;
        pointer.rawY = rawY;
        pointer.x    = pointer.getScreenX();
        pointer.y    = pointer.getScreenY();
        pointer.down = false;
    }

    public static void onTouchDragged(int pointerId, float rawX, float rawY)
    {
        if (!touchEnabled || pointerId >= MAX_POINTERS)
            return;

        TouchPointer pointer = pointers.get(pointerId);
        pointer.rawX = rawX;
        pointer.rawY = rawY;
        pointer.x    = pointer.getScreenX();
        pointer.y    = pointer.getScreenY();
    }

    public static TouchPointer getPointer(int id)
    {
        if (id < 0 || id >= MAX_POINTERS)
            return null;

        return pointers.get(id);
    }

    public static TouchPointer getPrimaryPointer()
    {
        return pointers.get(0);
    }

    public static boolean isTouching()
    {
        for (TouchPointer p : pointers)
            if (p.down) return true;

        return false;
    }

    public static boolean isTouching(int id)
    {
        if (id < 0 || id >= MAX_POINTERS)
            return false;

        return pointers.get(id).down;
    }

    public static boolean isTouchingArea(float x, float y, float w, float h)
    {
        for (TouchPointer p : pointers)
            if (p.down && p.x >= x && p.x <= x + w && p.y >= y && p.y <= y + h)
                return true;

        return false;
    }

    public static int touchCount()
    {
        int count = 0;

        for (TouchPointer p : pointers)
            if (p.down) count++;

        return count;
    }

    public static float toScreenX(float rawX)
    {
        return (rawX - offsetX) / scale;
    }

    public static float toScreenY(float rawY)
    {
        return (rawY - offsetY) / scale;
    }

    public static float toRawX(float screenX)
    {
        return screenX * scale + offsetX;
    }

    public static float toRawY(float screenY)
    {
        return screenY * scale + offsetY;
    }

    public static void clearPointers()
    {
        for (TouchPointer p : pointers)
            p.down = false;
    }

    public static boolean isLandscape()
    {
        return orientation == Orientation.LANDSCAPE;
    }

    public static boolean isPortrait()
    {
        return orientation == Orientation.PORTRAIT;
    }

    public static float getAspectRatio()
    {
        return (float) screenWidth / screenHeight;
    }
}
