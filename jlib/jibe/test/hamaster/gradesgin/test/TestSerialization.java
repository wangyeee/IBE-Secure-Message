package hamaster.gradesgin.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import hamaster.gradesgin.ibe.IBECipherText;
import hamaster.gradesgin.ibe.IBEConstraints;
import hamaster.gradesgin.ibe.IBEPlainText;
import hamaster.gradesgin.ibe.IBEPrivateKey;
import hamaster.gradesgin.ibe.IBEPublicParameter;
import hamaster.gradesgin.ibe.IBESystemParameter;
import hamaster.gradesgin.ibe.core.IBEEngine;
import hamaster.gradesgin.ibs.IBSCertificate;
import hamaster.gradesgin.ibs.IBSSignature;
import hamaster.gradesign.ibe.util.Hex;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

public class TestSerialization {

	private final static String pairing = "type a q 8780710799663312522437781984754049815806883199414208211028653399266475630880222957078625179422662221423155858769582317459277713367317481324925129998224791 h 12016012264891146079388821366740534204802954401251311822919615131047207289359704531102844802183906537786776 r 730750818665451621361119245571504901405976559617 exp2 159 exp1 107 sign1 1 sign0 1";

	String testUser = "wangyeee@gmail.com";

	String testData = "65cf3983fc810a12bf66231a826dd6374b171a5db24b8192e3c951dbdede06714f5c93e1fc013ece70a85b0df3ea365c6f333f6eefeeae408d60219c28b48ef6ef08f6c7a20064f51f29babe3432586ff8126b3f90befddcdf1162624bb071419bd3afedf1123a12100fa4839736cfe73579fa761df472d3f64b7e44";

	byte[] hash = "dfssdhufdshihfuisdhfuisdhfui".getBytes();

	@Test
	public void testIBEPrivateKeySerialization() {
		IBESystemParameter system = IBEEngine.setup(pairing.getBytes());
		IBEPrivateKey privateKey = IBEEngine.keygen(system, testUser);
		testExternalization(privateKey);
	}

	@Test
	public void testIBEPublicParameterSerialization() {
		IBESystemParameter system = IBEEngine.setup(pairing.getBytes());
		IBEPublicParameter parameter = system.getPublicParameter();
		testExternalization(parameter);
	}

	@Test
	public void testIBEPlainTextSerialization() {
		byte[] toTest = Hex.unhex(testData);
		IBEPlainText plainText = IBEPlainText.newIbePlainTextFormSignificantBytes(toTest);
		testExternalization(plainText);
	}

	@Test
	public void testIBECipherTextSerialization() {
		byte[] toTest = Hex.unhex(testData);
		IBESystemParameter system = IBEEngine.setup(pairing.getBytes());
		assertNotNull(system);
		IBEPrivateKey privateKey = IBEEngine.keygen(system, testUser);
		assertNotNull(privateKey);
		IBEPlainText plainText = IBEPlainText.newIbePlainTextFormSignificantBytes(toTest);
		System.out.println(Hex.hex(plainText.getContent()));
		IBECipherText cipherText = IBEEngine.encrypt(system.getPublicParameter(), plainText, testUser);
		assertNotNull(cipherText);
		testExternalization(cipherText);
	}

	@Test
	public void testIBSCertificateSerialization() {
		IBSCertificate root = new IBSCertificate();
		IBEPublicParameter pub = new IBEPublicParameter();
		pub.setPairing(pairing.getBytes());
		root.setPublicParameter(pub);
		root.setMasterKey(null);
		IBSCertificate certificate = IBEEngine.generateCertificate(testUser, root, new Date(), 1000000000L);
		assertNotNull(certificate);
		System.out.println(certificate);
		testExternalization(certificate);
		IBSSignature signature = IBEEngine.sign(certificate, hash, "unknown");
		assertNotNull(signature);
		testExternalization(signature);
		boolean res = IBEEngine.verify(signature, hash);
		System.out.println(res);
		assertTrue(res);
	}

	private void testExternalization(IBEConstraints obj) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			obj.writeExternal(out);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
			return;
		}
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		IBEConstraints obj1 = null;
		try {
			obj1 = obj.getClass().newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
			return;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return;
		}
		try {
			obj1.readExternal(in);
			System.out.println(obj1);
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
			return;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
			return;
		}
		assertEquals(obj, obj1);
	}
}
