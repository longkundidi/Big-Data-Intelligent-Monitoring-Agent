package com.algorithm.web.mapper.al;

import com.algorithm.web.model.entity.al.SysMetrics;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;

/**
 * 存储系统的评价指标(SysMetrics)表数据库访问层
 *
 * @author makejava
 * @since 2024-11-06 15:28:35
 */
@Mapper
public interface SysMetricsMapper extends BaseMapper<SysMetrics> {

}
