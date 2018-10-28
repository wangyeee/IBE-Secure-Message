package hamaster.gradesign.keydist.controller;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;

import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hamaster.gradesgin.ibe.IBEPrivateKey;
import hamaster.gradesgin.ibe.IBEPublicParameter;
import hamaster.gradesgin.ibs.IBSCertificate;
import hamaster.gradesgin.ibs.IBSSignature;
import hamaster.gradesgin.util.Hex;
import hamaster.gradesign.keydist.daemon.KeyGenClient;
import hamaster.gradesign.keydist.entity.User;
import hamaster.gradesign.keydist.service.ClientService;
import hamaster.gradesign.keydist.service.IDRequestService;
import hamaster.gradesign.keydist.service.UserService;
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
    @PostMapping("/api/key/{user}/{id}")
    public Map<String, String> getUserKey(@PathParam(value = "user") String username,
            @PathParam(value = "id") String keyID,
            @RequestParam(value = "p", required = true) String password,
            @RequestParam(value = "k", required = true) String keyPassword) {
        User owner = userService.loginWithUsername(username, password);
        if (owner == null) {
            // login failed
            return errorMessage(ClientService.ERR_WRONG_PWD, "Incorrect username or password");
        }
        if (idRequestService.doesIdBelongToUser(keyID, owner, keyPassword) > 0) {
            // check id ownership and decryption password
            return errorMessage(ClientService.ERR_WRONG_ID_PWD, "Incorrect ID decryption key");
        }
        IdentityDescriptionEntity ide = client.getIdentityDescription(keyID);
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

    private Map<String, String> errorMessage(int code, String message) {
        Map<String, String> resp = new HashMap<String, String>();
        resp.put("code", Integer.toString(code));
        resp.put("message", message);
        return resp;
    }

    /*
    byte[] applyUserKey(byte[] request) throws IOException;
    byte[] register(byte[] request) throws IOException;
    byte[] listIds(byte[] request) throws IOException;
    byte[] listSystems(byte[] request) throws IOException;
    byte[] login(byte[] request) throws IOException;
    */
}
