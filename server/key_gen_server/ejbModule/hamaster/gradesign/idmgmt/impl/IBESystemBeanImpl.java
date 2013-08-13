package hamaster.gradesign.idmgmt.impl;

import hamaster.gradesgin.ibe.IBESystemParameter;
import hamaster.gradesgin.ibe.core.IBEEngine;
import hamaster.gradesgin.ibs.IBSCertificate;
import hamaster.gradesgin.util.Hash;
import hamaster.gradesign.IBESystem;
import hamaster.gradesign.SecureConstraints;
import hamaster.gradesign.entity.IBESystemEntity;
import hamaster.gradesign.ibe.util.Hex;
import hamaster.gradesign.idmgmt.IBESystemBean;
import hamaster.gradesign.idmgmt.IBESystemBeanLocal;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

/**
 * IBESystemBeanImpl
 */
@Stateless(mappedName = "IBESystemBean")
public class IBESystemBeanImpl extends CommonDAOImpl implements IBESystemBean, IBESystemBeanLocal {

	public IBESystemBeanImpl() {
	}

	/*
	 * (non-Javadoc)
	 * @see hamaster.gradesign.idmgmt.IBESystemBean#list(int, int)
	 */
	@Override
	public Map<Integer, String> list(int page, int amount) {
		EntityManager manager = factory.createEntityManager();
		CriteriaBuilder cb = manager.getCriteriaBuilder();
		CriteriaQuery<IBESystemEntity> query = cb.createQuery(IBESystemEntity.class);
		query.from(IBESystemEntity.class);
		List<IBESystemEntity> systems = manager.createQuery(query)
				.setFirstResult(page * amount)
				.setMaxResults(amount).getResultList();
		Map<Integer, String> results = new HashMap<Integer, String>((int) (systems.size() * 1.4));
		for (IBESystemEntity system : systems)
			results.put(system.getSystemId(), system.getSystemOwner());
		return results;
	}

	/*
	 * (non-Javadoc)
	 * @see hamaster.gradesign.idmgmt.IBESystemBean#totalSystems()
	 */
	@Override
	public int totalSystems() {
		EntityManager manager = factory.createEntityManager();
		String jpql = "select count(s) as syscounts from IBESystemEntity s";
		Query query = manager.createQuery(jpql);
		Integer syscounts = (Integer) query.getSingleResult();
		return syscounts.intValue();
	}

	@Override
	public void generateDemoSystem() {
		String owner = "IBE_SERVER";
		String pairing = "type a q 8780710799663312522437781984754049815806883199414208211028653399266475630880222957078625179422662221423155858769582317459277713367317481324925129998224791 h 12016012264891146079388821366740534204802954401251311822919615131047207289359704531102844802183906537786776 r 730750818665451621361119245571504901405976559617 exp2 159 exp1 107 sign1 1 sign0 1 ";
		IBESystemParameter systemParameter = IBEEngine.setup(pairing.getBytes());
		IBSCertificate fake = new IBSCertificate();
		fake.setPublicParameter(systemParameter.getPublicParameter());
		IBSCertificate certificate = IBEEngine.generateCertificate(owner, fake, new Date(), Integer.MAX_VALUE);
		IBESystem system = new IBESystem();

		byte[] keyIV = new byte[SecureConstraints.IV_LENGTH_IN_BYTES + SecureConstraints.KEY_LENGTH_IN_BYTES];
		byte[] key = Hash.sha256(getSystemAccessPassword(0));
		byte[] iv = Hash.md5(getSystemAccessPassword(0));
		System.arraycopy(key, 0, keyIV, 0, key.length);
		System.arraycopy(iv, 0, keyIV, key.length, iv.length);
		system.setCryptionKeyAndIV(keyIV);
		system.setCertificate(certificate);
		system.setParameter(systemParameter);

		IBESystemEntity entity = new IBESystemEntity();
		entity.setSystem(system);
		entity.setSystemOwner(owner);
		entity.setSystemKeyHash(Hex.hex(Hash.sha1(getSystemAccessPassword(0))));
		save(entity);
	}
}
