package net.mobon.healthcheck.api.service;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class ADIDMongoService {

  @Value("${telegram.url.adid}")
  private String telegramUrlADIDMobon; // 전용 텔레봇

  @Value("${adid.filePath}")
  private String filePath;

  @Autowired
  private RestTemplate restTemplate;

  public void health() {
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<String> entity = new HttpEntity<>(headers);
    List<String> listParams = new ArrayList<>();
    listParams.add("160");
    listParams.add("161");
    listParams.add("162");
    listParams.add("163");
    listParams.add("164");
    listParams.add("165");
    listParams.add("166");
    listParams.add("167");
    listParams.add("168");
    listParams.add("169");
    listParams.add("170");
    listParams.add("171");

    File fileList = new File(filePath);
    if (fileList.exists()) {
      for (String param : listParams) {
        StringBuilder message = new StringBuilder();
        boolean isOk = false;
        try {
          JSONObject statResult;
          FilenameFilter statFilter = (dir, name) -> name.equals(param + "_stat.json");
          File[] statFiles = fileList.listFiles(statFilter);
          String statPath = Objects.requireNonNull(statFiles)[0].getCanonicalPath();
          List<String> statLines = FileUtils.readLines(new File(statPath), StandardCharsets.UTF_8);
          StringBuilder statText = new StringBuilder();
          for (String line : statLines) {
            statText.append(line);
          }
          if (StringUtils.isNotEmpty(statText)) {
            statResult = JSONObject.fromObject(statText.toString().trim()).getJSONObject("localhost:20001");
            isOk = stat(param, statResult, message, isOk);
          }

          JSONObject topResult;
          FilenameFilter topFilter = (dir, name) -> name.equals(param + "_top.json");
          if (fileList.exists()) {
            File[] topFiles = fileList.listFiles(topFilter);

            String topPath = Objects.requireNonNull(topFiles)[0].getCanonicalPath();
            List<String> topLines = FileUtils.readLines(new File(topPath), StandardCharsets.UTF_8);
            StringBuilder topText = new StringBuilder();
            for (String line : topLines) {
              topText.append(line);
            }
            if (StringUtils.isNotEmpty(topText)) {
              topResult = JSONObject.fromObject(topText.toString().trim());
              isOk = top(topResult, message, isOk);
            }
          }
          if (isOk) {
            restTemplate.exchange(telegramUrlADIDMobon + message.toString(), HttpMethod.GET, entity, String.class);
          }
          log.info(param + " health check OK");


        } catch (Exception e) {
          log.error(param + " : " + e.getMessage());
        }
      }
    }
  }

  /**
   * mongostat에서 생성된 값.
   *
   * @param param
   * @param result
   * @param message
   * @param isOK
   * @return boolean
   */
  public boolean stat(String param, JSONObject result, StringBuilder message, boolean isOK) {
    String[] qrws = result.getString("qrw").split("[|]");

    String[] arws = result.getString("arw").split("[|]");
    String query = result.getString("query");

    Pattern pattern = Pattern.compile("[^0-9]");
    Matcher matcher = pattern.matcher(result.getString("insert"));
    String insert = matcher.replaceAll("");

    matcher = pattern.matcher(result.getString("update"));
    String update = matcher.replaceAll("");

    matcher = pattern.matcher(result.getString("delete"));
    String delete = matcher.replaceAll("");

    if (Integer.valueOf(query) > 3000) {
      isOK = true;
      message.append("[HEALTHCHECK] QUERY is high(3000 초과)\n");
    } else if (Integer.valueOf(insert) > 1000) {
      isOK = true;
      message.append("[HEALTHCHECK] INSERT is high(1000 초과)\n");
    } else if (Integer.valueOf(qrws[0]) > 10) {
      isOK = true;
      message.append("[HEALTHCHECK] READ QUEUE is high(10 초과)\n");
    } else if (Integer.valueOf(qrws[1]) > 10) {
      isOK = true;
      message.append("[HEALTHCHECK] WRITE QUEUE is high(10 초과)\n");
    } else if (Integer.valueOf(arws[0]) > 15) {
      isOK = true;
      message.append("[HEALTHCHECK] READ ACTIVE is high(15 초과)\n");
    } else if (Integer.valueOf(arws[1]) > 10) {
      isOK = true;
      message.append("[HEALTHCHECK] WRITE ACTIVE is high(10 초과)\n");
    }
    message.append("[").append(param).append("-").append(result.getString("set")).append("] \n")
        .append("[MSTAT MAKE TIME] : ").append(result.getString("time")).append("\n")
        .append("CONN         : ").append(result.getString("conn")).append("\n")
        .append("QUERY(TPS)   : ").append(query).append("\n")
        .append("INSERT(TPS)  : ").append(insert).append("\n")
        .append("UPDATE(TPS)  : ").append(update).append("\n")
        .append("DELETE(TPS)  : ").append(delete).append("\n")
        .append("READ QUEUE   : ").append(qrws[0]).append("\n")
        .append("READ ACTIVE  : ").append(arws[0]).append("\n")
        .append("WRITE QUEUE  : ").append(qrws[1]).append("\n")
        .append("WRITE ACTIVE : ").append(arws[1]).append("\n");
    return isOK;
  }

  /**
   * mongotop 에서 생성된 값.
   *
   * @param topResult
   * @param message
   * @param isOK
   * @return boolean
   */
  public boolean top(JSONObject topResult, StringBuilder message, boolean isOK) {

    JSONObject result = topResult.getJSONObject("totals").getJSONObject("user_mapping.auth_id_info");
    int readTime = result.getJSONObject("read").getInt("time");
    int writeTime = result.getJSONObject("write").getInt("time");
    // read, write는 1000 ms 이상이면 알림.
    if (readTime > 1000 || writeTime > 1000) {
      isOK = true;
    }

    message.append("[MTOP MAKE TIME] : ").append(topResult.getString("time")).append("\n");
    message.append("READ TIME   : ").append(readTime).append(" ms").append("\n");
    message.append("WRITE TIME : ").append(writeTime).append(" ms");

    return isOK;
  }
}
