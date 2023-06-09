package com.changhong.sei.rule.sdk.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

/**
 * 实现功能: 规则执行服务方法的输入参数
 *
 * @author 王锦光 wangjg
 * @version 2021-01-17 15:51
 */
@ApiModel("规则执行服务方法的输入参数")
public class RuleServiceMethodParam implements Serializable {
    private static final long serialVersionUID = 9105930593111033729L;

    /**
     * 规则执行请求
     */
    @NotNull
    @ApiModelProperty(value = "规则执行请求", required = true)
    private RuleRunRequest request;

    /**
     * 返回的实体MAP
     */
    @ApiModelProperty(value = "返回的实体", notes = "返回的实体MAP,key为返回结果类型的代码(全类名)")
    private Map<String, RuleReturnEntity> returnEntityMap;

    public RuleRunRequest getRequest() {
        return request;
    }

    public void setRequest(RuleRunRequest request) {
        this.request = request;
    }

    public Map<String, RuleReturnEntity> getReturnEntityMap() {
        return returnEntityMap;
    }

    public void setReturnEntityMap(Map<String, RuleReturnEntity> returnEntityMap) {
        this.returnEntityMap = returnEntityMap;
    }
}
