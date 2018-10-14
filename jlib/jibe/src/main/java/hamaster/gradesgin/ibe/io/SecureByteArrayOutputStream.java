package hamaster.gradesgin.ibe.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * 安全字节数组输出流 完成操作后会擦出内存中分配空间的数据
 * @author Wang Ye
 */
public class SecureByteArrayOutputStream extends ByteArrayOutputStream {

    /**
     * 新建一个安全输出流 容量32字节并自动增长
     */
    public SecureByteArrayOutputStream() {
        super();
    }

    /**
     * 新建一个特定容量的安全输出流
     * @param size 初始容量
     */
    public SecureByteArrayOutputStream(int size) {
        super(size);
    }

    /**
     * 重置输出流并擦出已经输出的数据
     * @see java.io.ByteArrayOutputStream#reset()
     */
    @Override
    public synchronized void reset() {
        super.reset();
        Arrays.fill(buf, (byte) 0);
    }

    /**
     * 擦出已经输出的数据并关闭输出流
     * @throws IOException 擦出过程发生的任何异常都将被抛出
     * @see java.io.ByteArrayOutputStream#close()
     */
    @Override
    public void close() throws IOException {
        try {
            reset();
        } catch (Throwable t) {
            throw new IOException(t);
        }
        super.close();
    }
}
