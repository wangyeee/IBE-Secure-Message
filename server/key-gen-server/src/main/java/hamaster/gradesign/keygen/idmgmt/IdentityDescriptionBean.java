package hamaster.gradesign.keygen.idmgmt;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.springframework.scheduling.annotation.Async;

import hamaster.gradesign.keygen.IBECSR;
import hamaster.gradesign.keygen.entity.IdentityDescriptionEntity;

public interface IdentityDescriptionBean {

    /**
     * 根据用户身份字符串获取IdentityDescription
     * @param owner 用户身份字符串
     * @return IdentityDescriptionEntity实例
     */
    IdentityDescriptionEntity get(String owner);

    /**
     * 批量为用户生成身份描述文件
     * @param requests 身份文件请求
     * @return 异步请求处理结果
     */
    @Async
    Future<Map<String, Integer>> generateIdentityDescriptions(List<IBECSR> requests);

    Map<String, Integer> generateIdentityDescriptionsSync(List<IBECSR> requests);

    /**
     * 修改身份信息的加密密钥
     * @param id 要修改的身份信息序号
     * @param oldKey 旧密钥
     * @param newKey 新密钥
     * @return 异步IdentityDescriptionEntity实例
     */
    @Async
    Future<IdentityDescriptionEntity> changeEncryptionKey(Integer id, String oldKey, String newKey);

    IdentityDescriptionEntity changeEncryptionKeySync(Integer id, String oldKey, String newKey);
}
