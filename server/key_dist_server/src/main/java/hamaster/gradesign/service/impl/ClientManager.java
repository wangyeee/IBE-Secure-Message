package hamaster.gradesign.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import hamaster.gradesgin.ibe.IBEConstraints;
import hamaster.gradesgin.ibe.io.SecureByteArrayInputStream;
import hamaster.gradesgin.ibe.io.SecureByteArrayOutputStream;
import hamaster.gradesgin.util.Hash;
import hamaster.gradesign.IBECSR;
import hamaster.gradesign.IdentityDescription;
import hamaster.gradesign.daemon.EJBClient;
import hamaster.gradesign.entity.IDRequest;
import hamaster.gradesign.entity.IdentityDescriptionEntity;
import hamaster.gradesign.entity.User;
import hamaster.gradesign.ibe.util.Hex;
import hamaster.gradesign.idmgmt.IBESystemBean;
import hamaster.gradesign.idmgmt.IdentityDescriptionBean;
import hamaster.gradesign.service.ClientService;
import hamaster.gradesign.service.IDRequestService;
import hamaster.gradesign.service.UserService;

@Service
public class ClientManager implements ClientService {

    private IdentityDescriptionBean identityDescriptionBean;

    private UserService userService;

    private IDRequestService idRequestService;

    private IBESystemBean systemBean;

    private Logger logger;

    public ClientManager() {
        logger = LoggerFactory.getLogger(getClass());
    }

