package com.example.demo.model;

public class Body extends Vo{
	private Req[] body;
	private String[] result;
	private Period[] data;
	public Req[] getBody() {
		return body;
	}
	public void setBody(Req[] body) {
		this.body = body;
	}
	public String[] getResult() {
		return result;
	}
	public void setResult(String[] result) {
		this.result = result;
	}
	public Period[] getData() {
		return data;
	}
	public void setData(Period[] data) {
		this.data = data;
	}
}