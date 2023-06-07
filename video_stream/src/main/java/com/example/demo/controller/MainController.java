package com.example.demo.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import com.example.demo.config.Beans;

@Controller
public class MainController {
	private static final Logger logger= LoggerFactory.getLogger(MainController.class);

//	@Value("#{movieDir}")
//	private String movieDir= "D:\\dn\\[tvN] 미생.E01~20.HDTV.H264.720p-WITH";
	
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String test(Model model) {
		logger.info("test {}", new Date().toString());
		model.addAttribute("now", new Date().toString());
		return "index";
	}
	
	@RequestMapping(value = "/movie/fileName", method = {RequestMethod.GET, RequestMethod.POST})
	public ModelAndView test(ModelAndView model, @RequestParam HashMap<String, Object> json) {
		String fileName= json.get("fileName").toString();
		
		model.addObject("fileName", fileName);
		model.setViewName("streamView");
		return model;
	}
	
	@ResponseBody
	@RequestMapping(value = "/movie/fileList", method = {RequestMethod.GET, RequestMethod.POST})
	public Map<String, Object> filelist(Model model) {
		
		Map<String, Object> result= new HashMap<String, Object>();
		result.put("result", "succ");
		
		File file= new File(Beans.movieDir);
		File [] fileArr= file.listFiles();
		
		if((fileArr == null) || (fileArr.length==0)) {
			logger.info("fileArr {}, dir {}", fileArr, Beans.movieDir);
			result.put("result", "fail");
			result.put("list", new ArrayList());
			return result;
		}
		
		ArrayList<String> list= new ArrayList();
		for (File readFile : fileArr) {
			list.add(readFile.getName());
		}
		result.put("list", list);
		
		return result;
	}
}
