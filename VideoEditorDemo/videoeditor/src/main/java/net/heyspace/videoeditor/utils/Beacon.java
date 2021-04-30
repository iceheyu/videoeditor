package net.heyspace.videoeditor.utils;

public class Beacon {

    public final static long DEVIATION = 75_000L;

    public volatile long audioTimeUs;
    public volatile long videoTimeUs;

}
