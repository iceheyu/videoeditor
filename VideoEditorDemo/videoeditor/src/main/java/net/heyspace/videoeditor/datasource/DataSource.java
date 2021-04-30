package net.heyspace.videoeditor.datasource;

import android.media.MediaCodec;
import android.view.Surface;

import net.heyspace.videoeditor.exceptions.ProcessException;
import net.heyspace.videoeditor.extractor.Extractor;
import net.heyspace.videoeditor.pipeline.ProvidedStage;
import net.heyspace.videoeditor.structs.Frame;

/**
 * 数据源，可以是图片、视频、声音等
 * <p>
 * Created by Lorenzo on 2020/04/15.
 */
public abstract class DataSource extends ProvidedStage<Frame> {

    public String path;
    protected long rangeStartTimeUs;
    protected long rangeEndTimeUs;

    //音视频解码
    protected MediaCodec codec;
    protected Extractor extractor;
    protected MediaCodec.BufferInfo bufferInfo;

    //图像
    protected Surface surface;

    //累积时间
    protected long cumulateUs;

    protected boolean inputEos;
    protected boolean outputEos;

    public DataSource(String path, long startTimeUs, long endTimeUs) {
        this.path = path;
        rangeStartTimeUs = startTimeUs;
        rangeEndTimeUs = endTimeUs;
        bufferInfo = new MediaCodec.BufferInfo();
    }

    /**
     * @param surface
     * @param cumulateUs
     */
    public void config(Surface surface, long cumulateUs) {
        this.surface = surface;
        this.cumulateUs = cumulateUs;
    }

    @Override
    public void processFrame() throws ProcessException {
        super.processFrame();
    }

    public abstract boolean useUp();

    public abstract long getCumulateUs();
}
