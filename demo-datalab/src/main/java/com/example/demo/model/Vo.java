package com.example.demo.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Vo{
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}