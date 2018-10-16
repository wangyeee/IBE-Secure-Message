package hamaster.gradesign.dao;

import hamaster.gradesign.entity.IDRequest;
import hamaster.gradesign.entity.User;

import java.util.List;
import java.util.Map;

public interface IDRequestDAO extends DAO {

	@Deprecated
	List<IDRequest> list(User owner, int page, int amount);

	/**
	 * 根据所有者和身份获取ID申请
	 * @param owner 所有者
	 * @param idString 身份字符串
	 * @return ID申请或者null
	 */
	IDRequest getByOwner(User owner, String idString);

	/**
	 * 获取用户新发起的私钥申请
	 * @param amount 获取数量
	 * @return 私钥申请列表
	 */
	List<IDRequest> listNewRequests(int amount);

	/**
	 * 获取用户的身份请求
	 * @param owner 要获取的用户
	 * @param page 分页页码 从0开始
	 * @param amount 每一页数量
	 * @param status 当前状态 默认为全部状态
	 * @return 身份请求列表
	 */
	List<IDRequest> list(User owner, int page, int amount, int status);

	/**
	 * 获取未处理的请求列表
	 * @param amount 获取数量
	 * @return 请求列表
	 */
	List<IDRequest> listUnhandledRequests(int amount);

	/**
	 * 将处理后的请求写回数据库
	 * @param requests
	 */
	void requestHandled(Map<String, Integer> results);

	/**
	 * 获取某一用户所有身份请求的数量
	 * @param onwer 要获取的用户
	 * @return 身份请求数量
	 */
	int count(User onwer);

	/**
	 * 判断ID是否是某个用户所拥有的
	 * @param id 要判断的ID
	 * @param user 要判断的所有者
	 * @param idPassword 用户申请ID时所设置的密码
	 * @return 
	 * 当且仅当用户拥有该ID且密码正确时返回0
	 * 密码错误返回1
	 * ID不属于用户返回2
	 */
	int doesIdBelongToUser(String id, User user, String idPassword);

	/**
	 * 检查id是否被人申请过
	 * @param id 要检查的id
	 * @return 当切仅当没有是返回0
	 */
	int doesIdRequestExist(String id);
}
