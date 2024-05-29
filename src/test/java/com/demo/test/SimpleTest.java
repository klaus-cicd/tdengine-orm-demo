package com.demo.test;

import cn.hutool.json.JSONUtil;
import com.demo.tdengineorm.DemoApplication;
import com.demo.tdengineorm.entity.TestTdEntity;
import com.kalus.tdengineorm.enums.SelectJoinSymbolEnum;
import com.kalus.tdengineorm.enums.TdSelectFuncEnum;
import com.kalus.tdengineorm.mapper.TDengineMapper;
import com.kalus.tdengineorm.strategy.AbstractDynamicNameStrategy;
import com.kalus.tdengineorm.wrapper.AbstractTdQueryWrapper;
import com.kalus.tdengineorm.wrapper.TdQueryWrapper;
import com.kalus.tdengineorm.wrapper.TdWrappers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Klaus
 */
@Slf4j
@SpringBootTest(classes = DemoApplication.class)
public class SimpleTest {

    @Resource
    private TDengineMapper tdengineMapper;


    @Test
    void testCreateStableTable() {
        int stableTable = tdengineMapper.createStableTable(TestTdEntity.class);
        System.out.println("testCreateStableTable result =====> " + stableTable);
    }

    @Test
    void insertUsing() {
        long time = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
        TestTdEntity TestTdEntity = new TestTdEntity();
        TestTdEntity.setTs(new Timestamp(time));
        TestTdEntity.setAge(12);
        TestTdEntity.setFl1(12F);
        TestTdEntity.setName("12");
        TestTdEntity.setId(12L);
        TestTdEntity.setDb2(12D);
        TestTdEntity.setDb2(22D);
        TestTdEntity.setABCdEfggA("12");
        TestTdEntity.setTg1("12");
        TestTdEntity.setTg2(12);
        TestTdEntity.setCreateTime(new Timestamp(time));

        tdengineMapper.insertUsing(TestTdEntity, new AbstractDynamicNameStrategy() {
            @Override
            public String dynamicTableName(String s) {
                return s + "_0";
            }
        });
    }

    @Test
    void insert() {
        long time = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();

        TestTdEntity TestTdEntity = new TestTdEntity();
        TestTdEntity.setTs(new Timestamp(time));
        TestTdEntity.setAge(12);
        TestTdEntity.setFl1(12F);
        TestTdEntity.setName("12");
        TestTdEntity.setId(12L);
        TestTdEntity.setDb2(12D);
        TestTdEntity.setDb2(22D);
        TestTdEntity.setABCdEfggA("12");
        TestTdEntity.setCreateTime(new Timestamp(time));

        tdengineMapper.insert(TestTdEntity, new AbstractDynamicNameStrategy() {
            @Override
            public String dynamicTableName(String s) {
                return s + "_0";
            }
        });
    }


    /**
     * 查询最新一条数据
     */
    @Test
    void getLastOne() {
        System.out.println(tdengineMapper.getLastOneByTs(TestTdEntity.class));
    }


    /**
     * 按照指定条件查询单个数据(如果有多个则自动获取列表第一个)
     */
    @Test
    void getOne() {
        System.out.println(tdengineMapper.getOne(
                TdWrappers.queryWrapper(TestTdEntity.class)
                        .selectAll()
                        .eq(TestTdEntity::getTs, "2024-05-16T02:51:00.971")));
    }


    /**
     * 批量插入使用
     */
    @Test
    void batchInsertUsing() {
        List<TestTdEntity> list = new ArrayList<>();
        long time = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
        for (int i = 0; i < 10000; i++) {
            Timestamp timestamp = new Timestamp(time);
            TestTdEntity TestTdEntity = new TestTdEntity();
            TestTdEntity.setTs(timestamp);
            TestTdEntity.setAge(i);
            TestTdEntity.setFl1(13F);
            TestTdEntity.setName("" + i);
            TestTdEntity.setId((long) i);
            TestTdEntity.setDb2(13D);
            TestTdEntity.setDb2(23D);
            TestTdEntity.setABCdEfggA("" + i);
            // 对于同一张子表的Tag字段一定是相同的
            TestTdEntity.setTg1("13");
            TestTdEntity.setTg2(13);
            TestTdEntity.setCreateTime(timestamp);
            list.add(TestTdEntity);
        }
        tdengineMapper.batchInsertUsing(TestTdEntity.class, list, new AbstractDynamicNameStrategy() {
            @Override
            public String dynamicTableName(String s) {
                return s + "_1";
            }
        });
    }


