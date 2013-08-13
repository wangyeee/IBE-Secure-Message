package hamaster.gradesign.idmgmt;

import java.util.Collection;
import java.util.List;

/**
 * 全部业务接口的父接口
 * @author <a href="mailto:wangyeee@gmail.com">Wang Ye</a>
 */
public interface CommonDAO {

	/**
	 * 将实体持久化至数据库
	 * @param entity 要持久化的实体
	 */
	void save(Object entity);

	/**
	 * 将实体批量存入数据库
	 * @param entities 要储存的实体集合
	 */
	void batchSave(Collection<?> entities);

	/**
	 * 更新数据库中的实体
	 * @param entity 新实体
	 * @return 状态为已更新的实体
	 */
	Object update(Object entity);

	/**
	 * 从数据库中删除实体
	 * @param entityClass 实体类
	 * @param primaryKey 主键
	 */
	void delete(Class<?> entityClass, Object primaryKey);

	/**
	 * 根据实体的主键从数据库中获取到实体
	 * @param <T> 模板类
	 * @param entityClass 实体类
	 * @param primaryKey 主键
	 * @return 实体类的实例
	 */
	<T> T find(Class<T> entityClass, Object primaryKey);

	/**
	 * 获取实体列表
	 * @param entityClass 实体类型
	 * @param page 分页页码 从0开始
	 * @param amountPerPage 每一页数量
	 * @return 类实例列表
	 */
	<T> List<T> list(Class<T> entityClass, int page, int amountPerPage);
}
