package hamaster.gradesign.idmgmt.impl;

import hamaster.gradesign.idmgmt.CommonDAO;

import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceUnit;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

/**
 * 提供一个通用接口的默认实现
 * @author <a href="mailto:wangyeee@gmail.com">Wang Ye</a>
 */
public abstract class CommonDAOImpl implements CommonDAO {

	/**
	 * 实体管理器
	 */
	@PersistenceUnit(unitName="ibe")
	protected EntityManagerFactory factory;

	protected CommonDAOImpl() {
	}

	/*
	 * (non-Javadoc)
	 * @see hamaster.gradesign.idmgmt.CommonDAO#delete(java.lang.Class, java.lang.Object)
	 */
	@Override
	public void delete(Class<?> entityClass, Object primaryKey) {
		EntityManager manager = factory.createEntityManager();
		EntityTransaction et = manager.getTransaction();
		et.begin();
		manager.remove(manager.getReference(entityClass, primaryKey));
		et.commit();
	}

	/*
	 * (non-Javadoc)
	 * @see hamaster.gradesign.idmgmt.CommonDAO#find(java.lang.Class, java.lang.Object)
	 */
	@Override
	public <T> T find(Class<T> entityClass, Object primaryKey) {
		EntityManager manager = factory.createEntityManager();
		return manager.find(entityClass, primaryKey);
	}

	/*
	 * (non-Javadoc)
	 * @see hamaster.gradesign.idmgmt.CommonDAO#save(java.lang.Object)
	 */
	@Override
	public void save(Object entity) {
		EntityManager manager = factory.createEntityManager();
		EntityTransaction et = manager.getTransaction();
		et.begin();
		manager.persist(entity);
		et.commit();
	}

	/*
	 * (non-Javadoc)
	 * @see hamaster.gradesign.idmgmt.CommonDAO#save(java.util.Collection)
	 */
	@Override
	public void batchSave(Collection<?> entities) {
		EntityManager manager = factory.createEntityManager();
		FlushModeType orig = manager.getFlushMode();
		manager.setFlushMode(FlushModeType.COMMIT);
		EntityTransaction et = manager.getTransaction();
		et.begin();
		for (Object entity : entities)
			manager.persist(entity);
		et.commit();
		manager.setFlushMode(orig);
	}

	/*
	 * (non-Javadoc)
	 * @see hamaster.gradesign.idmgmt.CommonDAO#update(java.lang.Object)
	 */
	@Override
	public Object update(Object entity) {
		EntityManager manager = factory.createEntityManager();
		EntityTransaction et = manager.getTransaction();
		et.begin();
		Object merge = manager.merge(entity);
		et.commit();
		return merge;
	}

	/*
	 * (non-Javadoc)
	 * @see hamaster.gradesign.idmgmt.CommonDAO#list(java.lang.Class, int, int)
	 */
	@Override
	public <T> List<T> list(Class<T> entityClass, int page, int amountPerPage) {
		EntityManager manager = factory.createEntityManager();
		CriteriaBuilder cb = manager.getCriteriaBuilder();
		CriteriaQuery<T> query = cb.createQuery(entityClass);
		return manager.createQuery(query).
				setFirstResult(page * amountPerPage).
				setMaxResults(amountPerPage).
				getResultList();
	}

	protected static String getSystemAccessPassword(int systemId) {
		// TODO 实现一个从外部读取系统密钥的方法
		return "sjdfu838g9n?:{,;[]=-`1-29gyudfugnfdi93990(*dgf^%fgd$&45g*325(";
	}

	public void setFactory(EntityManagerFactory factory) {
		this.factory = factory;
	}
}
