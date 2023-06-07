package com.example.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.codehaus.plexus.util.FileUtils;
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
public class DemoAptdealApplication {
	private static final Logger logger= LoggerFactory.getLogger(DemoAptdealApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(DemoAptdealApplication.class, args);
	}

	public static boolean flag= false;

	@Autowired
	private SampleDao dao;

	public void scheduleFixedRateTask() {
		Map<String, String> map= dao.selectNow();
		logger.info("list {}", map);
	}
	
	@Scheduled(fixedDelay = 3000)
	public void run() {
		try {
			if(DemoAptdealApplication.flag) return;
			DemoAptdealApplication.flag= true;
			
			String logPath= "D:\\w\\weespk\\아파트분석\\전국_아파트_전월세_07_12\\매매\\";
			FileInputStream fileinput = null;
			BufferedReader reader = null;

			File []files = new File( logPath ).listFiles();

			if(files.length>0) {
				for( File f : files ) {
					logger.info("retry_files {}	{}	{}", f.lastModified(), new Date(f.lastModified()), f.getName() );
				}
			}

			int j=0;
			if ( files.length>0 ) {
				if(!files[j].isFile())return ;
				
				logger.info("files[j] - {}", files[j].getName());
				
				String tmp_name = files[j].getAbsolutePath() + "_ing";
				File tmp_file = new File( tmp_name );
				files[j].renameTo( tmp_file );
				
				try {
					fileinput = new FileInputStream( tmp_name );
					reader = new BufferedReader(new InputStreamReader(fileinput));
					String line = null;
					while( (line = reader.readLine()) != null){
						String []lines = line.split(",");
						if(lines.length>3) {
							if(!lines[0].equals("시군구")) {
								logger.info("line {}", lines[0]);
							}
						}
					}
				}catch(Exception e) {
				}finally {
					reader.close();
					fileinput.close();
//					File fileD = new File(tmp_name);
//					if(fileD.exists()&&fileD.isFile()) {
//						fileD.delete();
//					}
//					String filePathName = tmp_file.getAbsolutePath();
//					if(filePathName.indexOf("_ing")>0) {
//						File fileT  = new File (filePathName.replace("_ing",""));								
//						tmp_file.renameTo(fileT);
//					}
				}
			}
			logger.info("retryOk files.length - {}", files.length);
			
		}catch(Exception e) {
		}
		DemoAptdealApplication.flag= false;
	}
}
