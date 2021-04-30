package net.heyspace.videoeditor.utils;

import android.graphics.Bitmap;

/**
 * Created by Lorenzo on 2020/04/15.
 */
public class Watermark {
    public Bitmap bitmap;
    public float x;
    public float y;
    public float w;
    public float h;

    public Watermark(Bitmap bitmap, float x, float y, float w, float h) {
        this.bitmap = bitmap;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }
}
