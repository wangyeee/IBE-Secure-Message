package hamaster.gradesign.keygen.idmgmt.impl;

import static java.util.Objects.requireNonNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import hamaster.gradesgin.ibe.IBECipherText;
import hamaster.gradesgin.ibe.IBEPlainText;
import hamaster.gradesgin.ibe.IBEPrivateKey;
import hamaster.gradesgin.ibe.core.IBEEngine;
import hamaster.gradesgin.ibs.IBSCertificate;
import hamaster.gradesgin.util.Hash;
import hamaster.gradesgin.util.Hex;
import hamaster.gradesign.keygen.IBECSR;
import hamaster.gradesign.keygen.IBESystem;
import hamaster.gradesign.keygen.IdentityDescription;
import hamaster.gradesign.keygen.entity.IBESystemEntity;
import hamaster.gradesign.keygen.entity.IdentityDescriptionEntity;
import hamaster.gradesign.keygen.idmgmt.IBESystemBean;
import hamaster.gradesign.keygen.idmgmt.IdentityDescriptionBean;
import hamaster.gradesign.keygen.key.SecureKeyIO;
import hamaster.gradesign.keygen.repo.IBESystemRepository;
import hamaster.gradesign.keygen.repo.IdentityDescriptionRepository;

/**
 * IdentityDescriptionBeanImpl
 */
@Service
public class IdentityDescriptionBeanImpl implements IdentityDescriptionBean {

    private IdentityDescriptionRepository idRepo;
    private IBESystemRepository sysRepo;
    private SecureKeyIO secureKeyIO;
    private IBESystemBean systemBean;

    @Autowired
    public IdentityDescriptionBeanImpl(IdentityDescriptionRepository repo,
            IBESystemRepository sysRepo, SecureKeyIO secureKeyIO, IBESystemBean systemBean) {
        this.idRepo = requireNonNull(repo);
        this.sysRepo = requireNonNull(sysRepo);
        this.secureKeyIO = requireNonNull(secureKeyIO);
        this.systemBean = requireNonNull(systemBean);
    }

    /*
     * (non-Javadoc)
     * @see hamaster.gradesign.idmgmt.IdentityDescriptionBean#get(java.lang.String)
     */
    @Override
    public IdentityDescriptionEntity get(String owner) {
        IdentityDescriptionEntity id = new IdentityDescriptionEntity();
        id.setIdOwner(owner);
        Optional<IdentityDescriptionEntity> opt = idRepo.findOne(Example.of(id));
        if (opt.isPresent())
            return opt.get();
        return null;
    }

    @Override
    public IdentityDescriptionEntity get(String owner, String system) {
        Optional<IdentityDescriptionEntity> id = idRepo.findOneByOwnerAndSystem(owner, system);
        if (id.isPresent())
            return id.get();
        return null;
    }

    /*
     * (non-Javadoc)
     * @see hamaster.gradesign.idmgmt.IdentityDescriptionBean#changeEncryptionKey(java.lang.Integer, java.lang.String, java.lang.String)
     */
    @Override
    public Future<IdentityDescriptionEntity> changeEncryptionKey(Integer id, String oldKey, String newKey) {
        IdentityDescriptionEntity newId = changeEncryptionKeySync(id, oldKey, newKey);
        Future<IdentityDescriptionEntity> description = new AsyncResult<IdentityDescriptionEntity>(newId);  // TODO test
        return description;
    }

    @Override
    public Future<Map<String, Integer>> generateIdentityDescriptions(List<IBECSR> requests) {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        Future<Map<String, Integer>> future = executor.submit(() -> generateIdentityDescriptionsSync(requests));
        return future;
    }

    @Override
    public Map<String, Integer> generateIdentityDescriptionsSync(List<IBECSR> requests) {
        Collection<IdentityDescriptionEntity> objs = new ArrayList<IdentityDescriptionEntity>(requests.size());
        Map<String, Integer> map = new HashMap<String, Integer>(requests.size());
        for (IBECSR csr : requests) {
            try {
                IdentityDescriptionEntity id = generateIdentityDescriptionForUser(
                        csr.getIdentityString(),
                        csr.getPassword(),
                        csr.getIbeSystemId(),
                        csr.getApplicationDate(),
                        csr.getPeriod());
                objs.add(id);
                map.put(csr.getIdentityString(), IBECSR.APPLICATION_APPROVED);
            } catch (Exception e) {
                map.put(csr.getIdentityString(), IBECSR.APPLICATION_ERROR);
            }
        }
        idRepo.saveAll(objs);
        return map;
    }

    @Override
    public IdentityDescriptionEntity generateSingleIdentityDescriptionEntity(IBECSR csr) {
        IdentityDescriptionEntity id = generateIdentityDescriptionForUser(
                csr.getIdentityString(),
                csr.getPassword(),
                csr.getIbeSystemId(),
                csr.getApplicationDate(),
                csr.getPeriod());
        idRepo.save(id);
        return id;
    }

    @Override
    public IdentityDescriptionEntity changeEncryptionKeySync(Integer id, String oldKey, String newKey) {
        IdentityDescriptionEntity mod = idRepo.getOne(id);
        IdentityDescription data = mod.getIdentityDescription(oldKey.getBytes());
        mod.setIdentityDescription(data, newKey.getBytes());
        IdentityDescriptionEntity newId = idRepo.save(mod);
        return newId;
    }

    private IdentityDescriptionEntity generateIdentityDescriptionForUser(String owner,
            byte[] userPassword, Integer systemId, Date validAfter, long period) {
        IdentityDescription id = new IdentityDescription();
        IBESystemEntity system = sysRepo.getOne(systemId);
        String sha512 = Hex.hex(Hash.sha512(secureKeyIO.getSystemAccessPassword(systemId)));
        if (!system.getSystemKeyHash().equalsIgnoreCase(sha512)) {
            return null;
        }
        IBESystem sys = system.getSystem(secureKeyIO.getSystemAccessPassword(systemId));

        // 声称私钥和签名证书
        IBEPrivateKey privateKey = IBEEngine.keygen(sys.getParameter(), owner);
        IBSCertificate certificate = IBEEngine.generateCertificate(owner, sys.getCertificate(), validAfter, period);
        id.setSystemPublicParameter(sys.getParameter().getPublicParameter());
        id.setPrivateKey(privateKey);
        id.setCertificate(certificate);

        // 加密存储
        IdentityDescriptionEntity idCon = new IdentityDescriptionEntity();
        idCon.setIdOwner(owner);
        idCon.setSystem(system);
        idCon.setIdentityDescription(id, decryptSessionKeyWithServerKey(userPassword, systemBean.getPrivateKeyForSystem(systemId)));
        return idCon;
    }

    /**
     * Any session key (AES 256) sent to the key generation server is encrypted
     * with the server private IBE key, this method decrypts the key contents.<br>
     * @param sessionKey The encrypted session key
     * @param serverPrivateKey the IBE private key for the given system
     * @return the session key in plain text
     */
    private byte[] decryptSessionKeyWithServerKey(byte[] sessionKey, IBEPrivateKey serverPrivateKey) {
        ByteArrayInputStream in = new ByteArrayInputStream(sessionKey);
        IBECipherText cipher = new IBECipherText();
        try {
            cipher.readExternal(in);
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
            }
        }
        IBEPlainText plain = IBEEngine.decrypt(cipher, serverPrivateKey);
        return IBEPlainText.getSignificantBytes(plain);
    }
}
