package it.andrea.karaokelyricseditor;

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

public class MainController {
	
	private final AppConfig appConfig = new AppConfig(App.CONFIG_PATH);
	
	@FXML
	private ResourceBundle resources;
	
	@FXML private MenuItem mnuSave;
	@FXML private MenuItem mnuSaveAs;
	@FXML private MenuItem mnuRemoveTimestamps;
	@FXML private MenuItem mnuLyricsUppercase;
	
	@FXML private Button btnSave;
	@FXML private Button btnSaveAs;
	@FXML private Button btnAddTimestamp;
	
	@FXML private ComboBox<String> fontSizeCombo;
	
    @FXML private Label mp3Label;
	
    @FXML private TextArea lyricsArea;
	
	@FXML private Button btnPlay;
	@FXML private Button btnStop;
    @FXML private Slider progressSlider;
    @FXML private Label timeLabel;
    @FXML private Slider speedSlider;
	
    @FXML private Label statusLabel;

    private File currentMp3;
    private MediaPlayer mediaPlayer;
    private boolean sliderBeingDragged = false;
    private boolean blockTyping = false;

    @FXML
    public void initialize() {

        // Disabilita i pulsanti
        mnuSave.setDisable(true);
        mnuSaveAs.setDisable(true);
        mnuRemoveTimestamps.setDisable(true);
        mnuLyricsUppercase.setDisable(true);
        btnSave.setDisable(true);
        btnSaveAs.setDisable(true);
        btnAddTimestamp.setDisable(true);
        lyricsArea.setDisable(true);
        btnPlay.setDisable(true);
        btnStop.setDisable(true);
        progressSlider.setDisable(true);
        speedSlider.setDisable(true);

        // Blocca caratteri digitati (lettere, numeri, simboli)
        lyricsArea.addEventFilter(javafx.scene.input.KeyEvent.KEY_TYPED, event -> {
            if (blockTyping) {
                event.consume();
            }
        });

        // Blocca tasti speciali e combinazioni CTRL che modificano il testo
        lyricsArea.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            if (blockTyping) {

                switch (event.getCode()) {

                    // Tasti che modificano il testo
                    case BACK_SPACE:
                    case DELETE:
                    case ENTER:
                    case TAB:
                    case INSERT:
                        event.consume();
                        return;

                    // Combinazioni CTRL che modificano il testo
                    default:
                        if (event.isControlDown()) {
                            switch (event.getCode()) {
                                case V: // incolla
                                case X: // taglia
                                case Z: // undo
                                case Y: // redo
                                    event.consume();
                                    return;
                                default:
                                    break;
                            }
                        }
                }
            }
        });
        
        // Carica dimensione font dal config (crea la chiave se manca)
        int savedFontSize = appConfig.getInt("font.size", 12);

        // Imposta font area
        lyricsArea.setFont(Font.font(savedFontSize));

        // Popola la combo font size
        fontSizeCombo.getItems().addAll("12", "14", "16", "18", "20", "22", "24");
        fontSizeCombo.getSelectionModel().select(String.valueOf(savedFontSize));

        // Listener cambio dimensione font
        fontSizeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int size = Integer.parseInt(newVal);
                lyricsArea.setFont(Font.font(size));
                appConfig.setInt("font.size", size);
            }
        });
        
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setRate(newVal.doubleValue());
            }
        });
    }

    @FXML
    private void onLoadMp3() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(resources.getString("menu.file.open.choose"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP3 Files", "*.mp3"));
        Window window = lyricsArea.getScene().getWindow();
        File file = chooser.showOpenDialog(window);

        if (file != null) {
        	
        	mnuSave.setDisable(false);
        	mnuSaveAs.setDisable(false);
        	mnuRemoveTimestamps.setDisable(false);
        	mnuLyricsUppercase.setDisable(false);
        	btnSave.setDisable(false);
        	btnSaveAs.setDisable(false);
        	btnAddTimestamp.setDisable(false);
        	lyricsArea.setDisable(false);
        	btnPlay.setDisable(false);
        	btnStop.setDisable(false);
        	progressSlider.setDisable(false);
        	speedSlider.setDisable(false);
        	
            currentMp3 = file;
            mp3Label.setText(file.getAbsolutePath());
            statusLabel.setText("Caricato: " + file.getAbsolutePath());

            // Carica testo
            try {
                String lyrics = LyricsReader.readLyrics(file.getAbsolutePath());
                if (lyrics != null) {
                    lyricsArea.setText(lyrics);
                    statusLabel.setText(resources.getString("statusLabel.lyricsFound"));
                } else {
                    lyricsArea.clear();
                    statusLabel.setText(resources.getString("statusLabel.lyricsNotFound"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                statusLabel.setText(resources.getString("statusLabel.errorReadingLyrics") + " " + e.getMessage());
            }

            // Carica player
            loadPlayer(file);
        }
    }

    private void loadPlayer(File file) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.dispose();
            }

            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            // Aggiorna slider durante la riproduzione
            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (!sliderBeingDragged) {
                    progressSlider.setValue(newTime.toSeconds());
                }
                timeLabel.setText(AppUtils.formatTime(newTime));
            });

            // Imposta durata quando disponibile
            mediaPlayer.setOnReady(() -> {
                double total = mediaPlayer.getMedia().getDuration().toSeconds();
                progressSlider.setMax(total);
                timeLabel.setText("00:00");
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                blockTyping = false;
            });

            // Permetti di spostare lo slider
            progressSlider.setOnMousePressed(e -> sliderBeingDragged = true);
            progressSlider.setOnMouseReleased(e -> {
                sliderBeingDragged = false;
                mediaPlayer.seek(Duration.seconds(progressSlider.getValue()));
            });

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText(resources.getString("statusLabel.errorPlayer") + " " + e.getMessage());
        }
    }

    @FXML
    private void onPlay() {
        if (mediaPlayer != null) {
            blockTyping = true; // blocca la tastiera
            statusLabel.setText(resources.getString("statusLabel.playerPlaying"));
            mediaPlayer.setRate(speedSlider.valueProperty().get());
            mediaPlayer.play();
        }
    }

    @FXML
    private void onStop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            blockTyping = false; // riabilita la tastiera
            statusLabel.setText(resources.getString("statusLabel.playerStop"));
        }
    }

    @FXML
    private void onAddTimestamp() {
    	
        String text = lyricsArea.getText();
        int caret = lyricsArea.getCaretPosition();

        // Trova inizio riga
        int lineStart = text.lastIndexOf('\n', Math.max(0, caret - 1));
        if (lineStart == -1) lineStart = 0; else lineStart += 1;

        // Trova fine riga
        int lineEnd = text.indexOf('\n', lineStart);
        if (lineEnd == -1) lineEnd = text.length();

        String line = text.substring(lineStart, lineEnd).trim();

        // Timestamp da inserire
        String timestamp;

        if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            timestamp = AppUtils.getCurrentTimestamp(mediaPlayer);
        } else {
            timestamp = "[00:00]";
        }

        // Se la riga ha già un timestamp → sostituiscilo
        String newLine;
        if (line.matches("^\\[\\d{2}:\\d{2}].*")) {
            newLine = timestamp + line.substring(7);
        } else {
            newLine = timestamp + line;
        }

        // Ricostruisci il testo
        StringBuilder sb = new StringBuilder(text);
        sb.replace(lineStart, lineEnd, newLine);

        lyricsArea.setText(sb.toString());
        
        // Trova la prima riga successiva non vuota
        int nextLineStart = sb.indexOf("\n", lineStart);
        if (nextLineStart == -1) {
            lyricsArea.positionCaret(sb.length());
        } else {
            boolean caretPositioned = false;
            while (nextLineStart < sb.length()) {
                nextLineStart++;
                int nextLineEnd = sb.indexOf("\n", nextLineStart);
                if (nextLineEnd == -1) {
                    nextLineEnd = sb.length();
                }

                if (!sb.substring(nextLineStart, nextLineEnd).isBlank()) {
                    lyricsArea.positionCaret(nextLineEnd);
                    caretPositioned = true;
                    break;
                }

                if (nextLineEnd == sb.length()) {
                    break;
                }
                nextLineStart = nextLineEnd;
            }

            if (!caretPositioned) {
                lyricsArea.positionCaret(sb.length());
            }
        }

        lyricsArea.requestFocus();
    }

    @FXML
    private void onSave() {
        String text = lyricsArea.getText();
        
        if (text == null) {
            text = "";
        }

        try {
            LyricsWriter.writeLyrics(currentMp3.getAbsolutePath(), currentMp3.getAbsolutePath(), text);
            statusLabel.setText(resources.getString("statusLabel.saved") + " " + currentMp3.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText(resources.getString("statusLabel.errorSaving") + " " + e.getMessage());
        }
    }
    
    @FXML
    private void onSaveAs() {
        String text = lyricsArea.getText();
        
        if (text == null) {
            text = "";
        }

        // FileChooser per scegliere il nome del file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(resources.getString("menu.file.saveAs.choose"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("MP3 Files", "*.mp3")
        );
        
        // Se esiste un file MP3 caricato, posizionati nella sua cartella
        if (currentMp3 != null && currentMp3.exists()) {
            File parent = currentMp3.getParentFile();
            if (parent != null && parent.exists()) {
                fileChooser.setInitialDirectory(parent);
            }
        }

        // Suggerimento nome file
        String suggestedName = currentMp3.getName().replace(".mp3", "_lyrics.mp3");
        fileChooser.setInitialFileName(suggestedName);

        File chosenFile = fileChooser.showSaveDialog(statusLabel.getScene().getWindow());
        if (chosenFile == null) {
            statusLabel.setText(resources.getString("statusLabel.abortSave"));
            return;
        }

        try {
            LyricsWriter.writeLyrics(currentMp3.getAbsolutePath(), chosenFile.getAbsolutePath(), text);
            currentMp3 = chosenFile;
            mp3Label.setText(chosenFile.getAbsolutePath());
            statusLabel.setText(resources.getString("statusLabel.savedAs") + " " + chosenFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText(resources.getString("statusLabel.errorSaving") + " " + e.getMessage());
        }
    }
    
    @FXML
    private void onRemoveAllTimestamps() {

        String text = lyricsArea.getText();
        if (text == null || text.isEmpty()) {
            return;
        }

        // Rimuove tutti i timestamp [mm:ss]
        String cleaned = text.replaceAll("\\[\\d{2}:\\d{2}]", "");

        // Rimuove eventuali doppi spazi lasciati dai timestamp
        cleaned = cleaned.replaceAll(" {2,}", " ");

        // Rimuove spazi all'inizio riga
        cleaned = cleaned.replaceAll("(?m)^ +", "");

        lyricsArea.setText(cleaned);
        statusLabel.setText(resources.getString("statusLabel.removeTimestamps"));
    }
    
    @FXML
    private void onLanguageClick(ActionEvent event) {
        MenuItem item = (MenuItem) event.getSource();
        String lang = item.getId(); // "it" o "en"
        switchLanguage(lang);
    }
    
    @FXML
    private void onLyricsUppercase(ActionEvent event) {
        if (lyricsArea.getText() == null || lyricsArea.getText().isEmpty()) {
            return;
        }
        lyricsArea.setText(lyricsArea.getText().toUpperCase());
        statusLabel.setText(resources.getString("statusLabel.lyricsUppercase"));
    }

    /**
     * Cambia lingua, salva la scelta e ricarica l'interfaccia
     */
    private void switchLanguage(String lang) {
        try {
            if (mediaPlayer != null) {
                onStop();
                mediaPlayer.dispose();
                mediaPlayer = null;
            }
            blockTyping = false;
            sliderBeingDragged = false;

        	appConfig.set("language", lang);
        	
            Locale locale = Locale.of(lang);
            ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", locale);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"), bundle);
            Parent root = loader.load();

            Stage stage = (Stage) lyricsArea.getScene().getWindow();
            stage.getScene().setRoot(root);

            stage.sizeToScene();

        } catch (Exception e) {
            e.printStackTrace();
            if (statusLabel != null) {
                statusLabel.setText(resources.getString("statusLabel.errorLanguageChange") + " " + e.getMessage());
            }
        }
    }
    
}