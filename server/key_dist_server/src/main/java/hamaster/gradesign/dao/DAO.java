package hamaster.gradesign.dao;

import java.io.Serializable;
import java.util.List;

public interface DAO {

	/**
	 * 根据实体的主键从数据库中获取到实体
	 * @param entityClass 实体类
	 * @param primaryKey 主键
	 * @return 实体类的实例
	 */
	<T> T get(Class<T> entityClass, Serializable primaryKey);

	/**
	 * 获取特定数量的实体
	 * @param entityClass 实体类
	 * @param page 页码 从0开始
	 * @param amount 每一页数量 -1为全部
	 * @return 实体类列表
	 */
	<T> List<T> list(Class<T> entityClass, int page, int amount);

	/**
	 * 将实体持久化至数据库
	 * @param entity 要持久化的实体
	 */
	void add(Object entity);

	/**
	 * 更新数据库中的实体
	 * @param entity 新实体
	 */
	void update(Object entity);

	/**
	 * 从数据库中删除实体
	 * @param entityClass 实体类
	 * @param primaryKey 主键
	 */
	void delete(Object entity);
}
