package net.heyspace.videoeditor.datasource;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import net.heyspace.videoeditor.exceptions.ProcessException;
import net.heyspace.videoeditor.exceptions.SetupException;
import net.heyspace.videoeditor.extractor.Extractor;
import net.heyspace.videoeditor.pipeline.Stage;
import net.heyspace.videoeditor.structs.Frame;
import net.heyspace.videoeditor.utils.MediaUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class AudioSource extends DataSource {

    public final static String TAG = "Editor.AudioSource";

    private long lastDecoderOutputTimeUs;

    public AudioSource(String path, long startTimeUs, long endTimeUs) {
        super(path, startTimeUs, endTimeUs);
    }

    @Override
    public void setup() throws SetupException {
        extractor = new Extractor(cumulateUs, rangeStartTimeUs, rangeEndTimeUs);
        extractor.TAG += "audio";
        extractor.setDataSource(path);
        extractor.selectTrack(MediaFormat.MIMETYPE_AUDIO_AAC);
        MediaFormat inputFormat = extractor.getMediaFormat();
        String mimeType = inputFormat.getString(MediaFormat.KEY_MIME);
        try {
            codec = MediaCodec.createDecoderByType(mimeType);
            codec.configure(inputFormat, null, null, 0);
            codec.start();
        } catch (IOException e) {
            throw new SetupException("configure MediaCodec failed.", e);
        }
        setState(Stage.State.SETUPED);
    }

    @Override
    public void processFrame() throws ProcessException {
        try {
            super.processFrame();
            synchronized (this) {
                if (mWaitOutBuffers.size() >= Stage.DEFAULT_FRAME_COUNT) {
                    return;
                }
            }
            feedDataToMediaCodec();
            drainDecodedFrame();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ProcessException("decode failed", e);
        }
    }

    @Override
    public void release() {
        if (codec != null) {
            codec.stop();
            codec.release();
            codec = null;
        }
        if (extractor != null) {
            extractor.release();
            extractor = null;
        }
    }

    @Override
    protected void recycleBuffers(List<Frame> canReuseBuffers) {
        for (Frame frame : canReuseBuffers) {
            codec.releaseOutputBuffer(frame.bufferIndex, false);
        }
    }

    @Override
    public boolean useUp() {
        return mState == Stage.State.DONE;
    }

    @Override
    public long getCumulateUs() {
        return lastDecoderOutputTimeUs;
    }

    private void drainDecodedFrame() {
        if (outputEos) {
            return;
        }

        int decoderStatus = codec.dequeueOutputBuffer(bufferInfo, 0);

        if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
            return;
        }

        if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
            return;
        }

        if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            extractor.seekToRange();
            return;
        }

        if (decoderStatus < 0) {
            return;
        }

        ByteBuffer buffer = codec.getOutputBuffer(decoderStatus);
        Frame frame = new Frame();
        frame.buffer = buffer;
        frame.bufferIndex = decoderStatus;
        frame.offset = bufferInfo.offset;
        frame.size = bufferInfo.size;
        frame.presentationTimeUs = bufferInfo.presentationTimeUs;
        frame.flags = bufferInfo.flags;

        if (extractor.inRange(frame.presentationTimeUs) && !MediaUtils.hasEosFlag(frame.flags)) {
            synchronized (this) {
                mWaitOutBuffers.add(frame);
                lastDecoderOutputTimeUs = frame.presentationTimeUs;
            }
        } else {
            codec.releaseOutputBuffer(frame.bufferIndex, false);
        }

        if (MediaUtils.hasEosFlag(frame.flags)) {
            outputEos = true;
            setState(Stage.State.ALL_DATA_READY);
            Log.d(TAG, "audio decoding end ...");
        }
    }

    private void feedDataToMediaCodec() {
        if (inputEos) {
            return;
        }

        int inputBufIndex = codec.dequeueInputBuffer(0);
        if (inputBufIndex < 0) {
            return;
        }

        ByteBuffer inputBuf = codec.getInputBuffer(inputBufIndex);
        MediaCodec.BufferInfo bufferInfo = extractor.readSampleData(inputBuf);

        if (MediaUtils.hasEosFlag(bufferInfo.flags)) {
            inputEos = true;
        }

        codec.queueInputBuffer(inputBufIndex, bufferInfo.offset, bufferInfo.size,
                bufferInfo.presentationTimeUs, bufferInfo.flags);
    }
}
