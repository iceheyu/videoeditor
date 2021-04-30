package net.heyspace.videoeditor.extractor;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import net.heyspace.videoeditor.exceptions.SetupException;
import net.heyspace.videoeditor.utils.MediaUtils;

import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * Created by Lorenzo on 2020/04/15.
 */
public class Extractor {

    public String TAG = "Editor.Extractor";

    private MediaExtractor extractor;
    private MediaFormat mediaFormat;
    private long cumulateTimeUs;
    private long rangeStartTimeUs;
    private long rangeEndTimeUs;
    private long realStartTimeUs;
    private long deviationStartTimeUs;
    private MediaCodec.BufferInfo bufferInfo;

    public Extractor(long cumulateUs, long startUs, long endUs) {
        cumulateTimeUs = cumulateUs;
        rangeStartTimeUs = startUs;
        rangeEndTimeUs = endUs;
        realStartTimeUs = -1;
        deviationStartTimeUs = 0;
        extractor = new MediaExtractor();
        bufferInfo = new MediaCodec.BufferInfo();
    }

    public void setDataSource(String dataSource) throws SetupException {
        try {
            extractor.setDataSource(dataSource);
        } catch (IOException e) {
            throw new SetupException(e.getMessage());
        }
    }

    public int selectTrack(String mime) {
        int trackId = MediaUtils.getTrackId(extractor, mime);
        extractor.selectTrack(trackId);
        mediaFormat = extractor.getTrackFormat(trackId);
        return trackId;
    }

    public void seekToRange() {
        if (rangeStartTimeUs > 0) {
            extractor.seekTo(rangeStartTimeUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
        }
    }

    public boolean inRange(long timeUs) {
        long realTimeUs = timeUs - cumulateTimeUs + rangeStartTimeUs + deviationStartTimeUs;
        return rangeStartTimeUs <= realTimeUs && realTimeUs <= rangeEndTimeUs;
    }

    public MediaFormat getMediaFormat() {
        return mediaFormat;
    }

    public MediaCodec.BufferInfo readSampleData(ByteBuffer byteBuffer) {
        int len = extractor.readSampleData(byteBuffer, 0);
        long sampleTime = extractor.getSampleTime();
        int flags = extractor.getSampleFlags();

        bufferInfo.size = len;
        bufferInfo.offset = 0;
        bufferInfo.flags = flags;
        bufferInfo.presentationTimeUs = cumulateTimeUs + sampleTime - rangeStartTimeUs - deviationStartTimeUs;

        if (rangeStartTimeUs <= sampleTime && sampleTime <= rangeEndTimeUs) {
            if (realStartTimeUs == -1) {
                realStartTimeUs = sampleTime;
                deviationStartTimeUs = sampleTime - realStartTimeUs;
            }
        }

        if (len == 0 || sampleTime == -1 || sampleTime > rangeEndTimeUs) {
            byteBuffer.clear();
            bufferInfo.size = 0;
            bufferInfo.offset = 0;
            bufferInfo.flags = MediaCodec.BUFFER_FLAG_END_OF_STREAM;
            bufferInfo.presentationTimeUs = -1;
            Log.d(TAG, "range end");
        }

        extractor.advance();
        return bufferInfo;
    }

    public void release() {
        if (extractor != null) {
            extractor.release();
            extractor = null;
        }
    }
}
