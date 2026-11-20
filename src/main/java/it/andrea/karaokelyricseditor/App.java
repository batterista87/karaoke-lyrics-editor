package it.andrea.karaokelyricseditor;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    // File di configurazione accanto al JAR
    public static final Path CONFIG_PATH = Paths.get("config.properties");
    
    private final AppConfig appConfig = new AppConfig(App.CONFIG_PATH);

    @Override
    public void start(Stage stage) throws Exception {
    	
        // Lingua di sistema
        String systemLang = Locale.getDefault().getLanguage(); // "it", "en", ecc.
        
        // Lingua dell'applicazione: se non esiste, la imposto con la lingua di sistema
        String appLang = appConfig.get("language", systemLang);
        
        // Provo a caricare i messaggi con la lingua salvata nel file di configurazione
        String langToUse;
        try {
            ResourceBundle.getBundle("i18n.messages", Locale.of(appLang));
            langToUse = appLang;
        }
        catch (Exception e) {
            langToUse = "en";
            appConfig.set("language", "en");
        }

        // 2. Carica il bundle
        Locale locale = Locale.of(langToUse);
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", locale);

        // 3. Carica l'interfaccia
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"), bundle);
        Parent root = loader.load();

        Scene scene = new Scene(root);
        stage.setScene(scene);

        stage.setTitle(bundle.getString("app.title"));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}