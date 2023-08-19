package net.mobon.healthcheck.api.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileUtil {
	
	public static String readFile(String destFile){
		BufferedReader fr=null;
		StringBuffer sb=new StringBuffer();
		try{
			File f=new File(destFile);
			if (f.isFile()) {
				//fr = new BufferedReader(new FileReader(f));
				fr = new BufferedReader(new InputStreamReader(new FileInputStream(f),"UTF8"));
				String line;
				while((line=fr.readLine())!=null){
					sb.append(line).append("\n");
				}
			} else {
				return "";
			}
			return sb.toString();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try {
				if(fr!=null)fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ""; 
	}
	
}
