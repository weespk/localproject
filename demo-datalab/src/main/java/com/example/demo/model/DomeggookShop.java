package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DomeggookShop extends Vo{
	private String keyword;
	private String item_key;
	private String img_src;
	private String item_name;
	
	public DomeggookShop(String _keyword, String _item_key
			, String _img_src, String _item_name) {
		keyword= _keyword;
		item_key= _item_key;
		img_src= _img_src;
		item_name= _item_name;
	}
}
