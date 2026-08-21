package com.hp.ai_interview.common.exception;

import com.hp.ai_interview.common.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Result<Void> handleInvalidInput(MethodArgumentNotValidException e) {
		String detail = e.getBindingResult().getFieldErrors().stream()
				.map(f -> f.getField() + ": " + f.getDefaultMessage())
				.findFirst()
				.orElse("Dữ liệu không hợp lệ");
		return Result.error(400, detail);
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public Result<Void> handleUnexpected(Exception e) {
		log.error("Lỗi không mong đợi", e);
		return Result.error(500, "Có lỗi xảy ra, vui lòng thử lại");
	}

	@ExceptionHandler(MissingServletRequestPartException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Result<Void> handleMissingPart(MissingServletRequestPartException e) {
		return Result.error(400, "Thiếu file trong request, hãy gửi kèm field \"" + e.getRequestPartName() + "\"");
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Result<Void> handleIllegalArgument(IllegalArgumentException e) {
		return Result.error(400, e.getMessage());
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Result<Void> handleTooLargeResult(MaxUploadSizeExceededException e) {
		return Result.error(400, "File quá lớn, vui lòng chọn file nhỏ hơn");
	}
}
