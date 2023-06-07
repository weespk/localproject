package com.example.demo;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.example.demo.dao.SampleDao;
import com.example.demo.model.Body;
import com.example.demo.model.Cid;
import com.example.demo.model.CidKeyword;
import com.example.demo.model.DomeggookShop;
import com.example.demo.model.Period;
import com.example.demo.model.Rank;
import com.example.demo.model.Rate;
import com.example.demo.model.Req;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;

@SpringBootApplication
@EnableScheduling
public class DemoDatalabApplication {
	private static final Logger logger= LoggerFactory.getLogger(DemoDatalabApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(DemoDatalabApplication.class, args);
	}

	@Autowired
	SampleDao sampleDao;

	private Map<String, Integer> mapClickDate= (new ConcurrentHashMap<String, Integer>());
//	private Map<String, Integer> mapGenderDate= (new ConcurrentHashMap<String, Integer>());
//	private Map<String, Integer> mapAgeDate= (new ConcurrentHashMap<String, Integer>());
	

	@Scheduled(fixedDelay = 3000)
	@Async
	private void SCrun4() throws Exception {
		run4();
	}

	@Scheduled(fixedDelay = 5000)
	@Async
	private void SCrun2() throws Exception {
		run2();
	}
	
	@Scheduled(fixedDelay = 1000)
	@Async
	private void SCrun3() throws Exception {
		run3();
	}
	
	// 도매꾹 상품별 최소구매수량
	public void run4() {
		List<Map<String,?>> list= sampleDao.selectDomeggookShopList();
		for(Map row: list) {
			row.put("USERID", "");
			row.put("MIN_ORDER_CNT", 0);
			row.put("lInfoViewImgUse", "");
			row.put("lInfoItemTitle", "");
			row.put("lThumbImg", "");
			row.put("lItemPrice", "");
			try {
				String url= row.get("SHOP_URL").toString();
				String urlPath= String.format("%s", url);
				Connection conn= Jsoup.connect(urlPath).header("referer", url)
						.header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36");
				Document doc= conn.get();
				String USERID= String.format("%s", doc.select("#lSellerInfo td").eq(0).html());
				String MIN_ORDER_CNT= String.format("%s", doc.select(".lInfoItemContent b").eq(0).html()).replaceAll(",", "");
				String lInfoViewImgUse= String.format("%s", doc.select(".lInfoViewImgUse b").eq(0).html());
				String lInfoItemTitle= String.format("%s", doc.select("#lInfoItemTitle").eq(0).html());
				String lThumbImg= String.format("%s", doc.select("#lThumbImg").attr("src"));
				String lItemPrice= String.format("%s", doc.select(".lItemPrice").eq(0).html());
				
				MIN_ORDER_CNT= MIN_ORDER_CNT.equals("")?"0":MIN_ORDER_CNT;
				
				row.put("USERID", USERID);
				row.put("MIN_ORDER_CNT", MIN_ORDER_CNT);
				row.put("lInfoViewImgUse", lInfoViewImgUse);
				row.put("lInfoItemTitle", lInfoItemTitle);
				row.put("lThumbImg", lThumbImg);
				row.put("lItemPrice", lItemPrice.replaceAll("[^0-9]", ""));
				
				logger.info("{}",row);
				
			}catch(Exception e) {
				logger.error("",e);
			}
		}
		sampleDao.insertDomeggookShop(list);
		
	}
	
