package net.heyspace.videoeditor.encoder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

import net.heyspace.videoeditor.MovieWriter;
import net.heyspace.videoeditor.exceptions.ProcessException;
import net.heyspace.videoeditor.exceptions.SetupException;
import net.heyspace.videoeditor.pipeline.Provider;
import net.heyspace.videoeditor.structs.Frame;
import net.heyspace.videoeditor.utils.Beacon;


/**
 * Created by Lorenzo on 2020/04/15.
 */
public abstract class Encoder {

    protected MovieWriter movieWriter;
    protected Beacon muxerBeacon;
    protected MediaCodec codec;
    protected MediaFormat mediaFormat;
    protected Surface inputSurface;
    protected Provider<Frame> frameProvider;
    protected MediaCodec.BufferInfo bufferInfo;
    protected long lastEncoderOutputTimeUs;

    public Encoder(MovieWriter movieWriter, MediaFormat format, Beacon beacon) {
        this.movieWriter = movieWriter;
        mediaFormat = format;
        bufferInfo = new MediaCodec.BufferInfo();
        this.muxerBeacon = beacon;
    }

    public Surface getInputSurface() {
        return inputSurface;
    }

    public void setFrameProvider(Provider<Frame> provider) {
        frameProvider = provider;
    }

    public abstract void setup() throws SetupException;

    public abstract void processFrame() throws ProcessException;

    public abstract void release();

    public long getLastEncoderOutputTimeUs() {
        return lastEncoderOutputTimeUs;
    }
}
