package com.changhong.sei.rule.service.engine;

import com.changhong.sei.core.context.ContextUtil;
import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.log.LogUtil;
import com.changhong.sei.core.util.JsonUtils;
import com.changhong.sei.rule.dao.RuleEntityTypeDao;
import com.changhong.sei.rule.dao.RuleTypeDao;
import com.changhong.sei.rule.dto.ruletree.RuleTreeRoot;
import com.changhong.sei.rule.entity.RuleEntityType;
import com.changhong.sei.rule.entity.RuleServiceMethod;
import com.changhong.sei.rule.entity.RuleTreeNode;
import com.changhong.sei.rule.entity.RuleType;
import com.changhong.sei.rule.sdk.dto.RuleReturnEntity;
import com.changhong.sei.rule.sdk.dto.RuleRunRequest;
import com.changhong.sei.rule.sdk.dto.RuleRunResponse;
import com.changhong.sei.rule.sdk.dto.RuleServiceMethodParam;
import com.changhong.sei.rule.service.RuleTreeNodeService;
import com.changhong.sei.rule.service.aviator.AviatorExpressionService;
import com.changhong.sei.rule.service.aviator.function.MatchRuleComparatorFunction;
import com.changhong.sei.rule.service.bo.RuleChain;
import com.changhong.sei.rule.service.client.RuleServiceMethodClient;
import com.changhong.sei.rule.service.exception.RuleEngineException;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.changhong.sei.rule.service.aviator.AviatorExpressionService.RULE_CHAIN_PARAM_PREFIX;
import static com.changhong.sei.rule.service.aviator.AviatorExpressionService.RULE_TYPE_CODE;

/**
 * @author <a href="mailto:xiaogang.su@changhong.com">粟小刚</a>
 * @description 实现功能:规则引擎服务
 * @date 2021/01/18 16:48
 */
@Service
public class RuleEngineService {
    @Autowired
    private RuleTypeDao ruleTypeDao;
    @Autowired
    private RuleTreeNodeService ruleTreeNodeService;
    @Autowired
    private RuleEntityTypeDao ruleEntityTypeDao;
    @Autowired
    private RuleServiceMethodClient ruleServiceMethodClient;
    @Autowired
    private RuleChainService ruleChainService;
    @Autowired
    private AviatorExpressionService aviatorExpressionService;


