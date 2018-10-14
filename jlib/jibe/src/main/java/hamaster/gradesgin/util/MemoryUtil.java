package hamaster.gradesgin.util;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

/**
 * 安全清理内存的工具类
 * @author <a href="mailto:wangyeee@gmail.com">Wang Ye</a>
 */
final public class MemoryUtil {

    /**
     * 在新的线程中安全擦除内存块，这个方法在执行后会快速返回<br>
     * 此方法与在新线程中执行immediateSecureBuffers方法等效
     * @param buffers 待擦除的内存块
     */
    public final static void fastSecureBuffers(byte[] ... buffers) {
        new SecureBufferThread(buffers).start();
    }

    /**
     * 立刻安全擦除内存块
     * @param buffers 待擦除的内存块
     */
    public final static void immediateSecureBuffers(byte[] ... buffers) {
        BigInteger i = new BigInteger(Long.toHexString(System.nanoTime()), 16);
        Random random = new SecureRandom(i.toByteArray());
        for (byte[] buffer : buffers) {
            if (buffer != null)
                random.nextBytes(buffer);
        }
    }
}

final class SecureBufferThread extends Thread {

    private byte[][] buffers;

    public SecureBufferThread(byte[] ... buffers) {
        this.buffers = buffers;
    }

    @Override
    public void run() {
        MemoryUtil.immediateSecureBuffers(this.buffers);
    }
}
