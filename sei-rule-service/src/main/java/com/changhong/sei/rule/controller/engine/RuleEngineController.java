package com.changhong.sei.rule.controller.engine;

import com.changhong.sei.core.dto.ResultData;
import com.changhong.sei.core.log.LogUtil;
import com.changhong.sei.core.utils.ResultDataUtil;
import com.changhong.sei.rule.api.engine.RuleEngineTestApi;
import com.changhong.sei.rule.dto.engine.TestRunRequest;
import com.changhong.sei.rule.dto.engine.TestRunResponse;
import com.changhong.sei.rule.dto.ruletree.RuleTreeRoot;
import com.changhong.sei.rule.sdk.api.RuleEngineApi;
import com.changhong.sei.rule.sdk.dto.RuleRunRequest;
import com.changhong.sei.rule.sdk.dto.RuleRunResponse;
import com.changhong.sei.rule.service.RuleTreeNodeService;
import com.changhong.sei.rule.service.engine.RuleEngineService;
import io.swagger.annotations.Api;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.LinkedList;
import java.util.List;

/**
 * 实现功能: 规则引擎OpenAPI服务实现
 *
 * @author 王锦光 wangjg
 * @version 2021-01-17 15:45
 */
@RestController
@Api(value = "RuleEngineApi", tags = "规则引擎OpenAPI服务实现")
@RequestMapping(path = "ruleEngine", produces = MediaType.APPLICATION_JSON_VALUE)
public class RuleEngineController implements RuleEngineApi, RuleEngineTestApi {

    @Autowired
    private RuleEngineService service;
    @Autowired
    private RuleTreeNodeService ruleTreeNodeService;
    /**
     * 定义通用的严格匹配实体转换器
     */
    private static final ModelMapper strictModelMapper;
    // 初始化静态属性
    static {
        // 初始化转换器
        strictModelMapper = new ModelMapper();
        // 设置为严格匹配
        strictModelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    /**
     * 执行规则
     *
     * @param request 规则执行请求
     * @return 规则执行结果
     */
    @Override
    public ResultData<RuleRunResponse> run(@Valid RuleRunRequest request) {
        List<RuleRunResponse> responses;
        try {
            responses = service.run(request, Boolean.TRUE, Boolean.FALSE);
            if (CollectionUtils.isNotEmpty(responses)) {
                // 规则执行成功！
                return ResultDataUtil.success("00041", responses.get(0));
            }
        } catch (Exception e) {
            LogUtil.error("规则引擎执行异常:" + e.getMessage(), e);
            return ResultDataUtil.fail("规则引擎执行异常:" + e.getMessage());
        }
        return ResultDataUtil.success("00041", new RuleRunResponse());
    }

    /**
     * 执行所有规则链
     *
     * @param request 规则执行请求
     * @return 规则执行结果
     */
    @Override
    public ResultData<List<RuleRunResponse>> runChains(@Valid RuleRunRequest request) {
        return null;
    }

    /**
     * 执行测试规则
     *
     * @param request 规则执行请求
     * @return 规则执行结果
     */
    @Override
    public ResultData<TestRunResponse> testRun(@Valid TestRunRequest request) {
        List<RuleRunResponse> responses;
        try {
            responses = service.run(request, request.getExecuteMethod(), request.getAllChains());
        } catch (Exception e) {
            LogUtil.error("规则引擎执行异常:" + e.getMessage(), e);
            return ResultDataUtil.fail("规则引擎执行异常:" + e.getMessage());
        }
        if (CollectionUtils.isEmpty(responses)) {
            // 执行测试失败，没有匹配成功的规则！
            ResultDataUtil.fail("00042");
        }
        TestRunResponse testRunResponse = new TestRunResponse();
        // 获取根节点信息
        String nodeId = responses.get(0).getMatchedNodeId();
        RuleTreeRoot root = ruleTreeNodeService.findRootByNodeId(nodeId);
        testRunResponse.setRuleTreeRoot(root);
        // 构造测试结果
        testRunResponse.setResponses(responses);
        // 执行测试成功，存在匹配成功的规则！
        return ResultDataUtil.success(testRunResponse, "00043");
    }
}
