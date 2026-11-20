package it.andrea.karaokelyricseditor;

import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class AppUtils {
	
    public static String formatTime(Duration d) {
        int seconds = (int) d.toSeconds();
        int m = seconds / 60;
        int s = seconds % 60;
        return String.format("%02d:%02d", m, s);
    }
    
    public static String getCurrentTimestamp(MediaPlayer mediaPlayer) {
        if (mediaPlayer == null) return "[00:00]";

        double seconds = mediaPlayer.getCurrentTime().toSeconds();
        int m = (int) seconds / 60;
        int s = (int) seconds % 60;

        return String.format("[%02d:%02d]", m, s);
    }

}
