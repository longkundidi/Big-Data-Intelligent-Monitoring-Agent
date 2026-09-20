package sw.common.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MyException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private MyExceptionEnum myExceptionEnum;

}
