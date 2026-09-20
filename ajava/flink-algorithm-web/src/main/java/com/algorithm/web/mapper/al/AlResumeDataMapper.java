package com.algorithm.web.mapper.al;

import com.algorithm.web.model.entity.al.AlResumeData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.flink.table.planner.expressions.In;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlResumeDataMapper extends BaseMapper<AlResumeData> {

	/**
	 * 根据场景ID删除config_bom_perceived_variable_template表中的数据
	 * @param sceneId 场景ID
	 */
	@Delete("DELETE FROM config_bom_perceived_variable_template WHERE pro_id = #{sceneId}")
	void deletePerceivedTemplateBySceneId(@Param("sceneId") String sceneId);

	// 1. 查询所有 node_id
	@Select("SELECT node_id FROM config_gbom_tree WHERE scene_id = #{sceneId}")
	List<String> getNodeIdsBySceneId(@Param("sceneId") String sceneId);

	// 2. 批量删除 config_perceived_variable 表中的数据
	@Delete({ "<script>", "DELETE FROM config_perceived_variable WHERE node_id IN",
			"<foreach item='id' collection='nodeIds' open='(' separator=',' close=')'>", "#{id}", "</foreach>",
			"</script>" })
	void deletePerceivedByNodeIds(@Param("nodeIds") List<String> nodeIds);

	@Delete("DELETE FROM config_gbom_tree WHERE scene_id = #{sceneId}")
	void deleteConfigGbomTreeBySceneId(@Param("sceneId") String sceneId);

	@Delete("DELETE FROM config_bom_tree_template WHERE pro_id = #{modelId}")
	void deleteBomTreeTemplateByModelId(@Param("modelId") String modelId);

	@Delete("DELETE FROM config_bom_perceived_variable_template WHERE pro_id = #{modelId}")
	void deletePerceivedTemplateByModelId(@Param("modelId") String modelId);

	// 1. 根据 modelId 查询 node_code
	@Select("SELECT node_code FROM config_bom_tree_template WHERE pro_id = #{modelId}")
	List<String> getNodeCodesByModelId(@Param("modelId") String modelId);

	// 2. 根据 id 查询 project 字段
	@Select("SELECT project FROM al_resume_data WHERE id = #{modelId}")
	String getProjectByModelId(@Param("modelId") String modelId);

	// 3. 根据 project 和 bom_model=GBOM 查询 id（scene_id）
	@Select("SELECT id FROM al_resume_data WHERE project = #{project} AND bom_model = 'GBOM'")
	List<Long> getSceneIdsByProject(@Param("project") String project);

	// 4. 根据 node_code 和 scene_id 查询 config_gbom_tree 中的 used_count
	@Select("SELECT used_count FROM config_gbom_tree WHERE node_code = #{nodeCode} AND scene_id = #{sceneId}")
	Integer getUsedCount(@Param("nodeCode") String nodeCode, @Param("sceneId") String sceneId);

	// 5. 更新 used_count
	@Select("UPDATE config_gbom_tree SET used_count = #{usedCount} WHERE node_code = #{nodeCode} AND scene_id = #{sceneId}")
	void updateUsedCount(@Param("nodeCode") String nodeCode, @Param("sceneId") String sceneId,
			@Param("usedCount") int usedCount);

	@Select("SELECT COUNT(*) FROM al_resume_data WHERE product_model_id = #{modelId}")
	Integer getModelUsedCount(@Param("modelId") Long modelId);

}
