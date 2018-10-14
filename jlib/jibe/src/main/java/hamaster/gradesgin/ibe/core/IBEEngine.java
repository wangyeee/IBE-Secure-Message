package hamaster.gradesgin.ibe.core;

import hamaster.gradesgin.ibe.IBECipherText;
import hamaster.gradesgin.ibe.IBEConstraints;
import hamaster.gradesgin.ibe.IBEPlainText;
import hamaster.gradesgin.ibe.IBEPrivateKey;
import hamaster.gradesgin.ibe.IBEPublicParameter;
import hamaster.gradesgin.ibe.IBESystemParameter;
import hamaster.gradesgin.ibs.IBSCertificate;
import hamaster.gradesgin.ibs.IBSSignature;
import hamaster.gradesgin.util.Hash;
import hamaster.gradesign.ibe.IBELibrary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

/**
 * 基于身份加密(IBE)的核心库，提供相关的方法
 * Core utils of IBE
 * @author <a href="mailto:wangyeee@gmail.com">Wang Ye</a>
 */
public class IBEEngine {

    /**
     * 初始化一个IBE系统
     * Setup an IBE system
     * @param pairingIn 椭圆函数参数 the pairing
     * @return 系统参数 如果椭圆函数参数非法或者出现其它错误返回null
     * returns the IBESystemParameter object when success, otherwise return null
     */
    public static IBESystemParameter setup(byte[] pairingIn) {
        byte[] alphaOut = new byte[IBELibrary.PBC_ZR_SIZE];
        byte[] gOut = new byte[IBELibrary.PBC_G_SIZE];
        byte[] g1Out = new byte[IBELibrary.PBC_G_SIZE];
        byte[] hOut = new byte[IBELibrary.PBC_G_SIZE];
        int succ = IBELibrary.setup(alphaOut, gOut, g1Out, hOut, pairingIn);
        if (succ == 0) {
            IBEPublicParameter publicParameter = new IBEPublicParameter();
            publicParameter.setParamG(gOut);
            publicParameter.setParamG1(g1Out);
            publicParameter.setParamH(hOut);
            publicParameter.setPairing(pairingIn);
            IBESystemParameter parameter = new IBESystemParameter();
            parameter.setMasterKey(alphaOut);
            parameter.setPublicParameter(publicParameter);
            return parameter;
        }
        return null;
    }

