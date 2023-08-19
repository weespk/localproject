package net.mobon.healthcheck.api.service;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FrameRtbService {

	@Value("${telegram.url.frameRtb}")
	private String telegramUrlFrameRtb;

	@Value("${batchServer.url.frameRtb}")
	private String frameRtbCheckerUrl;

	private RestTemplate restTemplate = new RestTemplate();

	public void chkFrameRtbAbnormalList() {
		log.info("START chkFrameRtbAbnormalList");
		try {

			String[] batchResult = { frameRtbCheckerUrl };
			StringBuffer buffer = new StringBuffer("::클릭 미검출 프레임 리스트::\r\n");

			for (String row : batchResult) {

				String response = restTemplate.getForObject(row, String.class);
//				log.info("response=> " + response);

				if(!StringUtils.isEmpty(response)) {
					Gson gson = new Gson();

					List<Map<String, String>> jsonObject = gson.fromJson(response, new TypeToken<List<Map<String, String>>>(){}.getType());
//					log.info("{}", jsonObject);

					for(Map m :jsonObject) {
						buffer.append("프레임:").append(m.get("FRME_CODE"));
						buffer.append(", 지면:").append((m.get("MEDIA_SCRIPT_NO")));
						buffer.append("\r\n");
					}
//					log.info("{}", buffer.toString());
					HttpHeaders headers = new HttpHeaders();
					HttpEntity<String> entity = new HttpEntity<>(headers);
					restTemplate.exchange(telegramUrlFrameRtb + buffer.toString(), HttpMethod.GET, entity, String.class);
				}

			}

		} catch (Exception e) {
			log.error("chkFrameRtbAbnormalList ", e);
		}
	}

}
