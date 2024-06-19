package com.demo.tdengineorm.entity;

import com.kalus.tdengineorm.annotation.TdField;
import com.kalus.tdengineorm.annotation.TdTag;
import com.kalus.tdengineorm.entity.BaseTdEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用于测试关联查询
 *
 * @author Klaus
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TestDeviceB extends BaseTdEntity {
    /**
     * 关联设备TestDeviceA.deviceBId字段
     */
    private Long id;
    @TdField(length = 128)
    private String name;
    private Integer age;

    @TdTag
    @TdField(length = 128)
    private String deviceCode;
}
