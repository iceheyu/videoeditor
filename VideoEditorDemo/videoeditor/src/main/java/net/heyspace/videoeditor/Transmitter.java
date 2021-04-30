package net.heyspace.videoeditor;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.view.Surface;

import net.heyspace.videoeditor.exceptions.ProcessException;
import net.heyspace.videoeditor.opengl.gles.EglCore;
import net.heyspace.videoeditor.opengl.gles.OpenGlUtils;
import net.heyspace.videoeditor.opengl.gles.WindowSurface;
import net.heyspace.videoeditor.pipeline.ProvidedStage;
import net.heyspace.videoeditor.pipeline.Provider;
import net.heyspace.videoeditor.structs.Frame;
import net.heyspace.videoeditor.structs.TextureFrame;
import net.heyspace.videoeditor.utils.Filter;

import java.util.ArrayList;
import java.util.List;

/**
 * 将解码器输出绘制到编码器输入
 * <p>
 * 同时处理裁剪变形水印等
 * <p>
 * Created by Lorenzo on 2020/04/15.
 */
public class Transmitter extends ProvidedStage<TextureFrame>
        implements SurfaceTexture.OnFrameAvailableListener {

    public final static String TAG = "Editor.Transmitter";

    private final static int STATE_WAIT_INPUT = 1;
    private final static int STATE_WAIT_TEXTURE = 2;
    private final static int STATE_WAIT_RENDER = 3;

    private final int mWidth;
    private final int mHeight;

    private EglCore mEglCore;
    private List<Filter> filters;

    private Provider<Frame> mFrameProvider;
    private Surface consumerSurface;
    private WindowSurface consumer;

    private SurfaceTexture productSurfaceTexture;
    private Surface productSurface;
    private int productSurfaceTextureId;

    private boolean mFrameBufferIsUnusable = false;
    private int mState = STATE_WAIT_INPUT;

    private float[] mTextureTransform = new float[]{
            0.0F, 1.0F,
            0.0F, 1.0F,
            0.0F, 0.0F,
            0.0F, 1.0F,
            1.0F, 0.0F,
            0.0F, 1.0F,
            1.0F, 1.0F,
            0.0F, 1.0F
    };

    /**
     * @param surface 编码器输入
     * @param width
     * @param height
     */
    public Transmitter(Surface surface, int width, int height) {
        consumerSurface = surface;
        mWidth = width;
        mHeight = height;
    }

    public void setFrameProvider(Provider<Frame> provider) {
        mFrameProvider = provider;
    }

    public void addFilter(Filter filter) {
        filters.add(filter);
    }

    @Override
    public void setup() {
        mEglCore = new EglCore();

        consumer = new WindowSurface(mEglCore, consumerSurface, true);
        consumer.makeCurrent();

        productSurfaceTextureId = OpenGlUtils.getExternalOESTextureID();
        productSurfaceTexture = new SurfaceTexture(productSurfaceTextureId);
        productSurfaceTexture.setDefaultBufferSize(mWidth, mHeight);
        productSurfaceTexture.setOnFrameAvailableListener(this);
        productSurface = new Surface(productSurfaceTexture);

        filters = new ArrayList<>();
    }

    public Surface getProductSurface() {
        return productSurface;
    }

    public int getProductSurfaceTextureId() {
        return productSurfaceTextureId;
    }

    public EglCore getEglCore() {
        return mEglCore;
    }

    @Override
    public void processFrame() throws ProcessException {
        super.processFrame();
        if (mState == STATE_WAIT_INPUT) {
            Frame frame = mFrameProvider.dequeueOutputBuffer();
            if (frame != null) {
                mFrameProvider.enqueueOutputBuffer(frame);
                mState = STATE_WAIT_TEXTURE;
            }
        } else if (mState == STATE_WAIT_RENDER) {
            renderOesToFrameBuffer();
        }
    }

    private void renderOesToFrameBuffer() {
        if (mFrameBufferIsUnusable) {
            return;
        }
        consumer.makeCurrent();
        productSurfaceTexture.updateTexImage();
        productSurfaceTexture.getTransformMatrix(mTextureTransform);
        long timestamp = productSurfaceTexture.getTimestamp();
        consumer.setPresentationTime(timestamp);
        for (Filter filter : filters) {
            filter.draw(mTextureTransform);
        }
        consumer.swapBuffers();
        GLES20.glFinish();

        mState = STATE_WAIT_INPUT;
    }

    @Override
    protected void recycleBuffers(List<TextureFrame> canReuseBuffers) {
        mFrameBufferIsUnusable = false;
    }

    @Override
    public void release() {
        for (Filter filter : filters) {
            filter.release();
        }
        productSurface.release();
        productSurfaceTexture.release();
        OpenGlUtils.deleteTexture(productSurfaceTextureId);
        consumer.release();
        mEglCore.release();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mState = STATE_WAIT_RENDER;
    }

}
