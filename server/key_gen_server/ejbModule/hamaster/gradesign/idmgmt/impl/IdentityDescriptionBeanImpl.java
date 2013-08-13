package hamaster.gradesign.idmgmt.impl;

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
import hamaster.gradesign.idmgmt.IdentityDescriptionBeanLocal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.ejb.AsyncResult;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * IdentityDescriptionBeanImpl
 */
@Stateless(mappedName = "IdentityDescriptionBean")
public class IdentityDescriptionBeanImpl extends CommonDAOImpl implements IdentityDescriptionBean, IdentityDescriptionBeanLocal {

    public IdentityDescriptionBeanImpl() {
    }

    /*
     * (non-Javadoc)
     * @see hamaster.gradesign.idmgmt.IdentityDescriptionBean#get(java.lang.String)
     */
    @Override
    public IdentityDescriptionEntity get(String owner) {
    	EntityManager manager = factory.createEntityManager();
    	CriteriaBuilder cb = manager.getCriteriaBuilder();
		CriteriaQuery<IdentityDescriptionEntity> query = cb.createQuery(IdentityDescriptionEntity.class);
		Root<IdentityDescriptionEntity> root = query.from(IdentityDescriptionEntity.class);
		Predicate condition = cb.equal(root.get("idOwner"), owner);
		query.where(condition);
    	return manager.createQuery(query).getSingleResult();
    }

	/*
	 * (non-Javadoc)
	 * @see hamaster.gradesign.idmgmt.IdentityDescriptionBean#changeEncryptionKey(java.lang.Integer, java.lang.String, java.lang.String)
	 */
	@Override
	public Future<IdentityDescriptionEntity> changeEncryptionKey(Integer id, String oldKey, String newKey) {
		IdentityDescriptionEntity mod = find(IdentityDescriptionEntity.class, id);

    	IdentityDescription data = mod.getIdentityDescription(oldKey.getBytes());
//		data.setCryptionKeyAndIV(keyIV);
		mod.setIdentityDescription(data, newKey.getBytes());
//		mod.setIdentityDescription(data);

		IdentityDescriptionEntity newId = (IdentityDescriptionEntity) update(mod);
		Future<IdentityDescriptionEntity> description = new AsyncResult<IdentityDescriptionEntity>(newId);
//		MemoryUtil.fastSecureBuffers(keyIV0, key0, iv0, keyIV, key, iv);
		return description;
	}

	@Override
	public Future<Map<String, Integer>> generateIdentityDescriptions(List<IBECSR> requests) {
		final List<IBECSR> requests0 = requests;
		ExecutorService executor = Executors.newFixedThreadPool(1);
		Future<Map<String, Integer>> future = executor.submit(new Callable<Map<String, Integer>>() {
			@Override
			public Map<String, Integer> call() throws Exception {
				Collection<IdentityDescriptionEntity> objs = new ArrayList<IdentityDescriptionEntity>(requests0.size());
				Map<String, Integer> map = new HashMap<String, Integer>(requests0.size());
				for (IBECSR csr : requests0) {
					try {
						IdentityDescriptionEntity id = generateIdentityDescriptionForUser(csr.getIdentityString(), csr.getPassword(), csr.getIbeSystemId(), csr.getApplicationDate(), csr.getPeriod());
						objs.add(id);
						map.put(csr.getIdentityString(), IBECSR.APPLICATION_APPROVED);
					} catch (Exception e) {
						map.put(csr.getIdentityString(), IBECSR.APPLICATION_ERROR);
						e.printStackTrace(); // TODO debug
					}
				}
				batchSave(objs);
				return map;
			}
		});
		return future;
	}

	private IdentityDescriptionEntity generateIdentityDescriptionForUser(String owner, String userPassword, Integer systemId, Date validAfter, long period) {
		EntityManager manager = factory.createEntityManager();
		IdentityDescription id = new IdentityDescription();
		IBESystemEntity system = manager.find(IBESystemEntity.class, systemId);
		String sha1 = Hex.hex(Hash.sha1(getSystemAccessPassword(systemId)));
		if (!system.getSystemKeyHash().equalsIgnoreCase(sha1)) {
			return null;
		}
		byte[] keyIV0 = new byte[SecureConstraints.IV_LENGTH_IN_BYTES + SecureConstraints.KEY_LENGTH_IN_BYTES];
    	byte[] key0 = Hash.sha256(getSystemAccessPassword(systemId));
    	byte[] iv0 = Hash.md5(getSystemAccessPassword(systemId));
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
