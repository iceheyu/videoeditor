package net.heyspace.videoeditor;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import net.heyspace.videoeditor.exceptions.ProcessException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Lorenzo on 2018/10/12.
 */
public class MovieWriter {

    private final static int INVALID_INDEX = -1;

    private MediaMuxer mMediaMuxer;
    private AtomicInteger mTrackCount;
    private AtomicBoolean mMediaMuxerStarted;
    private AtomicBoolean isRunning;
    private int mVideoTrackIndex;
    private int mAudioTrackIndex;

    public MovieWriter() {
        mVideoTrackIndex = INVALID_INDEX;
        mAudioTrackIndex = INVALID_INDEX;
        mTrackCount = new AtomicInteger(0);
        mMediaMuxerStarted = new AtomicBoolean(false);
        isRunning = new AtomicBoolean(false);
    }

    public void prepare(String savePath) throws IOException {
        if (isRunning.get())
            return;
        mTrackCount.set(0);
        isRunning.set(false);
        mMediaMuxerStarted.set(false);
        mMediaMuxer = new MediaMuxer(savePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
    }

    public synchronized void addVideoTrack(MediaFormat format) throws ProcessException {
        if (mVideoTrackIndex != INVALID_INDEX) {
            throw new ProcessException("视频轨道已添加");
        }
        mVideoTrackIndex = mMediaMuxer.addTrack(format);
        int trackCount = mTrackCount.incrementAndGet();
        if (!mMediaMuxerStarted.get() && trackCount == 2) {
            isRunning.set(true);
            mMediaMuxer.start();
            mMediaMuxerStarted.set(true);
        }
    }

    public void writeVideo(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo) throws ProcessException {
        if (!isRunning.get())
            return;
        if (mVideoTrackIndex == INVALID_INDEX) {
            throw new ProcessException("视频轨道未添加");
        }
        if (mMediaMuxerStarted.get()) {
            writeSampleData(mVideoTrackIndex, byteBuffer, bufferInfo);
        }
    }

    public synchronized void addAudioTrack(MediaFormat format) throws ProcessException {
        if (mAudioTrackIndex != INVALID_INDEX) {
            throw new ProcessException("音频轨道已添加");
        }
        mAudioTrackIndex = mMediaMuxer.addTrack(format);
        mTrackCount.incrementAndGet();
        if (audioTrackReadyListener != null) {
            //确保音频轨道加入后再开始视频编码， 以避免视频丢失第一个关键帧
            audioTrackReadyListener.onReady();
        }
    }

    public void writeAudio(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo) throws ProcessException {
        if (!isRunning.get())
            return;
        if (mAudioTrackIndex == INVALID_INDEX) {
            throw new ProcessException("音频轨道未添加");
        }
        if (mMediaMuxerStarted.get()) {
            writeSampleData(mAudioTrackIndex, byteBuffer, bufferInfo);
        }
    }

    public void stop() {
        if (!isRunning.get())
            return;

        isRunning.set(false);
        signalEndOfStream();
        mMediaMuxer.stop();
        mMediaMuxer.release();
        mMediaMuxer = null;
    }

    private synchronized void writeSampleData(int trackIndex, ByteBuffer data, MediaCodec.BufferInfo bufferInfo) {
        mMediaMuxer.writeSampleData(trackIndex, data, bufferInfo);
    }

    private void signalEndOfStream() {
        MediaCodec.BufferInfo eos = new MediaCodec.BufferInfo();
        ByteBuffer buffer = ByteBuffer.allocate(0);
        eos.set(0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
        if (mVideoTrackIndex != INVALID_INDEX) {
            mMediaMuxer.writeSampleData(mVideoTrackIndex, buffer, eos);
        }
        if (mAudioTrackIndex != INVALID_INDEX) {
            mMediaMuxer.writeSampleData(mAudioTrackIndex, buffer, eos);
        }
        mVideoTrackIndex = INVALID_INDEX;
        mAudioTrackIndex = INVALID_INDEX;
    }

    private AudioTrackReadyListener audioTrackReadyListener;

    public void setAudioTrackReadyListener(AudioTrackReadyListener listener) {
        this.audioTrackReadyListener = listener;
    }

    public interface AudioTrackReadyListener {
        void onReady();
    }
}