	// 형태소분석기
	public void run3() {
//		logger.info("형태소분석기 START");
		Komoran komoran = new Komoran(DEFAULT_MODEL.FULL);
		
		List<Map<String,?>> list= sampleDao.selectKeywordReviewMorpheme();
		List<Map<String, ?>> pushList= new ArrayList<Map<String,?>>();
		for(Map row: list) {
			String strToAnalyze= row.get("html").toString();
			if("".equals(strToAnalyze) || strToAnalyze.startsWith("http")) {
				row.put("html_morpheme", "");
			}
			else {
				KomoranResult analyzeResultList= komoran.analyze(strToAnalyze);
		        List<Token> tokenList= analyzeResultList.getTokenList();
		        
		        StringBuffer sb= new StringBuffer();
		        for (Token token : tokenList) {
		        	if(token.getPos().startsWith("NN") || token.getPos().startsWith("VV")) {
		        		sb.append(String.format(" %s", token.getMorph()) ); //, token.getPos()));
		        	}
		            //System.out.format("(%2d, %2d) %s/%s\n", token.getBeginIndex(), token.getEndIndex(), token.getMorph(), token.getPos());
		        }
		        row.put("html_morpheme", sb.toString());
			}
			pushList.add(row);
	        logger.info(row.toString());
		}
		sampleDao.insertKeywordReviewMorpheme(pushList);
	}

	// 키워드수집
	public void run2() throws Exception {
		logger.info("키워드수집 START");
		
//		List<?> list= sampleDao.selectNow();
//		System.out.println(list);

//		String [][]list= new String[][] {{"50000000","패션의류"},{"50000001","패션잡화"},{"50000002","화장품/미용"},{"50000003","디지털/가전"}
//			,{"50000004","가구/인테리어"},{"50000005","출산/육아"},{"50000006","식품"},{"50000007","스포츠/레저"},{"50000008","생활/건강"}
//			,{"50000009","여가/생활편의"},{"50000010","면세점"},{"50005542","도서"}};

		String [][]list= new String[][] {{"empty","empty"}
		,{"50000001","패션잡화"}
		,{"50000003","디지털/가전"}
		,{"50000004","가구/인테리어"}
		,{"50000005","출산/육아"}
//		,{"50000006","식품"}
		,{"50000007","스포츠/레저"}
//		,{"50000008","생활/건강"}
			,{"50000165","공구"}
			,{"50000158","문구/사무묭품"}
			,{"50000054","화방용품"}
			,{"50000055","자동차용품"}
			,{"50000155","반려용품"}
			,{"50000061","주방용품"}
			,{"50000062","세탁용품"}
			,{"50000067","실버용품"}
			,{"50000068","재활운동용품"}
			,{"50000074","발건강용품"}
			,{"50000080","정원/원예용품"}
			,{"50000157","욕실용품"}
			,{"50000076","수납/정리용품"}
			,{"50000077","청소용품"}
			,{"50000078","생활용품"}
//		,{"50000009","여가/생활편의"}
		};
		
		Random rand = new Random();
		for (int i = 0; i < list.length; i++) {
			int randomIndexToSwap = rand.nextInt(list.length);
			String []temp = list[randomIndexToSwap];
			list[randomIndexToSwap] = list[i];
			list[i] = temp;
		}
		
		for(String[] cid: list) {
			if(!"empty".equals(cid[0])) {
				dataLabCategory(cid[0]);
				Thread.sleep(1000*60*20);
			}
		}
//		dataLabCategory("0");
		
//		System.exit(-1);
	}
	
