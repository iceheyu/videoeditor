package net.heyspace.videoeditor;

import android.graphics.Bitmap;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import net.heyspace.videoeditor.datasource.AudioSource;
import net.heyspace.videoeditor.datasource.DataSource;
import net.heyspace.videoeditor.datasource.ImageSource;
import net.heyspace.videoeditor.datasource.VideoSource;
import net.heyspace.videoeditor.datasource.VoiceSource;
import net.heyspace.videoeditor.structs.VideoFragment;
import net.heyspace.videoeditor.utils.Beacon;
import net.heyspace.videoeditor.utils.MediaUtils;
import net.heyspace.videoeditor.utils.Watermark;

import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Lorenzo on 2020/04/16.
 */
public class EditUtil {

    private MovieWriter movieWriter;
    private AudioCircular audioCircular;
    private VideoCircular videoCircular;
    private volatile int done = 0;
    private OnEditingListener onEditingListener;
    private long duration;
    private long startTimeMs;

    public long getDuration() {
        return duration;
    }

    /**
     * 分割
     *
     * @param fragment 视频地址以及需要保留的时间段, 一个视频分多段多次调用即可
     * @param dest
     */
    public void slice(VideoFragment fragment, String dest) throws IOException {
        duration = fragment.end - fragment.start;

        MediaFormat videoFormat = MediaUtils.getMediaFormat(fragment.path, MediaFormat.MIMETYPE_VIDEO_AVC);
        Queue<DataSource> videoSources = new LinkedBlockingQueue<>();
        DataSource video = new VideoSource(fragment.path, fragment.start, fragment.end);
        videoSources.add(video);

        MediaFormat audioFormat = MediaUtils.getMediaFormat(fragment.path, MediaFormat.MIMETYPE_AUDIO_AAC);
        DataSource audio = new AudioSource(fragment.path, fragment.start, fragment.end);
        Queue<DataSource> audioSources = new LinkedBlockingQueue<>();
        audioSources.add(audio);

        run(videoSources, videoFormat, audioSources, audioFormat, null, dest);
    }

    /**
     * 删除片段
     *
     * @param fragments 视频地址以及需要保留的时间段
     * @param dest
     */
    public void cut(List<VideoFragment> fragments, String dest) throws IOException {
        Queue<DataSource> videoSources = new LinkedBlockingQueue<>();
        Queue<DataSource> audioSources = new LinkedBlockingQueue<>();
        MediaFormat videoFormat = MediaUtils.getMediaFormat(fragments.get(0).path, MediaFormat.MIMETYPE_VIDEO_AVC);
        MediaFormat audioFormat = MediaUtils.getMediaFormat(fragments.get(0).path, MediaFormat.MIMETYPE_AUDIO_AAC);

        for (VideoFragment fragment : fragments) {
            DataSource video = new VideoSource(fragment.path, fragment.start, fragment.end);
            videoSources.add(video);

            DataSource audio = new AudioSource(fragment.path, fragment.start, fragment.end);
            audioSources.add(audio);
            duration += fragment.end - fragment.start;
        }

        run(videoSources, videoFormat, audioSources, audioFormat, null, dest);
    }

    /**
     * 合并
     *
     * @param videos
     * @param dest
     */
    public void concat(List<String> videos, String dest) throws IOException {
        Queue<DataSource> videoSources = new LinkedBlockingQueue<>();
        Queue<DataSource> audioSources = new LinkedBlockingQueue<>();
        MediaFormat videoFormat = null;
        MediaFormat audioFormat = null;

        for (String path : videos) {
            videoFormat = MediaUtils.getMediaFormat(path, MediaFormat.MIMETYPE_VIDEO_AVC);
            audioFormat = MediaUtils.getMediaFormat(path, MediaFormat.MIMETYPE_AUDIO_AAC);
            long dura = audioFormat.getLong(MediaFormat.KEY_DURATION);
            duration += dura;

            DataSource video = new VideoSource(path, 0, dura);
            videoSources.add(video);

            DataSource audio = new AudioSource(path, 0, dura);
            audioSources.add(audio);
        }

        run(videoSources, videoFormat, audioSources, audioFormat, null, dest);
    }

