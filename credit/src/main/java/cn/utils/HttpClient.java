package cn.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class HttpClient {

	private static final String CHARSET = "UTF-8";
	private static final String APPLICATION_JSON = "application/json";
	private static final String CONTENT_TYPE_TEXT_JSON = "application/json;charset=UTF-8";

	private static Log logger = LogFactory.getLog(HttpClient.class);

    public static  Map<String, Object> jsonPost(String url, Map<String, Object> params) {
        return jsonPost(url, params,null);
    }

    public static  Map<String, Object> jsonPost(String url, Map<String, Object> params,Map<String,String> heads) {
		com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject(params);
		String reqStr = json.toJSONString();
		logger.info("httpClient req jsonStr="+reqStr);
        String respStr = post(url, reqStr,heads);
		logger.info("httpclient  result String="+respStr);
        com.alibaba.fastjson.JSONObject resp = com.alibaba.fastjson.JSON.parseObject(respStr);
        return resp;
    }

	/**
	 * http post请求
	 *
	 * @param url
	 *            请求地址
	 * @param params
	 *            请求参数
	 * @return
	 */
	public static String post(String url, Map<String, Object> params) {
		try {
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(url);
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			for (Iterator<String> iterator = params.keySet().iterator(); iterator.hasNext();) {
				String key = iterator.next();
				if (params.get(key) != null) {
					parameters.add(new BasicNameValuePair(key, params.get(key).toString()));
				}
			}
			UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(parameters, CHARSET);
			uefEntity.setContentType(CONTENT_TYPE_TEXT_JSON);
			httpPost.setEntity(uefEntity);
			CloseableHttpResponse response = httpClient.execute(httpPost);
			try {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					return EntityUtils.toString(entity, CHARSET);
				}
			} finally {
				response.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	public static String post(String url, String params){
		return post(url,params,null);
	}

	/**
	 * http post请求
	 *
	 * @param url
	 *            请求地址
	 * @param params
	 *            请求参数
	 * @return
	 */
	public static String post(String url, String params,Map<String,String> header) {
		try {
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(url);
			httpPost.addHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON);
			if(header!=null && !header.isEmpty()){
				for (Map.Entry<String, String> entry : header.entrySet()) {
					httpPost.addHeader(entry.getKey(), entry.getValue());
				}
			}
			StringEntity sEntity = new StringEntity(params, CHARSET);
			sEntity.setContentType(CONTENT_TYPE_TEXT_JSON);
			sEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON));
			httpPost.setEntity(sEntity);
			CloseableHttpResponse response = httpClient.execute(httpPost);
			try {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					return EntityUtils.toString(entity, CHARSET);
				}
			} finally {
				response.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return null;
	}
	
	public static String postJSON(String URL, String JSONBody) {
		try {
			CloseableHttpClient httpClient = HttpClients.createDefault();
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(10000).setConnectTimeout(10000).build();//设置请求和传输超时时间
			HttpPost httpPost = new HttpPost(URL);
			httpPost.setConfig(requestConfig);
			httpPost.addHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON);
			StringEntity sEntity = new StringEntity(JSONBody, CHARSET);
			httpPost.setEntity(sEntity);
			CloseableHttpResponse response = httpClient.execute(httpPost);
			try {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					return EntityUtils.toString(entity, CHARSET);
				}
			} finally {
				response.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	public static String get(String fullUrl) {
		try {
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpGet httpGet = new HttpGet(fullUrl);
			httpGet.addHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON);
			CloseableHttpResponse response = httpClient.execute(httpGet);
			try {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					return EntityUtils.toString(entity, CHARSET);
				}
			} finally {
				response.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	public static String getUrl(String url, String uri) {
		StringBuffer sb = new StringBuffer();
		sb.append(url);
		if (url.endsWith("/") && uri.startsWith("/")) {
			uri = uri.substring(1);
		} else if (!url.endsWith("/") && !uri.startsWith("/")) {
			sb.append("/");
		}
		sb.append(uri);
		return sb.toString();
	}

}
