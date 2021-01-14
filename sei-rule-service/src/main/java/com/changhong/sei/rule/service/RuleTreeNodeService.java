package com.changhong.sei.rule.service;

import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dao.BaseTreeDao;
import com.changhong.sei.core.service.BaseTreeService;
import com.changhong.sei.rule.dao.RuleTreeNodeDao;
import com.changhong.sei.rule.entity.RuleTreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 规则树节点(RuleTreeNode)业务逻辑实现类
 *
 * @author sei
 * @since 2021-01-13 16:29:53
 */
@Service("ruleTreeNodeService")
public class RuleTreeNodeService extends BaseTreeService<RuleTreeNode> {
    @Autowired
    private RuleTreeNodeDao dao;

    @Override
    protected BaseTreeDao<RuleTreeNode> getDao() {
        return dao;
    }

    /**
     * 获取规则实体类型的所有根节点
     * @param ruleTypeId 规则类型Id
     * @param tenantCode 租户代码
     * @return 根节点清单
     */
    public List<RuleTreeNode> findRootNodes(String ruleTypeId, String tenantCode) {
        return dao.findRootNodes(ruleTypeId, ContextUtil.getTenantCode());
    }
}