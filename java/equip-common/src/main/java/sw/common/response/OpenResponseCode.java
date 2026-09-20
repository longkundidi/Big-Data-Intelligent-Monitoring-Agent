package sw.common.response;

public enum OpenResponseCode {

	SUCCESS(0, "操作成功"),
	/* SUCCESS(1, "操作成功"), */
	ERROR(500, "操作失败"), COPY_ERROR(2, "Bean拷贝异常"), EXCEPTION(1000, "服务端发生异常"), ILLEGAL_ARGUMENT(1001, "请求参数异常"),
	NAME_EXIST(1002, "命名重复"), PARAMETER_INCOMPLETE(1003, "参数不完整"),
	// http状态码
	SC_CONTINUE(100, ""), SC_SWITCHING_PROTOCOLS(101, ""), SC_PROCESSING(102, ""), SC_OK(200, "请求成功"),
	SC_CREATED(201, "新增成功"), SC_ACCEPTED(202, "更新成功"), SC_NON_AUTHORITATIVE_INFORMATION(203, ""),
	SC_NO_CONTENT(204, "成功处理了请求"), SC_RESET_CONTENT(205, ""), SC_PARTIAL_CONTENT(206, ""), SC_MULTI_STATUS(207, ""),
	SC_DELETE(208, "删除成功"), SC_MULTIPLE_CHOICES(300, ""), SC_MOVED_PERMANENTLY(301, ""), SC_MOVED_TEMPORARILY(302, ""),
	SC_SEE_OTHER(303, ""), SC_NOT_MODIFIED(304, ""), SC_USE_PROXY(305, ""), SC_TEMPORARY_REDIRECT(307, ""),
	SC_BAD_REQUEST(400, ""), SC_UNAUTHORIZED(401, ""), SC_PAYMENT_REQUIRED(300, ""), SC_FORBIDDEN(403, ""),
	SC_NOT_FOUND(404, ""), SC_METHOD_NOT_ALLOWED(405, ""), SC_NOT_ACCEPTABLE(406, "更新失败"),
	SC_PROXY_AUTHENTICATION_REQUIRED(407, ""), SC_REQUEST_TIMEOUT(408, ""), SC_CONFLICT(409, ""), SC_GONE(410, ""),
	SC_LENGTH_REQUIRED(411, ""), SC_PRECONDITION_FAILED(412, ""), SC_REQUEST_TOO_LONG(413, ""),
	SC_REQUEST_URI_TOO_LONG(414, ""), SC_UNSUPPORTED_MEDIA_TYPE(415, ""), SC_REQUESTED_RANGE_NOT_SATISFIABLE(416, ""),
	SC_EXPECTATION_FAILED(417, ""), SC_INSUFFICIENT_SPACE_ON_RESOURCE(418, ""), SC_METHOD_FAILURE(420, ""),
	SC_UNPROCESSABLE_ENTITY(422, ""), SC_LOCKED(423, ""), SC_FAILED_DEPENDENCY(424, ""), SC_RNAL_SERVER_ERROR(500, ""),
	SC_NOT_IMPLEMENTED(501, ""), SC_BAD_GATEWAY(502, ""), SC_SERVICE_UNAVAILABLE(503, ""), SC_GATEWAY_TIMEOUT(504, ""),
	SC_HTTP_VERSION_NOT_SUPPORTED(505, ""), SC_INSUFFICIENT_STORAGE(507, ""),

	OBJ_STATION_AROUND_MMID_CANNOT_NULL(2001, "专业ID不能为空"), REVIT_MESSAGE_NOT_EXIST(2002, "Web端信息获取失败"),;

	private int code;

	private String description;

	OpenResponseCode(int code, String description) {
		this.code = code;
		this.description = description;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
