package com.changhong.sei.rule.dto;

import com.changhong.sei.core.dto.BaseEntityDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 服务方法定义(RuleServiceMethod)DTO类
 *
 * @author sei
 * @since 2021-01-13 16:07:32
 */
@ApiModel(description = "$tool.trim($!{tableInfo.comment})DTO")
public class RuleServiceMethodDto extends BaseEntityDto {
    private static final long serialVersionUID = 8413444629839916683L;
    /**
     * 规则业务实体Id
     */
    @NotBlank
    @Size(max = 36)
    @ApiModelProperty(value = "$tool.trim(${column.comment})", required = true)
    private String ruleEntityTypeId;
    /**
     * 方法名
     */
    @NotBlank
    @Size(max = 100)
    @ApiModelProperty(value = "$tool.trim(${column.comment})", required = true)
    private String method;
    /**
     * 名称
     */
    @NotBlank
    @Size(max = 50)
    @ApiModelProperty(value = "$tool.trim(${column.comment})", required = true)
    private String name;
    /**
     * API相对路径
     */
    @NotBlank
    @Size(max = 100)
    @ApiModelProperty(value = "$tool.trim(${column.comment})", required = true)
    private String path;

    public String getRuleEntityTypeId() {
        return ruleEntityTypeId;
    }

    public void setRuleEntityTypeId(String ruleEntityTypeId) {
        this.ruleEntityTypeId = ruleEntityTypeId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}