# 🎵 Karaoke Lyrics Editor  
Editor per Lyrics3v2 con interfaccia JavaFX

Karaoke Lyrics Editor è un’applicazione desktop sviluppata in Java 25 + JavaFX 25, progettata per modificare tag Lyrics3v2 in modo semplice e compatibile con dispositivi hardware e plugin.

Questo README spiega come generare l’eseguibile Windows tramite **jpackage**, utilizzando una JDK che include JavaFX.

---

## 🚀 Requisiti

### Java Development Kit
Per creare l’eseguibile è necessario utilizzare una JDK che **include JavaFX nativamente**.

La scelta consigliata è:

### 👉 **BellSoft Liberica JDK 25 Full (con JavaFX integrato)**  
`https://bell-sw.com/pages/downloads/#/javafx` [(bell-sw.com in Bing)](https://www.bing.com/search?q="https%3A%2F%2Fbell-sw.com%2Fpages%2Fdownloads%2F%23%2Fjavafx")

Questa versione contiene:

- JavaFX Graphics  
- JavaFX Controls  
- JavaFX FXML  
- JavaFX Media  
- Runtime completo  
- Supporto nativo per jpackage  

I JDK standard (OpenJDK, Microsoft, Oracle) **non includono JavaFX**, quindi non possono eseguire o impacchettare correttamente applicazioni JavaFX.

---

## 📦 Build del progetto

Compilare il progetto con Maven:

```bash
mvn clean package
```

Il JAR risultante sarà in:

```
target/karaoke-lyrics-editor-1.0.0-SNAPSHOT.jar
```

---

## 🧰 Creazione dell’eseguibile Windows (EXE)

Eseguire il seguente comando dalla **root del progetto** (dove si trova `pom.xml`):

```bash
"C:\jdk\bellsoft-jdk-25.0.2-full\bin\jpackage.exe" ^
  --type app-image ^
  --name KaraokeLyricsEditor ^
  --input target ^
  --main-jar karaoke-lyrics-editor-1.0.0-SNAPSHOT.jar ^
  --main-class it.andrea.karaokelyricseditor.App ^
  --dest dist ^
  --app-version 1.0.0 ^
  --vendor "Andrea Bevilacqua" ^
  --description "Karaoke Lyrics Editor - Editor per Lyrics3v2" ^
  --copyright "© 2026 Andrea Bevilacqua"
```

### Output
Il comando genera:

```
dist/
  KaraokeLyricsEditor/
    bin/KaraokeLyricsEditor.exe
    app/
    runtime/
```

L’eseguibile è completamente standalone:

- include JavaFX  
- include il runtime Java  
- non richiede installazione di Java sul sistema  
- non apre console  
- contiene le Windows Version Info  

---

## ❓ Perché serve una JDK esterna come BellSoft Liberica Full?

Dal 2019 JavaFX **non è più incluso** nei JDK standard.  
Questo significa che:

- `java` non trova `javafx.application.Application`  
- `jpackage` non può creare un runtime funzionante  
- l’app non parte e genera errori come:  
  ```
  NoClassDefFoundError: javafx/application/Application
  ```

BellSoft Liberica **Full** risolve tutto perché:

- include JavaFX nativamente  
- funziona con progetti **non modulari**  
- non richiede `module-info.java`  
- non richiede `--module-path`  
- non richiede `jlink`  
- permette a jpackage di creare un EXE completo e funzionante  

In pratica:

### 👉 Con un JDK normale → JavaFX non parte  
### 👉 Con BellSoft Full → tutto funziona al primo colpo  

---

## 📄 Licenza

© 2026 Andrea Bevilacqua  
Tutti i diritti riservati.
"# karaoke-lyrics-editor" 
