package net.heyspace.videoeditor;

public interface OnEditingListener {

    void onEncodingEnd(int id);

    void progress(float value, long timeMs);
}
