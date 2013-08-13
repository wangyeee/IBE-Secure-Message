package hamaster.gradesign.idmgmt;

import hamaster.gradesign.IBECSR;
import hamaster.gradesign.entity.IdentityDescriptionEntity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.ejb.Asynchronous;
import javax.ejb.Remote;

@Remote
public interface IdentityDescriptionBean extends CommonDAO {

	/**
	 * 根据用户身份字符串获取IdentityDescription
	 * @param owner 用户身份字符串
	 * @return IdentityDescriptionEntity实例
	 */
	IdentityDescriptionEntity get(String owner);

//	/**
//	 * 为用户生成身份描述文件
//	 * @param owner 用户身份字符串
//	 * @param userPassword 用户设置的访问密码
//	 * @param systemId 用户所属于的IBE系统
//	 * @param validAfter 签名证书生效日期
//	 * @param period 签名证书有效期 单位毫秒
//	 * @return 异步IdentityDescriptionEntity实例
//	 */
//	@Asynchronous
//	@Deprecated
//	Future<IdentityDescriptionEntity> generateIdentityDescription(String owner, String userPassword, Integer systemId, Date validAfter, long period);

	/**
	 * 批量为用户生成身份描述文件
	 * @param requests 身份文件请求
	 * @return 异步请求处理结果
	 */
	@Asynchronous
	Future<Map<String, Integer>> generateIdentityDescriptions(List<IBECSR> requests);
	
	/**
	 * 修改身份信息的加密密钥
	 * @param id 要修改的身份信息序号
	 * @param oldKey 旧密钥
	 * @param newKey 新密钥
	 * @return 异步IdentityDescriptionEntity实例
	 */
	@Asynchronous
	Future<IdentityDescriptionEntity> changeEncryptionKey(Integer id, String oldKey, String newKey);
}
