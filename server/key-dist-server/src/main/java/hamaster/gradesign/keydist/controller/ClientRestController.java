package hamaster.gradesign.keydist.controller;

import static java.util.Objects.requireNonNull;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hamaster.gradesgin.ibe.IBEPrivateKey;
import hamaster.gradesgin.ibe.IBEPublicParameter;
import hamaster.gradesgin.ibs.IBSCertificate;
import hamaster.gradesgin.ibs.IBSSignature;
import hamaster.gradesgin.util.Hash;
import hamaster.gradesgin.util.Hex;
import hamaster.gradesign.keydist.aop.UserAuth;
import hamaster.gradesign.keydist.daemon.KeyGenClient;
import hamaster.gradesign.keydist.entity.IDRequest;
import hamaster.gradesign.keydist.entity.User;
import hamaster.gradesign.keydist.entity.UserToken;
import hamaster.gradesign.keydist.service.ClientService;
import hamaster.gradesign.keydist.service.IDRequestService;
import hamaster.gradesign.keydist.service.UserService;
import hamaster.gradesign.keygen.IBECSR;
import hamaster.gradesign.keygen.IdentityDescription;
import hamaster.gradesign.keygen.entity.IdentityDescriptionEntity;

@RestController
public class ClientRestController {

    private KeyGenClient client;
    private UserService userService;
    private IDRequestService idRequestService;

    @Autowired
    public ClientRestController(KeyGenClient client, UserService userService, IDRequestService idRequestService) {
        this.client = requireNonNull(client);
        this.userService = requireNonNull(userService);
        this.idRequestService = requireNonNull(idRequestService);
    }

