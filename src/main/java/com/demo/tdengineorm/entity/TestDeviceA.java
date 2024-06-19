package com.demo.tdengineorm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kalus.tdengineorm.annotation.TdField;
import com.kalus.tdengineorm.annotation.TdTag;
import com.kalus.tdengineorm.entity.BaseTdEntity;
import com.kalus.tdengineorm.enums.TdFieldTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.Timestamp;

/**
 * 设备A实体类
 * 随意写了各种类型各种名称的字段，用来测试字段解析
 *
 * @author klaus
 * @date 2024/06/19
 */
@Data
@TableName("tb_test_device_a")
@EqualsAndHashCode(callSuper = true)
public class TestDeviceA extends BaseTdEntity {
    /**
     * 使用 @TdField 注解指定字段类型以及长度, 未指定时默认为INT类型
     */
    @TdField(type = TdFieldTypeEnum.NCHAR, length = 20)
    private String name;
    private Integer age;
    @TdField(type = TdFieldTypeEnum.DOUBLE)
    private Double db1;
    @TdField(type = TdFieldTypeEnum.DOUBLE)
    private Double db2;
    @TdField(type = TdFieldTypeEnum.FLOAT)
    private Float fl1;
    @TdField(type = TdFieldTypeEnum.BIGINT)
    private Long id;
    @TdField(type = TdFieldTypeEnum.TIMESTAMP)
    private Timestamp createTime;
    @TdField(type = TdFieldTypeEnum.BIGINT)
    @TableField("device_b_id")
    private Long deviceBId;
    /**
     * 使用MP的 @TableField 注解来指定字段别名
     */
    @TableField("a_b_cd_efgg_a")
    @TdField(type = TdFieldTypeEnum.NCHAR, length = 20)
    private String aBCdEfggA;
    /**
     * 使用 @TdTag 注解标记字段为Tag字段
     */
    @TdTag
    @TdField(type = TdFieldTypeEnum.NCHAR, length = 20)
    private String tg1;

    @TdTag
    @TdField(type = TdFieldTypeEnum.NCHAR, length = 20)
    private Integer tg2;
}