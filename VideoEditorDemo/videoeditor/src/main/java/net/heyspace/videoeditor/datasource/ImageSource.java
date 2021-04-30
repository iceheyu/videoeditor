package net.heyspace.videoeditor.datasource;

import android.graphics.Bitmap;

import net.heyspace.videoeditor.exceptions.ProcessException;
import net.heyspace.videoeditor.exceptions.SetupException;
import net.heyspace.videoeditor.opengl.filter.WaterFilter;
import net.heyspace.videoeditor.opengl.gles.EglCore;
import net.heyspace.videoeditor.opengl.gles.WindowSurface;
import net.heyspace.videoeditor.structs.Frame;
import net.heyspace.videoeditor.utils.BitmapUtil;

import java.util.List;

/**
 * Created by Lorenzo on 2020/04/16.
 */
public class ImageSource extends DataSource {

    public final static String TAG = "Editor.ImageSource";

    private Bitmap bitmap;
    private EglCore eglCore;
    private WindowSurface windowSurface;
    private WaterFilter waterFilter;

    private float[] mTextureTransform = new float[]{
            1.0F, 0.0F,
            0.0F, 0.0F,
            0.0F, -1.0F,
            0.0F, 0.0F,
            0.0F, 0.0F,
            1.0F, 0.0F,
            0.0F, 1.0F,
            0.0F, 1.0F
    };

    private int framerate = 15;
    private long timeNsPerFrame = 1_000_000_000 / framerate;
    private long lastDecoderOutputTimeNs;

    public ImageSource(String path, long startTimeUs, long endTimeUs) {
        super(path, startTimeUs, endTimeUs);
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public void setup() throws SetupException {
        rangeStartTimeUs += cumulateUs;
        rangeEndTimeUs += cumulateUs;
        lastDecoderOutputTimeNs = rangeStartTimeUs * 1000;
        if (path != null) {
            bitmap = BitmapUtil.loadBitmap(path);
        }
        eglCore = new EglCore();
        windowSurface = new WindowSurface(eglCore, surface, false);
        windowSurface.makeCurrent();
        waterFilter = new WaterFilter(bitmap.getWidth(), bitmap.getHeight());
        waterFilter.setPosition(0f, 0f, 1f, 1f);
    }

    @Override
    public void processFrame() throws ProcessException {
        super.processFrame();
        Frame frame = new Frame();
        synchronized (this) {
            mWaitOutBuffers.add(frame);
        }
    }

    @Override
    protected void recycleBuffers(List<Frame> canReuseBuffers) {
        for (Frame frame : canReuseBuffers) {
            windowSurface.makeCurrent();
            windowSurface.setPresentationTime(lastDecoderOutputTimeNs);
            waterFilter.drawFrame(bitmap, mTextureTransform);
            windowSurface.swapBuffers();
            lastDecoderOutputTimeNs += timeNsPerFrame;
        }
    }

    @Override
    public void release() {
        windowSurface.release();
        bitmap.recycle();
        waterFilter.release();
        eglCore.release();
    }

    @Override
    public boolean useUp() {
        return lastDecoderOutputTimeNs > rangeEndTimeUs * 1000;
    }

    @Override
    public long getCumulateUs() {
        return lastDecoderOutputTimeNs / 1000;
    }

}
