package net.heyspace.videoeditor.opengl.filter;

import android.opengl.GLES20;

import net.heyspace.videoeditor.opengl.gles.Drawable2d;

import java.nio.FloatBuffer;

public class NoFilter {

    protected static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uTexMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "    gl_Position = uMVPMatrix * aPosition;\n" +
                    "    vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n" +
                    "}\n";

    protected static final String FRAGMENT_SHADER_EXT =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";

    protected final Drawable2d mRectDrawable = new Drawable2d(Drawable2d.Prefab.CUSTOMIZE);

    protected int mProgramHandleOES;
    protected int maPositionLocOES;
    protected int maTextureCoordLocOES;
    protected int muMVPMatrixLocOES;
    protected int muTexMatrixLocOES;
    protected int muUniformTextureOES;

    protected int mProgramHandle2D;
    protected int maPositionLoc2D;
    protected int maTextureCoordLoc2D;
    protected int muMVPMatrixLoc2D;
    protected int muTexMatrixLoc2D;
    protected int muUniformTexture2D;

    protected FloatBuffer mVertexArray;
    protected FloatBuffer mTexCoordArray;

    protected int width;
    protected int height;

    protected int drawMode = GLES20.GL_TRIANGLE_STRIP;
    protected int corrdinatesCount = 4;
}
