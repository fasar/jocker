package it.dockins.jocker.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 * Implement https://en.wikipedia.org/wiki/Chunked_transfer_encoding
 */
public class ChunkedInputStream extends InputStream {

    private int next = 0;
    private boolean eof = false;
    private final InputStream chunked;
    private boolean first = true;

    public ChunkedInputStream(InputStream chunked) {
        this.chunked = chunked;
    }

    public boolean isEof() {
        return eof;
    }

    @Override
    public int read() throws IOException {
        if (readInteral() < 0) return -1;
        next--;
        return chunked.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (readInteral() < 0) return -1;
        len = Math.min(next, len);
        byte[] buff = new byte[len];
        final int read = chunked.read(buff, off, len);
        System.arraycopy(buff, 0, b, 0, read);
        next -= read;
        return read;
    }

    private int readInteral() throws IOException {
        if (eof) return -1;
        if (next > 0) return next;
        if (first) first = false;
        else readLine(); // CTRLF at the end of previous chunk

        final String chunk = readLine();
        next = Integer.parseInt(chunk, 16);
        if (next == 0 /* EOF */) {
            eof = true;
            readLine(); // last CTRLF
            return -1;
        }
        return next;
    }

    private String readLine() throws IOException {
        StringBuilder s = new StringBuilder();
        char c;
        while((c = (char) chunked.read()) != '\r') {
            s.append(c);
        }
        chunked.read(); // '\n'
        return s.toString();
    }
}
