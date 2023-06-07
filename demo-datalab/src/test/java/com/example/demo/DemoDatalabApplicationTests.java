package com.example.demo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import kr.co.shineware.nlp.komoran.model.Token;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;

//@SpringBootTest
class DemoDatalabApplicationTests {

//	@Test
	public static void main(String []ar) throws IOException {
//		Map<String, Integer> result= (new HashMap<String, Integer>());
//		result.put("product_cnt", 0);
//		try {
//			String url="https://search.shopping.naver.com/search/all";
//			String urlPath= String.format("%s?query=%s&bt=-1&frm=NVSCPRO"
//					, url, ("제도판"));
//			Connection conn= Jsoup.connect(urlPath).header("referer", "https://search.naver.com/search.naver")
//					.header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36");
//			Document doc= conn.get();
//
//			List<Element> list= doc.select(".seller_filter_area");
//			for(Element row: list) {
//				List<Element> rows= row.select("li span");
//				for(Element row2: rows) {
//					if(row2.className().startsWith("subFilter_num")) {
//						result.put("product_cnt", Integer.parseInt(row2.html().replaceAll("[^0-9]", "")));
//					}
//				}
//			}
//		}catch(Exception e) {
//		}finally {
//		}
//	}
//
//	public void run3() {
		Komoran komoran = new Komoran(DEFAULT_MODEL.LIGHT);
        String []strToAnalyze= new String[] {"배송 빠르게 잘 받았습니다", 
        		"공간이 조금 좁아서 여러 클럽은 안들어 가지만", 
        		"연습장 가기는 딱입니다,휴대용으로 좋아요", 
        		"연습장 갈때 잘 사용 힐거 같아요", 
        		"그런데 힘이없어 흐물흐물 해서 세워두기가 어려워요 ㅠ,가볍고 좋아요.", 
        		"넣을 때 손잡이 고무 마찰로 잘 안들어 가기도 합니다.", 
        		"전체가 다 열리도록 지퍼를 개선하면 좋겠네요.,가볍고 저렴하고 맘에 드네요 아직 연습장에 안가봤어요 근데 넣어봤는데 괜찮습니다 맘에 들어요,색감 좋고, 튼튼합니다. 가성비 굿,그닥 고븝스러운 느낌은 없습니다.", 
        		"간단히 연습장 들고 다니기 편합니다.", 
        		"드랑버,유틸, 아이언,어프로치.퍼터 까지 들어갑니다.", 
        		"밑단부분에 PVC 재질인 비닐천같은게"};

        for(String tk: strToAnalyze) {
	        KomoranResult analyzeResultList = komoran.analyze(tk);
	        System.out.println(analyzeResultList.getPlainText());
	        List<Token> tokenList= analyzeResultList.getTokenList();
	        for (Token token : tokenList) {
	        	if(token.getPos().startsWith("NN") || token.getPos().startsWith("VV"))
	        		System.out.format("(%2d, %2d) %s/%s\n", token.getBeginIndex(), token.getEndIndex(), token.getMorph(), token.getPos());
	        }
        }
	}
	public void test() {
		Map<String,Integer> map= new HashMap<String,Integer>();
		map.put("2022-11-12", 10);
		map.put("b", 10);
		map.put("c", 30);
		map.put("d", 11);
		map.put("e", 44);
		
		for(String k: map.keySet()) {
			System.out.println(k.replace("-", ""));
		}
		
		List<?> list=  new ArrayList<>(map.values());
		Collections.reverse(list);
		
		System.out.println(list);
		
		double sum= Stream.of(map.values()).map(
					set -> set.stream().collect(Collectors.summingInt(Integer::intValue))
				).collect(
					Collectors.averagingDouble(Integer::doubleValue)
				);
		
		System.out.println(sum/map.keySet().size());
	}

}
