package sw.model3d.stateAssessment.service;

import com.baomidou.mybatisplus.extension.service.IService;

import sw.model3d.stateAssessment.entity.DcError;

import java.util.List;

/**
 * (DcError)表服务接口
 *
 * @author makejava
 * @since 2024-05-29 09:49:12
 */
public interface DcErrorService extends IService<DcError> {

	List<DcError> getVarerrorByTaskId(String taskId, String algoShortname);

}
