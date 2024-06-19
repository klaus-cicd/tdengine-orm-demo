package com.demo.test;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.demo.tdengineorm.DemoApplication;
import com.demo.tdengineorm.entity.TestDeviceA;
import com.demo.tdengineorm.entity.TestDeviceB;
import com.kalus.tdengineorm.enums.JoinTypeEnum;
import com.kalus.tdengineorm.enums.SelectJoinSymbolEnum;
import com.kalus.tdengineorm.enums.TdSelectFuncEnum;
import com.kalus.tdengineorm.mapper.TDengineMapper;
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
        int testDeviceASTableResult = tdengineMapper.createStableTable(TestDeviceA.class);
        int testDeviceBSTableResult = tdengineMapper.createStableTable(TestDeviceB.class);
        log.info("testCreateStableTable result =====> testDeviceASTableResult:{}, testDeviceBSTableResult:{}", testDeviceASTableResult, testDeviceBSTableResult);
    }

    /**
     * 使用INSERT USING插入数据（缺少子表则自动创建）
     */
    @Test
    void insertUsing() {
        long time = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
        String deviceCode = "0";
        TestDeviceB testDeviceB = buildDeviceB(time, deviceCode);
        tdengineMapper.insertUsing(testDeviceB, s -> s + "_" + deviceCode);

        TestDeviceA testDeviceA = buildDeviceA(time, testDeviceB.getId());

        tdengineMapper.insertUsing(testDeviceA, s -> s + "_" + deviceCode);
    }


    /**
     * 直接插入数据，缺少子表时会报错
     */
    @Test
    void insert() {
        long time = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
        String deviceCode = "0";
        TestDeviceB testDeviceB = buildDeviceB(time, deviceCode);
        TestDeviceA testDeviceA = buildDeviceA(time, testDeviceB.getId());

        tdengineMapper.insert(testDeviceA, s -> s + "_0");
        tdengineMapper.insert(testDeviceB, s -> s + "_0");
    }


    /**
     * 查询最新一条数据
     */
    @Test
    void getLastOne() {
        System.out.println(tdengineMapper.getLastOneByTs(TestDeviceA.class));
        System.out.println(tdengineMapper.getLastOneByTs(TestDeviceB.class));
    }


    /**
     * 按照指定条件查询单个数据(如果有多个则自动获取列表第一个)
     */
    @Test
    void getOne() {
        System.out.println(tdengineMapper.getOne(
                TdWrappers.queryWrapper(TestDeviceA.class)
                        .selectAll()
                        .eq(TestDeviceA::getTs, "2024-06-19 19:34:08")));
    }


    /**
     * 批量插入使用
     */
    @Test
    void batchInsertUsing() {
        List<TestDeviceA> listA = new ArrayList<>();
        List<TestDeviceB> listB = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            long time = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
            TestDeviceB testDeviceB = buildDeviceB(time, "0");
            TestDeviceA testDeviceA = buildDeviceA(time, testDeviceB.getId());

            listA.add(testDeviceA);
            listB.add(testDeviceB);
        }
        tdengineMapper.batchInsertUsing(TestDeviceA.class, listA, s -> s + "_0");
        tdengineMapper.batchInsertUsing(TestDeviceB.class, listB, s -> s + "_0");
    }


    @Test
    void testLambdaList() {
        AbstractTdQueryWrapper<TestDeviceA> wrapper =
                TdWrappers.queryWrapper(TestDeviceA.class)
                        .selectAll()
                        .eq(TestDeviceA::getTg1, "13")
                        .limit(5000);

        List<TestDeviceA> list = tdengineMapper.list(wrapper);
        log.info("{}", JSONUtil.toJsonStr(list));
    }

    @Test
    void testStrList() {
        TdQueryWrapper<TestDeviceA> wrapper = TdWrappers.queryWrapper(TestDeviceA.class)
                .selectAll()
                .eq("tg1", "13").limit(2, 3000);
        System.out.println(tdengineMapper.list(wrapper));
    }

    @Test
    void simpleWindowFuncLambdaQuery() {
        TdQueryWrapper<TestDeviceA> wrapper = TdWrappers.queryWrapper(TestDeviceA.class)
                .selectFunc(TdSelectFuncEnum.FIRST, TestDeviceA::getTs, TestDeviceA::getName, TestDeviceA::getAge)
                .selectFunc(TdSelectFuncEnum.LAST, TestDeviceA::getId)
                .eq(TestDeviceA::getTg1, "13")
                .intervalWindow("30m");
        System.out.println(tdengineMapper.list(wrapper));
    }


    /**
     * 简单窗口函数使用
     */
    @Test
    void simpleWindowFuncQuery() {
        TdQueryWrapper<TestDeviceA> wrapper = TdWrappers.queryWrapper(TestDeviceA.class)
                .selectFunc(TdSelectFuncEnum.FIRST, "ts", "name", "age")
                .selectFunc(TdSelectFuncEnum.LAST, "id")
                .eq("tg1", "13")
                .intervalWindow("30m");
        System.out.println(tdengineMapper.list(wrapper));
    }


    @Test
    void complexWindowFunc() {
        TdQueryWrapper<TestDeviceA> wrapper = TdWrappers.queryWrapper(TestDeviceA.class);
    }


    @Test
    void nestingLambdaQuery() {
        TdQueryWrapper<TestDeviceA> wrapper = TdWrappers.queryWrapper(TestDeviceA.class)
                .innerQueryWrapper(innerWrapper ->
                        innerWrapper
                                .selectFunc(TdSelectFuncEnum.FIRST, TestDeviceA::getTs)
                                .eq(TestDeviceA::getTg1, "13")
                                .intervalWindow("30m")
                )
                .selectAll()
                .orderByDesc(TestDeviceA::getTs);

        System.out.println(tdengineMapper.list(wrapper));
    }

    @Test
    void selectJoinQuery() {
        TdQueryWrapper<TestDeviceA> wrapper = TdWrappers.queryWrapper(TestDeviceA.class)
                .selectAll()
                .innerQueryWrapper(innerWrapper -> innerWrapper
                        .selectCalc(TestDeviceA::getAge, consumer -> {
                            consumer.select(TdSelectFuncEnum.FIRST, TestDeviceA::getAge)
                                    .operate(SelectJoinSymbolEnum.PLUS)
                                    .select(TdSelectFuncEnum.LAST, TestDeviceA::getAge);
                        })
                        .eq(TestDeviceA::getTg1, "13")
                        .intervalWindow("30m")
                )
                .orderByDesc(TestDeviceA::getAge)
                .limit(12);

        System.out.println(tdengineMapper.list(wrapper));
    }


    @Test
    void simpleJoinQueryTest() {
        AbstractTdQueryWrapper<TestDeviceA> wrapper =
                TdWrappers.queryWrapper(TestDeviceA.class)
                        .select(TestDeviceA::getAge)
                        .select(TestDeviceB.class, TestDeviceB::getName)
                        .join(JoinTypeEnum.LEFT_JOIN, TestDeviceB.class)
                        .eq("test_device_b.id", 1803270034955735040L)
                        .limit(5000);

        List<TestDeviceA> list = tdengineMapper.list(wrapper);
        log.info("{}", JSONUtil.toJsonStr(list));
    }


    private static TestDeviceA buildDeviceA(long time, Long testDeviceBId) {
        TestDeviceA testDeviceA = new TestDeviceA();
        testDeviceA.setTs(new Timestamp(time));
        testDeviceA.setAge(12);
        testDeviceA.setFl1(12F);
        testDeviceA.setName("12");
        testDeviceA.setId(12L);
        testDeviceA.setDb2(12D);
        testDeviceA.setDb2(22D);
        testDeviceA.setABCdEfggA("12");
        testDeviceA.setTg1("12");
        testDeviceA.setTg2(12);
        testDeviceA.setDeviceBId(testDeviceBId);
        testDeviceA.setCreateTime(new Timestamp(time));
        return testDeviceA;
    }

    private static TestDeviceB buildDeviceB(long time, String deviceBCode) {
        TestDeviceB testDeviceB = new TestDeviceB();
        testDeviceB.setTs(new Timestamp(time));
        testDeviceB.setDeviceCode(deviceBCode);
        testDeviceB.setName("aaa");
        testDeviceB.setAge(11);
        testDeviceB.setId(IdUtil.getSnowflake().nextId());
        return testDeviceB;
    }

}
