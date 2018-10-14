package hamaster.gradesgin.util;

import hamaster.gradesgin.ibe.IBEConstraints;

import java.io.Serializable;

/**
 * 定义将数据使用密码加密后存储的接口
 * @author <a href="mailto:wangyeee@gmail.com">Wang Ye</a>
 */
public interface IBECapsule extends Serializable, IBEConstraints, AutoCloseable {

    /**
     * 设置加密数据
     * @param data 要加密的数据
     */
    void protect(byte[] data);

    /**
     * 把一个实现接口的类加密存储
     * @param object 要存储的类
     */
    void protect(Serializable object);

    /**
     * @return 原始数据
     */
    byte[] getData();

    /**
     * @return 原始类
     */
    Object getDataAsObject() throws ClassNotFoundException;

    /**
     * 设置加密密钥
     * @param key 密钥
     */
    void setKey(byte[] key);

    /**
     * @return 哈希算法名称
     */
    String getHashAlgorithm();

    /**
     * @return 加密算法名称
     */
    String getCrypto();

    /*
     * (non-Javadoc)
     * @see java.lang.AutoCloseable#close()
     */
    void close();
}
