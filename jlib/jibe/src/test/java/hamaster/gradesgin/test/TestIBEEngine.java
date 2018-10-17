package hamaster.gradesgin.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import hamaster.gradesgin.ibe.IBECipherText;
import hamaster.gradesgin.ibe.IBEPlainText;
import hamaster.gradesgin.ibe.IBEPrivateKey;
import hamaster.gradesgin.ibe.IBEPublicParameter;
import hamaster.gradesgin.ibe.IBESystemParameter;
import hamaster.gradesgin.ibe.core.IBEEngine;
import hamaster.gradesgin.util.Hex;

public class TestIBEEngine {

    private final static String pairing =
        "type a q 8780710799663312522437781984754049815806883199414208211028653399266475630880222957078625179422662221423155858769582317459277713367317481324925129998224791 h 12016012264891146079388821366740534204802954401251311822919615131047207289359704531102844802183906537786776 r 730750818665451621361119245571504901405976559617 exp2 159 exp1 107 sign1 1 sign0 1 ";

    String testUser = "wangyeee@gmail.com";

    String testData =
        "65cf3983fc810a12bf66231a826dd6374b171a5db24b8192e3c951dbdede06714f5c93e1fc013ece70a85b0df3ea365c6f333f6eefeeae408d60219c28b48ef6ef08f6c7a20064f51f29babe3432586ff8126b3f90befddcdf1162624bb071419bd3afedf1123a12100fa4839736cfe73579fa761df472d3f64b7e44";

    @Test
    public void testEngine() {
        for (int i = 0; i < 1; i++) {
            byte[] toTest = Hex.unhex(testData);
            IBESystemParameter system = IBEEngine.setup(pairing.getBytes());
            assertNotNull(system);
            IBEPrivateKey privateKey = IBEEngine.keygen(system, testUser);
            assertNotNull(privateKey);
            IBEPlainText plainText = IBEPlainText.newIbePlainTextFormSignificantBytes(toTest);
            System.out.println(Hex.hex(plainText.getContent()));
            IBECipherText cipherText = IBEEngine.encrypt(system.getPublicParameter(), plainText, testUser);
            assertNotNull(cipherText);
            IBEPlainText decrypt = IBEEngine.decrypt(cipherText, privateKey);
            assertNotNull(decrypt);
            System.out.println(Hex.hex(decrypt.getContent()));
            assertEquals(plainText, decrypt);
            assertArrayEquals(toTest, IBEPlainText.getSignificantBytes(decrypt));
        }
    }