    @Test
    void testLambdaList() {
        AbstractTdQueryWrapper<TestTdEntity> wrapper =
                TdWrappers.queryWrapper(TestTdEntity.class)
                        .selectAll()
                        .eq(TestTdEntity::getTg1, "13")
                        .limit(5000);

        List<TestTdEntity> list = tdengineMapper.list(wrapper);
        log.info("{}", JSONUtil.toJsonStr(list));
    }

    @Test
    void testStrList() {
        TdQueryWrapper<TestTdEntity> wrapper = TdWrappers.queryWrapper(TestTdEntity.class)
                .selectAll()
                .eq("tg1", "13").limit(2, 3000);
        System.out.println(tdengineMapper.list(wrapper));
    }

    @Test
    void simpleWindowFuncLambdaQuery() {
        TdQueryWrapper<TestTdEntity> wrapper = TdWrappers.queryWrapper(TestTdEntity.class)
                .selectFunc(TdSelectFuncEnum.FIRST, TestTdEntity::getTs, TestTdEntity::getName, TestTdEntity::getAge)
                .selectFunc(TdSelectFuncEnum.LAST, TestTdEntity::getId)
                .eq(TestTdEntity::getTg1, "13")
                .intervalWindow("30m");
        System.out.println(tdengineMapper.list(wrapper));
    }


    /**
     * 简单窗口函数使用
     */
    @Test
    void simpleWindowFuncQuery() {
        TdQueryWrapper<TestTdEntity> wrapper = TdWrappers.queryWrapper(TestTdEntity.class)
                .selectFunc(TdSelectFuncEnum.FIRST, "ts", "name", "age")
                .selectFunc(TdSelectFuncEnum.LAST, "id")
                .eq("tg1", "13")
                .intervalWindow("30m");
        System.out.println(tdengineMapper.list(wrapper));
    }


    @Test
    void complexWindowFunc() {
        TdQueryWrapper<TestTdEntity> wrapper = TdWrappers.queryWrapper(TestTdEntity.class);
    }


    @Test
    void nestingLambdaQuery() {
        TdQueryWrapper<TestTdEntity> wrapper = TdWrappers.queryWrapper(TestTdEntity.class)
                .innerQueryWrapper(innerWrapper ->
                        innerWrapper
                                .selectFunc(TdSelectFuncEnum.FIRST, TestTdEntity::getTs)
                                .eq(TestTdEntity::getTg1, "13")
                                .intervalWindow("30m")
                )
                .selectAll()
                .orderByDesc(TestTdEntity::getTs);

        System.out.println(tdengineMapper.list(wrapper));
    }

    @Test
    void selectJoinQuery() {
        TdQueryWrapper<TestTdEntity> wrapper = TdWrappers.queryWrapper(TestTdEntity.class)
                .selectAll()
                .innerQueryWrapper(innerWrapper -> innerWrapper
                        .selectCalc(TestTdEntity::getAge, consumer -> {
                            consumer.select(TdSelectFuncEnum.FIRST, TestTdEntity::getAge)
                                    .operate(SelectJoinSymbolEnum.PLUS)
                                    .select(TdSelectFuncEnum.LAST, TestTdEntity::getAge);
                        })
                        .eq(TestTdEntity::getTg1, "13")
                        .intervalWindow("30m")
                )
                .orderByDesc(TestTdEntity::getAge)
                .limit(12);

        System.out.println(tdengineMapper.list(wrapper));
    }
}
