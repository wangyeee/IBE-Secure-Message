package hamaster.gradesign;

public interface SecureConstraints {

	String DATABASE_CRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding";

	int KEY_LENGTH_IN_BYTES = 32;

	int IV_LENGTH_IN_BYTES = 16;
}
