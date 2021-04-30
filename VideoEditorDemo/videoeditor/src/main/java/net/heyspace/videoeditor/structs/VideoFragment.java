package net.heyspace.videoeditor.structs;

import android.media.MediaFormat;

import net.heyspace.videoeditor.utils.MediaUtils;

import java.io.IOException;

public class VideoFragment {

    public VideoFragment(String path) throws IOException {
        this.path = path;
        this.start = 0L;
        MediaFormat mediaFormat = MediaUtils.getMediaFormat(path, "video");
        long duration = mediaFormat.getLong(MediaFormat.KEY_DURATION);
        this.end = duration;
    }

    /**
     * @param path
     * @param start microseconds
     * @throws IOException
     */
    public VideoFragment(String path, long start) throws IOException {
        this.path = path;
        this.start = start;
        MediaFormat mediaFormat = MediaUtils.getMediaFormat(path, "video");
        long duration = mediaFormat.getLong(MediaFormat.KEY_DURATION);
        this.end = duration;
    }

    /**
     * @param path
     * @param start microseconds
     * @param end   microseconds
     */
    public VideoFragment(String path, long start, long end) {
        this.path = path;
        this.start = start;
        this.end = end;
    }

    //路径
    public String path;

    //输入起始位置 microseconds
    public long start;

    //输入截止位置 microseconds
    public long end;
}