    /**
     * 片头片尾
     *
     * @param videos
     * @param dest
     * @param title
     * @param titleDuration s
     * @param trail
     * @param trailDuration s
     * @throws IOException
     */
    public void addCreditTitle(List<String> videos, String dest, Bitmap title, long titleDuration,
                               Bitmap trail, long trailDuration) throws IOException {

        Queue<DataSource> videoSources = new LinkedBlockingQueue<>();
        Queue<DataSource> audioSources = new LinkedBlockingQueue<>();
        MediaFormat videoFormat = null;
        MediaFormat audioFormat = null;

        duration += titleDuration * 1_000_000;
        duration += trailDuration * 1_000_000;

        if (titleDuration > 0) {
            DataSource titleImageSource = new ImageSource(null,
                    0, titleDuration * 1_000_000);
            ((ImageSource) titleImageSource).setBitmap(title);
            videoSources.add(titleImageSource);

            DataSource titleVoiceSource = new VoiceSource(null,
                    0, titleDuration * 1_000_000);
            audioSources.add(titleVoiceSource);
        }

        for (String path : videos) {
            videoFormat = MediaUtils.getMediaFormat(path, MediaFormat.MIMETYPE_VIDEO_AVC);
            audioFormat = MediaUtils.getMediaFormat(path, MediaFormat.MIMETYPE_AUDIO_AAC);
            long dura = audioFormat.getLong(MediaFormat.KEY_DURATION);
            duration += dura;

            DataSource video = new VideoSource(path, 0, dura);
            videoSources.add(video);

            DataSource audio = new AudioSource(path, 0, dura);
            audioSources.add(audio);
        }

        if (trailDuration > 0) {
            DataSource trailImageSource = new ImageSource(null,
                    0, trailDuration * 1_000_000);
            ((ImageSource) trailImageSource).setBitmap(trail);
            videoSources.add(trailImageSource);

            DataSource trailVoiceSource = new VoiceSource(null,
                    0, trailDuration * 1_000_000);
            audioSources.add(trailVoiceSource);
        }

        run(videoSources, videoFormat, audioSources, audioFormat, null, dest);
    }

    /**
     * 添加水印
     *
     * @param src
     * @param dest
     * @param watermark
     */
    public void addWatermark(String src, String dest, Watermark watermark) throws IOException {
        MediaFormat videoFormat = MediaUtils.getMediaFormat(src, MediaFormat.MIMETYPE_VIDEO_AVC);
        duration = videoFormat.getLong(MediaFormat.KEY_DURATION);
        Queue<DataSource> videoSources = new LinkedBlockingQueue<>();
        DataSource video = new VideoSource(src, 0, duration);
        videoSources.add(video);

        MediaFormat audioFormat = MediaUtils.getMediaFormat(src, MediaFormat.MIMETYPE_AUDIO_AAC);
        DataSource audio = new AudioSource(src, 0, duration);
        Queue<DataSource> audioSources = new LinkedBlockingQueue<>();
        audioSources.add(audio);

        run(videoSources, videoFormat, audioSources, audioFormat, watermark, dest);
    }


    /**
     * 编辑(分割、删除、封面、水印、配音等)
     *
     * @param videoSources
     * @param videoFormat
     * @param audioSources
     * @param audioFormat
     * @param watermark
     * @param dest
     * @throws IOException
     */
    private void run(Queue<DataSource> videoSources, MediaFormat videoFormat,
                     Queue<DataSource> audioSources, MediaFormat audioFormat,
                     Watermark watermark, String dest)
            throws IOException {

        movieWriter = new MovieWriter();
        movieWriter.prepare(dest);
        movieWriter.setAudioTrackReadyListener(audioTrackReadyListener);

        Beacon beacon = new Beacon();

        audioCircular = new AudioCircular(audioSources, movieWriter, beacon);
        audioCircular.setOnEditingListener(editorListener);
        audioCircular.setMediaFormat(audioFormat);
        audioCircular.start();

        videoCircular = new VideoCircular(videoSources, movieWriter, beacon);
        videoCircular.setOnEditingListener(editorListener);
        videoCircular.setMediaFormat(videoFormat);
        videoCircular.setWatermark(watermark);

        startTimeMs = SystemClock.elapsedRealtime();
    }

    private MovieWriter.AudioTrackReadyListener audioTrackReadyListener = new MovieWriter.AudioTrackReadyListener() {
        @Override
        public void onReady() {
            sendEditMessage(1, 0);
        }
    };

    public void setOnEditingListener(OnEditingListener listener) {
        this.onEditingListener = listener;
    }

    private OnEditingListener editorListener = new OnEditingListener() {
        @Override
        public void onEncodingEnd(int id) {
            done |= id;
            if (done == 11) {
                movieWriter.stop();
                sendEditMessage(2, 1f);
            }
        }

        @Override
        public void progress(float percent, long timeMs) {
            sendEditMessage(3, percent);
        }
    };

    //1: audio ready,  2: encoding end,   3: progress
    private void sendEditMessage(int type, float value) {
        Message message = editMessageHandler.obtainMessage();
        Bundle data = message.getData();
        data.putInt("type", type);
        data.putFloat("value", value);
        editMessageHandler.sendMessage(message);
    }

    private Handler editMessageHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Bundle data = msg.getData();
            int type = data.getInt("type");
            float value = data.getFloat("value");
            switch (type) {
                case 1:
                    videoCircular.start();
                    break;

                case 2:
                    if (onEditingListener != null) {
                        onEditingListener.onEncodingEnd(11);
                    }
                    break;

                case 3:
                    if (onEditingListener != null) {
                        float percent = value / duration;
                        onEditingListener.progress(percent, SystemClock.elapsedRealtime() - startTimeMs);
                    }
                    break;
            }
            return false;
        }
    });
}