	private void dataLabCategory(String cid) throws Exception {
		try {
			String url= "https://datalab.naver.com/shoppingInsight/getCategory.naver";
			String urlPath= String.format("%s?cid=%s", url,cid);
			Connection conn= Jsoup.connect(urlPath).header("referer", "https://datalab.naver.com/shoppingInsight/sCategory.naver")
					.header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36");
			Document doc= conn.get();
			String body= String.format("%s", doc.select("body").html());
			JsonReader reader= new JsonReader(new StringReader(body));
			reader.setLenient(true);
			JsonObject body2= new JsonParser().parse(reader).getAsJsonObject();

			Cid root= new Gson().fromJson(body2, Cid.class);
			for(Cid row: root.getChildList()) {
				row.setPage(1);
				Thread.sleep(5000);
				dataLabCategoryKeyword(row);
				dataLabCategory(row.getCid());
			}
		} catch(Exception e) {
			logger.error("err ", e);
		}
	}
	@Async
	private void dataLabCategoryKeyword(Cid cid) throws Exception {
		try {
			String url= "https://datalab.naver.com/shoppingInsight/getCategoryKeywordRank.naver";
			String sdate= LocalDateTime.now().minusDays(365).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			String sdate2= LocalDateTime.now().minusDays(15).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			String edate= LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			String urlPath= String.format("%s?cid=%s&timeUnit=week&startDate=%s&endDate=%s&age=&gender=&device=mo&page=%s&count=20"
					, url,cid.getCid(),sdate, edate, cid.getPage());
			Connection conn= Jsoup.connect(urlPath).header("referer", "https://datalab.naver.com/shoppingInsight/sCategory.naver")
					.header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36");
			Document doc= conn.get();
			String body= String.format("%s", doc.select("body").html());
			JsonReader reader= new JsonReader(new StringReader(body));
			reader.setLenient(true);
			JsonObject body2= new JsonParser().parse(reader).getAsJsonObject();

			long idx= sampleDao.insertRankCateData(cid.getCid(), cid.getName());
			
			CidKeyword root= new Gson().fromJson(body2, CidKeyword.class);
			List<Rank> rankList= new ArrayList<Rank>();
			for(Rank row: root.getRanks()) {
				if(sampleDao.selectKeywordSkip(row.getKeyword(), row.getRank())) {
//					logger.info("keyword={}, rank={} skip", row.getKeyword(), row.getRank());
					continue;
				}
				
				Thread.sleep(500);

				Map<String,Integer> trandClick= dataLabCategoryKeywordTrand(cid, row.getKeyword());
				Map<String,Integer> trandClick2= new HashMap();
				Map<String,Integer> trandAge= dataLabCategoryKeywordTrandAge(cid, row.getKeyword());
				Map<String,Integer> trandGender= dataLabCategoryKeywordTrandGender(cid, row.getKeyword());
				Map<String,Integer> trandDevice= dataLabCategoryKeywordTrandDevice(cid, row.getKeyword());
				Map<String,Integer> product= dataLabCategoryKeywordProductInfo(cid, row.getKeyword());

				// 1년평균
				double avg= Stream.of(trandClick.values()).map(set -> set.stream().collect(Collectors.summingInt(Integer::intValue)
						)).collect(Collectors.averagingDouble(Integer::doubleValue))/trandClick.entrySet().size();
				
				// 단기평균
//				StringBuffer trandClickString= new StringBuffer();
				for(String key: trandClick.keySet()) {
//					trandClickString.append(String.format("	%s", trandClick.get(key)));
					if(Integer.parseInt(key) > Integer.parseInt(sdate2.replaceAll("[^0-9]", "")) ) {
						trandClick2.put(key, trandClick.get(key));
					}
				}
				double avg2= Stream.of(trandClick2.values()).map(set -> set.stream().collect(Collectors.summingInt(Integer::intValue)
						)).collect(Collectors.averagingDouble(Integer::doubleValue))/trandClick2.entrySet().size();
				
				row.setPid(cid.getPid());
				row.setCid(cid.getCid());
				row.setB_idx(idx);
				row.setPage(cid.getPage());
				//row.setTrand_clickmap(trandClickString.toString());
				//row.setTrand_click(trandClickString.toString());
				row.setTrand_click(((int)avg)+"");
				row.setTrand_click2(((int)avg2)+"");
				row.setTrand_man(trandGender.get("m"));
				row.setTrand_woman(trandGender.get("f"));
				row.setTrand_age10(trandAge.get("10"));
				row.setTrand_age20(trandAge.get("20"));
				row.setTrand_age30(trandAge.get("30"));
				row.setTrand_age40(trandAge.get("40"));
				row.setTrand_age50(trandAge.get("50"));
				row.setTrand_age60(trandAge.get("60"));
				row.setTrand_pc(trandDevice.get("pc"));
				row.setTrand_mo(trandDevice.get("mo"));
				row.setProduct_cnt(product.get("product_cnt"));
				rankList.add(row);
				
//				List<DomeggookShop> dList= KeywordProductFromDomeggook(row.getKeyword());
//				sampleDao.insertKeywordFromDomeggook(dList);
				
				logger.info( String.format("%s	%s	%s	%s	%s	%s"
						, idx,cid.getCid(),cid.getName(),cid.getPage(),row.getRank(),row.getKeyword()) );
//				break;
			}
			
			sampleDao.insertRankKeywordTrand(rankList);
			
			
			if(cid.getPage()<25) {
				Thread.sleep(3000);
				cid.setPage(cid.getPage()+1);
				dataLabCategoryKeyword(cid);
			}
		} catch(Exception e) {
			logger.error("err 2 ", e);
		}
	}
	private Map<String,Integer> dataLabCategoryKeywordTrand(Cid cid, String keyword) throws Exception {
		Map<String, Integer> result= (new HashMap<String, Integer>());
		try {
			String url= "https://datalab.naver.com/shoppingInsight/getKeywordClickTrend.naver";
			String sdate= LocalDateTime.now().minusDays(365).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			String edate= LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			String urlPath= String.format("%s?cid=%s&timeUnit=week&startDate=%s&endDate=%s&age=&gender=&device=mo&keyword=%s"
					, url,cid.getCid(), sdate, edate ,keyword);
			Connection conn= Jsoup.connect(urlPath).header("referer", "https://datalab.naver.com/shoppingInsight/sCategory.naver")
					.header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36");
			Document doc= conn.get();
			String body= String.format("%s", doc.select("body").html());
			JsonReader reader= new JsonReader(new StringReader(body));
			reader.setLenient(true);
			JsonObject body2= new JsonParser().parse(reader).getAsJsonObject();
			JsonObject body3= new JsonParser().parse(body2.get("result").getAsJsonArray().get(0).toString()).getAsJsonObject();

			Period []data= new Gson().fromJson(body3.get("data"), Period[].class);
			for(Period row: data) {
				if(mapClickDate.get(row.getPeriod())==null) {
					mapClickDate.put(row.getPeriod(), 0);
				} else {
					mapClickDate.put(row.getPeriod(), Math.round(row.getValue()));
				}
			}
		}catch(Exception e) {
			logger.error("err 3 ", e);
		}finally {
			for(String key: mapClickDate.keySet()) {
				result.put(key, mapClickDate.get(key));
				mapClickDate.put(key,0);
			}
		}
		return result;
	}
	private Map<String,Integer> dataLabCategoryKeywordTrandAge(Cid cid, String keyword) throws Exception {
		Map<String, Integer> result= (new HashMap<String, Integer>()); //(Map<String, Integer>) ImmutableMap.<String,Integer>builder().put("10", 0).put("20", 0).put("30", 0).put("40", 0).put("50", 0).put("60", 0).build(); // (new HashMap<String, Integer>());
		result.put("10", 0);
		result.put("20", 0);
		result.put("30", 0);
		result.put("40", 0);
		result.put("50", 0);
		result.put("60", 0);
		
		try {
			String url= "https://datalab.naver.com/shoppingInsight/getKeywordAgeRate.naver";
			String sdate= LocalDateTime.now().minusDays(365).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			String edate= LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			String urlPath= String.format("%s?cid=%s&timeUnit=week&startDate=%s&endDate=%s&age=&gender=&device=&keyword=%s"
					, url,cid.getCid(), sdate, edate ,keyword);
			Connection conn= Jsoup.connect(urlPath).header("referer", "https://datalab.naver.com/shoppingInsight/sCategory.naver")
					.header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36");
			Document doc= conn.get();
			String body= String.format("%s", doc.select("body").html());
			JsonReader reader= new JsonReader(new StringReader(body));
			reader.setLenient(true);
			JsonObject body2= new JsonParser().parse(reader).getAsJsonObject();
			JsonObject body3= new JsonParser().parse(body2.get("result").getAsJsonArray().get(0).toString()).getAsJsonObject();

			Rate []data= new Gson().fromJson(body3.get("data"), Rate[].class);
			for(Rate row: data) {
				if(result.get(row.getCode())==null) {
					result.put(row.getCode(), 0);
				} else {
					result.put(row.getCode(), Math.round(row.getRatio()));
				}
			}
		}catch(Exception e) {
			logger.error("err 3 ", e);
		}finally {
		}
		return result;
	}
	private Map<String,Integer> dataLabCategoryKeywordTrandGender(Cid cid, String keyword) throws Exception {
		Map<String, Integer> result= (new HashMap<String, Integer>()); //(Map<String, Integer>) ImmutableMap.<String,Integer>builder().put("m", 0).put("f", 0).build(); //
		result.put("m", 0);
		result.put("f", 0);
		
		try {
			String url= "https://datalab.naver.com/shoppingInsight/getKeywordGenderRate.naver";
			String sdate= LocalDateTime.now().minusDays(365).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			String edate= LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			String urlPath= String.format("%s?cid=%s&timeUnit=week&startDate=%s&endDate=%s&age=&gender=&device=&keyword=%s"
					, url,cid.getCid(), sdate, edate ,keyword);
			Connection conn= Jsoup.connect(urlPath).header("referer", "https://datalab.naver.com/shoppingInsight/sCategory.naver")
					.header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36");
			Document doc= conn.get();
			String body= String.format("%s", doc.select("body").html());
			JsonReader reader= new JsonReader(new StringReader(body));
			reader.setLenient(true);
			JsonObject body2= new JsonParser().parse(reader).getAsJsonObject();
			JsonObject body3= new JsonParser().parse(body2.get("result").getAsJsonArray().get(0).toString()).getAsJsonObject();

			Rate []data= new Gson().fromJson(body3.get("data"), Rate[].class);
			for(Rate row: data) {
				if(result.get(row.getCode())==null) {
					result.put(row.getCode(), 0);
				} else {
					result.put(row.getCode(), Math.round(row.getRatio()));
				}
			}
		}catch(Exception e) {
			logger.error("err 3 ", e);
		}finally {
		}
		return result;
	}
	private Map<String,Integer> dataLabCategoryKeywordTrandDevice(Cid cid, String keyword) throws Exception {
		Map<String, Integer> result= (new HashMap<String, Integer>()); //(Map<String, Integer>) ImmutableMap.<String,Integer>builder().put("m", 0).put("f", 0).build(); //
		result.put("mo", 0);
		result.put("pc", 0);
		
		try {
			String url= "https://datalab.naver.com/shoppingInsight/getKeywordDeviceRate.naver";
			String sdate= LocalDateTime.now().minusDays(365).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			String edate= LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			String urlPath= String.format("%s?cid=%s&timeUnit=week&startDate=%s&endDate=%s&age=&gender=&device=&keyword=%s"
					, url,cid.getCid(), sdate, edate ,keyword);
			Connection conn= Jsoup.connect(urlPath).header("referer", "https://datalab.naver.com/shoppingInsight/sCategory.naver")
					.header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36");
			Document doc= conn.get();
			String body= String.format("%s", doc.select("body").html());
			JsonReader reader= new JsonReader(new StringReader(body));
			reader.setLenient(true);
			JsonObject body2= new JsonParser().parse(reader).getAsJsonObject();
			JsonObject body3= new JsonParser().parse(body2.get("result").getAsJsonArray().get(0).toString()).getAsJsonObject();

			Rate []data= new Gson().fromJson(body3.get("data"), Rate[].class);
			for(Rate row: data) {
				if(result.get(row.getCode())==null) {
					result.put(row.getCode(), 0);
				} else {
					result.put(row.getCode(), Math.round(row.getRatio()));
				}
			}
		}catch(Exception e) {
			logger.error("err 3 ", e);
		}finally {
		}
		return result;
	}
	private Map<String,Integer> dataLabCategoryKeywordProductInfo(Cid cid, String keyword) throws Exception {
		Map<String, Integer> result= (new HashMap<String, Integer>());
		result.put("product_cnt", 0);
		try {
			String url="https://search.shopping.naver.com/search/all";
			String urlPath= String.format("%s?query=%s&bt=-1&frm=NVSCPRO"
					, url, (keyword));
			Connection conn= Jsoup.connect(urlPath).header("referer", "https://search.naver.com/search.naver")
					.header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36");
			Document doc= conn.get();

			List<Element> list= doc.select(".seller_filter_area");
			for(Element row: list) {
				List<Element> rows= row.select("li span");
				for(Element row2: rows) {
					if(row2.className().startsWith("subFilter_num")) {
						result.put("product_cnt", Integer.parseInt(row2.html().replaceAll("[^0-9]", "")));
						break;
					}
				}
			}
		}catch(Exception e) {
		}finally {
		}
		return result;
	}
	private List<DomeggookShop> KeywordProductFromDomeggook(String keyword) throws Exception {
		List<DomeggookShop> result= new ArrayList<DomeggookShop>();
		try {
			String url= "https://domemedb.domeggook.com/index/item/supplyList.php";
			String urlPath= String.format("%s?sf=subject&enc=utf8&fromOversea=0&mode=search&so=rq&pageLimit=20&sw=%s"
					, url, keyword);
			Connection conn= Jsoup.connect(urlPath)
					.header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36");
			Document doc= conn.get();
			List<Element> items= doc.select(".sub_cont_bane1");
			for(Element item: items) {
				String item_key= item.select(".input_check3").val();
				String img_src= item.select(".bane_brd1 img").attr("src");
				String item_name= item.select(".itemName").html();
				String price= item.select(".main_cont_text1 strong").html();
				String mall_name= item.select(".main_cont_text3").attr("title");
				String href= "http://domeme.domeggook.com/s/"+ item_key;
//				System.out.println(String.format("item_key=%s, img_src=%s, item_name=%s, href=%s"
//						, item_key, img_src, item_name, href));
				result.add(new DomeggookShop(keyword,item_key,img_src,item_name));
			}
		}catch(Exception e) {
		}finally {		}
		
		return result;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void dataLabHome(String[] cid) throws Exception {
		String url= String.format("https://datalab.naver.com/shoppingInsight/getKeywordRank.naver?timeUnit=date&cid=%s", cid[0]);
		Connection conn= Jsoup.connect(url)
				.header("referer", "https://datalab.naver.com/");
		Document doc= conn.get();
		String body= String.format("{\"body\":%s}", doc.select("body").html());
		JsonReader reader= new JsonReader(new StringReader(body));
		reader.setLenient(true);
		JsonObject body2= new JsonParser().parse(reader).getAsJsonObject();

		Body root= new Gson().fromJson(body2, Body.class);
		for (Req root2: root.getBody()) {
			String title= (String.format("%s_%s_%s", cid[0], cid[1], root2.getDate().replaceAll("/", "")));
//			for (Rank entry: root2.getRanks()) {
//				System.out.println(String.format("%s %s", entry.getRank(), entry.getLinkId()));
//			}
		}
	}
	
//	@Scheduled(fixedDelay = 5000)
	public void run() throws IOException {
		Connection conn= Jsoup.connect("https://arteriver.cafe24.com/t.html");
		Document doc= conn.get();
		Elements el= doc.select(".keyword_rank");
		
		for( Element r: el) {
			String title_cell= r.select(".title_cell").html();
			Elements lists= r.select(".list");
			
			for(Element list: lists) {
				String href= list.select(".list_area").attr("href").toString();
				String num= list.select(".num").html();
				String title= list.select(".title").html();
				
				System.out.println(String.format("%s %s %s %s", title_cell, href, num, title));
			}
		}
		
		System.exit(-1);
	}
}
