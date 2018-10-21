package hamaster.gradesign.keydist.mail;

import static java.util.Objects.requireNonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import hamaster.gradesgin.ibe.core.IBEEngine;
import hamaster.gradesgin.ibs.IBSSignature;
import hamaster.gradesgin.util.Hash;
import hamaster.gradesign.keydist.client.Base64Encoder;
import hamaster.gradesign.keydist.client.Encoder;
import hamaster.gradesign.keydist.daemon.KeyGenClient;
import hamaster.gradesign.keydist.dao.UserDAO;
import hamaster.gradesign.keydist.entity.IDRequest;
import hamaster.gradesign.keydist.entity.User;
import hamaster.gradesign.keydist.service.IDRequestService;
import hamaster.gradesign.keygen.IBECSR;

@Component
public class IBEMailParameterGenerator {

    public final static String CONTENT_KEY = "c";// content
    public final static String SIGNATURE_KEY = "s";//signature

    final private static long oneweek = 604800000L;

    private UserDAO userDAO;
    private IDRequestService requestDAO;
    private KeyGenClient system;

    @Autowired
    public IBEMailParameterGenerator(UserDAO userDAO, IDRequestService requestDAO, KeyGenClient system) {
        this.userDAO = requireNonNull(userDAO);
        this.requestDAO = requireNonNull(requestDAO);
        this.system = requireNonNull(system);
    }

    public Properties sign(ActivationContent content) {
        Properties props = new Properties();
        byte[] bs = ActivationContent.toBytes(content);
        byte[] digest = Hash.sha512(bs);
        IBSSignature signature = IBEEngine.sign(system.serverCertificate(), digest, "SHA-512");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            signature.writeExternal(out);
            out.flush();
        } catch (IOException e) {
        }
        byte[] sign = out.toByteArray();

        Encoder base64 = new Base64Encoder();
        String ct = base64.encode(bs);
        ct = ct.replace('+', '*');
        ct = ct.replace('/', '-');
        ct = StringUtils.replace(ct, "=", "%3D");

        String sg = base64.encode(sign);
        sg = sg.replace('+', '*');
        sg = sg.replace('/', '-');
        sg = StringUtils.replace(sg, "=", "%3D");
        props.setProperty(CONTENT_KEY, ct);
        props.setProperty(SIGNATURE_KEY, sg);
        return props;
    }

    public int verify(byte[] content, byte[] signature) {
        byte[] digest = Hash.sha512(content);
        IBSSignature sign = new IBSSignature();
        ByteArrayInputStream in = new ByteArrayInputStream(signature);
        try {
            sign.readExternal(in);
        } catch (IOException e) {
        } catch (ClassNotFoundException e) {
        }
        boolean b0 = IBEEngine.verify(sign, digest);
        if (!b0)
            return 1;// 错误
        ActivationContent activationContent = ActivationContent.fromBytes(content);
        long now = System.currentTimeMillis();
        long start = activationContent.getActiveDate().getTime();
        if (now - start < oneweek) {
            User applicant = userDAO.getOne(activationContent.getUserId());
            if (activationContent.getType() == ActivationContent.ACTIVE_ID) {
                IDRequest request = requestDAO.getByOwner(applicant, activationContent.getEmail());
                if (request.getStatus() != IBECSR.APPLICATION_NOT_VERIFIED)
                    return 3;//已经激活
                request.setStatus(IBECSR.APPLICATION_STARTED);
                requestDAO.save(request);
            } else if (activationContent.getType() == ActivationContent.ACTIVE_USER) {
                if (applicant.getStatus() != User.USER_REG)
                    return 3;
                applicant.setStatus(User.USER_ACTIVE);
                userDAO.save(applicant);
            }
            return 0;// 成功
        }
        return 2;// 过期
    }
}
