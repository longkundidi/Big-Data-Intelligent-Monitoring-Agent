package sw.model3d.configModel.mapper;

import org.apache.ibatis.annotations.Mapper;
import sw.model3d.configModel.entity.ConfigModelVariable;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @author Tyrrell
 * @description 针对表【config_model_variable】的数据库操作Mapper
 * @createDate 2024-04-01 15:36:19
 * @Entity sw.model3d.configModel.entity.ConfigModelVariable
 */
@Mapper
public interface ConfigModelVariableMapper extends BaseMapper<ConfigModelVariable> {

}
