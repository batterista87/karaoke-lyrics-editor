package it.andrea.karaokelyricseditor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class LyricsWriter {

    private static final Charset CHARSET = StandardCharsets.ISO_8859_1;
    private static final String LYRICS_BEGIN = "LYRICSBEGIN";
    private static final String LYRICS_END   = "LYRICS200";
    private static final String TAG_ID3V1    = "TAG";

    public static void writeLyrics(String inputMp3Path, String outputMp3Path, String text) throws IOException {

        // Normalizza accapi
        String normalized = text == null ? "" :
                text.replace("\r\n", "\n")
                    .replace("\r", "\n");

        boolean isEmptyText = normalized.isBlank();

        // Leggi MP3 originale
        byte[] original = Files.readAllBytes(Paths.get(inputMp3Path));
        String full = new String(original, CHARSET);

        int length = original.length;

        // 1. Trova eventuale TAG (ID3v1)
        int tagStart = length;
        if (length >= 128) {
            String maybeTag = new String(original, length - 128, 3, CHARSET);
            if (TAG_ID3V1.equals(maybeTag)) {
                tagStart = length - 128;
            }
        }
        boolean hadTag = (tagStart != length);

        // 2. Trova eventuale Lyrics3v2
        int audioEnd = tagStart;
        int idxLyricsEnd = full.lastIndexOf(LYRICS_END, tagStart);
        if (idxLyricsEnd != -1) {
            int sizeStart = idxLyricsEnd - 6;
            if (sizeStart >= 0) {
                String sizeStr = full.substring(sizeStart, idxLyricsEnd);
                try {
                    int blockSize = Integer.parseInt(sizeStr);
                    int blockStart = sizeStart - blockSize;
                    if (blockStart >= 0 && full.startsWith(LYRICS_BEGIN, blockStart)) {
                        audioEnd = blockStart;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        if (audioEnd == tagStart && tagStart == length) {
            audioEnd = length;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // 3. Scrivi audio puro
        out.write(original, 0, audioEnd);

        // 4. Se testo vuoto → niente Lyrics
        if (isEmptyText) {
            if (hadTag) {
                out.write(original, tagStart, 128);
            }
            Files.write(Paths.get(outputMp3Path), out.toByteArray());
            return;
        }

        // 5. Costruisci campo LYR
        String[] parts = normalized.split("\n", -1);
        StringBuilder lyrData = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            lyrData.append(parts[i]);
            if (i < parts.length - 1) {
                lyrData.append("\r\n");
            }
        }
        byte[] lyrBytes = lyrData.toString().getBytes(CHARSET);

        // 6. Scrivi blocco Lyrics3v2
        out.write(LYRICS_BEGIN.getBytes(CHARSET));

        // Campo IND
        String flagLyricsPresent = "1";
        boolean hasTimestamps = normalized.matches("(?s).*\\[\\d{2}:\\d{2}].*");
        String flagSynchronized = hasTimestamps ? "1" : "0";

        String indData = flagLyricsPresent + flagSynchronized;
        String indSize = String.format("%05d", indData.length());

        out.write("IND".getBytes(CHARSET));
        out.write(indSize.getBytes(CHARSET));
        out.write(indData.getBytes(CHARSET));

        // Campo LYR
        String lyrSize = String.format("%05d", lyrBytes.length);

        out.write("LYR".getBytes(CHARSET));
        out.write(lyrSize.getBytes(CHARSET));
        out.write(lyrBytes);

        // blockSize
        int blockSize =
                LYRICS_BEGIN.length() +
                (3 + 5 + indData.length()) +
                (3 + 5 + lyrBytes.length);

        String blockSizeStr = String.format("%06d", blockSize);

        out.write(blockSizeStr.getBytes(CHARSET));
        out.write(LYRICS_END.getBytes(CHARSET));

        // 7. TAG
        if (hadTag && (length - tagStart) >= 128) {
            out.write(original, tagStart, 128);
        } else {
            byte[] emptyTag = new byte[128];
            emptyTag[0] = 'T';
            emptyTag[1] = 'A';
            emptyTag[2] = 'G';
            out.write(emptyTag);
        }

        // 8. Salva nel percorso richiesto
        Files.write(Paths.get(outputMp3Path), out.toByteArray());
    }
}