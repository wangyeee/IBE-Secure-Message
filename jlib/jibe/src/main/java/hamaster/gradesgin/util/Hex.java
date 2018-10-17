package hamaster.gradesgin.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 十六进制编码工具类
 * @author <a href="mailto:wangyeee@gmail.com">王烨</a>
 */
public final class Hex {

    private Hex() {
    }

    /**
     * 使用十六进制编码数据
     * @param data 要编码的数据
     * @return 编码后的字符串
     */
    public final static String hex(byte[] data) {
        return hex(data, 0, data.length);
    }

    /**
     * 使用十六进制编码数据
     * @param data 要编码的数据
     * @param offset 其实偏移 0开始
     * @param count 编码字节的数量
     * @return 编码后的字符串
     */
    public final static String hex(byte[] data, int offset, int count) {
        if (offset < 0)
            throw new IllegalArgumentException(new StringBuilder("invalid offset:").append(offset).toString());
        if (count < 0)
            throw new IllegalArgumentException(new StringBuilder("invalid count:").append(offset).toString());
        if (offset > data.length - count)
            throw new ArrayIndexOutOfBoundsException(offset + count);
        ByteArrayOutputStream out = new ByteArrayOutputStream(data.length + data.length);
        PrintStream writer = new PrintStream(out);
        for (int i = offset; i < offset + count; i++) {
            writer.printf("%02x", data[i]);
        }
        return out.toString();
    }

    /**
     * 将十六进制编码的数据还原
     * @param hex 要还原的字符串
     * @return 还原后的数据
     * @throws NumberFormatException 字符串中包含无法格式化为数字的字符
     */
    public final static byte[] unhex(String hex) {
        char[] tb = hex.toUpperCase().toCharArray();
        List<Byte> bs = new ArrayList<Byte>();
        for (int i = 0; i < tb.length; i += 2) {
            String t = new String();
            t += tb[i];
            t += tb[i + 1];
            int safe = Integer.parseInt(t, 16);
            if (safe > 127)
                safe -= 256;
            bs.add(Byte.parseByte(String.valueOf(safe)));
        }
        byte[] raw = new byte[bs.size()];
        for (int i = 0; i < bs.size(); i++)
            raw[i] = bs.get(i);
        return raw;
    }

    /**
     * 将64位的long转换成8个字节的数组
     * @param l 要转换的long
     * @return 转换后的数组
     */
    public final static byte[] longToBytes(long l) {
        byte[] buffer = new byte[8];
        buffer[0] = (byte) (0xff & (l >> 56));
        buffer[1] = (byte) (0xff & (l >> 48));
        buffer[2] = (byte) (0xff & (l >> 40));
        buffer[3] = (byte) (0xff & (l >> 32));
        buffer[4] = (byte) (0xff & (l >> 24));
        buffer[5] = (byte) (0xff & (l >> 16));
        buffer[6] = (byte) (0xff & (l >> 8));
        buffer[7] = (byte) (0xff & l);
        return buffer;
    }

    /**
     * 将8个字节组装成一个long
     * @param bytes 要转换的字节数组
     * @return 组装后的long
     */
    public final static long bytesToLong(byte[] bytes) {
        return bytesToLong(bytes, 0);
    }

    /**
     * 将8个字节组装成一个long
     * @param bytes 要转换的字节数组
     * @param offset 数组中开始读取的偏移量
     * @return 组装后的long
     */
    public final static long bytesToLong(byte[] bytes, int offset) {
        if (bytes == null || offset + 8 > bytes.length)
            throw new IllegalArgumentException("invalid byte array to convert!");
        return (((long) (bytes[0 + offset] & 0xff) << 56) |
                ((long) (bytes[1 + offset] & 0xff) << 48) |
                ((long) (bytes[2 + offset] & 0xff) << 40) |
                ((long) (bytes[3 + offset] & 0xff) << 32) |
                ((long) (bytes[4 + offset] & 0xff) << 24) |
                ((long) (bytes[5 + offset] & 0xff) << 16) |
                ((long) (bytes[6 + offset] & 0xff) <<  8) |
                ((long) (bytes[7 + offset] & 0xff)));
    }

    /**
     * 将32bit的int转换为字节数组
     * @param i 要转换的int
     * @return 转换完成的数组 长度4字节
     */
    public final static byte[] intToByte(int i) {
        byte[] bt = new byte[4];
        bt[0] = (byte) ((0xff000000 & i) >> 24);
        bt[1] = (byte) ((0xff0000 & i) >> 16);
        bt[2] = (byte) ((0xff00 & i) >> 8);
        bt[3] = (byte) (0xff & i);
        return bt;
    }

    /**
     * 将4字节的数组转换成int
     * @param bytes 要转换的字节数组
     * @return 转换后的int
     */
    public final static int bytesToInt(byte[] bytes) {
        return bytesToInt(bytes,0);
    }

    /**
     * 将数组中的部分数据转换为int
     * @param bytes 要转换的数组
     * @param offset 起始偏移量 从0开始
     * @return 读取4个字节的内容 将它们转换为32bit的int并返回
     */
    public final static int bytesToInt(byte[] bytes, int offset) {
        if (bytes == null || offset + 4 > bytes.length)
            throw new IllegalArgumentException("invalid byte array to convert!");
        return (((bytes[0 + offset] & 0xff) << 24) |
                ((bytes[1 + offset] & 0xff) << 16) |
                ((bytes[2 + offset] & 0xff) <<  8) |
                (bytes[3 + offset] & 0xff));
    }
}
