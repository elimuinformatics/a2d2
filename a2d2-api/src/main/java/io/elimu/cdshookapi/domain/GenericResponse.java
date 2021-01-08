package io.elimu.cdshookapi.domain;

public class GenericResponse<T> {

	private T result;

	public GenericResponse(T result) {
		super();
		this.result = result;
	}

	public T getResult() {
		return result;
	}

	public void setResult(T result) {
		this.result = result;
	}

}