    /**
     * 为一个用户生成私钥
     * Generate a private key for a user
     * @param system IBE系统 the system parameter
     * @param user 私钥请求用户 the user
     * @return 用户的私钥 the generated private key
     */
    public static IBEPrivateKey keygen(IBESystemParameter system, String user) {
        byte[] hIDOut = new byte[IBELibrary.PBC_G_SIZE];
        byte[] rIDOut = new byte[IBELibrary.PBC_ZR_SIZE];
        try {
            int succ = IBELibrary.keygen(hIDOut, rIDOut, user.getBytes(IBEConstraints.USER_STRING_ENCODING), system.getMasterKey(),
                                         system.getPublicParameter().getParamG(), system.getPublicParameter().getParamH(), system.getPublicParameter().getPairing());
            if (succ != 0)
                throw new IOException("Cannot generate private key for user:" + user);
            IBEPrivateKey privateKey = new IBEPrivateKey();
            privateKey.sethID(hIDOut);
            privateKey.setrID(rIDOut);
            privateKey.setUserString(user);
            privateKey.setPairing(system.getPublicParameter().getPairing());
            return privateKey;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 对数据进行加密
     * encryption
     * @param publicParameter 接收方公共参数 the recipients' public key
     * @param plainText 明文 the plain text
     * @param receiver 接收着身份 the recipient
     * @return 密文 cipher text
     */
    public static IBECipherText encrypt(IBEPublicParameter publicParameter, IBEPlainText plainText, String receiver) {
        byte[] cipherBufferOut = new byte[IBELibrary.PBC_G_SIZE * 3];
        try {
            int succ = IBELibrary.encrypt(cipherBufferOut, plainText.getContent(), publicParameter.getParamG(), publicParameter.getParamG1(),
                                          publicParameter.getParamH(), receiver.getBytes(IBEConstraints.USER_STRING_ENCODING), publicParameter.getPairing());
            if (succ != 0)
                throw new IOException("Cannot encrypt messege for user:" + receiver);
            IBECipherText cipherText = new IBECipherText();
            cipherText.setUvw(cipherBufferOut);
            cipherText.setLength(plainText.getLength());
            return cipherText;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 对密文解密
     * decryption
     * @param cipherText 密文
     * @param privateKey 接收方私钥
     * @return 解密后明文 plain text
     */
    public static IBEPlainText decrypt(IBECipherText cipherText, IBEPrivateKey privateKey) {
        byte[] plainBufferOut = new byte[IBELibrary.PBC_G_SIZE];
        int i = IBELibrary.decrypt(plainBufferOut, cipherText.getUvw(), privateKey.getrID(), privateKey.gethID(), privateKey.getPairing());
        if (i == 0) {
            IBEPlainText plainText = new IBEPlainText() {
                private static final long serialVersionUID = -2705082103669151761L;
            };
            plainText.setContent(plainBufferOut);
            plainText.setLength(cipherText.getLength());
            return plainText;
        }
        return null;
    }

    /**
     * 生成证书
     * Generate certificate
     * @param user 证书所有者 certificate owner
     * @param root 签名用根证书 如果生成根证书 则传入一个只包含椭圆函数信息的证书对象
     * the root certificate, if this paramater only contains a pairing, then this method will generate a root certificate
     * @param validAfter 有效期开始日期 the certificate effective start date
     * @param period 有效时间 单位毫秒 the certificate valid time period, in millisecond.
     * @return 生成的证书 the certificate
     */
    public static IBSCertificate generateCertificate(String user, IBSCertificate root, Date validAfter, long period) {
        IBSCertificate root0 = root;
        IBESystemParameter parameter = setup(root0.getPublicParameter().getPairing());
        IBSCertificate certificate = new IBSCertificate();
        certificate.setMasterKey(parameter.getMasterKey());
        certificate.setPublicParameter(parameter.getPublicParameter());
        certificate.setOwnerString(user);
        certificate.setNoEarlyThan(validAfter);
        certificate.setNoLateThan(new Date(period + validAfter.getTime()));
        if (root0.getMasterKey() == null)
            root0 = certificate;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream writer = new ObjectOutputStream(out);
            writer.writeObject(certificate);
            writer.flush();
            writer.close();
            byte[] data = out.toByteArray();
            out.close();
            byte[] hash = Hash.sha256(data);
            IBSSignature signature = sign(root0, hash, "SHA-256");
            certificate.setSignature(signature);
            return certificate;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 进行数字签名
     * Digital signing prcess
     * @param certificate 签名证书
     * @param digest 待签名数据摘要
     * @param hash 摘要方法 e.g. SHA1 or MD5
     * @return 数字签名
     */
    public static IBSSignature sign(IBSCertificate certificate, byte[] digest, String hash) {
        IBSSignature signature = new IBSSignature();
        signature.setSignatureParameter(certificate.getPublicParameter());
        signature.setDigest(digest);
        signature.setHashAlgorithm(hash);
        signature.setSigningDate(new Date());
        try {
            IBEPrivateKey privateKey = keygen(certificate, new String(digest, IBEConstraints.USER_STRING_ENCODING));
            signature.sethID(privateKey.gethID());
            signature.setrID(privateKey.getrID());
            signature.setUserString(privateKey.getUserString());
            return signature;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    /**
     * 验证数字签名
     * Verify a digital signature
     * @param signature 数字签名
     * @param digest 待验证数据摘要
     * @return 当且仅当数据一致且签名合法时验证通过 only if the verification passes it returns true
     */
    public static boolean verify(IBSSignature signature, byte[] digest) {
        if (signature == null || digest == null)
            return false;
        boolean integrity = Arrays.equals(digest, signature.getDigest());
        if (!integrity)
            return false;
        byte[] significantBytes = new byte[120];
        new Random().nextBytes(significantBytes);
        IBEPlainText plainText = IBEPlainText.newIbePlainTextFormSignificantBytes(significantBytes);
        try {
            IBECipherText cipherText = encrypt(signature.getSignatureParameter(), plainText, new String(digest, IBEConstraints.USER_STRING_ENCODING));
            IBEPlainText decrypt = decrypt(cipherText, signature);
            return decrypt.equals(plainText);
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }
}
