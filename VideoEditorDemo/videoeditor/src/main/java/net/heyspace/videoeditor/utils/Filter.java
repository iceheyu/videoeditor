package net.heyspace.videoeditor.utils;

import android.graphics.Bitmap;

import net.heyspace.videoeditor.opengl.filter.CropFilter;
import net.heyspace.videoeditor.opengl.filter.WaterFilter;

/**
 * Created by Lorenzo on 2020/04/15.
 */
public class Filter {

    public final static int CROP = 1;
    public final static int WATERMARK = 2;

    private int type;
    private CropFilter cropFilter;
    private WaterFilter waterFilter;

    private int textureId;
    private Bitmap watermark;

    public Filter(int type) {
        this.type = type;
    }

    public void setup(int width, int height, float x, float y, float w, float h) {
        switch (type) {
            case CROP:
                cropFilter = new CropFilter(width, height);
                cropFilter.crop(x, y, w, h);
                break;

            case WATERMARK:
                waterFilter = new WaterFilter(width, height);
                waterFilter.setPosition(x, y, w, h);
                break;
        }
    }

    public void setWatermark(Bitmap bitmap) {
        watermark = bitmap;
    }

    public void setTexture(int id) {
        textureId = id;
    }

    public void draw(float[] matrix) {
        switch (type) {
            case CROP:
                cropFilter.drawFrame(textureId, matrix);
                break;

            case WATERMARK:
                waterFilter.drawFrame(watermark, matrix);
                break;
        }
    }

    public void release() {
        if (cropFilter != null) {
            cropFilter.release();
        }
        if (waterFilter != null) {
            waterFilter.release();
        }
    }
}
