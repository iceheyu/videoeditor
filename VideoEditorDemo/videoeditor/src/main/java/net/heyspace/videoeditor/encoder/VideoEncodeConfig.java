package net.heyspace.videoeditor.encoder;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;


/**
 * Created by Lorenzo on 2020/04/01.
 */
public class VideoEncodeConfig {

    final int width;
    final int height;
    final int bitrate;
    final int framerate;
    final int iframeInterval;
    final int maxInputSize;
    final int colorFormat;
    final String codecName;
    final String mimeType;
    final MediaCodecInfo.CodecProfileLevel codecProfileLevel;

    public VideoEncodeConfig(int width, int height, int framerate) {
        this.codecName = "OMX.google.h264.encoder";
        this.mimeType = MediaFormat.MIMETYPE_VIDEO_AVC;
        this.width = width;
        this.height = height;
        this.bitrate = width * height * 2;
        this.framerate = framerate;
        this.iframeInterval = 3;
        this.maxInputSize = width * height * 3 / 2;
        this.colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
        this.codecProfileLevel = null;
    }

    public MediaFormat toFormat() {
        MediaFormat format = MediaFormat.createVideoFormat(mimeType, width, height);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
        //format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, maxInputSize);
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, framerate);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iframeInterval);

        if (codecProfileLevel != null && codecProfileLevel.profile != 0 && codecProfileLevel.level != 0) {
            format.setInteger(MediaFormat.KEY_PROFILE, codecProfileLevel.profile);
            format.setInteger("level", codecProfileLevel.level);
        }

        //format.setInteger(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 10_000_000);
        return format;
    }

    @Override
    public String toString() {
        return "VideoEncodeConfig{" +
                "width=" + width +
                ", height=" + height +
                ", bitrate=" + bitrate +
                ", framerate=" + framerate +
                ", iframeInterval=" + iframeInterval +
                ", codecName='" + codecName + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", codecProfileLevel=null" +
                '}';
    }
}
