package net.heyspace.videoeditor.datasource;

import net.heyspace.videoeditor.exceptions.ProcessException;
import net.heyspace.videoeditor.exceptions.SetupException;
import net.heyspace.videoeditor.structs.Frame;

import java.nio.ByteBuffer;
import java.util.List;

public class VoiceSource extends DataSource {

    public final static String TAG = "Editor.VoiceSource";

    private byte[] audioData = new byte[4096];
    private long timeUsPerFrame = 23219;
    private long lastDecoderOutputTimeUs;

    public VoiceSource(String path, long startTimeUs, long endTimeUs) {
        super(path, startTimeUs, endTimeUs);
    }

    @Override
    public void setup() throws SetupException {
        rangeStartTimeUs += cumulateUs;
        rangeEndTimeUs += cumulateUs;
        lastDecoderOutputTimeUs = rangeStartTimeUs;
    }

    @Override
    public void processFrame() throws ProcessException {
        super.processFrame();
        Frame frame = new Frame();
        ByteBuffer buffer = ByteBuffer.wrap(audioData);
        frame.buffer = buffer;
        frame.bufferIndex = 0;
        frame.offset = 0;
        frame.size = audioData.length;
        frame.presentationTimeUs = lastDecoderOutputTimeUs;
        lastDecoderOutputTimeUs += timeUsPerFrame;
        frame.flags = bufferInfo.flags;
        synchronized (this) {
            mWaitOutBuffers.add(frame);
        }
    }

    @Override
    public void release() {
    }

    @Override
    protected void recycleBuffers(List<Frame> canReuseBuffers) {
        for (Frame frame : canReuseBuffers) {
            frame.buffer.clear();
        }
    }

    @Override
    public boolean useUp() {
        return lastDecoderOutputTimeUs > rangeEndTimeUs;
    }

    @Override
    public long getCumulateUs() {
        return lastDecoderOutputTimeUs;
    }
}

