package net.heyspace.videoeditor;

import android.media.MediaFormat;
import android.os.SystemClock;

import net.heyspace.videoeditor.datasource.DataSource;
import net.heyspace.videoeditor.encoder.Encoder;
import net.heyspace.videoeditor.exceptions.ProcessException;
import net.heyspace.videoeditor.exceptions.SetupException;
import net.heyspace.videoeditor.utils.Beacon;
import net.heyspace.videoeditor.utils.Watermark;

import java.util.Queue;


/**
 * Created by Lorenzo on 2020/04/15.
 */
public abstract class Circular extends Thread {

    public final static String TAG = "Editor.Circular";

    private static final int DEFAULT_FRAME_PROCESS_INTERVAL = 1;

    protected Queue<DataSource> dataSources;
    protected DataSource currentDataSource;
    protected MediaFormat inputMediaFormat;
    protected Transmitter transmitter;
    protected Encoder encoder;
    protected MovieWriter movieWriter;
    protected Beacon muxerBeacon;
    protected Watermark watermark;
    protected volatile boolean isLooping;
    protected OnEditingListener onEditingListener;

    public Circular(Queue<DataSource> dataSources, MovieWriter movieWriter, Beacon beacon) {
        this.movieWriter = movieWriter;
        this.dataSources = dataSources;
        this.muxerBeacon = beacon;
    }

    public void setOnEditingListener(OnEditingListener listener) {
        onEditingListener = listener;
    }

    public void setMediaFormat(MediaFormat format) {
        inputMediaFormat = format;
    }

    public void setWatermark(Watermark watermark) {
        this.watermark = watermark;
    }

    @Override
    public void run() {
        try {
            setup();
            isLooping = true;
            while (isLooping) {
                long frameStartTime = SystemClock.elapsedRealtime();
                processFrame();
                // 如果一帧的处理时长太短，增加sleep，防止占用太高CPU。
                long frameCost = SystemClock.elapsedRealtime() - frameStartTime;
                if (frameCost < DEFAULT_FRAME_PROCESS_INTERVAL) {
                    try {
                        Thread.sleep(DEFAULT_FRAME_PROCESS_INTERVAL - frameCost);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Thread.currentThread().interrupt();
                    }
                }
            }
            release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract void setup() throws SetupException;

    protected abstract void processFrame() throws ProcessException;

    protected abstract void release();
}
