package hamaster.gradesign.keygen.idmgmt;

import java.util.Map;

public interface IBESystemBean {

    /**
     * Create credentials based on given parameters, this is part of
     * the initial setup procedure
     * @param owner The uniq owner string for the system
     * @param pairing The pairing string shared by all users in the system
     * @param password The password used to protect system master key
     */
    void createIBSSystem(String owner, byte[] pairing, byte[] password);

    // for test purpose only
    void generateDemoSystem();

    /**
     * 获取IBE系统编号和名称的对应关系
     * @param page 分页页码 从0开始
     * @param amount 每一页数量
     * @return Map对象
     */
    Map<Integer, String> list(int page, int amount);

    /**
     * Get the system ID (primary key in DB) from the owner
     * @param owner the owner name
     * @return system ID
     */
    Integer getIDByName(String owner);

    /**
     * @return 当前所有的系统数量
     */
    long totalSystems();
}
