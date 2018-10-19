package hamaster.gradesign.keygen.key;

import org.springframework.stereotype.Component;

@Component
public class DummySecureKeyIOImpl implements SecureKeyIO {

    public DummySecureKeyIOImpl() {
    }

    @Override
    public byte[] getSystemAccessPassword(int systemId) {
        return "sjdfu838g9n?:{,;[]=-`1-29gyudfugnfdi93990(*dgf^%fgd$&45g*325(".getBytes();
    }
}
