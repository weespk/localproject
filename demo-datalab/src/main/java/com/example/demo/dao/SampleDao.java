package com.example.demo.dao;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.example.demo.DemoDatalabApplication;
import com.example.demo.model.DomeggookShop;
import com.example.demo.model.Rank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Repository
public class SampleDao {
	private static final Logger logger= LoggerFactory.getLogger(SampleDao.class);

	@Autowired
	private JdbcTemplate jt;

	public List<Map<String, ?>> selectNow() {
		return jt.query("select now() now", (rs, rowNum) -> {
			Map<String, String> mss = new HashMap<>();
			mss.put("now", rs.getString(1));
			return mss;
		});
	}

	public List<Map<String, ?>> selectDomeggookShopList() {
		String query= "select concat('http://domeggook.com/',ORDER_PCODE)SHOP_URL, ORDER_PCODE "
				+ "FROM tbl_board_comment_domeggook_pcode a "
				+ "where not exists ( select ORDER_PCODE from tbl_board_comment_domeggook b where a.ORDER_PCODE=b.ORDER_PCODE)"
				+ "and a.ORDER_PCODE!='1' " 
				+ "limit 1";
		List<Map<String,?>> result= jt.query(query
				, (rs, rowNum) -> {
			Map<String,Object> mss = new HashMap<>();
			try {
				mss.put("SHOP_URL", rs.getString("SHOP_URL"));
				mss.put("ORDER_PCODE", rs.getString("ORDER_PCODE"));
			}catch(Exception e) {
				mss.put("SHOP_URL", "");
				mss.put("ORDER_PCODE", "");
			}
			return mss;
		});
		return result;
	}
	public void insertDomeggookShop(List<Map<String,?>> list) {
		String sql = "INSERT INTO tbl_board_comment_domeggook"
				+ "(USERID, ORDER_PCODE, MIN_ORDER_CNT, REG_DATE"
				+ ", lInfoViewImgUse, lInfoItemTitle, lThumbImg, lItemPrice)"
				+ "VALUES (?,?,?, NOW()"
				+ ", ?, ?, ?, ?)"
				+ "ON DUPLICATE KEY UPDATE UPDATE_DATE= now()";
		jt.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				Map<String,?> row= list.get(i);
				logger.info("{}", row);
				
				int idx=1;
				ps.setObject(idx++, row.get("USERID").toString());
				ps.setObject(idx++, row.get("ORDER_PCODE").toString());
				ps.setObject(idx++, row.get("MIN_ORDER_CNT").toString());
				ps.setObject(idx++, row.get("lInfoViewImgUse").toString());
				ps.setObject(idx++, row.get("lInfoItemTitle").toString());
				ps.setObject(idx++, row.get("lThumbImg").toString());
				ps.setObject(idx++, row.get("lItemPrice").toString());
			}
	
