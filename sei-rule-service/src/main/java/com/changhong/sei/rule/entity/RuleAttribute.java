package com.changhong.sei.rule.entity;

import com.changhong.sei.core.entity.BaseAuditableEntity;
import com.changhong.sei.rule.dto.enums.RuleAttributeType;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 规则属性定义(RuleAttribute)实体类
 *
 * @author sei
 * @since 2021-01-13 15:36:44
 */
@Entity
@Table(name = "rule_attribute")
@DynamicInsert
@DynamicUpdate
public class RuleAttribute extends BaseAuditableEntity {
    private static final long serialVersionUID = 5777185297168424678L;
    /**
     * 规则业务实体Id
     */
    @Column(name = "rule_entity_type_id")
    private String ruleEntityTypeId;
    /**
     * 属性名
     */
    @Column(name = "attribute")
    private String attribute;
    /**
     * 属性名称
     */
    @Column(name = "name")
    private String name;
    /**
     * 属性类型(字符串：STRING,日期：DATETIME,数值：NUMBER,布尔：BOOLEAN)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "rule_attribute_type")
    private RuleAttributeType ruleAttributeType = RuleAttributeType.STRING;
    /**
     * UI组件
     */
    @Column(name = "ui_component")
    private String uiComponent;
    /**
     * 匹配值使用的字段
     */
    @Column(name = "value_field")
    private String valueField;
    /**
     * 匹配值显示的字段
     */
    @Column(name = "display_field")
    private String displayField;
    /**
     * 获取数据的url
     */
    @Column(name = "find_data_url")
    private String findDataUrl;

    public RuleAttribute() {
    }

    public RuleAttribute(String ruleEntityTypeId, String attribute, String name, RuleAttributeType ruleAttributeType, String uiComponent) {
        this.ruleEntityTypeId = ruleEntityTypeId;
        this.attribute = attribute;
        this.name = name;
        this.ruleAttributeType = ruleAttributeType;
        this.uiComponent = uiComponent;
    }

    public RuleAttribute(String ruleEntityTypeId, String attribute, String name, RuleAttributeType ruleAttributeType, String uiComponent, String valueField, String displayField, String findDataUrl) {
        this.ruleEntityTypeId = ruleEntityTypeId;
        this.attribute = attribute;
        this.name = name;
        this.ruleAttributeType = ruleAttributeType;
        this.uiComponent = uiComponent;
        this.valueField = valueField;
        this.displayField = displayField;
        this.findDataUrl = findDataUrl;
    }

    public String getRuleEntityTypeId() {
        return ruleEntityTypeId;
    }

    public void setRuleEntityTypeId(String ruleEntityTypeId) {
        this.ruleEntityTypeId = ruleEntityTypeId;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RuleAttributeType getRuleAttributeType() {
        return ruleAttributeType;
    }

    public void setRuleAttributeType(RuleAttributeType ruleAttributeType) {
        this.ruleAttributeType = ruleAttributeType;
    }

    public String getUiComponent() {
        return uiComponent;
    }

    public void setUiComponent(String uiComponent) {
        this.uiComponent = uiComponent;
    }

    public String getValueField() {
        return valueField;
    }

    public void setValueField(String valueField) {
        this.valueField = valueField;
    }

    public String getDisplayField() {
        return displayField;
    }

    public void setDisplayField(String displayField) {
        this.displayField = displayField;
    }

    public String getFindDataUrl() {
        return findDataUrl;
    }

    public void setFindDataUrl(String findDataUrl) {
        this.findDataUrl = findDataUrl;
    }
}