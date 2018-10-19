package hamaster.gradesign.keygen.key;

public interface SecureKeyIO {

    byte[] getSystemAccessPassword(int systemId);
}
