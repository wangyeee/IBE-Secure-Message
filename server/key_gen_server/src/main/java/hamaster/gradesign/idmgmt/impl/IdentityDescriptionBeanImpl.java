package hamaster.gradesign.idmgmt.impl;

import static java.util.Objects.requireNonNull;

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

import hamaster.gradesgin.ibe.IBEPrivateKey;
import hamaster.gradesgin.ibe.core.IBEEngine;
import hamaster.gradesgin.ibs.IBSCertificate;
import hamaster.gradesgin.util.Hash;
import hamaster.gradesign.IBECSR;
import hamaster.gradesign.IBESystem;
import hamaster.gradesign.IdentityDescription;
import hamaster.gradesign.SecureConstraints;
import hamaster.gradesign.entity.IBESystemEntity;
import hamaster.gradesign.entity.IdentityDescriptionEntity;
import hamaster.gradesign.ibe.util.Hex;
import hamaster.gradesign.idmgmt.IdentityDescriptionBean;
import hamaster.gradesign.key.SecureKeyIO;
import hamaster.gradesign.repo.IBESystemRepository;
import hamaster.gradesign.repo.IdentityDescriptionRepository;

/**
 * IdentityDescriptionBeanImpl
 */
@Service
public class IdentityDescriptionBeanImpl implements IdentityDescriptionBean {

    private IdentityDescriptionRepository idRepo;
    private IBESystemRepository sysRepo;
    private SecureKeyIO secureKeyIO;

    @Autowired
    public IdentityDescriptionBeanImpl(IdentityDescriptionRepository repo,
            IBESystemRepository sysRepo, SecureKeyIO secureKeyIO) {
        this.idRepo = requireNonNull(repo);
        this.sysRepo = requireNonNull(sysRepo);
        this.secureKeyIO = requireNonNull(  secureKeyIO);
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
        IdentityDescriptionBeanImpl.this.idRepo.saveAll(objs);
        return map;
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
            String userPassword, Integer systemId, Date validAfter, long period) {
        IdentityDescription id = new IdentityDescription();
        IBESystemEntity system = sysRepo.getOne(systemId);
        String sha512 = Hex.hex(Hash.sha512(secureKeyIO.getSystemAccessPassword(systemId)));
        if (!system.getSystemKeyHash().equalsIgnoreCase(sha512)) {
            return null;
        }
        byte[] keyIV0 = new byte[SecureConstraints.IV_LENGTH_IN_BYTES + SecureConstraints.KEY_LENGTH_IN_BYTES];
        byte[] key0 = Hash.sha256(secureKeyIO.getSystemAccessPassword(systemId));
        byte[] iv0 = Hash.md5(secureKeyIO.getSystemAccessPassword(systemId).getBytes());
        System.arraycopy(key0, 0, keyIV0, 0, key0.length);
        System.arraycopy(iv0, 0, keyIV0, key0.length, iv0.length);
    
        IBESystem sys = system.getSystem(keyIV0);
    
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
        idCon.setIdentityDescription(id, userPassword.getBytes());
        return idCon;
    }
}
