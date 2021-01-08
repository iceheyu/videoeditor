package net.heyspace.videoeditor.opengl.filter;

import net.heyspace.videoeditor.opengl.gles.GlUtil;

public class RotateFilter extends BaseFilter {

    private int olddegree;

    private static float textureCoordinates[] = {
            0.0f, 0.0f,      // 0 bottom left
            1.0f, 0.0f,      // 1 bottom right
            0.0f, 1.0f,      // 2 top left
            1.0f, 1.0f,      // 3 top right
    };

    public RotateFilter(int width, int height) {
        super(width, height);
    }

    public void viewport(int width, int height, int degree) {
        this.width = width;
        this.height = height;

        degree = degree % 360;

        if (olddegree != degree) {
            olddegree = degree;
            if (degree == 0) {
                textureCoordinates = new float[]{
                        0.0f, 0.0f,      // 0 bottom left
                        1.0f, 0.0f,      // 1 bottom right
                        0.0f, 1.0f,      // 2 top left
                        1.0f, 1.0f,      // 3 top right
                };
            } else if (degree == 180 || degree == -180) {
                textureCoordinates = new float[]{
                        1.0f, 1.0f,      // 3 top right
                        0.0f, 1.0f,      // 2 top left
                        1.0f, 0.0f,      // 1 bottom right
                        0.0f, 0.0f,      // 0 bottom left
                };
            } else if (degree == 90 || degree == -270) {
                textureCoordinates = new float[]{
                        1.0f, 0.0f,      // 1 bottom right
                        1.0f, 1.0f,      // 3 top right
                        0.0f, 0.0f,      // 0 bottom left
                        0.0f, 1.0f,      // 2 top lef
                };
            } else if (degree == -90 || degree == 270) {
                textureCoordinates = new float[]{
                        0.0f, 1.0f,      // 2 top left
                        0.0f, 0.0f,      // 0 bottom left
                        1.0f, 1.0f,      // 3 top right
                        1.0f, 0.0f,      // 1 bottom right
                };
            }
            mTexCoordArray = GlUtil.createFloatBuffer(textureCoordinates);
        }
    }

}
