package net.mobon.healthcheck.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by bnjjong on 2018-02-09 0009.
 */
@Service
@Slf4j
public class HCRedisService implements HealthChecker {
	private static final String CLUSTER_IP = "10.251.0.102";
	private static final int CLUSTER_PORT = 7200;
	private static final String KAKAO_CLUSTER_IP = "10.251.0.102";
	private static final int KAKAO_CLUSTER_PORT = 7000;
	private static final String SINGLE_LOG_IP = "10.251.0.102";
	private static final int SINGLE_LOG_PORT = 8000;
	private static final int SINGLE_LOG_TIMEOUT = 3000;
	private static final String SINGLE_LOG_PASSWD = "Fhrmtnwlq2017!23$";
	private static final int SINGLE_LOG_LIMIT_COUNT = 300000;


	@Value("${telegram.url.redis}")
	private String telegramRedisApi;

	@Autowired
	private RestTemplate restTemplate;

	@Override
	public void execute() {
		JedisCluster jc = null;
		Jedis jedis = null;
		try{
			jc = getMainJedisCluster();
			jc.incr("ping");
		} catch(Exception e){
			restTemplate.exchange(telegramRedisApi + " Redis service ping fail!!!" + e.getMessage(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
		} finally {
			if(jc != null) {
				try {
					jc.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}


		try{
			jc = getMainKakaoJedisCluster();
			jc.incr("ping");
		} catch(Exception e){
			restTemplate.exchange(telegramRedisApi + " Redis kakao service ping fail!!!" + e.getMessage(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
		} finally {
			if(jc != null) {
				try {
					jc.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		try{
			jedis = getLogJedis();
			String result = jedis.ping();
			if(!"pong".equals(result.toLowerCase())) {
				restTemplate.exchange(telegramRedisApi + " Redis log ping fail!!!", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
			}

			jedis.select(13);
			int size = jedis.keys("*").size();
			if(size > SINGLE_LOG_LIMIT_COUNT){
				restTemplate.exchange(telegramRedisApi + " log size more than "+ SINGLE_LOG_LIMIT_COUNT +"!!! must delete error log!", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
			}
		} catch(Exception e){
			restTemplate.exchange(telegramRedisApi + " Redis log fail!!!" + e.getMessage(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
		} finally {
			if(jedis != null){
				jedis.close();
			}
		}
	}

	private JedisCluster getMainJedisCluster(){
		Set<HostAndPort> jedisClusterNodes = new HashSet<>();
		//Jedis Cluster will attempt to discover cluster nodes automatically
		jedisClusterNodes.add(new HostAndPort(CLUSTER_IP, CLUSTER_PORT));
		return new JedisCluster(jedisClusterNodes);
	}

	private JedisCluster getMainKakaoJedisCluster(){
		Set<HostAndPort> jedisClusterNodes = new HashSet<>();
		//Jedis Cluster will attempt to discover cluster nodes automatically
		jedisClusterNodes.add(new HostAndPort(KAKAO_CLUSTER_IP, KAKAO_CLUSTER_PORT));
		return new JedisCluster(jedisClusterNodes);
	}

	private Jedis getLogJedis(){
		Jedis jedis = new Jedis(SINGLE_LOG_IP, SINGLE_LOG_PORT, SINGLE_LOG_TIMEOUT);
		jedis.auth(SINGLE_LOG_PASSWD);
		return jedis;

	}
}
