package com.algorithm.web.service.al;

import com.algorithm.web.model.entity.al.AlTask;
import com.algorithm.web.model.entity.al.AlTaskVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AlTaskService extends IService<AlTask> {

	Page<AlTaskVo> getPage(Page page, String serverName);

	Page<AlTask> getByName(Page page, String alModelName);

	boolean updateIsService(AlTaskVo alTaskVo);

}
