安卓视频编辑库。支持视频分割、删除片段、添加封面、添加水印、音轨替换等功能。

使用方法

1、添加依赖
allprojects {
    repositories {

        maven {
            url 'https://jitpack.io'
        }
    }
}


dependencies {
	 implementation 'com.github.iceheyu:videoeditor:v1.0'
}

2、使用
EditUtil editUtil = new EditUtil();
editUtil.setOnEditingListener(null);
String videoPath = "/mnt/sdcard/drama.mp4";
String savePath = "/mnt/sdcard/drama1.mp4";
VideoFragment fragment = new VideoFragment(videoPath, 3_000_000, 9_000_000);
try {
    editUtil.slice(fragment, savePath);
} catch (IOException e) {
    e.printStackTrace();
}