    @Test
    public void fuckPriKey() {
        String hex =
            "74763adb1936dc8e345efe78b31267aedbd87de02fe98134367d102a32a214b0bf39dca7be04131fadd64f18e5d81981e461b4992685e68d23dc5bb42a730b7e6b49aa9536aed1c7d1ceb89b8cf31cb3cc87148c8f412051bbe2df941bec13d196ebbe8ee01a1ba9430a35625c8d8f6c382a6084c70d84cefa0f61eb409a137cd586e057c7eace331f3647b705d2c3f97e458e1f0000001277616e677965656540676d61696c2e636f6d00000167747970652061207120383738303731303739393636333331323532323433373738313938343735343034393831353830363838333139393431343230383231313032383635333339393236363437353633303838303232323935373037383632353137393432323636323232313432333135353835383736393538323331373435393237373731333336373331373438313332343932353132393939383232343739312068203132303136303132323634383931313436303739333838383231333636373430353334323034383032393534343031323531333131383232393139363135313331303437323037323839333539373034353331313032383434383032313833393036353337373836373736207220373330373530383138363635343531363231333631313139323435353731353034393031343035393736353539363137206578703220313539206578703120313037207369676e312031207369676e30203120";
        byte[] raw = Hex.unhex(hex);
        System.out.println(Arrays.toString(raw));
        IBEPrivateKey priKey = new IBEPrivateKey();
        ByteArrayInputStream in = new ByteArrayInputStream(raw);
        try {
            priKey.readExternal(in);
            in.close();
            System.out.println("---------------");
            System.out.println(priKey);
            System.out.println("---------------");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void fuckParam() {
        String hex =
            "685a0e7fc44a99bce11f020525c3ca7d9c1b23d988636c0e422955a6f33c469ea909e97e8ce837e037af38053fa6f32025005f3becbae1bd027bddf2d56b31d594a6d380ae99b4deee551e03120835a0b18de2f71628f5527907a840d591134d567099c5daf6fd88c8fff3a97201d25ddadb17d603380d210b53481ceb2c87747c7fe6f3a2444e0e55e49e274a7ac46677614ddd8bf12e2c27e0b2274aedfc99b52054d2645ff8774ceb46292bb67e2c0e2eb4ee1735d968bf32ea1458f74e736574391624e029e768451458fdffab82a573e2a16d0422e8f06b48d5c4387e4bd1969478174cbb9a4292933b4276d9cf85d15546409412382179178c169ecb1c93d5812927b98c302a76a769981a6070b1901a74a80e2e2c82e8326c6325f61c7dc5ee577ed096f6bb58568c7b84aabe8bf96f7c1e234dde47fd7bcfd3fe782e87d1f9ddd002bc296d0dca97d15664376d98290ad194cb87b5515a92ac7c0350571fd0056d1ac8d030cdf197dee0c20e51c5fb8ab7ef720cd51efeea0b93ee8100000167747970652061207120383738303731303739393636333331323532323433373738313938343735343034393831353830363838333139393431343230383231313032383635333339393236363437353633303838303232323935373037383632353137393432323636323232313432333135353835383736393538323331373435393237373731333336373331373438313332343932353132393939383232343739312068203132303136303132323634383931313436303739333838383231333636373430353334323034383032393534343031323531333131383232393139363135313331303437323037323839333539373034353331313032383434383032313833393036353337373836373736207220373330373530383138363635343531363231333631313139323435353731353034393031343035393736353539363137206578703220313539206578703120313037207369676e312031207369676e30203120";
        byte[] raw = Hex.unhex(hex);
        System.out.println(Arrays.toString(raw));
        IBEPublicParameter pubKey = new IBEPublicParameter();
        ByteArrayInputStream in = new ByteArrayInputStream(raw);
        try {
            pubKey.readExternal(in);
            System.out.println(pubKey);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHelloJavaAndObjC() {
        String secret = "Hello World!";
        System.out.println(secret.getBytes().length);
        String hexParam =
            "9314fa36d6b8c086b2f5cfe1ddf04fb14b21fece9bc3804a03c02c370fc2b939b91496f13abfba7b5489f67e80f525455a0674ff5a8d7e43f80279a75c0990793a90b132caf4ceebcd188e818ded4d51351bde8a942e29369684f131e5cd46d51a9d37bc775b347ca6432c1a309737ed4d583c134a216072fd6330bb259f35ff97490861e5e8b33bd18d46cb625e6e324eff16417a8ac4cbd9c05a3e86f3bd29db470b3a0fb5b3cc6c89a65ab821de28280d0fd395b3aefea3bc892ac8d7962c1f98c58e37dcd3aada7dbc7822ce380993c3d5782a49633ad77e85e55a2674ba2bb877f92eaa9fda8436d7ca74b36305a14cdee57e8f3f8a11edbb67e8e8f3d9a562c55d9be8282c899eed5a30201c6f1e5446629777971615a22d4a50bda793c274eb99db69435e5c4c5f1ab996586cd6daf76e215ee3a4cff331f44047d947a0ba1cb48ed1d743639022e36dfb3aa0d521afed02373666c796b4f1c716ba95d0043ccf458bebd492ef4323febda75a5fe0ce5a71ae0fb542afd5376085625a00000167747970652061207120383738303731303739393636333331323532323433373738313938343735343034393831353830363838333139393431343230383231313032383635333339393236363437353633303838303232323935373037383632353137393432323636323232313432333135353835383736393538323331373435393237373731333336373331373438313332343932353132393939383232343739312068203132303136303132323634383931313436303739333838383231333636373430353334323034383032393534343031323531333131383232393139363135313331303437323037323839333539373034353331313032383434383032313833393036353337373836373736207220373330373530383138363635343531363231333631313139323435353731353034393031343035393736353539363137206578703220313539206578703120313037207369676e312031207369676e30203120";
        byte[] raw = Hex.unhex(hexParam);
        IBEPublicParameter pubKey = new IBEPublicParameter();
        ByteArrayInputStream in = new ByteArrayInputStream(raw);
        try {
            pubKey.readExternal(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }
        IBEPlainText plain = IBEPlainText.newIbePlainTextFormSignificantBytes(secret.getBytes());
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            plain.writeExternal(bout);
            byte[] exp = bout.toByteArray();
            System.out.println("plain:");
            System.out.println(Hex.hex(exp));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        IBECipherText cipher = IBEEngine.encrypt(pubKey, plain, testUser);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            cipher.writeExternal(out);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        byte[] cs = out.toByteArray();
        System.out.println("cipher:");
        System.out.println(Hex.hex(cs));

        String hexKey =
            "09993c27fcee62bae4031a6783da9e9c0a8bc67b2764ccd00995baacc3260d20f321ddfc525ff45feb3f1513da1d23660dd7702881bd3b979eb85f4e5bb71265508de720c7ec5467b9b496867ebfe85a43a9328d76f333cc22eab81b4b1429bed24cd2e296d3daaa5b19c689d631a77b918e6e7c534aca70bd961fbe2dc5ce05c1715403e3ffe882b3d95f11a9575542e347dede0000001277616e677965656540676d61696c2e636f6d00000167747970652061207120383738303731303739393636333331323532323433373738313938343735343034393831353830363838333139393431343230383231313032383635333339393236363437353633303838303232323935373037383632353137393432323636323232313432333135353835383736393538323331373435393237373731333336373331373438313332343932353132393939383232343739312068203132303136303132323634383931313436303739333838383231333636373430353334323034383032393534343031323531333131383232393139363135313331303437323037323839333539373034353331313032383434383032313833393036353337373836373736207220373330373530383138363635343531363231333631313139323435353731353034393031343035393736353539363137206578703220313539206578703120313037207369676e312031207369676e30203120";
        byte[] priKeyBuf = Hex.unhex(hexKey);
        ByteArrayInputStream bin = new ByteArrayInputStream(priKeyBuf);
        IBEPrivateKey priKey = new IBEPrivateKey();
        try {
            priKey.readExternal(bin);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }
        IBEPlainText plain1 = IBEEngine.decrypt(cipher, priKey);
        System.out.println(new String(IBEPlainText.getSignificantBytes(plain1)));
    }

    @Test
    public void testDecObjcMsg() {
        String hexCipher =
            "17e2ac4ff757bc09b5a70f434e361e7e2dc8b0b7c0a567882365028ae437d719c1904c08ea8aec407ae6e4781eb96da96a0ef1c71e16849a06f3482cb383600a826b787d810e09782c9a4fdc68bf8d9877cfceb525715d5d35ee1ebe74dca33a569ab4269f4317a1fc70a9dfc4b342c19a3919fe9e7e45cac8fc92f454a97c719c39972df7166a4c5fd22b3d5153815255a873d1f91477b036709c7e3a801d88f315c3cafca695e775881b846c01cdba8e430c4fa3a248b2ad469b90b7130c97885f0cd6dcc7f1056ec682b7bab05f2f56eb56f7eca925cc16e15430e2d1ed55eb51b16e5b971746ad5c3fa9dcc3dbadcf727da54cd705c27d5a93f5e1aec9e77274b7a57b262a6ee917b89085aa25c7c3c57a91cc5878bb39b2860b5116431ee4aba0bc80528a0a94aa68c2b2cea037eb2e21e2b41c477a049044e86e3b0b0e6ee4f552ea95eadcd760d8a88afab2a73b39f92f8b709f2d4fc4d032e62c08b4cf4c70f8cca60b1ea1a33bcb439f4ba6e9300c0233a96aa7eecedf556802c6857c";
        String hexKey =
            "74763adb1936dc8e345efe78b31267aedbd87de02fe98134367d102a32a214b0bf39dca7be04131fadd64f18e5d81981e461b4992685e68d23dc5bb42a730b7e6b49aa9536aed1c7d1ceb89b8cf31cb3cc87148c8f412051bbe2df941bec13d196ebbe8ee01a1ba9430a35625c8d8f6c382a6084c70d84cefa0f61eb409a137cd586e057c7eace331f3647b705d2c3f97e458e1f0000001277616e677965656540676d61696c2e636f6d00000167747970652061207120383738303731303739393636333331323532323433373738313938343735343034393831353830363838333139393431343230383231313032383635333339393236363437353633303838303232323935373037383632353137393432323636323232313432333135353835383736393538323331373435393237373731333336373331373438313332343932353132393939383232343739312068203132303136303132323634383931313436303739333838383231333636373430353334323034383032393534343031323531333131383232393139363135313331303437323037323839333539373034353331313032383434383032313833393036353337373836373736207220373330373530383138363635343531363231333631313139323435353731353034393031343035393736353539363137206578703220313539206578703120313037207369676e312031207369676e30203120";

        byte[] cipherBuf = Hex.unhex(hexCipher);
        ByteArrayInputStream in = new ByteArrayInputStream(cipherBuf);
        IBECipherText cipher = new IBECipherText();
        try {
            cipher.readExternal(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        byte[] priKeyBuf = Hex.unhex(hexKey);
        ByteArrayInputStream bin = new ByteArrayInputStream(priKeyBuf);
        IBEPrivateKey priKey = new IBEPrivateKey();
        try {
            priKey.readExternal(bin);
            bin.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        IBEPlainText plainText = IBEEngine.decrypt(cipher, priKey);
        byte[] sig = IBEPlainText.getSignificantBytes(plainText);
        String expt = Hex.hex(sig);
        System.out.println(expt);
        System.out.println(
            expt.equals("65cf3983fc810a12bf66231a826dd6374b171a5db24b8192e3c951dbdede06714f5c93e1fc013ece70a85b0df3ea365c6f333f6eefeeae408d60219c28b48ef6ef08f6c7a20064f51f29babe3432586ff8126b3f90befddcdf1162624bb071419bd3afedf1123a12100fa4839736cfe73579fa761df472d3f64b7e44"));
    }

    @Test
    public void testKeygen() {
        IBESystemParameter system = IBEEngine.setup(pairing.getBytes());
        IBEPrivateKey key = IBEEngine.keygen(system, testUser);
        try {
            ByteArrayOutputStream sysout = new ByteArrayOutputStream();
            system.getPublicParameter().writeExternal(sysout);
            sysout.flush();
            byte[] pubArray = sysout.toByteArray();
            sysout.close();
            String pub = Hex.hex(pubArray);
            System.out.println("public paramater:");
            System.out.println(pub);
            ByteArrayOutputStream keyout = new ByteArrayOutputStream();
            key.writeExternal(keyout);
            keyout.flush();
            byte[] keyArray = keyout.toByteArray();
            keyout.close();
            String keystr = Hex.hex(keyArray);
            System.out.println("private key:");
            System.out.println(keystr);
            System.out.println("owner:");
            System.out.println(testUser);
        } catch (IOException e) {
        }
    }
}
