package hamaster.gradesign.service;

import java.io.IOException;

public interface ClientService {

    /**
     * 操作成功完成
     */
    int ERR_SUCCESS = 0;

    /**
     * 未知操作
     */
    int ERR_UNKNOWN_OP = 1;

    /**
     * 处理请求过程中发生异常
     */
    int ERR_PROC_REQ = 2;

    /**
     * 数据缺失
     */
    int ERR_EOF = 3;

    /**
     * 错误的用户名/密码
     */
    int ERR_WRONG_PWD = 4;

    /**
     * 错误的ID访问密码
     */
    int ERR_WRONG_ID_PWD = 5;

    /**
     * 用户试图获取他人身份描述
     */
    int ERR_ID_THEFT = 6;

    /**
     * ID已经被人使用
     */
    int ERR_ID_USED = 7;

    /**
     * 注册所用的电子邮件地址已被使用
     */
    int ERR_EMAIL_USED = 8;

    /**
     * 数据错误
     */
    int ERR_DATA_ERROR = 9;

    /**
     * 用户等未被激活
     */
    int ERR_NOT_ACTIVE = 10;

    byte[] getUserKey(byte[] request);
    byte[] applyUserKey(byte[] request) throws IOException;
    byte[] register(byte[] request) throws IOException;
    byte[] listIds(byte[] request) throws IOException;
    byte[] listSystems(byte[] request) throws IOException;
    byte[] login(byte[] request) throws IOException;
}