    /**
     * 执行规则
     *
     * @param request 规则执行请求
     * @param executeMethod 是否执行服务方法
     * @param allChains 执行所有规则链
     * @return 结果返回对象
     */
    public List<RuleRunResponse> run(RuleRunRequest request,
                                     boolean executeMethod,
                                     boolean allChains) throws RuleEngineException {
        List<RuleRunResponse> responses = new LinkedList<>();
        String ruleTypeCode = request.getRuleTypeCode();
        if (StringUtils.isBlank(ruleTypeCode)) {
            //规则类型代码不能为空！
            throw new RuleEngineException("00026");
        }
        RuleType ruleType = ruleTypeDao.findByCodeAndTenantCode(ruleTypeCode, ContextUtil.getTenantCode());
        if (Objects.isNull(ruleType)) {
            // 指定规则类型不存在！【{0}】
            throw new RuleEngineException("00027", ruleTypeCode);
        }
        List<RuleTreeRoot> rootNodes = ruleTreeNodeService.findRootNodes(ruleType.getId());
        // 获取启用的规则
        List<RuleTreeRoot> roots = rootNodes.stream().filter(RuleTreeRoot::getEnabled).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(roots)) {
            // 指定规则类型没有定义可用的规则！
            throw new RuleEngineException("00028");
        }
        Map<String, Object> env = new HashMap<>();
        Object param;
        try {
            param = JsonUtils.fromJson(request.getRuleEntityJson(), Object.class);
        } catch (RuntimeException e) {
            LogUtil.error("规则引擎转换JSON字符串异常:" + e.getMessage(), e);
            //JSON字符串转换异常，请检查是否为JsonObject格式！
            throw new RuleEngineException("00029");
        }
        env.put(RULE_CHAIN_PARAM_PREFIX, param);
        env.put(RULE_TYPE_CODE, request.getRuleTypeCode());
        // 打印执行规则的输入参数
        LogUtil.bizLog("执行规则【{}】，输入参数：{}", request.getRuleTypeCode(), JsonUtils.toJson(param));
        try {
            //根据优先级依次匹配多个规则
            for (RuleTreeRoot root : roots) {
                // 先判断根节点是否通过规则检查
                if (!runNodeExpression(env, root.getId())) {
                    continue;
                }
                // 从缓存获取规则链
                List<RuleChain> ruleChains = ruleChainService.getRuleChainsFromCache(root.getId());
                if (CollectionUtils.isEmpty(ruleChains)) {
                    // 再从规则树生成
                    RuleTreeNode tree = ruleTreeNodeService.getRuleTree(root.getId());
                    ruleChains = ruleChainService.getExpressionByTree(tree);
                }
                // 如果没有可用的规则链，则退出
                if (CollectionUtils.isEmpty(ruleChains)) {
                    continue;
                }
                for (RuleChain ruleChain : ruleChains) {
                    //是否匹配成功
                    if (ruleChainMatch(env, ruleChain)) {
                        // 匹配成功后执行操作
                        responses.add(matchSuccess(request, ruleChain, executeMethod));
                        // 匹配上一个直接返回
                        if (!allChains) {
                            return responses;
                        }
                    } else if (allChains) {
                        responses.add(matchingFailed(request, ruleChain));
                    }
                }
                // 如果存在匹配成功的规则链，则返回
                if (CollectionUtils.isNotEmpty(responses)) {
                    return responses;
                }
            }
        } catch (Exception e) {
            throw new RuleEngineException(e.getMessage(), e);
        } finally {
            //清理掉自定义比较器的线程的缓存,避免web容器等线程池复用线程导致缓存不清理
            MatchRuleComparatorFunction.getCacheHolder().remove();
        }
        //未匹配上 返回默认响应
        return responses;
    }

    /**
     * 运行一个节点的逻辑表达式
     * @param env 规则运行参数
     * @param nodeId 规则节点Id
     * @return 逻辑表达式结果
     */
    public boolean runNodeExpression(Map<String, Object> env, String nodeId) {
        RuleTreeNode node = ruleTreeNodeService.findOne(nodeId);
        if (Objects.isNull(node)) {
            return Boolean.FALSE;
        }
        // 如果是真节点，直接返回true
        if (node.getTrueNode()) {
            return Boolean.TRUE;
        }
        // 装配节点信息
        ruleTreeNodeService.assembleNodeInfo(node);
        String exception = aviatorExpressionService.convertToExpression(node);
        return ruleChainMatch(env, exception);
    }

    /**
     * 规则链匹配
     *
     * @param env       匹配参数
     * @param expression 逻辑表达式
     * @return 匹配结果
     */
    private boolean ruleChainMatch(Map<String, Object> env, String expression) {
        // 规则链匹配,空表达式返回TRUE
        if (StringUtils.isBlank(expression)) {
            return Boolean.TRUE;
        }
        // 编译表达式
        Expression compiledExp = AviatorEvaluator.compile(expression, true);
        try {
            return (Boolean) compiledExp.execute(env);
        } catch (NullPointerException e) {
            //00032 = 必要的规则匹配参数未传入,请检查！
            throw new RuleEngineException("00032");
        }
    }
    /**
     * 规则链匹配
     *
     * @param env       匹配参数
     * @param ruleChain 规则链
     * @return 匹配结果
     */
    private boolean ruleChainMatch(Map<String, Object> env, RuleChain ruleChain) {
        return ruleChainMatch(env, ruleChain.getExpression());
    }

    /**
     * 匹配成功后执行的方法
     *
     * @param request   匹配请求
     * @param ruleChain 规则链
     * @param executeMethod 是否执行服务方法
     * @return 匹配结果
     */
    private RuleRunResponse matchSuccess(RuleRunRequest request, RuleChain ruleChain, boolean executeMethod) {
        RuleRunResponse response = new RuleRunResponse();
        //规则类型[{0}]已匹配上规则节点[{1}]，输入参数:{2}，匹配表达式:[{3}]
        LogUtil.bizLog(ContextUtil.getMessage("00031", request.getRuleTypeCode(), ruleChain.getRuleTreeNodeId(), request.getRuleEntityJson(), ruleChain.getExpression()));
        //设置是否匹配标识
        response.setMatched(true);
        response.setMatchedNodeId(ruleChain.getRuleTreeNodeId());
        response.setMatchedNodeName(ruleChain.getRuleTreeNodeName());
        //返回对象
        response.setReturnConstant(ruleChain.getReturnConstant());
        List<RuleReturnEntity> returnEntities = ruleChain.getReturnEntities();
        if (CollectionUtils.isNotEmpty(returnEntities)) {
            //组装Map key：类名 entries:实体对象
            Map<String, RuleReturnEntity> returnEntityMap = new HashMap<>();
            returnEntities.forEach(e -> returnEntityMap.put(e.getClassName(), e));
            response.setReturnEntityMap(returnEntityMap);
        }
        if (executeMethod) {
            //执行方法
            RuleServiceMethod method = ruleChain.getRuleServiceMethod();
            if (Objects.nonNull(method)) {
                serviceMethodExecute(request, response, ruleChain, method);
            }
        }
        return response;
    }

    /**
     * 匹配失败的结果
     *
     * @param request   匹配请求
     * @param ruleChain 规则链
     * @return 匹配结果
     */
    private RuleRunResponse matchingFailed(RuleRunRequest request, RuleChain ruleChain) {
        RuleRunResponse response = new RuleRunResponse();
        // 设置是否匹配标识
        response.setMatched(Boolean.FALSE);
        response.setMatchedNodeId(ruleChain.getRuleTreeNodeId());
        response.setMatchedNodeName(ruleChain.getRuleTreeNodeName());
        // 规则链未匹配成功！
        response.setReturnConstant(ContextUtil.getMessage("00045"));
        return response;
    }

    /**
     * 执行自定义规则执行方法
     *
     * @param request   匹配请求
     * @param response  匹配结果
     * @param ruleChain 规则链
     * @param method    自定义规则方法
     */
    private void serviceMethodExecute(RuleRunRequest request, RuleRunResponse response, RuleChain ruleChain, RuleServiceMethod method) {
        RuleEntityType ruleEntityType = ruleEntityTypeDao.findOne(method.getRuleEntityTypeId());
        if (Objects.isNull(ruleEntityType)) {
            //指定规则业务实体[{0}]不存在！
            throw new RuleEngineException("00030", method.getRuleEntityTypeId());
        }
        try {
            StringBuilder url = new StringBuilder();
            if (method.getPath().endsWith("/")) {
                url.append(method.getPath());
            } else {
                url.append(method.getPath()).append("/");
            }
            url.append(method.getMethod());
            RuleServiceMethodParam methodParam = new RuleServiceMethodParam();
            methodParam.setReturnEntityMap(response.getReturnEntityMap());
            methodParam.setRequest(request);
            ResultData<?> apiResult = ruleServiceMethodClient.post(ruleEntityType.getServiceName(), url.toString(), methodParam, ruleChain.getAsyncExecute());
            if (apiResult.successful()) {
                //设置方法已执行
                response.setExecuted(true);
            } else {
                LogUtil.error("访问外部服务模块返回失败:" + apiResult.getMessage());
                //访问外部服务模块{0}，path={1}自定义方法返回失败:[message={2}]，请查看应用模块[{0}]的异常日志！
                throw new RuleEngineException("00001", ruleEntityType.getServiceName(), url.toString(), apiResult.getMessage());
            }
        } catch (Exception e) {
            LogUtil.error("访问外部服务模块自定义方法异常:" + e.getMessage(), e);
            throw new RuleEngineException("访问外部服务模块自定义方法异常:" + e.getMessage(), e);
        }
    }
}