			@Override
			public int getBatchSize() {
				return list.size();
			}
		});
	}

	public List<Map<String, ?>> selectKeywordReviewMorpheme() {
		String query= "select idx, html "
				+ "from tbl_board_comment_keyword_review "
				+ "where html_morpheme is null order by idx desc limit 100";
		List<Map<String,?>> result= jt.query(query
				, (rs, rowNum) -> {
			Map<String,Object> mss = new HashMap<>();
			try {
				mss.put("idx", rs.getInt("idx"));
				mss.put("html", rs.getString("html"));
			}catch(Exception e) {
				mss.put("idx", "");
				mss.put("html", "");
			}
			return mss;
		});
		return result;
	}
	public void insertKeywordReviewMorpheme(List<Map<String, ?>> list) {
		String sql = "update tbl_board_comment_keyword_review "
				+ "set html_morpheme= ? "
				+ "where idx= ? ";
		jt.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				Map row= list.get(i);
				int idx=1;
				ps.setObject(idx++, row.get("html_morpheme").toString());
				ps.setObject(idx++, row.get("idx").toString());
			}
	
			@Override
			public int getBatchSize() {
				return list.size();
			}
		});
	}

	public boolean selectKeywordSkip(String keyword, int rank) {
		String query= "select count(1)cnt "
				+ "from tbl_board_comment_keyword "
				+ "where keyword='"+ keyword +"'";
//				+ " and rank='"+ rank +"' ";
		List<Map<String,Integer>> result= jt.query(query
				, (rs, rowNum) -> {
			Map<String, Integer> mss = new HashMap<>();
			try {
				mss.put("cnt", rs.getInt("cnt"));
			}catch(Exception e) {
				mss.put("cnt", 0);
			}
			return mss;
		});
		if(result.get(0).get("cnt")>0) {
			return true;
		}
	    return false;
	}

	public long insertRankCateData(String cateCode, String cateName) {
		String query= "select count(b_id)cnt, max(idx)idx "
				+ "from tbl_board where b_id='014' and etc1='"+cateCode+"' ";
		List<Map<String,Integer>> result= jt.query(query
				, (rs, rowNum) -> {
			Map<String, Integer> mss = new HashMap<>();
			try {
				mss.put("cnt", rs.getInt("cnt"));
				mss.put("idx", rs.getInt("idx"));
			}catch(Exception e) {
				mss.put("cnt", 0);
			}
			return mss;
		});
		long idx= 0;
		if(result.get(0).get("cnt")>0) {
			idx= result.get(0).get("idx");
			
		} else {
			KeyHolder keyHolder= new GeneratedKeyHolder();
		    PreparedStatementCreator preparedStatementCreator = (connection) -> {
		        PreparedStatement prepareStatement = connection.prepareStatement(
		        		"insert into tbl_board(b_id, reg_date, reg_dates, title, content, etc1, etc2) "
						+ "values('014', now(), left(now(),10), ?,?,?,?)"
		        		, new String[]{"title","content","etc1","etc2"});
		        prepareStatement.setString(1, String.format("%s_%s", cateCode,cateName));
		        prepareStatement.setString(2, String.format("%s_%s", cateCode,cateName));
		        prepareStatement.setString(3, cateCode);
		        prepareStatement.setString(4, cateName);
		        return prepareStatement;
		    };
		    jt.update(preparedStatementCreator, keyHolder);
		    
		    idx= keyHolder.getKey().longValue();
		}
	    return idx;
	}
	public void insertRankKeywordTrand(List<Rank> rankList) {
		String sql = "INSERT INTO tbl_board_comment_keyword"
				+ "(reg_date, reg_dates"
				+ ", b_idx, title, page, rank, pid, cid, keyword"
				+ ", trand_click, trand_click2, trand_click3"
				+ ", trand_man, trand_woman"
				+ ", trand_age10, trand_age20, trand_age30, trand_age40, trand_age50, trand_age60"
				+ ", trand_pc, trand_mo"
				+ ", product_cnt)"
				+ "VALUES (now(), left(now(),10)"
				+ ", ?, ?, ?, ?, ?, ?, ?"
				+ ", ?, ?, ?"
				+ ", ?, ?"
				+ ", ?, ?, ?, ?, ?, ?"
				+ ", ?, ?"
				+ ", ?)"
				+ "ON DUPLICATE KEY UPDATE update_date=now()"
				+ ", trand_click=values(trand_click)"
				+ ", trand_click2=values(trand_click2)"
				+ ", trand_click3=values(trand_click3)"
				+ ", trand_man=values(trand_man)"
				+ ", trand_woman=values(trand_woman)"
				+ ", trand_age10=values(trand_age10)"
				+ ", trand_age20=values(trand_age20)"
				+ ", trand_age30=values(trand_age30)"
				+ ", trand_age40=values(trand_age40)"
				+ ", trand_age50=values(trand_age50)"
				+ ", trand_age60=values(trand_age60)"
				+ ", trand_pc=values(trand_pc)"
				+ ", trand_mo=values(trand_mo)"
				+ ", product_cnt=values(product_cnt)";
		jt.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				Rank row= rankList.get(i);
				int idx=1;
				ps.setObject(idx++, row.getB_idx());
				ps.setObject(idx++, String.format("%s_%s", row.getCid(),row.getKeyword()));
				ps.setObject(idx++, row.getPage());
				ps.setObject(idx++, row.getRank());
				ps.setObject(idx++, row.getPid());
				ps.setObject(idx++, row.getCid());
				try {
					ps.setObject(idx++, URLDecoder.decode(row.getKeyword(),"UTF-8"));
				} catch (UnsupportedEncodingException e) {
					ps.setObject(idx++, row.getKeyword());
				}
				ps.setObject(idx++, row.getTrand_click());
				ps.setObject(idx++, row.getTrand_click2());
				ps.setObject(idx++, 0);
				ps.setObject(idx++, row.getTrand_man());
				ps.setObject(idx++, row.getTrand_woman());
				ps.setObject(idx++, row.getTrand_age10());
				ps.setObject(idx++, row.getTrand_age20());
				ps.setObject(idx++, row.getTrand_age30());
				ps.setObject(idx++, row.getTrand_age40());
				ps.setObject(idx++, row.getTrand_age50());
				ps.setObject(idx++, row.getTrand_age60());
				ps.setObject(idx++, row.getTrand_pc());
				ps.setObject(idx++, row.getTrand_mo());
				ps.setObject(idx++, row.getProduct_cnt());
			}
	
			@Override
			public int getBatchSize() {
				return rankList.size();
			}
		});
	}
	public void insertKeywordFromDomeggook(List<DomeggookShop> rankList) {
		String sql = "INSERT INTO tbl_board_comment_keyword_domeggook"
				+ "(reg_date, keyword"
				+ ", item_key, img_src, item_name)"
				+ "VALUES (now(), ?,?,?,?)"
				+ "ON DUPLICATE KEY UPDATE update_date=now()";
		jt.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				DomeggookShop row= rankList.get(i);
				int idx=1;
				ps.setObject(idx++, URLDecoder.decode(row.getKeyword()));
				ps.setObject(idx++, row.getItem_key());
				ps.setObject(idx++, row.getImg_src());
				ps.setObject(idx++, row.getItem_name());
			}
	
			@Override
			public int getBatchSize() {
				return rankList.size();
			}
		});
	}
	
}
