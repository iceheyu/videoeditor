package net.heyspace.videoeditor.encoder;

import android.media.MediaCodec;
import android.media.MediaFormat;

import net.heyspace.videoeditor.MovieWriter;
import net.heyspace.videoeditor.exceptions.ProcessException;
import net.heyspace.videoeditor.exceptions.SetupException;
import net.heyspace.videoeditor.structs.Frame;
import net.heyspace.videoeditor.utils.Beacon;
import net.heyspace.videoeditor.utils.MediaUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Lorenzo on 2020/04/04.
 */
public class AudioEncoder extends Encoder {

    public final static String TAG = "Editor.AudioEncoder";

    public AudioEncoder(MovieWriter movieWriter, MediaFormat format, Beacon beacon) {
        super(movieWriter, format, beacon);
    }

    @Override
    public void setup() throws SetupException {
        String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
        try {
            codec = MediaCodec.createEncoderByType(mime);
            codec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            codec.start();
        } catch (IOException e) {
            e.printStackTrace();
            throw new SetupException(e.getMessage());
        }
    }

    @Override
    public void processFrame() throws ProcessException {
        Frame frame = frameProvider.dequeueOutputBuffer();
        if (frame == null) {
            return;
        }
        feedDataToMediaCodec(frame);
        drainEncodedFrame();
        frameProvider.enqueueOutputBuffer(frame);
    }

    @Override
    public void release() {
        muxerBeacon.audioTimeUs = -1;
        codec.stop();
        codec.release();
    }

    protected void feedDataToMediaCodec(Frame frame) {
        int inputBufferIndex = codec.dequeueInputBuffer(5000);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = codec.getInputBuffer(inputBufferIndex);
            inputBuffer.clear();
            inputBuffer.put(frame.buffer);
            codec.queueInputBuffer(inputBufferIndex, frame.offset, frame.size,
                    frame.presentationTimeUs, frame.flags);
        }
    }

    protected void drainEncodedFrame() throws ProcessException {
        while (true) {
            int outputBufferId = codec.dequeueOutputBuffer(bufferInfo, 5000);

            if (outputBufferId == MediaCodec.INFO_TRY_AGAIN_LATER) {
                break;
            }

            if (outputBufferId == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                break;
            }

            if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                movieWriter.addAudioTrack(codec.getOutputFormat());
                break;
            }

            if (outputBufferId < 0) {
                break;
            }

            if (bufferInfo.flags != MediaCodec.BUFFER_FLAG_CODEC_CONFIG &&
                    bufferInfo.size != 0) {
                ByteBuffer buffer = codec.getOutputBuffer(outputBufferId);
                buffer.position(bufferInfo.offset);
                buffer.limit(bufferInfo.offset + bufferInfo.size);
                muxerBeacon.audioTimeUs = bufferInfo.presentationTimeUs;
                waitVideo();
                if (bufferInfo.presentationTimeUs > lastEncoderOutputTimeUs + 7739) {
                    movieWriter.writeAudio(buffer, bufferInfo);
                    lastEncoderOutputTimeUs = bufferInfo.presentationTimeUs;
                } else {
                    buffer.clear();
                }
            }

            codec.releaseOutputBuffer(outputBufferId, false);

            if (MediaUtils.hasEosFlag(bufferInfo.flags)) {
                muxerBeacon.audioTimeUs = -1;
                break;
            }
        }
    }

    private void waitVideo() {
        while (muxerBeacon.videoTimeUs != -1
                && lastEncoderOutputTimeUs - muxerBeacon.videoTimeUs > Beacon.DEVIATION) {
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
