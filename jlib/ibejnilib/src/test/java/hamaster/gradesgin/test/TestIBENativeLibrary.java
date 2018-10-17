package hamaster.gradesgin.test;

import static hamaster.gradesign.ibe.IBELibrary.decrypt;
import static hamaster.gradesign.ibe.IBELibrary.encrypt;
import static hamaster.gradesign.ibe.IBELibrary.keygen;
import static hamaster.gradesign.ibe.IBELibrary.setup;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class TestIBENativeLibrary {

    String param =
        "type a q 8780710799663312522437781984754049815806883199414208211028653399266475630880222957078625179422662221423155858769582317459277713367317481324925129998224791 h 12016012264891146079388821366740534204802954401251311822919615131047207289359704531102844802183906537786776 r 730750818665451621361119245571504901405976559617 exp2 159 exp1 107 sign1 1 sign0 1 ";
    String data =
        "00023065cf3983fc810a12bf66231a826dd6374b171a5db24b8192e3c951dbdede06714f5c93e1fc013ece70a85b0df3ea365c6f333f6eefeeae408d60219c2800b48ef6ef08f6c7a20064f51f29babe3432586ff8126b3f90befddcdf1162624bb071419bd3afedf1123a12100fa4839736cfe73579fa761df472d3f64b7e44";
    String user = "wangyeee@gmail.com";

    @Test
    public void test() {
        byte[] pairing_str_in = param.getBytes();
        byte[] h_out = new byte[128];
        byte[] g1_out = new byte[128];
        byte[] g_out = new byte[128];
        byte[] alpha_out = new byte[20];
        int i = setup(alpha_out, g_out, g1_out, h_out, pairing_str_in);

        byte[] hID_out = new byte[128];
        byte[] rID_out = new byte[20];
        i = keygen(hID_out, rID_out, user.getBytes(), alpha_out, g_out, h_out, pairing_str_in);

        byte[] cipher_buffer_out = new byte[128 * 3];
        byte[] plain_in = unhex(data);
        byte[] alice_in = user.getBytes();
        i = encrypt(cipher_buffer_out, plain_in, g_out, g1_out, h_out, alice_in, pairing_str_in);

        byte[] plain_buffer_out = new byte[128];
        i = decrypt(plain_buffer_out, cipher_buffer_out, rID_out, hID_out, pairing_str_in);
        System.out.println("result:" + i);
        String dec = hex(plain_buffer_out);
        System.out.println("[dec]\n" + dec);
        assertEquals(dec, data);
    }

    private final static String hex(byte[] data) {
        return hex(data, 0, data.length);
    }

    private final static String hex(byte[] data, int offset, int count) {
        if (offset < 0)
            throw new IllegalArgumentException(new StringBuilder("invalid offset:").append(offset).toString());
        if (count < 0)
            throw new IllegalArgumentException(new StringBuilder("invalid count:").append(offset).toString());
        if (offset > data.length - count)
            throw new ArrayIndexOutOfBoundsException(offset + count);
        ByteArrayOutputStream out = new ByteArrayOutputStream(data.length + data.length);
        PrintStream writer = new PrintStream(out);
        for (int i = offset; i < offset + count; i++) {
            writer.printf("%02x", data[i]);
        }
        return out.toString();
    }

    private final static byte[] unhex(String hex) {
        char[] tb = hex.toUpperCase().toCharArray();
        List<Byte> bs = new ArrayList<Byte>();
        for (int i = 0; i < tb.length; i += 2) {
            String t = new String();
            t += tb[i];
            t += tb[i + 1];
            int safe = Integer.parseInt(t, 16);
            if (safe > 127)
                safe -= 256;
            bs.add(Byte.parseByte(String.valueOf(safe)));
        }
        byte[] raw = new byte[bs.size()];
        for (int i = 0; i < bs.size(); i++)
            raw[i] = bs.get(i);
        return raw;
    }
}
