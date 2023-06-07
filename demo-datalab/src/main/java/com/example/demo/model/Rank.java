package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Rank extends Vo{
	private String pid;
	private String cid;
	private int rank;
	private String keyword;
	private String linkId;
	
	private long b_idx;
	private int page;
//	private String trand_clickmap;
	private String trand_click;
	private String trand_click2;
	private String trand_click3;
	private int trand_age10;
	private int trand_age20;
	private int trand_age30;
	private int trand_age40;
	private int trand_age50;
	private int trand_age60;
	private int trand_man;
	private int trand_woman;
	private int trand_pc;
	private int trand_mo;
	private int product_cnt;
	
}