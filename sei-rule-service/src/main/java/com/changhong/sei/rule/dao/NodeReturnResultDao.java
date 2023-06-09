package com.changhong.sei.rule.dao;

import com.changhong.sei.core.dao.BaseEntityDao;
import com.changhong.sei.rule.entity.LogicalExpression;
import com.changhong.sei.rule.entity.NodeReturnResult;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 规则树节点返回结果(NodeReturnResult)数据库访问类
 *
 * @author sei
 * @since 2021-01-13 17:18:45
 */
@Repository
public interface NodeReturnResultDao extends BaseEntityDao<NodeReturnResult> {
    /**
     * 获取规则树节点配置的返回结果
     * @param ruleTreeNodeId 规则树节点Id
     * @return 返回结果清单
     */
    List<NodeReturnResult> findByRuleTreeNodeId(String ruleTreeNodeId);

    /**
     * 获取规则树节点配置的返回结果的Id清单
     * @param ruleTreeNodeId 规则树节点Id
     * @return 返回结果清单
     */
    @Query("select r.id from NodeReturnResult r where r.ruleTreeNodeId=?1 ")
    List<String> findIdsByRuleTreeNodeId(String ruleTreeNodeId);

    /**
     * 根据规则树节点id删除所有表达式
     *
     * @param ruleTreeNodeId 规则树节点Id
     */
    void deleteByRuleTreeNodeId(String ruleTreeNodeId);
}