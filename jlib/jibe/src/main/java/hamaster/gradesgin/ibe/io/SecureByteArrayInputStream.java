package hamaster.gradesgin.ibe.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * 安全的字节数组输入流 操作完成后可以清理内存
 * @author Wang Ye
 */
public class SecureByteArrayInputStream extends ByteArrayInputStream {

    /**
     * 将buf中内容复制并作为输入源
     * @param buf 要处理的数组
     */
    public SecureByteArrayInputStream(byte[] buf) {
        super(new byte[1]);
        super.buf = new byte[buf.length];
        System.arraycopy(buf, 0, super.buf, 0, buf.length);
        pos = 0;
        count = buf.length;
    }

    /**
     * 将buf中内容复制并作为输入源
     * @param buf 要处理的数组
     * @param offset 开始读取的字节偏移量
     * @param length 最多读取的长度
     */
    public SecureByteArrayInputStream(byte[] buf, int offset, int length) {
        super(new byte[1], offset, length);
        super.buf = new byte[buf.length];
        System.arraycopy(buf, 0, super.buf, 0, buf.length);
        pos = offset;
        count = Math.min(offset + length, buf.length);
        mark = offset;
    }

    /**
     * 擦出内存中数据并关闭输入流
     * @throws IOException 清理内存中发生的任意异常都会被抛出
     * @see java.io.ByteArrayInputStream#close()
     */
    @Override
    public void close() throws IOException {
        try {
            Arrays.fill(buf, (byte) 0);
        } catch (Throwable t) {
            throw new IOException(t);
        }
    }
}
