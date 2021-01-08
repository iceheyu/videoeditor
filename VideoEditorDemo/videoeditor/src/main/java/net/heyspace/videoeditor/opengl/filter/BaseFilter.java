package net.heyspace.videoeditor.opengl.filter;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import net.heyspace.videoeditor.opengl.gles.GlUtil;

public class BaseFilter extends NoFilter {

    public BaseFilter(int width, int height) {
        mVertexArray = mRectDrawable.getVertexArray();
        mTexCoordArray = mRectDrawable.getTexCoordArray();
        this.width = width;
        this.height = height;
        makeProgram();
    }

    protected void makeProgram() {
        mProgramHandleOES = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER_EXT);
        maPositionLocOES = GLES20.glGetAttribLocation(mProgramHandleOES, "aPosition");
        maTextureCoordLocOES = GLES20.glGetAttribLocation(mProgramHandleOES, "aTextureCoord");
        muMVPMatrixLocOES = GLES20.glGetUniformLocation(mProgramHandleOES, "uMVPMatrix");
        muTexMatrixLocOES = GLES20.glGetUniformLocation(mProgramHandleOES, "uTexMatrix");
        muUniformTextureOES = GLES20.glGetUniformLocation(mProgramHandleOES, "sTexture");
    }

    public void viewsize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void drawFrame(int textureId, float[] matrix) {
        //GLES20.glClearColor(0.0f, 0f, 0f, 0f);
        //GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GlUtil.checkGlError("drawFrame");
        GLES20.glViewport(0, 0, width, height);
        GlUtil.checkGlError("glViewport");
        GLES20.glUseProgram(mProgramHandleOES);
        GlUtil.checkGlError("glUseProgram");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GlUtil.checkGlError("glBindTexture");
        GLES20.glUniform1i(muUniformTextureOES, 0);
        GlUtil.checkGlError("glUniform1i");

        GLES20.glUniformMatrix4fv(muMVPMatrixLocOES, 1,
                false, GlUtil.IDENTITY_MATRIX, 0);
        GlUtil.checkGlError("glUniformMatrix4fv");

        GLES20.glUniformMatrix4fv(muTexMatrixLocOES, 1,
                false, matrix, 0);
        GlUtil.checkGlError("glUniformMatrix4fv");

        GLES20.glEnableVertexAttribArray(maPositionLocOES);
        GLES20.glVertexAttribPointer(maPositionLocOES, 2, GLES20.GL_FLOAT,
                false, 0,
                mVertexArray);
        GlUtil.checkGlError("glVertexAttribPointer");

        GLES20.glEnableVertexAttribArray(maTextureCoordLocOES);
        GLES20.glVertexAttribPointer(maTextureCoordLocOES, 2, GLES20.GL_FLOAT,
                false, 0,
                mTexCoordArray);
        GlUtil.checkGlError("glVertexAttribPointer");

        GLES20.glDrawArrays(drawMode, 0, corrdinatesCount);
        GLES20.glDisableVertexAttribArray(maPositionLocOES);
        GLES20.glDisableVertexAttribArray(maTextureCoordLocOES);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
    }

    public void release() {
        GLES20.glDeleteProgram(mProgramHandleOES);
        mProgramHandleOES = -1;
    }
}
