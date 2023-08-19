/*
 * COPYRIGHT (c) Enliple 2020
 * This software is the proprietary of Enliple
 *
 * @author <a href="mailto:kwseo@enliple.com">kwseo</a>
 * @since 2020-11-04
 */

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

/**
 * create on 2020-11-04.
 * <p> UserLog Mongo </p>
 *
 * @author kwseo
 * @version 1.0
 * @since 지원하는 자바버전 (ex : 5+ 5이상)
 */
@Service
@Slf4j
public class UserLogMongoService {

  @Value("${telegram.url.userlog}")
  private String telegramUrlUserLogMongo; // 전용 텔레봇

  @Value("${userlog.filePath}")
  private String filePath;

  @Autowired
  private RestTemplate restTemplate;

  /**
   * USERLOG 몽고 헬스 체크.
   */
  public void health() {
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<String> entity = new HttpEntity<>(headers);

    List<String> listParams = new ArrayList<>();
    listParams.add("120");
    listParams.add("121");
    listParams.add("122");
    listParams.add("123");
    listParams.add("124");
    listParams.add("125");
    listParams.add("130");
    listParams.add("131");
    listParams.add("132");
    listParams.add("133");
    listParams.add("134");
    listParams.add("135");
    listParams.add("136");
    listParams.add("137");
    listParams.add("138");
    listParams.add("139");
    listParams.add("140");
    listParams.add("141");
    listParams.add("142");
    listParams.add("143");
    listParams.add("144");
    listParams.add("145");
    listParams.add("146");
    listParams.add("147");
    listParams.add("148");
    listParams.add("149");
    listParams.add("150");
    listParams.add("151");
    listParams.add("152");
    listParams.add("153");
    listParams.add("154");
    listParams.add("155");
    listParams.add("156");
    listParams.add("157");
    listParams.add("158");
    listParams.add("159");
    listParams.add("220");
    listParams.add("221");
    listParams.add("222");
    listParams.add("223");

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
            restTemplate.exchange(telegramUrlUserLogMongo + message.toString(), HttpMethod.GET, entity, String.class);
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
   * @param statResult
   * @param message
   * @param isOk
   * @return
   */
  public boolean stat(String param, JSONObject statResult, StringBuilder message, boolean isOk) {
    String[] qrws = statResult.getString("qrw").split("[|]");

    String[] arws = statResult.getString("arw").split("[|]");
    String query = statResult.getString("query");
    String conn = statResult.getString("conn");

    Pattern pattern = Pattern.compile("[^0-9]");
    Matcher matcher = pattern.matcher(statResult.getString("insert"));
    String insert = matcher.replaceAll("");

    matcher = pattern.matcher(statResult.getString("update"));
    String update = matcher.replaceAll("");

    matcher = pattern.matcher(statResult.getString("delete"));
    String delete = matcher.replaceAll("");

    if (Integer.valueOf(query) > 3000) {
      isOk = true;
      message.append("[HEALTHCHECK] QUERY(TPS) is high(3000 초과)\n");
    } else if (Integer.valueOf(insert) > 5000) {
      isOk = true;
      message.append("[HEALTHCHECK] INSERT(TPS) is high(5000 초과)\n");
    } else if (Integer.valueOf(qrws[0]) > 30) {
      isOk = true;
      message.append("[HEALTHCHECK] READ QUEUE is high(30 초과)\n");
    } else if (Integer.valueOf(qrws[1]) > 30) {
      isOk = true;
      message.append("[HEALTHCHECK] WRITE QUEUE is high(30 초과)\n");
    } else if (Integer.valueOf(arws[0]) > 40) {
      isOk = true;
      message.append("[HEALTHCHECK] READ ACTIVE is high(40 초과)\n");
    } else if (Integer.valueOf(arws[1]) > 30) {
      isOk = true;
      message.append("[HEALTHCHECK] WRITE ACTIVE is high(30 초과)\n");
    } else if (Integer.valueOf(conn) > 8600) {
      // 2021-10-18 기준 제일 높은 Connection 기준으로 설정 후 모니터링 진행
      isOk = true;
      message.append("[HEALTHCHECK] CONN is high(8600 초과)\n");
    }
    message.append("[").append(param).append("-").append(statResult.getString("set")).append("-").append(statResult.getString("repl")).append("] \n")
        .append("[MSTAT MAKE TIME] : ").append(statResult.getString("time")).append("\n")
        .append("CONN         : ").append(statResult.getString("conn")).append("\n")
        .append("QUERY(TPS)   : ").append(query).append("\n")
        .append("INSERT(TPS)  : ").append(insert).append("\n")
        .append("UPDATE(TPS)  : ").append(update).append("\n")
        .append("DELETE(TPS)  : ").append(delete).append("\n")
        .append("READ QUEUE   : ").append(qrws[0]).append("\n")
        .append("READ ACTIVE  : ").append(arws[0]).append("\n")
        .append("WRITE QUEUE  : ").append(qrws[1]).append("\n")
        .append("WRITE ACTIVE : ").append(arws[1]).append("\n");
    return isOk;
  }

  /**
   * mongotop 에서 생성된 값
   *
   * @param topResult
   * @param message
   * @param isOk
   * @return
   */
  public boolean top(JSONObject topResult, StringBuilder message, boolean isOk) {
    String[] number = new String[]{"00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18",
        "19"};

    for (String collectionNo : number) {
      JSONObject result = topResult.getJSONObject("totals").getJSONObject("userlog.CIData_" + collectionNo);
      int readTime = result.getJSONObject("read").getInt("time");
      int writeTime = result.getJSONObject("write").getInt("time");
      // read, write는 3000 ms 이상이면 알림.
      if (readTime > 30000 || writeTime > 30000) {
        isOk = true;
        message.append("[CIData").append(collectionNo).append(" MTOP MAKE TIME] : ").append(topResult.getString("time")).append("\n");
        message.append("READ TIME   : ").append(readTime).append(" ms").append("\n");
        message.append("WRITE TIME : ").append(writeTime).append(" ms").append("\n");
      }

    }

    return isOk;
  }

}

