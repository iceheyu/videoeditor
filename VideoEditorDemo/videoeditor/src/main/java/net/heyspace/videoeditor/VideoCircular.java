package net.heyspace.videoeditor;

import android.media.MediaFormat;

import net.heyspace.videoeditor.datasource.DataSource;
import net.heyspace.videoeditor.encoder.VideoEncodeConfig;
import net.heyspace.videoeditor.encoder.VideoEncoder;
import net.heyspace.videoeditor.exceptions.ProcessException;
import net.heyspace.videoeditor.exceptions.SetupException;
import net.heyspace.videoeditor.utils.Beacon;
import net.heyspace.videoeditor.utils.Filter;

import java.io.IOException;
import java.util.Queue;


/**
 * Created by Lorenzo on 2020/04/15.
 */
public class VideoCircular extends Circular {

    public final static String TAG = "Editor.VideoCircular";

    public VideoCircular(Queue<DataSource> dataSources, MovieWriter movieWriter, Beacon beacon) {
        super(dataSources, movieWriter, beacon);
    }

    @Override
    protected void setup() throws SetupException {
        try {
            int width = inputMediaFormat.getInteger(MediaFormat.KEY_WIDTH);
            int height = inputMediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
            int framerate = inputMediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE);

            VideoEncodeConfig encodeConfig = new VideoEncodeConfig(width, height, framerate);
            encoder = new VideoEncoder(movieWriter, encodeConfig.toFormat(), muxerBeacon);
            encoder.setup();
            transmitter = new Transmitter(encoder.getInputSurface(), width, height);
            transmitter.setup();

            Filter filter = new Filter(Filter.CROP);
            filter.setup(width, height, 0f, 0f, 1f, 1f);
            filter.setTexture(transmitter.getProductSurfaceTextureId());
            transmitter.addFilter(filter);

            if (watermark != null) {
                Filter waterfilter = new Filter(Filter.WATERMARK);
                waterfilter.setup(width, height, watermark.x, watermark.y, watermark.w, watermark.h);
                waterfilter.setWatermark(watermark.bitmap);
                transmitter.addFilter(waterfilter);
            }

            nextDataSource(0);
        } catch (IOException e) {
            throw new SetupException(e.getMessage());
        }
    }

    @Override
    protected void processFrame() throws ProcessException {
        currentDataSource.processFrame();
        if (currentDataSource.useUp()) {
            try {
                nextDataSource(currentDataSource.getCumulateUs());
            } catch (SetupException e) {
                throw new ProcessException(e.getMessage());
            }
        }
        transmitter.processFrame();
        encoder.processFrame();
    }

    @Override
    protected void release() {
        onEditingListener.onEncodingEnd(1);
        encoder.release();
        transmitter.release();
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
        currentDataSource.config(transmitter.getProductSurface(), cumulateUs);
        currentDataSource.setup();
        transmitter.setFrameProvider(currentDataSource);
    }

}
