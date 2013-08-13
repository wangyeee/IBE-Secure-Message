package hamaster.gradesign.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public class IDRequestDAOHibernateAndMySQLImpl extends IDRequestDAOHibernateImpl {

	public IDRequestDAOHibernateAndMySQLImpl() {
	}

	/**
	 * 在MySQL下更新处理结果的实现
	 * @see hamaster.gradesign.dao.impl.IDRequestDAOHibernateImpl#requestHandledDBMSSpecImpl(java.sql.Connection, java.util.Map)
	 */
	@Override
	protected void requestHandledDBMSSpecImpl(Connection conn, Map<String, Integer> results) throws SQLException {
		String sql = "UPDATE IBE_ID_REQUEST SET PASSWORD=SHA1(PASSWORD), APPLICATION_STATUS=? WHERE IDENTITY_STRING=? AND APPLICATION_STATUS<2";
		boolean orig = conn.getAutoCommit();
		conn.setAutoCommit(false);
		// 更新处理结果的过程中加锁
		// 防止其它线程脏读、不可重复读、幻读
		conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
		PreparedStatement pstmt = conn.prepareStatement(sql);
		for (String user : results.keySet()) {
			int status = results.get(user);
			pstmt.setInt(1, status);
			pstmt.setString(2, user);
			pstmt.addBatch();
		}
		pstmt.executeBatch();
		conn.commit();
		conn.setAutoCommit(orig);
		conn.close();
	}
}
