package net.heyspace.videoeditor.opengl.filter;

import android.opengl.GLES20;

import net.heyspace.videoeditor.opengl.gles.GlUtil;

import java.nio.FloatBuffer;

public class CircleFilter extends BaseFilter {

    private final int NUMPOINTS = 72;
    private float delta;

    public CircleFilter(int width, int height) {
        super(width, height);
        this.width = width;
        this.height = height;
        this.delta = (float) width / (float) height;
        this.drawMode = GLES20.GL_TRIANGLE_FAN;
        this.corrdinatesCount = NUMPOINTS + 2;
        mVertexArray = generateVertexCoordinates();
        mTexCoordArray = generateTextureCoordinates();
    }

    private FloatBuffer generateVertexCoordinates() {
        int offset = 0;
        float radius = 1f;
        float[] coordinates = new float[(NUMPOINTS + 2) * 2];
        coordinates[offset++] = 0f;
        coordinates[offset++] = 0f;
        for (int i = 0; i <= NUMPOINTS; i++) {
            float angleInRadians =
                    ((float) i / (float) NUMPOINTS)
                            * ((float) Math.PI * 2f);
            coordinates[offset++] = radius * (float) Math.cos(angleInRadians) / delta;
            coordinates[offset++] = radius * (float) Math.sin(angleInRadians);
        }
        return GlUtil.createFloatBuffer(coordinates);
    }

    private FloatBuffer generateTextureCoordinates() {
        int offset = 0;
        float radius = 0.5f;
        float[] coordinates = new float[(NUMPOINTS + 2) * 2];
        coordinates[offset++] = 0.5f;
        coordinates[offset++] = 0.5f;
        for (int i = 0; i <= NUMPOINTS; i++) {
            float angleInRadians =
                    ((float) i / (float) NUMPOINTS)
                            * ((float) Math.PI * 2f);
            coordinates[offset++] = 0.5f + radius * (float) Math.cos(angleInRadians) / delta;
            coordinates[offset++] = 0.5f + radius * (float) Math.sin(angleInRadians);
        }
        return GlUtil.createFloatBuffer(coordinates);
    }
}

