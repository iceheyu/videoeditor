package net.heyspace.videoeditor.encoder;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;

/**
 * Created by Lorenzo on 2020/04/04.
 */
public class AudioEncodeConfig {

    public final String mimeType;
    public final int sampleRate;
    public final int channnelCount;
    public final int bitrate;
    public final int maxInputSize;

    public AudioEncodeConfig(int sampleRate, int channnelCount) {
        this.mimeType = MediaFormat.MIMETYPE_AUDIO_AAC;
        this.sampleRate = sampleRate;
        this.channnelCount = channnelCount;
        this.bitrate = 64000;
        this.maxInputSize = 8192;
    }

    public MediaFormat toFormat() {
        MediaFormat format = MediaFormat.createAudioFormat(mimeType, sampleRate, channnelCount);
        format.setString(MediaFormat.KEY_MIME, mimeType);
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, sampleRate);
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, channnelCount);
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        //format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, maxInputSize);
        return format;
    }

    @Override
    public String toString() {
        return "AudioEncodeConfig{" +
                "mimeType=" + mimeType +
                ", sampleRate=" + sampleRate +
                ", channnelCount=" + channnelCount +
                ", bitrate=" + bitrate +
                ", maxInputSize=" + maxInputSize +
                '}';
    }

}
