package com.hp.ai_interview.common.result;

public record Result<T>(int code, String message, T data) {

	public static <T> Result<T> ok(T data) {
		return new Result<>(0, "success", data);
	}

	public static <T> Result<T> error(int code, String message) {
		return new Result<>(code, message, null);
	}
}
