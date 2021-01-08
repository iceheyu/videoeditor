package net.heyspace.videoeditor.opengl.filter;

import net.heyspace.videoeditor.opengl.gles.GlUtil;

public class CropFilter extends BaseFilter {

    private static float textureCoordinates[] = {
            0.0f, 0.0f,      // 0 bottom left
            1.0f, 0.0f,      // 1 bottom right
            0.0f, 1.0f,      // 2 top left
            1.0f, 1.0f,      // 3 top right
    };

    public CropFilter(int width, int height) {
        super(width, height);
    }

    public void crop(float x, float y, float w, float h) {
        float minX = x;
        float minY = y;
        float maxX = minX + w;
        float maxY = minY + h;

        // 0 bottom left
        textureCoordinates[0] = minX;
        textureCoordinates[1] = minY;

        // 1 bottom right
        textureCoordinates[2] = maxX;
        textureCoordinates[3] = minY;

        // 2 top left
        textureCoordinates[4] = minX;
        textureCoordinates[5] = maxY;

        // 3 top right
        textureCoordinates[6] = maxX;
        textureCoordinates[7] = maxY;

        mTexCoordArray = GlUtil.createFloatBuffer(textureCoordinates);
    }

}
