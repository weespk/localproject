package com.example.demo.dao;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SampleDao {

	@Autowired
	private JdbcTemplate jt;

	public List<Map<String, ?>> selectAll() {
		return jt.query("select * from tbl_board where b_id='023'", (rs, rowNum) -> {
			System.out.println(rs);
			return null;
		});
	}
	
	public List<Map<String, String>> selectFiles() {
		return jt.query("select b_idx, a.idx, a.filename from tbl_board_file a join tbl_board b on a.b_idx =b.idx and b.b_id='023' and filesize='' limit 3", (rs, rowNum) -> {
			Map<String, String> mss = new HashMap<>();
			mss.put("b_idx", rs.getString(1));
			mss.put("idx", rs.getString(2));
			mss.put("filename", rs.getString(3));
			return mss;
		});
	}
	
	public void updateFile(String idx, String base64) {
		jt.update(String.format("update tbl_board_file set filesize='%s' where idx='%s' ", base64.length(), idx));
	}
	
	public String getByteArrayFromImageURL(String url) {
		try {
			URL imageUrl = new URL(url);
			URLConnection ucon = imageUrl.openConnection();
			InputStream is = ucon.getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int read = 0;
			while ((read = is.read(buffer, 0, buffer.length)) != -1) {
				baos.write(buffer, 0, read);
			}
			baos.flush();
			return Base64.getEncoder().encodeToString(baos.toByteArray());
		} catch (Exception e) {
		}
		return null;
	}
}
