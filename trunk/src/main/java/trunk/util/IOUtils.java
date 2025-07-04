package trunk.util;

import java.io.BufferedReader;
import java.io.IOException;

public final class IOUtils {
    public static String readData(BufferedReader br, int contentLength) throws IOException {
        char[] body = new char[contentLength];
        br.read(body, 0, contentLength);
        return String.copyValueOf(body);
    }
}
