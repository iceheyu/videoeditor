package net.heyspace.videoeditor;

import android.media.MediaFormat;

import net.heyspace.videoeditor.datasource.DataSource;
import net.heyspace.videoeditor.encoder.AudioEncodeConfig;
import net.heyspace.videoeditor.encoder.AudioEncoder;
import net.heyspace.videoeditor.exceptions.ProcessException;
import net.heyspace.videoeditor.exceptions.SetupException;
import net.heyspace.videoeditor.utils.Beacon;

import java.util.Queue;

public class AudioCircular extends Circular {

    public final static String TAG = "Editor.AudioCircular";

    public AudioCircular(Queue<DataSource> dataSources, MovieWriter movieWriter, Beacon beacon) {
        super(dataSources, movieWriter, beacon);
    }

    @Override
    protected void setup() throws SetupException {
        int samplerate = inputMediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        int channelcount = inputMediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
        AudioEncodeConfig encodeConfig = new AudioEncodeConfig(samplerate, channelcount);
        encoder = new AudioEncoder(movieWriter, encodeConfig.toFormat(), muxerBeacon);
        encoder.setup();
        nextDataSource(0);
    }

    @Override
    protected void processFrame() throws ProcessException {
        currentDataSource.processFrame();
        if (currentDataSource.useUp()) {
            try {
                nextDataSource(currentDataSource.getCumulateUs());
            } catch (SetupException e) {
                e.printStackTrace();
                throw new ProcessException(e.getMessage());
            }
        }
        encoder.processFrame();
        if(onEditingListener != null){
            onEditingListener.progress(encoder.getLastEncoderOutputTimeUs(), 0);
        }
    }

    @Override
    protected void release() {
        onEditingListener.onEncodingEnd(10);
        encoder.release();
    }

    /**
     * 切换数据源
     *
     * @param cumulateUs 累计时间
     * @throws SetupException
     */
    private void nextDataSource(long cumulateUs) throws SetupException {
        if (currentDataSource != null) {
            currentDataSource.release();
        }
        if (dataSources.isEmpty()) {
            isLooping = false;
            return;
        }
        currentDataSource = dataSources.poll();
        currentDataSource.config(null, cumulateUs);
        currentDataSource.setup();
        encoder.setFrameProvider(currentDataSource);
    }
}
