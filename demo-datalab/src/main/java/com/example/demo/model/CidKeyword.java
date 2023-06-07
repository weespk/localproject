package com.example.demo.model;

public class CidKeyword extends Vo{
	String date;
	String datetime;
	String range;
	Rank []ranks;
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getDatetime() {
		return datetime;
	}
	public void setDatetime(String datetime) {
		this.datetime = datetime;
	}
	public String getRange() {
		return range;
	}
	public void setRange(String range) {
		this.range = range;
	}
	public Rank[] getRanks() {
		return ranks;
	}
	public void setRanks(Rank[] ranks) {
		this.ranks = ranks;
	}
}