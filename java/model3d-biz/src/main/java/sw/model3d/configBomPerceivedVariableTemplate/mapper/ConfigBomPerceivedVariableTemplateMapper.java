package sw.model3d.configBomPerceivedVariableTemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import sw.model3d.configBomPerceivedVariableTemplate.entity.ConfigBomPerceivedVariableTemplate;

import java.util.List;

/**
 * 项目结构树节点对应的感知变量信息(ConfigBomPerceivedVariableTemplate)表数据库访问层
 *
 * @author makejava
 * @since 2024-06-27 18:37:00
 */
@Mapper
public interface ConfigBomPerceivedVariableTemplateMapper extends BaseMapper<ConfigBomPerceivedVariableTemplate> {

	@Select("select id from al_resume_data where project=#{project} and product_model=#{productModel} and bom_model='SBOM'")
	Long getProId(@Param("project") String project, @Param("productModel") String productModel);

	@Delete("DELETE FROM config_bom_perceived_variable_template WHERE pro_id = #{proId};")
	void deleteByProId(@Param("proId") Integer proId);

	@Delete("DELETE FROM config_bom_perceived_variable_template WHERE var_id = #{varId};")
	boolean deleteByVarId(@Param("varId") String varId);

	@Select("SELECT * FROM config_bom_perceived_variable_template WHERE var_id = #{varId};")
	List<ConfigBomPerceivedVariableTemplate> getAllByVarId(@Param("varId") String varId);

}
