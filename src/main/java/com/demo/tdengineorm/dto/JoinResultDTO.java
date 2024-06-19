package com.demo.tdengineorm.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.sql.Timestamp;

/**
 * 对于关联查询结果，最好使用一个新的对象接收
 *
 * @author Klaus
 */
@Data
public class JoinResultDTO {

    private Timestamp ts;
    private Long id;
    private String name;
    private Integer age;
    @TableField("a_b_cd_efgg_a")
    private String aBCdEfggA;
}
