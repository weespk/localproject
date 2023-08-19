package net.mobon.healthcheck.api.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.extern.slf4j.Slf4j;
import net.mobon.healthcheck.api.service.BillingService.KafkaGroupSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class RebuildService {

  @Value("${telegram.url.rebuild}")
  private String telegramUrlRebuild;
  @Autowired
  private RestTemplate restTemplate;

  public void conversionServiceChk() {
    try {
      StringBuffer sbf = new StringBuffer("컨버전 \n");
      boolean chk = false;
      String[] runingChk = {
          "http://192.168.2.76:8000/rebuildConvCheck.txt"};

      for (String row : runingChk) {
        String response = restTemplate.getForObject(row, String.class);
        List<Map> list = new Gson().fromJson(response, ArrayList.class);

        for (Map i : list) {
          log.debug("i - {}", i);
          if (!"OK".equals(i.get("CHK").toString())) {
            sbf.append(String.format("[%s]마지막 구동시간 - %s\n", i.get("REG_USER_ID").toString(), i.get("REG_DTTM").toString()));
            chk = true;
          }
        }
      }

      if (chk) {
        log.info("msg - {}", sbf.toString());

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        restTemplate.exchange(telegramUrlRebuild + sbf.toString(), HttpMethod.GET, entity, String.class);
      }

    } catch (Exception e) {
      log.error("RebuildService conversionServiceChk {}", e);
    }
  }


  public void kafkaTopicLagChk() {
    log.info("START BillingService kafkaTopicLagChk");
    try {
      StringBuffer sbf = new StringBuffer("리빌드 - LAG DELAY \n");
      String[][] info = new String[][]{  // 각 토픽별 5분 딜레이시 지연된 갯수
//					{ "clickhouse.inflow.table", "advertiser.common", "1500000" },
          {"adver.conversion", "advertiser.conversion", "10000"}
          , {"advertiser.common.inclination", "advertiser.common", "10000"}
          , {"advertiser.product.inclination", "advertiser.inclinations.product", "10000"}
          , {"keyword.click.collect", "advertiser.product", "15000"}
          , {"keyword.cart.collect", "advertiser.cart", "10000"}
          , {"keyword.adver.conversion", "advertiser.conversion", "10000"}
          , {"keyword.external.collect", "advertiser.common", "30000"}
          , {"keyword.internal.collect", "advertiser.common", "30000"}
      };
      boolean chk = false;

      for (String[] row : info) {
        ArrayList<HashMap> list = chkingTopicLag(row[0], row[1], Long.parseLong(row[2]));
        if (list.size() > 0) {
          Map map = list.get(0);
          sbf.append(String.format("%s, %s\n", row[0], map.get(row[1])));
          chk = true;
        }
      }

      if (chk) {
        log.info("msg - {}", sbf.toString());

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        restTemplate.exchange(telegramUrlRebuild + sbf.toString(), HttpMethod.GET, entity, String.class);
      }

    } catch (Exception e) {
      log.error("BillingService kafkaTopicLagChk", e);
    }
  }

  private ArrayList chkingTopicLag(String groupName, String topic, long maxLagCnt) {
    ArrayList result = new ArrayList();
    try {
      String kafkaGroupSummeryUrl = "http://192.168.2.77:9015/api/status/kafka/" + groupName + "/KF/groupSummary";
      String response = restTemplate.getForObject(kafkaGroupSummeryUrl, String.class);

      JsonObject root = new JsonParser().parse(response).getAsJsonObject();
      Gson gson = new Gson();
      for (Entry<String, JsonElement> entry : root.entrySet()) {
        KafkaGroupSummary summery = gson.fromJson(entry.getValue(), KafkaGroupSummary.class);
        log.debug("lag - {}, entry - {}", summery.getTotalLag(), entry.getKey());

        if (topic.equals(entry.getKey())) {
          if (summery.getTotalLag() > maxLagCnt) {
            Map map = new HashMap();
            map.put(topic, summery.getTotalLag());
            result.add(map);
            break;
          }
        }
      }
    } catch (Exception e) {
      log.error("err ", e);
    }
    return result;
  }
}
