package it.andrea.karaokelyricseditor;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class AppConfig {
	
	private final Path path;
    private final Properties props = new Properties();

    public AppConfig(Path path) {
        this.path = path;
        load();
    }

    // Carica il file se esiste, altrimenti crea un file vuoto
    private void load() {
        try {
            if (Files.exists(path)) {
                try (InputStream in = Files.newInputStream(path)) {
                    props.load(in);
                }
            } else {
                save(); // crea file vuoto
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Salva tutte le proprietà
    private void save() {
        try (OutputStream out = Files.newOutputStream(path)) {
            props.store(out, "Karaoke Lyrics Editor configuration");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Lettura generica con default + auto-creazione chiave
    public String get(String key, String defaultValue) {
        String value = props.getProperty(key);

        if (value == null) {
            props.setProperty(key, defaultValue);
            save();
            return defaultValue;
        }

        return value;
    }

    // Scrittura generica (aggiorna o crea)
    public void set(String key, String value) {
        props.setProperty(key, value);
        save();
    }

    // Helper per int
    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(get(key, String.valueOf(defaultValue)));
        } catch (Exception e) {
            set(key, String.valueOf(defaultValue));
            return defaultValue;
        }
    }

    public void setInt(String key, int value) {
        set(key, String.valueOf(value));
    }

}