    /**
     * 
     * @param username the username
     * @param keyID the ID, can be different from user's email used in registration
     * @param password user password
     * @param keyPassword ID password
     * @return <pre>如果操作成功 返回用户身份描述信息
     * 包含一字节的成功代码和数字节的身份描述信息内容
     * 如果操作失败 返回包含错误码的长度一字节的数组</pre>
     * 操作成功返回的内容：<ul><li>
     * 操作结果</li><li>
     * 参数g</li><li>
     * 参数g1</li><li>
     * 参数h</li><li>
     * 椭圆函数参数</li><li>
     * 私钥rID</li><li>
     * 私钥hID</li><li>
     * 数字签名公共参数</li><li>
     * 签名主密钥</li><li>
     * 证书有效期开始</li><li>
     * 证书有效期</li><li>
     * 所有者字符串长度</li><li>
     * 根证书签名</li></ul>
     */
    @UserAuth
    @PostMapping("/api/key/{user}/{id}")
    public Map<String, String> getUserKey(@PathVariable(value = "user") String username,
            @RequestParam(value = "p", required = true) String password,
            @PathVariable(value = "id") String keyID,
            @RequestParam(value = "k", required = true) String keyPassword) {
        User owner = userService.loginWithUsername(username, password);
        if (idRequestService.doesIdBelongToUser(keyID, owner, keyPassword) > 0) {
            // check id ownership and decryption password
            return errorMessage(ClientService.ERR_WRONG_ID_PWD, "Incorrect ID decryption key");
        }
        IdentityDescriptionEntity ide = client.getIdentityDescription(idRequestService.getByOwner(owner, keyID).getIbeSystemId(), keyID);
        IdentityDescription id = ide.getIdentityDescription(keyPassword.getBytes());
        IBEPublicParameter sysPublicparam = id.getSystemPublicParameter();
        IBEPrivateKey pkey = id.getPrivateKey();
        IBSCertificate certificate = id.getCertificate();
        IBEPublicParameter certParam = certificate.getPublicParameter();
        IBSSignature signature = certificate.getSignature();
        Map<String, String> result = errorMessage(ClientService.ERR_SUCCESS, "success");
        result.put("g", Hex.hex(sysPublicparam.getParamG()));
        result.put("g1", Hex.hex(sysPublicparam.getParamG1()));
        result.put("h", Hex.hex(sysPublicparam.getParamH()));
        result.put("p", Hex.hex(sysPublicparam.getPairing()));
        result.put("hID", Hex.hex(pkey.gethID()));
        result.put("rID", Hex.hex(pkey.getrID()));
        result.put("crtg", Hex.hex(certParam.getParamG()));
        result.put("crtg1", Hex.hex(certParam.getParamG1()));
        result.put("crth", Hex.hex(certParam.getParamH()));
        result.put("crtp", Hex.hex(certParam.getPairing()));
        result.put("crta", Hex.hex(certificate.getMasterKey()));
        result.put("crtst", Long.toString(certificate.getNoEarlyThan().getTime()));
        result.put("crtet", Long.toString(certificate.getNoLateThan().getTime()));
        result.put("crto", certificate.getOwnerString());
        result.put("sig", Hex.hex(signature.toByteArray()));
        return result;
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
     */
    @UserAuth
    @PutMapping("/api/keyreq/{user}/{sys}/{id}")
    public Map<String, String> applyUserKey(@PathVariable(value = "user") String username,
            @RequestParam(value = "p", required = true) String password,
            @PathVariable(value = "sys") String systemStr,
            @PathVariable(value = "id") String keyID,
            @RequestParam(value = "k", required = true) String keyPassword) {
        User owner = userService.loginWithUsername(username, password);
        IDRequest exist = idRequestService.getByIDString(keyID);
        if (exist != null) {
            if (exist.getApplicant().equals(owner)) {
                return errorMessage(ClientService.ERR_SUCCESS, "success");
            } else {
                return errorMessage(ClientService.ERR_ID_THEFT, "Users only have access to their own IDs");
            }
        }
        Integer system;
        try {
            system = Integer.decode(systemStr);
        } catch (NumberFormatException e) {
            system = client.getCurrentSystemID();
        }
        exist = new IDRequest();
        exist.setApplicant(owner);
        exist.setApplicationDate(new Date());
        exist.setIbeSystemId(system);
        exist.setIdentityString(keyID);
        exist.setStatus(IBECSR.APPLICATION_STARTED);
        exist.setPassword(Hex.hex(Hash.sha512(keyPassword)));
        exist.setPasswordToKeyGen(client.encryptSessionKeyForSystem(keyPassword.getBytes(), system));
        idRequestService.save(exist);
        return errorMessage(ClientService.ERR_SUCCESS, "success");
    }

    /**
     * 注册一个新用户
     * @param request 注册用户请求 包含：<ul><li>
     * 用户名（长度1字节 内容最多255字节 UTF-8编码）</li><li>
     * 电子邮件（长度1字节 内容最多255字节 UTF-8编码）</li><li>
     * 密码（长度1字节 内容最多255字节）</li></ul>
     * @return 包含处理结果的字节数组 长度为1
     */
    @PostMapping("/api/reg/{user}")
    public Map<String, String> register(@PathVariable(value = "user") String username,
            @RequestParam(value = "p", required = true) String password,
            @RequestParam(value = "email", required = true) String email) {
        if (userService.isUsernameExist(username))
            return errorMessage(ClientService.ERR_ID_USED, String.format("user name %s has been used", username));
        if (userService.isEmailExist(email))
            return errorMessage(ClientService.ERR_EMAIL_USED, String.format("email %s has been used", email));
        User user = new User();
        user.setEmail(email);
        user.setRegDate(new Date());
        user.setUsername(username);
        userService.register(user, password);
        return errorMessage(ClientService.ERR_SUCCESS, "success");
    }

    @UserAuth
    @GetMapping("/api/allid/{user}")
    public Map<String, ?> listIds(@PathVariable(value = "user") String username,
            @RequestParam(value = "p", required = true) String password,
            @RequestParam(value = "pg", required = false) String pageStr,
            @RequestParam(value = "n", required = false) String amountStr) {
        int page, amount;
        try {
            page = Integer.parseInt(pageStr);
        } catch (NumberFormatException e) {
            page = 1;
        }
        try {
            amount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            amount = 10;
        }
        User owner = userService.loginWithUsername(username, password);
        List<IDRequest> list = idRequestService.list(owner, page - 1, amount);
        Map<String, Object> resp = new HashMap<String, Object>();
        resp.put("code", "0");
        resp.put("ids", list);
        return resp;
    }

    @UserAuth
    @GetMapping("/api/login/{user}")
    public Map<String, ?> appLogin(@PathVariable(value = "user") String username,
            @RequestParam(value = "p", required = true) String password) {
        UserToken token = userService.appLogin(username, password, null);
        Map<String, Object> resp = new HashMap<String, Object>();
        resp.put("code", "0");
        resp.put("user", token.getUser());
        resp.put("sessionKey", token.getUuid());
        resp.put("effDate", token.getGenerationDate());
        return resp;
    }

    @GetMapping("/api/logout/{user}")
    public Map<String, String> appLogout(@PathVariable(value = "user") String username,
            @RequestParam(value = "t", required = true) String uuid) {
        userService.appLogout(username, uuid);
        return errorMessage(0, "Success");
    }

    private Map<String, String> errorMessage(int code, String message) {
        Map<String, String> resp = new HashMap<String, String>();
        resp.put("code", Integer.toString(code));
        resp.put("message", message);
        return resp;
    }
}
