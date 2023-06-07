package com.example.demo.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.demo.vo.Row;

@Repository
public class SampleDao {
	private static final Logger logger= LoggerFactory.getLogger(SampleDao.class);

	@Autowired
	private JdbcTemplate jt;

	public Map<String, String> selectNow() {
		List<Map<String, String>> map= jt.query("select now() now", (rs, rowNum) -> {
			logger.info("row {}, {}", rs, rowNum);
			
			Map<String, String> mss = new HashMap<>();
			mss.put("now", rs.getString(1));
			return mss;
		});
		return map.get(0);
	}
	
	public void insert(List<Row> roomInventories) {
		String sql = "INSERT INTO tbl_board_apt"
				+ "(roomtype_id, inventory_date, available_count, created_at, modified_at)"
				+ "VALUES"
				+ "(?, ?, ?, ?, ?)";

		jt.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				Row row = roomInventories.get(i);
				ps.setLong(1, row.getRoomType().getId());
				ps.setObject(2, row.getInventoryDate());
				ps.setInt(3, row.getAvailableCount());
				ps.setObject(4, LocalDateTime.now());
				ps.setObject(5, LocalDateTime.now());
			}

			@Override
			public int getBatchSize() {
				return roomInventories.size();
			}
		});
	}
	
}
