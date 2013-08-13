package hamaster.gradesgin.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import hamaster.gradesgin.ibe.IBEPublicParameter;
import hamaster.gradesgin.ibe.core.IBEEngine;
import hamaster.gradesgin.ibs.IBSCertificate;
import hamaster.gradesgin.ibs.IBSSignature;

import org.junit.Test;

public class TestIBSignature {

	private final static String pairing = "type a q 8780710799663312522437781984754049815806883199414208211028653399266475630880222957078625179422662221423155858769582317459277713367317481324925129998224791 h 12016012264891146079388821366740534204802954401251311822919615131047207289359704531102844802183906537786776 r 730750818665451621361119245571504901405976559617 exp2 159 exp1 107 sign1 1 sign0 1";

	String testUser = "wangyeee@gmail.com";

	byte[] hash = "dfssdhufdshihfuisdhfuisdhfui".getBytes();

	@Test
	public void test() {
		IBSCertificate root = new IBSCertificate();
		IBEPublicParameter pub = new IBEPublicParameter();
		pub.setPairing(pairing.getBytes());
		root.setPublicParameter(pub);
		root.setMasterKey(null);
		for (int i = 0; i < 10; i++) {
			IBSCertificate certificate = IBEEngine.generateCertificate(testUser, root, new Date(), 1000000000L);
			assertNotNull(certificate);
			IBSSignature signature = IBEEngine.sign(certificate, hash, "unknown");
			assertNotNull(signature);
			boolean res = IBEEngine.verify(signature, hash);
			System.out.println(res);
			assertTrue(res);
		}
	}
}
