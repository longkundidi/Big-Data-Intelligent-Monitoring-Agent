package sw.model3d.configBomTreeTemplate.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import sw.utils.entity.TreeEntity;

import java.io.Serializable;
import java.time.LocalDate;

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

public class ConfigBomTreeTemplateVo extends TreeEntity {

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
