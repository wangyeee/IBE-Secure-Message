package hamaster.gradesign.ibe;

import hamaster.gradesign.ibe.jni.IBENative;

/**
 * 对IBE的Java封装
 * @author <a href="mailto:wangyeee@gmail.com">王烨</a>
 */
public final class IBELibrary {
    public final static int PBC_G_SIZE = 128;
    public final static int PBC_ZR_SIZE = 20;

    private IBELibrary() {
    }

    /**
     * 生成系统参数
     * @param alphaOut 系统主密钥，长度20字节
     * @param gOut 参数g，长度128字节
     * @param g1Out 参数g1，长度128字节
     * @param hOut 参数h，长度128字节
     * @param pairingIn 椭圆曲线参数
     * @return
     */
    public final static int setup(byte[] alphaOut, byte[] gOut, byte[] g1Out, byte[] hOut, byte[] pairingIn) {
        ensureArrayCapacity(alphaOut, PBC_ZR_SIZE);
        ensureArrayCapacity(gOut, PBC_G_SIZE);
        ensureArrayCapacity(g1Out, PBC_G_SIZE);
        ensureArrayCapacity(hOut, PBC_G_SIZE);
        return IBENative.setup_str(alphaOut, gOut, g1Out, hOut, pairingIn);
    }

    /**
     * 为用户生成私钥
     * @param hIDOut 私钥hID参数，长度128字节
     * @param rIDOut 私钥rID参数，长度20字节
     * @param userIn 用户身份，如电子邮件地址
     * @param alphaIn 系统主密钥，长度20字节
     * @param gIn 参数g，长度128字节
     * @param hIn 参数h，长度128字节
     * @param pairingIn 椭圆曲线参数
     * @return
     */
    public final static int keygen(byte[] hIDOut, byte[] rIDOut, byte[] userIn, byte[] alphaIn, byte[] gIn, byte[] hIn, byte[] pairingIn) {
        ensureArrayCapacity(rIDOut, PBC_ZR_SIZE);
        ensureArrayCapacity(hIDOut, PBC_G_SIZE);
        return IBENative.keygen_str(hIDOut, rIDOut, userIn, alphaIn, gIn, hIn, pairingIn, true);
    }

    /**
     * 加密数据
     * @param cipherBufferOut 输出密文，长度384字节，按照uvw顺序排列
     * @param plainIn 明文，长度128字节
     * @param gIn 接收方参数g，长度128字节
     * @param g1In 接收方参数g1，长度128字节
     * @param hIn 接收方参数h，长度128字节
     * @param aliceIn 接收方身份，长度20字节
     * @param pairingIn 椭圆曲线参数
     * @return
     */
    public final static int encrypt(byte[] cipherBufferOut, byte[] plainIn, byte[] gIn, byte[] g1In, byte[] hIn, byte[] aliceIn, byte[] pairingIn) {
        ensureArrayCapacity(cipherBufferOut, 3 * PBC_G_SIZE);
        return IBENative.encrypt_str(cipherBufferOut, plainIn, gIn, g1In, hIn, aliceIn, pairingIn);
    }

    /**
     * 解密数据
     * @param plainBufferOut 输出明文，长度128字节
     * @param cipherIn 输入密文，长度384字节，按照uvw顺序排列
     * @param rIDIn 接收方私钥rID，长度20字节
     * @param hIDIn 接收方私钥hID，长度128字节
     * @param pairingIn 椭圆曲线参数
     * @return
     */
    public final static int decrypt(byte[] plainBufferOut, byte[] cipherIn, byte[] rIDIn, byte[] hIDIn, byte[] pairingIn) {
        ensureArrayCapacity(plainBufferOut, PBC_G_SIZE);
        return IBENative.decrypt_str(plainBufferOut, cipherIn, rIDIn, hIDIn, pairingIn);
    }

    private final static void ensureArrayCapacity(byte[] array, int min) {
        if (array == null)
            throw new InvalidKeySizeException(new StringBuilder("array size must be at least:").append(min).append(",actural:0").toString());
        if (min < 1)
            throw new IllegalArgumentException(new StringBuilder("minimum size must be above zero:").append(min).toString());
        if (array.length < min)
            throw new InvalidKeySizeException(new StringBuilder("array size must be at least:").append(min).append(",actural:").append(array.length).toString());
    }
}
