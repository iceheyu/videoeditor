package net.heyspace.videoeditor.opengl.filter;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import net.heyspace.videoeditor.opengl.gles.GlUtil;
import net.heyspace.videoeditor.opengl.gles.OpenGlUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


public class WaterFilter extends BaseFilter {

    protected static final String FRAGMENT_SHADER_2D =
            "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform sampler2D sTexture;\n" +
                    "void main() {\n" +
                    "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";

    private int waterTextureId;

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;

    //watermark
    private static final float[] mWaterMarkTriangleVerticesData = {
            // X, Y, Z, U, V
            0.5f, 0.5f, 0, 0.f, 1.f,
            1.0f, 0.5f, 0, 1.f, 1.f,
            0.5f, 1.0f, 0, 0.f, 0.f,
            1.0f, 1.0f, 0, 1.f, 0.f,
    };

    private FloatBuffer mWaterMarkTriangleVertices;

    public WaterFilter(int width, int height) {
        super(width, height);
        makeProgram();
    }

    @Override
    protected void makeProgram() {
        mProgramHandle2D = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER_2D);
        maPositionLoc2D = GLES20.glGetAttribLocation(mProgramHandle2D, "aPosition");
        maTextureCoordLoc2D = GLES20.glGetAttribLocation(mProgramHandle2D, "aTextureCoord");
        muMVPMatrixLoc2D = GLES20.glGetUniformLocation(mProgramHandle2D, "uMVPMatrix");
        muTexMatrixLoc2D = GLES20.glGetUniformLocation(mProgramHandle2D, "uTexMatrix");
        muUniformTexture2D = GLES20.glGetUniformLocation(mProgramHandle2D, "sTexture");

        waterTextureId = OpenGlUtils.getTexture2DTextureID();

        mWaterMarkTriangleVertices = ByteBuffer.allocateDirect(
                mWaterMarkTriangleVerticesData.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mWaterMarkTriangleVertices.put(mWaterMarkTriangleVerticesData).position(0);
    }

    /**
     * @param x 水印左侧坐标 / 屏幕width
     * @param y 水印顶部坐标 / 屏幕height
     * @param w 水印width  / 屏幕width
     * @param h 水印height / 屏幕height
     */
    public void setPosition(float x, float y, float w, float h) {
        float[] verticesData = {
                -1f + 2f * x,       1f - 2f * (y + h), 0, 0.f, 0.f,
                -1f + 2f * (x + w), 1f - 2f * (y + h), 0, 1.f, 0.f,
                -1f + 2f * x,       1f - 2f * y,       0, 0.f, 1.f,
                -1f + 2f * (x + w), 1f - 2f * y,       0, 1.f, 1.f,
        };
        mWaterMarkTriangleVertices.clear();
        mWaterMarkTriangleVertices.position(0);
        mWaterMarkTriangleVertices.put(verticesData).position(0);
    }

    public void drawFrame(Bitmap bitmap, float[] matrix) {

        GLES20.glUseProgram(mProgramHandle2D);
        GlUtil.checkGlError("glUseProgram");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, waterTextureId);
        GLES20.glUniform1i(muUniformTexture2D, 1);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        GLES20.glEnableVertexAttribArray(maPositionLocOES);
        mWaterMarkTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(maPositionLoc2D, 3, GLES20.GL_FLOAT,
                false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mWaterMarkTriangleVertices);
        GlUtil.checkGlError("glVertexAttribPointer maPosition");

        GLES20.glEnableVertexAttribArray(maTextureCoordLoc2D);
        mWaterMarkTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(maTextureCoordLoc2D, 2, GLES20.GL_FLOAT,
                false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mWaterMarkTriangleVertices);
        GlUtil.checkGlError("glVertexAttribPointer maTextureHandle");

        GLES20.glUniformMatrix4fv(muMVPMatrixLoc2D, 1, false,
                GlUtil.IDENTITY_MATRIX, 0);
        GLES20.glUniformMatrix4fv(muTexMatrixLoc2D, 1, false,
                matrix, 0);

        GLES20.glDrawArrays(drawMode, 0, corrdinatesCount);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    @Override
    public void release() {
        GLES20.glDeleteProgram(mProgramHandle2D);
        mProgramHandle2D = -1;
    }
}
