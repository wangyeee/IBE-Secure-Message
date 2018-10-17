package hamaster.gradesign.keydist.client;

public interface Encoder {

    String encode(byte[] data);

    byte[] decode(String code);

    String name();
}
