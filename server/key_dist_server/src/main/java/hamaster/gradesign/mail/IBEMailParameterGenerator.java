package hamaster.gradesign.mail;

import hamaster.gradesgin.ibe.core.IBEEngine;
import hamaster.gradesgin.ibs.IBSSignature;
import hamaster.gradesgin.util.Hash;
import hamaster.gradesign.IBECSR;
import hamaster.gradesign.client.Base64Encoder;
import hamaster.gradesign.client.Encoder;
import hamaster.gradesign.daemon.EJBClient;
import hamaster.gradesign.dao.IDRequestDAO;
import hamaster.gradesign.dao.UserDAO;
import hamaster.gradesign.entity.IDRequest;
import hamaster.gradesign.entity.User;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

public class IBEMailParameterGenerator {

	public final static String CONTENT_KEY = "c";// content
	public final static String SIGNATURE_KEY = "s";//signature

	final private static long oneweek = 604800000L;	// 7天

	private UserDAO userDAO;
	private IDRequestDAO requestDAO;

	public IBEMailParameterGenerator() {
	}

	public Properties sign(ActivationContent content) {
		Properties props = new Properties();
		byte[] bs = ActivationContent.toBytes(content);
		byte[] digest = Hash.sha256(bs);
		EJBClient client = EJBClient.getInstance();
		IBSSignature signature = IBEEngine.sign(client.serverCertificate(), digest, "SHA-256");
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
		ct = ct.replaceAll("=", "%3D");
		
		String sg = base64.encode(sign);
		sg = sg.replace('+', '*');
		sg = sg.replace('/', '-');
		sg = sg.replaceAll("=", "%3D");
		props.setProperty(CONTENT_KEY, ct);
		props.setProperty(SIGNATURE_KEY, sg);
		return props;
	}

	public int verify(byte[] content, byte[] signature) {
		byte[] digest = Hash.sha256(content);
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
			User applicant = userDAO.get(User.class, activationContent.getUserId());
			if (activationContent.getType() == ActivationContent.ACTIVE_ID) {
				IDRequest request = requestDAO.getByOwner(applicant, activationContent.getEmail());
				if (request.getStatus() != IBECSR.APPLICATION_NOT_VERIFIED)
					return 3;//已经激活
				request.setStatus(IBECSR.APPLICATION_STARTED);
				requestDAO.update(request);
			} else if (activationContent.getType() == ActivationContent.ACTIVE_USER) {
				if (applicant.getStatus() != User.USER_REG)
					return 3;
				applicant.setStatus(User.USER_ACTIVE);
				userDAO.update(applicant);
			}
			return 0;// 成功
		}
		return 2;// 过期
	}

	public void setRequestDAO(IDRequestDAO requestDAO) {
		this.requestDAO = requestDAO;
	}

	public void setUserDAO(UserDAO userDAO) {
		this.userDAO = userDAO;
	}
}
