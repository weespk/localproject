package com.example.demo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.example.demo.dao.SampleDao;

@SpringBootApplication
@EnableScheduling
public class Demo1Application {
	private static final Logger logger= LoggerFactory.getLogger(Demo1Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Demo1Application.class, args);
	}
	
	@Autowired
	private SampleDao dao;

	@Scheduled(fixedDelay = 3000)
	public void scheduleFixedRateTask() {
		logger.info("START image getting");
		
		List<Map<String,String>> list= dao.selectFiles();

		if(list.size()<1) {
			logger.info("nothing images");
			System.exit(-1);
		}
		else {
			for(Map<String,String> mss: list) {
				String b_idx= mss.get("b_idx");
				String idx= mss.get("idx");
				String base64= dao.getByteArrayFromImageURL(mss.get("filename"));
				
				System.out.println(String.format("%s,%s,%s", b_idx, idx, base64.subSequence(0, 10)));
				dao.updateFile(idx, base64);
	
				byte[] data= Base64.getDecoder().decode(base64);
				try (OutputStream stream= new FileOutputStream(new File(String.format("file/%s-%s.png", b_idx, idx)))) {
					stream.write(data);
				} catch (Exception e) {
					System.err.println(mss.toString());
				}
			}
			
			System.out.println("Fixed rate task - " + System.currentTimeMillis() / 1000);
		}
	}

}
