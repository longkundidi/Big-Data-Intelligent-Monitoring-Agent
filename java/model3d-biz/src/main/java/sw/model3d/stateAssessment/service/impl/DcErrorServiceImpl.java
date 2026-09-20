package sw.model3d.stateAssessment.service.impl;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import sw.model3d.stateAssessment.entity.DcAlgorithm;
import sw.model3d.stateAssessment.mapper.DcErrorMapper;
import sw.model3d.stateAssessment.entity.DcError;
import sw.model3d.stateAssessment.service.DcErrorService;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * (DcError)表服务实现类
 *
 * @author makejava
 * @since 2024-05-29 09:49:12
 */
@Service
public class DcErrorServiceImpl extends ServiceImpl<DcErrorMapper, DcError> implements DcErrorService {

	@Override
	public List<DcError> getVarerrorByTaskId(String taskId, String algoShortname) {
		Date date = new DateTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
		sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
		String current_time = sdf.format(date);
		LambdaQueryWrapper<DcError> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(DcError::getTaskId, taskId)
			.eq(DcError::getAlgoShortname, algoShortname)
			.ge(DcError::getDcTime, current_time);
		List<DcError> dcErrorList = this.getBaseMapper().selectList(queryWrapper);

		return dcErrorList;
	}

}
