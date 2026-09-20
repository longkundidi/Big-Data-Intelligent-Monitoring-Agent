package sw.ai.mapper;

import org.apache.ibatis.annotations.Mapper;
import sw.ai.domain.AlFaultDiagnosis;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * @author tjhe
 * @description 针对表【al_fault_diagnosis】的数据库操作Mapper
 * @createDate 2024-04-17 16:28:14
 * @Entity sw.ai.domain.AlFaultDiagnosis
 */
@Mapper
public interface AlFaultDiagnosisMapper extends BaseMapper<AlFaultDiagnosis> {

	List<AlFaultDiagnosis> getAll();

}
