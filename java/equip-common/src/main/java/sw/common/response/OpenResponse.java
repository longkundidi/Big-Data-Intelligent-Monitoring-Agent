package sw.common.response;

public class OpenResponse<T> {

	private int code;

	private String message;

	private T data;

	private String extraInfo;

	public String getExtraInfo() {
		return extraInfo;
	}

	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}

	public OpenResponse() {
		this.code = OpenResponseCode.SUCCESS.getCode();
		this.message = OpenResponseCode.SUCCESS.getDescription();
	}

	public OpenResponse(OpenResponseCode responseCode) {
		this.code = responseCode.getCode();
		this.message = responseCode.getDescription();
	}

	public OpenResponse(OpenResponseCode responseCode, T data) {
		this.code = responseCode.getCode();
		this.message = responseCode.getDescription();
		this.data = data;
	}

	public OpenResponse(T data) {
		this.code = OpenResponseCode.SUCCESS.getCode();
		this.message = OpenResponseCode.SUCCESS.getDescription();
		this.data = data;
	}

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public T getData() {
		return data;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public void setCode(OpenResponseCode code) {
		this.code = code.getCode();
		this.message = code.getDescription();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setData(T data) {
		this.data = data;
	}

}
