package com.algorithm.web.mapper.al;

import com.algorithm.web.model.entity.al.DomainModel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 领域模型基本信息表(DomainModel)表数据库访问层
 *
 * @author makejava
 * @since 2024-11-12 17:46:20
 */
@Mapper
public interface DomainModelMapper extends BaseMapper<DomainModel> {

	String exitName(@Param("name") String name);

}