    /**
     * 获取用户身份描述信息
     * @param request 请求体 具体内容：<ul><li>
     * 用户注册时电子邮件地址（长度信息1字节 数据最多255字节 UTF-8编码）</li><li>
     * 密码（长度信息1字节 数据最多255字节）</li><li>
     * 要请求的ID（长度信息1字节 数据最多255字节 UTF-8编码）</li><li>
     * 用户为要请求的ID所设置的访问密码（长度信息1字节 数据最多255字节）</li><ul>
     * @return <pre>如果操作成功 返回用户身份描述信息（采用用户设置的密码加密后的结果）
     * 包含一字节的成功代码和数字节的身份描述信息内容
     * 如果操作失败 返回包含错误码的长度一字节的数组</pre>
     * 操作成功返回的内容：<ul><li>
     * 操作结果 1字节</li><li>
     * 参数g 128字节</li><li>
     * 参数g1 128字节</li><li>
     * 参数h 128字节</li><li>
     * 椭圆函数参数（长度4字节 内容数字节）</li><li>
     * 私钥rID 20字节</li><li>
     * 私钥hID 128字节</li><li>
     * 数字签名公共参数（长度不定）</li><li>
     * 签名主密钥 20字节</li><li>
     * 证书有效期开始 8字节</li><li>
     * 证书有效期 8字节</li><li>
     * 所有者字符串长度（长度4字节 内容数字节）</li><li>
     * 根证书签名（长度不定）</li></ul>
     * @throws IOException
     */
    public byte[] getUserKey(byte[] request) {
        byte[] ret = new byte[1];
        ByteArrayInputStream in = new SecureByteArrayInputStream(request);
        try {
            int len = in.read();
            byte[] emailBin = new byte[len];
            if (emailBin.length != in.read(emailBin)) {
                ret[0] = ERR_EOF;
                return ret;
            }
            String email = new String(emailBin, IBEConstraints.USER_STRING_ENCODING);

            len = in.read();
            byte[] passwordBin = new byte[len];
            if (passwordBin.length != in.read(passwordBin)) {
                ret[0] = ERR_EOF;
                return ret;
            }
            String password = new String(passwordBin);

            len = in.read();
            byte[] idBin = new byte[len];
            if (idBin.length != in.read(idBin)) {
                ret[0] = ERR_EOF;
                return ret;
            }
            String id = new String(idBin, IBEConstraints.USER_STRING_ENCODING);

            len = in.read();
            byte[] idPwdBin = new byte[len];
            if (idPwdBin.length != in.read(idPwdBin)) {
                ret[0] = ERR_EOF;
                return ret;
            }
            String idPassword = new String(idPwdBin);

            User user = userService.login(email, password);
            if (user == null) {
                ret[0] = ERR_WRONG_PWD;
                return ret;
            }
            if (user.getStatus() != User.USER_ACTIVE) {
                ret[0] = ERR_NOT_ACTIVE;
                return ret;
            }

            int belongsTo = idRequestService.doesIdBelongToUser(id, user, idPassword);
            switch (belongsTo) {
            case 1:
                ret[0] = ERR_WRONG_ID_PWD;
                return ret;
            case 2:
                logger.warn("user with id:" + user.getEmail() + " attemptd to steal id:" + id + " at " + new Date());
                ret[0] = ERR_ID_THEFT;
                return ret;
            }
            IdentityDescriptionEntity ide = identityDescriptionBean.get(id);
            IdentityDescription userId = ide.getIdentityDescription(idPassword.getBytes());

            byte[] idbs = userId.toByteArray();
            byte[] succ = new byte[1 + idbs.length];
            succ[0] = ERR_SUCCESS;
            System.arraycopy(idbs, 0, succ, 1, idbs.length);
            Arrays.fill(idbs, (byte) 0);
            return succ;
        } catch (IOException e) {
            return ret;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * 用户为一个ID申请描述信息
     * @param request 请求体 具体内容：<ul><li>
     * 用户注册时电子邮件地址（长度信息1字节 数据最多255字节 UTF-8编码）</li><li>
     * 用户密码（长度信息1字节 数据最多255字节）</li><li>
     * 用户所要使用的IBE系统（4字节）</li><li>
     * 要请求的ID（长度信息1字节 数据最多255字节 UTF-8编码）</li><li>
     * 用户为要请求的ID所设置的访问密码（长度信息1字节 数据最多255字节）</li></ul>
     * @return 包含处理结果的字节数组 长度为1
     * @throws IOException
     */
    public byte[] applyUserKey(byte[] request) {
        byte[] ret = new byte[1];
        ByteArrayInputStream in = new SecureByteArrayInputStream(request);
        try {
            int len = in.read();
            byte[] emailBin = new byte[len];
            if (emailBin.length != in.read(emailBin)) {
                ret[0] = ERR_EOF;
                return ret;
            }
            String email = new String(emailBin, IBEConstraints.USER_STRING_ENCODING);

            len = in.read();
            byte[] passwordBin = new byte[len];
            if (passwordBin.length != in.read(passwordBin)) {
                in.close();
                ret[0] = ERR_EOF;
                return ret;
            }
            String password = new String(passwordBin);

            byte[] sysIdBin = new byte[4];
            if (4 != in.read(sysIdBin)) {
                ret[0] = ERR_EOF;
                return ret;
            }
            int ibeSystemId = Hex.bytesToInt(sysIdBin);

            len = in.read();
            byte[] idBin = new byte[len];
            if (idBin.length != in.read(idBin)) {
                ret[0] = ERR_EOF;
                return ret;
            }
            String id = new String(idBin, IBEConstraints.USER_STRING_ENCODING);
            int idExist = idRequestService.doesIdRequestExist(id);
            if (0 < idExist) {
                ret[0] = ERR_ID_USED;
                return ret;
            }

            len = in.read();
            byte[] idPwdBin = new byte[len];
            if (idPwdBin.length != in.read(idPwdBin)) {
                in.close();
                ret[0] = ERR_EOF;
                return ret;
            }
            String idPassword = new String(idPwdBin);
            in.close();

            User user = userService.login(email, password);
            if (user == null) {
                ret[0] = ERR_WRONG_PWD;
                return ret;
            }
            if (user.getStatus() != User.USER_ACTIVE) {
                ret[0] = ERR_NOT_ACTIVE;
                return ret;
            }

            IDRequest idr = new IDRequest();
            idr.setApplicant(user);
            idr.setApplicationDate(new Date());
            idr.setIbeSystemId(ibeSystemId);
            idr.setIdentityString(id);
            idr.setPassword(idPassword);
            idr.setStatus(IBECSR.APPLICATION_NOT_VERIFIED);

            idRequestService.save(idr);
            ret[0] = ERR_SUCCESS;
            return ret;
        } catch (IOException e) {
            return ret;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * 注册一个新用户
     * @param request 注册用户请求 包含：<ul><li>
     * 用户名（长度1字节 内容最多255字节 UTF-8编码）</li><li>
     * 电子邮件（长度1字节 内容最多255字节 UTF-8编码）</li><li>
     * 密码（长度1字节 内容最多255字节）</li></ul>
     * @return 包含处理结果的字节数组 长度为1
     * @throws IOException
     */
    public byte[] register(byte[] request) {
        byte[] ret = new byte[1];
        ByteArrayInputStream in = new SecureByteArrayInputStream(request);
        try {
            int len = in.read();
            byte[] usernameBin = new byte[len];
            if (usernameBin.length != in.read(usernameBin)) {
                ret[0] = ERR_EOF;
                return ret;
            }
            String username = new String(usernameBin, IBEConstraints.USER_STRING_ENCODING);

            len = in.read();
            byte[] emailBin = new byte[len];
            if (emailBin.length != in.read(emailBin)) {
                ret[0] = ERR_EOF;
                return ret;
            }
            String email = new String(emailBin, IBEConstraints.USER_STRING_ENCODING);

            boolean emailExist = userService.isEmailExist(email);
            if (emailExist) {
                ret[0] = ERR_EMAIL_USED;
                return ret;
            }

            len = in.read();
            byte[] passwordBin = new byte[len];
            if (passwordBin.length != in.read(passwordBin)) {
                in.close();
                ret[0] = ERR_EOF;
                return ret;
            }
            String password = new String(passwordBin);
            in.close();

            Date regDate = new Date();
            User user = new User();
            user.setEmail(email);
            user.setUsername(username);
            user.setRegDate(regDate);
            user.setStatus(User.USER_REG);

            String salt = EJBClient.util.format(regDate);
            byte[] hash = Hash.sha512(new StringBuilder(password).append(salt).toString());
            user.setPassword(Hex.hex(hash));

            userService.save(user);
            ret[0] = ERR_SUCCESS;
            return ret;
        } catch (IOException e) {
            return ret;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * 列出用户所拥有的ID信息
     * @param request 请求体 包含：<ul><li>
     * 用户注册时电子邮件地址（长度信息1字节 数据最多255字节 UTF-8编码）</li><li>
     * 用户密码（长度信息1字节 数据最多255字节）</li><li>
     * 页码 从0开始 长度4字节</li><li>
     * 每一页数量 1字节 值从1到255</li><li>
     * 当前状态 1字节 默认全部状态</li></ul>
     * @return 包含身份请求信息的列表：<ul><li>
     * 操作结果 1字节</li><li>
     * 身份请求信息数量 1字节</li><li>
     * 数个身份请求信息</li></ul>
     * @throws IOException
     */
    public byte[] listIds(byte[] request) {
        byte[] ret = new byte[1];
        ByteArrayInputStream in = new SecureByteArrayInputStream(request);
        try {
            int len = in.read();
            byte[] emailBin = new byte[len];
            if (emailBin.length != in.read(emailBin)) {
                ret[0] = ERR_EOF;
                return ret;
            }
            String email = new String(emailBin, IBEConstraints.USER_STRING_ENCODING);

            len = in.read();
            byte[] passwordBin = new byte[len];
            if (passwordBin.length != in.read(passwordBin)) {
                in.close();
                ret[0] = ERR_EOF;
                return ret;
            }
            String password = new String(passwordBin);

            byte[] pageBin = new byte[4];
            if (pageBin.length != in.read(pageBin)) {
                ret[0] = ERR_EOF;
                return ret;
            }
            int page = Hex.bytesToInt(pageBin);
            if (page < 0) {
                ret[0] = ERR_DATA_ERROR;
                return ret;
            }

            int amount = in.read();
            if (amount == -1) {
                ret[0] = ERR_EOF;
                return ret;
            }

            int status = in.read();
            if (status == -1)
                status = 0;
            User user = userService.login(email, password);
            if (user == null) {
                ret[0] = ERR_WRONG_PWD;
                return ret;
            }
            if (user.getStatus() != User.USER_ACTIVE) {
                ret[0] = ERR_NOT_ACTIVE;
                return ret;
            }

            List<IDRequest> requests = idRequestService.list(user, page, amount, status);
            ByteArrayOutputStream out = new SecureByteArrayOutputStream();
            out.write(ERR_SUCCESS);
            out.write(requests.size()); // 最多255个
            for (IDRequest idRequest : requests) {
                out.write(idRequest.toBytes());
            }
            out.flush();
            byte[] retRqs = out.toByteArray();
            out.close();
            return retRqs;
        } catch (IOException e) {
            return ret;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * 列出当前可用的IBE系统
     * @param request 请求 包含：<ul><li>
     * 分业页码 从0开始 4字节</li><li>
     * 每一页数量 值从1到255 1字节</li></ul>
     * @return 系统列表：<ul><li>
     * 操作结果 1字节</li><li>
     * 本次返回的系统数量 1字节</li><li>
     * 系统列表 数字节（每一个系统列表项包含4字节的系统编号 1字节的系统名长度和数字节的系统名）</li></ul>
     * @throws IOException
     */
    public byte[] listSystems(byte[] request) {
        byte[] ret = new byte[1];
        ByteArrayInputStream in = new SecureByteArrayInputStream(request);
        try {
            byte[] pageBin = new byte[4];
            if (pageBin.length != in.read(pageBin)) {
                ret[0] = ERR_EOF;
                return ret;
            }
            int page = Hex.bytesToInt(pageBin);
            if (page < 0) {
                ret[0] = ERR_DATA_ERROR;
                return ret;
            }
            int amount = in.read();
            if (amount == -1) {
                ret[0] = ERR_EOF;
                return ret;
            }
            Map<Integer, String> systems = systemBean.list(page, amount);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(ERR_SUCCESS);
            out.write(systems.size());
            for (Integer sysId : systems.keySet()) {
                String sysName = systems.get(sysId);
                byte[] sysNameBin = sysName.getBytes(IBEConstraints.USER_STRING_ENCODING);
                out.write(Hex.intToByte(sysId.intValue()));
                out.write(sysNameBin.length);
                out.write(sysNameBin);
            }
            out.flush();
            return out.toByteArray();
        } catch (IOException e) {
            return ret;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * 检查用户的电子邮件地址和密码是否匹配
     * @param request 请求体 包含：<ul><li>
     * 用户注册时电子邮件地址（长度信息1字节 数据最多255字节 UTF-8编码）</li><li>
     * 用户密码（长度信息1字节 数据最多255字节）</li></ul>
     * @return 包含处理结果的字节数组 长度为1
     * @throws IOException
     */
    public byte[] login(byte[] request) {
        byte[] ret = new byte[1];
        ByteArrayInputStream in = new SecureByteArrayInputStream(request);
        try {
        int len = in.read();
        byte[] emailBin = new byte[len];
        if (emailBin.length != in.read(emailBin)) {
            ret[0] = ERR_EOF;
            return ret;
        }
        String email = new String(emailBin, IBEConstraints.USER_STRING_ENCODING);

        len = in.read();
        byte[] passwordBin = new byte[len];
        if (passwordBin.length != in.read(passwordBin)) {
            in.close();
            ret[0] = ERR_EOF;
            return ret;
        }
        String password = new String(passwordBin);
        logger.debug(String.format("User %s login at %s, password: %s", email, new Date().toString(), password));
        User user = userService.login(email, password);
        if (user == null) {
            logger.debug("Login failure: wrong password for %s", email);
            ret[0] = ERR_WRONG_PWD;
            return ret;
        }
        if (user.getStatus() != User.USER_ACTIVE) {
            logger.debug("Login failure: user %s not active", user.getUsername());
            ret[0] = ERR_NOT_ACTIVE;
            return ret;
        }
        logger.debug("User %s successfully logged in", email);
        ret[0] = ERR_SUCCESS;
        return ret;
        } catch (IOException e) {
            return ret;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
            }
        }
    }
}
