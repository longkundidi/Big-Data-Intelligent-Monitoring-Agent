package sw.model3d.configBomTreeTemplate.entity;

import java.io.Serializable;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Base64;

import com.baomidou.mybatisplus.annotation.TableField;
import org.springframework.format.annotation.DateTimeFormat;
import com.baomidou.mybatisplus.annotation.IdType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 项目风场不同机型风机结构表(ConfigBomTreeTemplate)表实体类
 *
 * @author makejava
 * @since 2024-06-24 17:16:52
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("config_bom_tree_template")
public class ConfigBomTreeTemplate implements Serializable {

	// 结构树节点id
	@TableId(type = IdType.ASSIGN_ID)
	private String nodeId;

	// 结构树节点编码
	private String nodeCode;

	// 风机编码
	private String turbineCode;

	// 重写nodeCode的setter方法以自动生成随机ID

	// 结构树节点名称
	private String nodeName;

	// 所属项目id
	private String proId;

	// 同一层节点的顺序号
	private Integer nodeNo;

	// 排序
	private Float swsort;

	// 节点层级(1-n)
	private Integer nodeLevel;

	// 节点类型 Root-Leaf：根节点、叶子节点；Root：根节点（有叶子节点）；Leaf：叶子节点；Mid：中间节点
	private String nodeType;

	// 结构树更新时间
	private String updateTime;

	// 结构树备注说明
	private String memo;

	private String ScadaTestLatestDataset;

	private String ScadaTestPreviousDataset;

	private String ScadaTrainLatestDataset;

	private String ScadaTrainPreviousDataset;

	private String ScadaProvider;

	private String ScadaSource;

	private String CmsTrainLatestDataset;

	private String CmsTrainPreviousDataset;

	private String CmsTestLatestDataset;

	private String CmsTestPreviousDataset;

	private String CmsProvider;

	private String CmsSource;

	@TableField("cms_collection_start_date")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate cmsCollectionStartDate;

	/** SCADA 采集开始日期 */
	@TableField("scada_collection_start_date")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate scadaCollectionStartDate;

	/** CMS 采集结束日期 */
	@TableField("cms_collection_end_date")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate cmsCollectionEndDate;

	/** SCADA 采集结束日期 */
	@TableField("scada_collection_end_date")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate scadaCollectionEndDate;

	/** CMS 采集频率 */
	@TableField("cms_collection_frequency")
	private String cmsCollectionFrequency;

	/** SCADA 采集频率 */
	@TableField("scada_collection_frequency")
	private String scadaCollectionFrequency;

	/** CMS 数据类型 */
	@TableField("cms_data_type")
	private String cmsDataType;

	/** SCADA 数据类型 */
	@TableField("scada_data_type")
	private String scadaDataType;

	/** CMS 字段总数 */
	@TableField("cms_field_count")
	private Integer cmsFieldCount;

	/** SCADA 字段总数 */
	@TableField("scada_field_count")
	private Integer scadaFieldCount;

	/** CMS 存储大小 */
	@TableField("cms_storage_size")
	private Double cmsStorageSize;

	/** SCADA 存储大小 */
	@TableField("scada_storage_size")
	private Double scadaStorageSize;

	/** CMS 缺失情况 */
	@TableField("cms_missing_condition")
	private String cmsMissingCondition;

	/** SCADA 缺失情况 */
	@TableField("scada_missing_condition")
	private String scadaMissingCondition;

	private String cmsDatasetName;

	private String scadaDatasetName;

}
