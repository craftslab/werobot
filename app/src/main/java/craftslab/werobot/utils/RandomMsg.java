package craftslab.werobot.utils;

import java.util.Random;

public class RandomMsg {
    public static String gb2312(int length) {
        Random random = new Random();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; ++i) {
            // GB2312 for Chinese: High: 0xB0-0xF7, Low: 0xA1-0xFE, Exlude: 0xD7FA-0xD7FE
            int head = 0xB0 + random.nextInt( 0x20);
            int body = 0xA + random.nextInt( 0x6);
            int tail = random.nextInt(0x10);
            int val = (head << 8) | (body << 4) | tail;
            builder.appendCodePoint(val);
        }
        return builder.toString();
    }

    public static String unicode(int length) {
        Random random = new Random();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; ++i) {
            // Unicode for Chinese: 0x4E00-0x9FBF
            builder.appendCodePoint(0x4E00 + random.nextInt(0x51c0));
        }
        return builder.toString();
    }
}
