package sw.model3d.configBomTreeTemplate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import sw.model3d.configBomTreeTemplate.entity.ConfigBomTreeTemplate;
import org.apache.ibatis.annotations.Mapper;
import sw.model3d.configBomTreeTemplate.entity.vo.ConfigBomTreeTemplateVo;
import sw.model3d.configModel.entity.vo.ConfigBomTreeVo;

import java.util.List;
import java.util.Map;

/**
 * 项目风场不同机型风机结构表(ConfigBomTreeTemplate)表数据库访问层
 *
 * @author makejava
 * @since 2024-06-24 17:16:52
 */
@Mapper
public interface ConfigBomTreeTemplateMapper extends BaseMapper<ConfigBomTreeTemplate> {

	List<ConfigBomTreeTemplateVo> getSonNodes(@Param("proId") Long proId, @Param("nodeCode") String nodeCode);

	List<ConfigBomTreeTemplateVo> reqSonNodesByModelId(@Param("modelId") String modelId,
			@Param("nodeCode") String nodeCode);

	List<ConfigBomTreeTemplateVo> getTreeNodes(@Param("proId") Long proId, @Param("nodeLevel") String nodeLevel);

	List<ConfigBomTreeTemplateVo> reqTreeNodesByModelId(@Param("modelId") String modelId,
			@Param("nodeLevel") String nodeLevel);

	void delSonNode(@Param("proId") Long proId, @Param("nodeCode") String nodeCode);

	@Select("SELECT COALESCE(max(node_no),0) FROM config_bom_tree_template WHERE node_code REGEXP CONCAT('^',#{nodeCode},'-[0-9]*$') and pro_id=#{proId}")
	Integer getSonMaxNo(@Param("nodeCode") String nodeCode, @Param("proId") Long proId);

	List<ConfigBomTreeTemplate> getAllSubNodesByNodeCode(@Param("proId") Long proId,
			@Param("nodeCode") String nodeCode);

	// 根据节点编码，找到当前节点及其所有下属节点的Id，不包括本节点
	@Select("SELECT node_id FROM config_bom_tree_template WHERE (node_level = #{nodeLevel} and node_code REGEXP CONCAT('^',#{nodeCode},'-')) and pro_id=#{proId}")
	List<String> getNextLevelNodeIdsByNodeCode(@Param("nodeCode") String nodeCode,
			@Param("nodeLevel") Integer nodeLevel, @Param("proId") String proId);

	@Delete("delete from config_bom_tree_template where pro_id=#{proId}")
	void delTempalteByProId(@Param("proId") String proId);

	@Select("SELECT scada_train_latest_dataset, scada_test_latest_dataset FROM config_bom_tree_template WHERE node_id = #{nodeId}")
	Map<String, Object> getScadaDataUrl(@Param("nodeId") String nodeId);

	@Select("SELECT scada_provider,scada_train_latest_dataset, scada_test_latest_dataset, scada_train_previous_dataset, scada_test_previous_dataset FROM config_bom_tree_template WHERE node_id = #{nodeId}")
	Map<String, Object> getAllScadaDataUrl(@Param("nodeId") String nodeId);

	@Select("SELECT cms_train_latest_dataset, cms_test_latest_dataset FROM config_bom_tree_template WHERE node_id = #{nodeId}")
	Map<String, Object> getCmsDataUrl(@Param("nodeId") String objectId);

	@Select("SELECT cms_provider,cms_train_latest_dataset, cms_test_latest_dataset, cms_train_previous_dataset, cms_test_previous_dataset  FROM config_bom_tree_template WHERE node_id = #{nodeId}")
	Map<String, Object> getAllCmsDataUrl(@Param("nodeId") String objectId);
	//
	// @Select("SELECT provider FROM config_bom_tree_template WHERE node_id = #{nodeId}")
	// String getDataProvider(@Param("nodeId") String nodeId);

	@Select("SELECT node_code FROM config_bom_tree_template WHERE pro_id = #{proId}")
	List<String> getNodeCodesByProId(@Param("proId") String proId);

	// Mapper接口

	int updateDatasetInfo(Map<String, Object> params);

}
