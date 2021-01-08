package net.heyspace.videoeditor.utils;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.io.IOException;

public class MediaUtils {

    public static final String KEY_ROTATION = "rotation-degrees";

    public static boolean hasEosFlag(int flags) {
        return (flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;
    }

    public static int getTrackId(MediaExtractor extractor, String mineType) {
        int trackId = 0;
        int trackCount = extractor.getTrackCount();
        for (int i = 0; i < trackCount; i++) {
            MediaFormat trackFormat = extractor.getTrackFormat(i);
            if (trackFormat.getString(MediaFormat.KEY_MIME).contains(mineType)) {
                trackId = i;
                break;
            }
        }
        return trackId;
    }

    public static MediaFormat getMediaFormat(String video, String mimeType) throws IOException {
        MediaExtractor extractor = new MediaExtractor();
        extractor.setDataSource(video);
        int videoTrackId = getTrackId(extractor, mimeType);
        extractor.selectTrack(videoTrackId);
        MediaFormat mediaFormat = extractor.getTrackFormat(videoTrackId);
        extractor.release();
        return mediaFormat;
    }

    public static void checkState(boolean expression, Object errorMessage) {
        if (!expression) {
            throw new IllegalStateException(String.valueOf(errorMessage));
        }
    }
}
