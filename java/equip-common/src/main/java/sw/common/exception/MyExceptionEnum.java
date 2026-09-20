package sw.common.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum MyExceptionEnum {

	DEMO_EXCEPTION(400, "自定义异常示例"), FILE_EXCEPTION(500, "FASTDFS无该文件"), DATABASE_EXCEPTION(600, "数据库操作失败"),
	MESH_CODE_EXCEPTION(500, "MESH格式编码有误"),

	UPLOAD_CODE_EXCEPTION(501, "上传文件，模型编码不能为空"), DOWNLOAD_CODE_EXCEPTION(502, "下载文件，模型编码不能为空"),
	DELETE_FILE_EXCEPTION(503, "文件删除失败"),

	TWIN_MODEL_CREATE_EXCEPTION(504, "孪生模型新增失败"), TWIN_MODEL_UPDATE_EXCEPTION(505, "孪生模型更改失败"),
	TWIN_MODEL_DELETE_EXCEPTION(506, "孪生模型删除失败");

	private int code;

	private String msg;

}
