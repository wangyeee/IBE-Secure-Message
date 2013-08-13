package hamaster.gradesign.dao.impl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import hamaster.gradesgin.util.Hash;
import hamaster.gradesign.daemon.EJBClient;
import hamaster.gradesign.dao.UserDAO;
import hamaster.gradesign.entity.User;
import hamaster.gradesign.ibe.util.Hex;

public class UserDAOHibernateImpl extends DAOSupport implements UserDAO {

	public UserDAOHibernateImpl() {
	}

	/*
	 * (non-Javadoc)
	 * @see hamaster.gradesign.dao.UserDAO#isEmailExist(java.lang.String)
	 */
	@Override
	public boolean isEmailExist(String email) {
		Session session = factory.openSession();
		Criteria criteria = session.createCriteria(User.class)
				.add(Restrictions.eq("email", email))
				.setMaxResults(1);
		List<?> res = criteria.list();
		boolean exist = res.size() != 0;
		session.disconnect();
		return exist;
	}

	/*
	 * (non-Javadoc)
	 * @see hamaster.gradesign.dao.UserDAO#login(java.lang.String, java.lang.String)
	 */
	@Override
	public User login(String email, String password) {
		Session session = factory.openSession();
		Criteria criteria = session.createCriteria(User.class)
				.add(Restrictions.eq("email", email))
				.setMaxResults(1);
		List<?> res = criteria.list();
		if (res.size() == 0) {
			session.disconnect();
			return null;
		}
		User user = (User) res.iterator().next();
		String salt = EJBClient.util.format(user.getRegDate());
		byte[] hash = Hash.sha1(new StringBuilder(password).append(salt).toString());
		if (Hex.hex(hash).equalsIgnoreCase(user.getPassword()))
			return user;
		session.disconnect();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see hamaster.gradesign.dao.UserDAO#listNewRegisteredUsers(int)
	 */
	@Override
	public List<User> listNewRegisteredUsers(int amount) {
		Session session = factory.openSession();
		Criteria criteria = session.createCriteria(User.class)
				.add(Restrictions.eq("status", Integer.valueOf(User.USER_REG)))
				.setMaxResults(amount);
		@SuppressWarnings("unchecked")
		List<User> res = criteria.list();
		return res;
	}
}
