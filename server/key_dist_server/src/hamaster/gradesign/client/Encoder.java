package hamaster.gradesign.client;

public interface Encoder {

	String encode(byte[] data);

	byte[] decode(String code);

	String name();
}
