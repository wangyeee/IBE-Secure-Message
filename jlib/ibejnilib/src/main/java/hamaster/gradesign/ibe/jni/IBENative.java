package hamaster.gradesign.ibe.jni;

/**
 * 底层的JNI接口类，提供对本地IBE库的访问
 * @author <a href="mailto:wangyeee@gmail.com">王烨</a>
 */
public final class IBENative {

    final private static String LIB_NAME = "ibejni";

    static {
        System.loadLibrary(LIB_NAME);
    }

    /**
     * 生成系统参数
     * @param alpha_out 系统主密钥，长度20字节
     * @param g_out 参数g，长度128字节
     * @param g1_out 参数g1，长度128字节
     * @param h_out 参数h，长度128字节
     * @param pairing_str_in 椭圆曲线参数
     * @return
     */
    public static native int setup_str(byte[] alpha_out, byte[] g_out, byte[] g1_out, byte[] h_out, byte[] pairing_str_in);

    /**
     * 为用户生成私钥
     * @param hID_out 私钥hID参数，长度128字节
     * @param rID_out 私钥rID参数，长度20字节
     * @param user_in 用户身份，如电子邮件地址
     * @param alpha_in 系统主密钥，长度20字节
     * @param g_in 参数g，长度128字节
     * @param h_in 参数h，长度128字节
     * @param pairing_str_in 椭圆曲线参数
     * @param random_rID 是否随即生成rID
     * @return
     */
    public static native int keygen_str(byte[] hID_out, byte[] rID_out, byte[] user_in, byte[] alpha_in, byte[] g_in, byte[] h_in, byte[] pairing_str_in,
                                        boolean random_rID);

    /**
     * 加密数据
     * @param cipher_buffer_out 输出密文，长度384字节，按照uvw顺序排列
     * @param plain_in 明文，长度128字节
     * @param g_in 接收方参数g，长度128字节
     * @param g1_in 接收方参数g1，长度128字节
     * @param h_in 接收方参数h，长度128字节
     * @param alice_in 接收方身份，长度20字节
     * @param pairing_str_in 椭圆曲线参数
     * @return
     */
    public static native int encrypt_str(byte[] cipher_buffer_out, byte[] plain_in, byte[] g_in, byte[] g1_in, byte[] h_in, byte[] alice_in,
                                         byte[] pairing_str_in);

    /**
     * 解密数据
     * @param plain_buffer_out 输出明文，长度128字节
     * @param cipher_in 输入密文，长度384字节，按照uvw顺序排列
     * @param rID_in 接收方私钥rID，长度20字节
     * @param hID_in 接收方私钥hID，长度128字节
     * @param pairing_str_in 椭圆曲线参数
     * @return
     */
    public static native int decrypt_str(byte[] plain_buffer_out, byte[] cipher_in, byte[] rID_in, byte[] hID_in, byte[] pairing_str_in);

    private IBENative() {
    }
}
