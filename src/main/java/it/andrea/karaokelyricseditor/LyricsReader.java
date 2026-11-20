package it.andrea.karaokelyricseditor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LyricsReader {

    public static String readLyrics(String mp3Path) throws IOException {
        byte[] mp3DataBytes = Files.readAllBytes(Paths.get(mp3Path));
        String mp3DataStr = new String(mp3DataBytes, StandardCharsets.ISO_8859_1);

        // Cerca la firma finale "LYRICS200"
        int idxLyrics200 = mp3DataStr.lastIndexOf("LYRICS200");

        if(idxLyrics200 == -1) {
            return null; // nessun testo trovato
        }
        
        // I 6 caratteri prima di LYRICS200 sono la dimensione dell'intero blocco del testo (001234LYRICS200)
        int idxBlockSize = idxLyrics200 - 6; // Calcolo l'indice del primo carattere rappresentante la dimensione del blocco
        if(idxBlockSize < 0) {
        	return null;
        }
        
        // Leggo la dimensione del blocco e la trasformo in intero
        String blockSizeStr = mp3DataStr.substring(idxBlockSize, idxLyrics200);
        int blockSize;
        
        try {
            blockSize = Integer.parseInt(blockSizeStr);
        } catch (NumberFormatException e) {
            return null; // tag corrotto
        }
        
        // Calcola l'inizio del blocco, quindi calcola l'indice da cui partire a leggere il testo
        int idxBlockStart = idxBlockSize - blockSize;
        if(idxBlockStart < 0) {
        	return null;
        }
        
        // Verifica che il blocco di testo inizi con "LYRICSBEGIN"
        String header = mp3DataStr.substring(idxBlockStart, idxBlockStart + "LYRICSBEGIN".length());
        if(!"LYRICSBEGIN".equals(header)) {
            return null; // non è un tag valido
        }

        // Calcolo l'indice da cui partire ad estrarre il contenuto del blocco (dopo LYRICSBEGIN)
        int idxContentStart = idxBlockStart + "LYRICSBEGIN".length();
        
        // Estraggo il contenuto
        String content = mp3DataStr.substring(idxContentStart, idxBlockSize);
        
        // Cerca il campo LYR
        int idxLyr = content.indexOf("LYR");
        if(idxLyr == -1) {
        	return null;
        }
        
        // SIZE è lunga 5 caratteri (LYR00123) (lunghezza del testo)
        String lyrSizeStr = content.substring(idxLyr + 3, idxLyr + 8);
        int lyrSize = Integer.parseInt(lyrSizeStr);
        
        // DATA-Testo inizia dopo LYR (3) + SIZE (5)
        int lyrDataStart = idxLyr + 8;
        int lyrDataEnd = lyrDataStart + lyrSize;

        String lyrRaw = content.substring(lyrDataStart, lyrDataEnd);

        // Converti CRLF reali in newline Java
        lyrRaw = lyrRaw.replace("\r\n", "\n");
        
        return lyrRaw;
    }
}