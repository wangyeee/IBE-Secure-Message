package hamaster.gradesign.dao.impl;

import hamaster.gradesign.dao.DAO;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public abstract class DAOSupport implements DAO {

	protected SessionFactory factory;

	protected DAOSupport() {
	}

	@Override
	public <T> T get(Class<T> entityClass, Serializable primaryKey) {
		Session session = factory.openSession();
		@SuppressWarnings("unchecked")
		T t = (T) session.get(entityClass, primaryKey);
		return t;
	}

	@Override
	public <T> List<T> list(Class<T> entityClass, int page, int amount) {
		Session session = factory.openSession();
		Criteria criteria = session.createCriteria(entityClass);
		if (amount > 0) {
			criteria.setFetchSize(amount);
			criteria.setFirstResult(page * amount);
		}
		@SuppressWarnings("unchecked")
		List<T> r = criteria.list();
		return r;
	}

	@Override
	public void add(Object entity) {
		Session session = factory.openSession();
		session.beginTransaction();
		session.save(entity);
		session.getTransaction().commit();
		if (session.isOpen())
			session.close();
	}

	@Override
	public void update(Object entity) {
		Session session = factory.openSession();
		session.beginTransaction();
		session.merge(entity);
		session.getTransaction().commit();
		if (session.isOpen())
			session.close();
	}

	@Override
	public void delete(Object entity) {
		Session session = factory.openSession();
		session.beginTransaction();
		session.delete(session.merge(entity));
		session.getTransaction().commit();
		if (session.isOpen())
			session.close();
	}

	public void setFactory(SessionFactory factory) {
		this.factory = factory;
	}
}
