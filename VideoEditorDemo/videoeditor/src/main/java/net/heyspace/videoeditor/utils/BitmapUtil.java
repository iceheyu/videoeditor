package net.heyspace.videoeditor.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapUtil {

    public static Bitmap loadBitmap(String pathName) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, opts);
        opts.inSampleSize = 1;
        opts.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(pathName, opts);
        return bitmap;
    }
}
