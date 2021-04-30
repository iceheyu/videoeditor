package net.heyspace.videoeditor.encoder;

import android.media.MediaCodec;
import android.media.MediaFormat;

import net.heyspace.videoeditor.MovieWriter;
import net.heyspace.videoeditor.exceptions.ProcessException;
import net.heyspace.videoeditor.exceptions.SetupException;
import net.heyspace.videoeditor.utils.Beacon;
import net.heyspace.videoeditor.utils.MediaUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Lorenzo on 2020/04/15.
 */
public class VideoEncoder extends Encoder {

    public final static String TAG = "Editor.VideoEncoder";

    public VideoEncoder(MovieWriter movieWriter, MediaFormat format, Beacon beacon) {
        super(movieWriter, format, beacon);
    }

    @Override
    public void setup() throws SetupException {
        String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
        try {
            codec = MediaCodec.createEncoderByType(mime);
            codec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            inputSurface = codec.createInputSurface();
            codec.start();
        } catch (IOException e) {
            throw new SetupException(e.getMessage());
        }
    }

    @Override
    public void processFrame() throws ProcessException {
        while (true) {
            int bufferIndex = codec.dequeueOutputBuffer(bufferInfo, 5000);

            if (bufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                break;
            }

            if (bufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                movieWriter.addVideoTrack(codec.getOutputFormat());
                break;
            }

            if (bufferIndex < 0) {
                break;
            }

            if (bufferInfo.flags != MediaCodec.BUFFER_FLAG_CODEC_CONFIG &&
                    bufferInfo.size != 0) {
                ByteBuffer buffer = codec.getOutputBuffer(bufferIndex);
                muxerBeacon.videoTimeUs = bufferInfo.presentationTimeUs;
                waitAudio();
                movieWriter.writeVideo(buffer, bufferInfo);
                lastEncoderOutputTimeUs = bufferInfo.presentationTimeUs;
            }

            codec.releaseOutputBuffer(bufferIndex, false);

            if (MediaUtils.hasEosFlag(bufferInfo.flags)) {
                muxerBeacon.videoTimeUs = -1;
                break;
            }
        }
    }

    @Override
    public void release() {
        muxerBeacon.videoTimeUs = -1;
        codec.stop();
        codec.release();
    }


    private void waitAudio() {
        while (muxerBeacon.audioTimeUs != -1
                && lastEncoderOutputTimeUs - muxerBeacon.audioTimeUs > Beacon.DEVIATION) {
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
