package hamaster.gradesign.dao.impl;

import hamaster.gradesgin.util.Hash;
import hamaster.gradesign.IBECSR;
import hamaster.gradesign.dao.IDRequestDAO;
import hamaster.gradesign.entity.IDRequest;
import hamaster.gradesign.entity.User;
import hamaster.gradesign.ibe.util.Hex;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.jdbc.Work;

public abstract class IDRequestDAOHibernateImpl extends DAOSupport implements IDRequestDAO {

	public IDRequestDAOHibernateImpl() {
	}

	/*
	 * (non-Javadoc)
	 * @see hamaster.gradesign.dao.IDRequestDAO#getByOwner(hamaster.gradesign.entity.User, java.lang.String)
	 */
	@Override
	public IDRequest getByOwner(User owner, String idString) {
		Session session = factory.openSession();
		Criteria criteria = session.createCriteria(IDRequest.class)
				.add(Restrictions.eq("applicant", owner))
				.add(Restrictions.eq("identityString", idString))
				.setMaxResults(1);
		List<?> res = criteria.list();
		if (res.size() == 0)
			return null;
		return (IDRequest) res.get(0);
	}

	/*
	 * (non-Javadoc)
	 * @see hamaster.gradesign.dao.IDRequestDAO#listNewRequests(int)
	 */
	@Override
	public List<IDRequest> listNewRequests(int amount) {
		Session session = factory.openSession();
		Criteria criteria = session.createCriteria(IDRequest.class)
				.add(Restrictions.eq("status", Integer.valueOf(IBECSR.APPLICATION_NOT_VERIFIED)))
				.setMaxResults(amount);
		@SuppressWarnings("unchecked")
		List<IDRequest> res = criteria.list();
		return res;
	}

	/*
	 * (non-Javadoc)
	 * @see hamaster.gradesign.dao.IDRequestDAO#list(hamaster.gradesign.entity.User, int, int)
	 */
	@Override
	public List<IDRequest> list(User owner, int page, int amount) {
		Session session = factory.openSession();
		Criteria criteria = session.createCriteria(IDRequest.class)
				.setFirstResult(page * amount)
				.setMaxResults(amount);
		@SuppressWarnings("unchecked")
		List<IDRequest> res = criteria.list();
		return res;
	}

	/*
	 * (non-Javadoc)
	 * @see hamaster.gradesign.dao.IDRequestDAO#list(hamaster.gradesign.entity.User, int, int, int)
	 */
	@Override
	public List<IDRequest> list(User owner, int page, int amount, int status) {
		Session session = factory.openSession();
		Criteria criteria = session.createCriteria(IDRequest.class)
				.add(Restrictions.eq("applicant", owner))
				.setFirstResult(page * amount)
				.setMaxResults(amount);
		if (status > 0)
			criteria.add(Restrictions.eq("status", status));
		@SuppressWarnings("unchecked")
		List<IDRequest> res = criteria.list();
		return res;
	}

	/*
	 * (non-Javadoc)
	 * @see hamaster.gradesign.dao.IDRequestDAO#count(hamaster.gradesign.entity.User)
	 */
	@Override
	public int count(User onwer) {
		Session session = factory.openSession();
		Number n = (Number) session.createCriteria(IDRequest.class)
				.add(Restrictions.eq("applicant", onwer))
				.setProjection(Projections.rowCount())
				.uniqueResult();
		int res = n.intValue();
		session.disconnect();
		return res;
	}

	/*
	 * (non-Javadoc)
	 * @see hamaster.gradesign.dao.IDRequestDAO#listUnhandledRequests(int)
	 */
	@Override
	public List<IDRequest> listUnhandledRequests(int amount) {
		Session session = factory.openSession();
		Criteria criteria = session.createCriteria(IDRequest.class)
				.add(Restrictions.eq("status", IBECSR.APPLICATION_STARTED))
				.setMaxResults(amount);
		@SuppressWarnings("unchecked")
		// TODO 会出现关闭数据库连接后访问的情况
		List<IDRequest> res = criteria.list();
		return res;
	}

	/*
	 * (non-Javadoc)
	 * @see hamaster.gradesign.dao.IDRequestDAO#requestHandled(java.util.Map)
	 */
	public void requestHandled(Map<String, Integer> results) {
		Session session = factory.openSession();
		final Map<String, Integer> results0 = results;
		session.doWork(new Work() {
			@Override
			public void execute(Connection connection) throws SQLException {
				requestHandledDBMSSpecImpl(connection, results0);
			}
		});
		if (session.isConnected())
			session.disconnect();
	}

	/**
	 * 处理结果的数据库相关实现
	 * @param conn 数据库连接
	 * @param results 要处理的结果
	 * @throws SQLException 发生SQL异常 如数据库不支持加锁或数据更新时发生异常
	 */
	protected abstract void requestHandledDBMSSpecImpl(Connection conn, Map<String, Integer> results) throws SQLException;

	/*
	 * (non-Javadoc)
	 * @see hamaster.gradesign.dao.IDRequestDAO#doesIdBelongToUser(java.lang.String, hamaster.gradesign.entity.User, java.lang.String)
	 */
	@Override
	public int doesIdBelongToUser(String id, User user, String idPassword) {
		Session session = factory.openSession();
		Criteria criteria = session.createCriteria(IDRequest.class)
				.add(Restrictions.eq("applicant", user))
				.add(Restrictions.eq("identityString", id))
				.setMaxResults(1);
		List<?> result = criteria.list();
		if (result.size() == 0) {
			session.disconnect();
			return 2;	// 用户尝试获取不属于自己的ID
		}
		IDRequest req = (IDRequest) result.iterator().next();
		String hash = req.getPassword();
		String exptHash = Hex.hex(Hash.sha1(idPassword));
		session.disconnect();
		if (!exptHash.equalsIgnoreCase(hash))
			return 1;	// 密码错误
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see hamaster.gradesign.dao.IDRequestDAO#doesIdRequestExist(java.lang.String)
	 */
	@Override
	public int doesIdRequestExist(String id) {
		Session session = factory.openSession();
		Criteria criteria = session.createCriteria(IDRequest.class)
				.add(Restrictions.eq("identityString", id))
				.add(Restrictions.not(Restrictions.eq("status", IBECSR.APPLICATION_NOT_VERIFIED)))
				.setMaxResults(1);
		List<?> res = criteria.list();
		int ret = 0;
		if (res.size() > 0) {
			IDRequest req = (IDRequest) res.iterator().next();
			ret = req.getStatus() + 1;
		}
		session.disconnect();
		return ret;
	}
}
