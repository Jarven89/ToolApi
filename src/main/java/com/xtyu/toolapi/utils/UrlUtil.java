package com.xtyu.toolapi.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.asynchttpclient.Dsl.asyncHttpClient;

/**
 * @author dylan
 */
@Slf4j
public class UrlUtil {

    private UrlUtil() {
        throw new IllegalStateException("Utility class");
    }


    public static ResponseEntity<byte[]> getVideosData(String videoId, List<String> urls) {
        ResponseEntity<byte[]> videoData = null;
        for (int i = 0; i < urls.size(); i++) {
            videoData = getVideoData(videoId, urls.get(i));
            if (videoData.getStatusCodeValue() == HttpStatus.OK.value()) {
                break;
            }
        }
        return videoData;
    }

    public static ResponseEntity<byte[]> getVideoData(String videoId, String url) {
        try {
            // 发起GET请求获取视频数据
            AsyncHttpClient asyncHttpClient = asyncHttpClient();
            BoundRequestBuilder boundRequestBuilder = asyncHttpClient.prepareGet(url);
            boundRequestBuilder.setHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36");
            boundRequestBuilder.setHeader("cookie", "s_v_web_id=verify_m09hj3jp_m8UbgrR3_YbqR_4IM3_AfCw_SurpvxgY3Q1j; passport_csrf_token=a4047c47f225a17c83233e350a2f7e05; passport_csrf_token_default=a4047c47f225a17c83233e350a2f7e05; ttwid=1%7CyzS0YLJ3XUMiSDtsmVlRVhhwMjiCeuFYtPmshoVAo78%7C1724585278%7Cee849d6e5a1d68a88de2eaad27b3e4eb0fdf36f879a0d96c44098905997d1570; UIFID_TEMP=04334f064e21198b2492613256b037a8641b36104347f0fcdf493d9e3675e398f55cb1aa8455e69b3050fe332dc7fce9a6b3fec997b076948cd186fed6a2d4cced3c7da9d53c81170b69984218c17dd2; hevc_supported=true; odin_tt=c5c69d4955dc70118ce8ff6d63e2a6971d81dce3a91a66589f554de30321033f4af3b33f9a9e53d6bec799e9a857ba964b052b68117fdce1bbd8bb60bb1baf9848599e9e5ea73d984ed2020ed3ad7a19; fpk1=U2FsdGVkX1+2WmYSoWtBrLoXFME/qWlDgaiVm68QHYhKhcxT7jHqKrzo6frOTBSG5nuqjBPxGXwzXHDkcbimQA==; fpk2=6a23775729fd6c068d20d383cbe27f9b; bd_ticket_guard_client_web_domain=2; UIFID=04334f064e21198b2492613256b037a8641b36104347f0fcdf493d9e3675e3981e0c5b3f9260804ef913de62821e8b0fe3a9b13529cbec97219f210b46a55fb3c5d5ac47bd63362dfe1eadc6d892b52f9a69b178b99d887b96ce8d002aa779a0be8a8a80e463af125f21b4f59d1840be01a9b2f8707b25da7334d6b8806b8887799240cca6c24e8690d6b0b270c755f8764f4b3c1d8be3626890c8faf0e0e057; xgplayer_user_id=48627996060; volume_info=%7B%22isUserMute%22%3Afalse%2C%22isMute%22%3Atrue%2C%22volume%22%3A0.5%7D; __ac_nonce=066dbfc3300e9c53f9eac; __ac_signature=_02B4Z6wo00f01qEIYdwAAIDCRgg2yABXVAqhKGVAAM6m1b; douyin.com; xg_device_score=7.292574705267544; device_web_cpu_core=6; device_web_memory_size=8; IsDouyinActive=true; home_can_add_dy_2_desktop=%220%22; dy_swidth=1680; dy_sheight=1050; stream_recommend_feed_params=%22%7B%5C%22cookie_enabled%5C%22%3Atrue%2C%5C%22screen_width%5C%22%3A1680%2C%5C%22screen_height%5C%22%3A1050%2C%5C%22browser_online%5C%22%3Atrue%2C%5C%22cpu_core_num%5C%22%3A6%2C%5C%22device_memory%5C%22%3A8%2C%5C%22downlink%5C%22%3A10%2C%5C%22effective_type%5C%22%3A%5C%224g%5C%22%2C%5C%22round_trip_time%5C%22%3A100%7D%22; csrf_session_id=e1d0b45bbfeb2b4003c01d37002989a2; strategyABtestKey=%221725692980.857%22; bd_ticket_guard_client_data=eyJiZC10aWNrZXQtZ3VhcmQtdmVyc2lvbiI6MiwiYmQtdGlja2V0LWd1YXJkLWl0ZXJhdGlvbi12ZXJzaW9uIjoxLCJiZC10aWNrZXQtZ3VhcmQtcmVlLXB1YmxpYy1rZXkiOiJCSTRkbmpDRmg3Qi9LV2ZPbi83QUlhMXQ5OGpqdENmRFJlbkhqTDFQdFRxeThIaldCRnUxVXVkZXZTb2R1Tm5DWmF6N3NIU0gzalVSM0JVN1lNQk1TMnc9IiwiYmQtdGlja2V0LWd1YXJkLXdlYi12ZXJzaW9uIjoxfQ%3D%3D; biz_trace_id=8c34011e; FORCE_LOGIN=%7B%22videoConsumedRemainSeconds%22%3A180%7D; stream_player_status_params=%22%7B%5C%22is_auto_play%5C%22%3A0%2C%5C%22is_full_screen%5C%22%3A0%2C%5C%22is_full_webscreen%5C%22%3A1%2C%5C%22is_mute%5C%22%3A1%2C%5C%22is_speed%5C%22%3A1%2C%5C%22is_visible%5C%22%3A1%7D%22");
            boundRequestBuilder.setHeader("Referer", url);
            Response response = boundRequestBuilder.execute().get(60000, TimeUnit.MILLISECONDS);
            // 检查响应状态码是否为200
            if (response.getStatusCode() == HttpStatus.OK.value()) {
                byte[] responseBodyAsBytes = response.getResponseBodyAsBytes();
                // 设置响应头信息
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(response.getContentType().toString()));
                headers.setContentLength(responseBodyAsBytes.length);
                // 返回视频数据
                return new ResponseEntity<>(responseBodyAsBytes, headers, HttpStatus.OK);
            } else if (response.getStatusCode() == HttpStatus.FOUND.value()) {
                // 解析HTML字符串
                Document doc = Jsoup.parse(response.getResponseBody());
                //取 href
                Elements links = doc.select("a[href]");
                if (!links.isEmpty()) {
                    return getVideoData(videoId, links.get(0).attr("href"));
                }
                //取头信息
                String locationUrl = response.getHeaders().get("location");
                if (StringUtils.isNotEmpty(locationUrl)) {
                    log.info("触发重定向：{}", locationUrl);
                    return getVideoData(videoId, locationUrl);
                }
                log.error("Request to :{} video id:{} service failed with status code: {}\t body:{}", url, videoId, response.getStatusCode(), response.getResponseBody());
                return new ResponseEntity<>(HttpStatus.valueOf(response.getStatusCode()));
            } else {
                // 如果视频服务返回非200状态码，则转发该状态码
                log.error("Request to video service failed with status code: {}\t body:{}", response.getStatusCode(), response.getResponseBody());
                return new ResponseEntity<>(HttpStatus.valueOf(response.getStatusCode()));
            }
        } catch (Exception e) {
            log.error("Error occurred while processing request: " + e.getMessage(), e);
            // 处理异常情况
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    public static String pickUrl(List<String> urls) {
        if (CollectionUtils.isEmpty(urls)) {
            return null;
        }
        for (int i = 0; i < urls.size(); i++) {
            if (validUrl(urls.get(i))) {
                return urls.get(i);
            }
        }
        return null;
    }

    public static boolean validUrl(String sUrl) {
        if (StringUtils.isBlank(sUrl)) {
            log.error("{} is not a valid url", sUrl);
            return false;
        }
        URL url = null;
        try {
            url = new URL(sUrl);
        } catch (MalformedURLException e) {
            log.error("URL:{}\t错误:{}", sUrl, e.getMessage(), e);
            return false;
        }
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            // 设置请求方法为GET
            connection.setRequestMethod("GET");
            connection.setRequestProperty("sec-ch-ua-platform","macOS");
            connection.setRequestProperty("Referer", sUrl);
//            connection.setRequestProperty("cookie","s_v_web_id=verify_m09hj3jp_m8UbgrR3_YbqR_4IM3_AfCw_SurpvxgY3Q1j; passport_csrf_token=a4047c47f225a17c83233e350a2f7e05; passport_csrf_token_default=a4047c47f225a17c83233e350a2f7e05; ttwid=1%7CyzS0YLJ3XUMiSDtsmVlRVhhwMjiCeuFYtPmshoVAo78%7C1724585278%7Cee849d6e5a1d68a88de2eaad27b3e4eb0fdf36f879a0d96c44098905997d1570; UIFID_TEMP=04334f064e21198b2492613256b037a8641b36104347f0fcdf493d9e3675e398f55cb1aa8455e69b3050fe332dc7fce9a6b3fec997b076948cd186fed6a2d4cced3c7da9d53c81170b69984218c17dd2; hevc_supported=true; FORCE_LOGIN=%7B%22videoConsumedRemainSeconds%22%3A180%7D; odin_tt=c5c69d4955dc70118ce8ff6d63e2a6971d81dce3a91a66589f554de30321033f4af3b33f9a9e53d6bec799e9a857ba964b052b68117fdce1bbd8bb60bb1baf9848599e9e5ea73d984ed2020ed3ad7a19; fpk1=U2FsdGVkX1+2WmYSoWtBrLoXFME/qWlDgaiVm68QHYhKhcxT7jHqKrzo6frOTBSG5nuqjBPxGXwzXHDkcbimQA==; fpk2=6a23775729fd6c068d20d383cbe27f9b; bd_ticket_guard_client_web_domain=2; UIFID=04334f064e21198b2492613256b037a8641b36104347f0fcdf493d9e3675e3981e0c5b3f9260804ef913de62821e8b0fe3a9b13529cbec97219f210b46a55fb3c5d5ac47bd63362dfe1eadc6d892b52f9a69b178b99d887b96ce8d002aa779a0be8a8a80e463af125f21b4f59d1840be01a9b2f8707b25da7334d6b8806b8887799240cca6c24e8690d6b0b270c755f8764f4b3c1d8be3626890c8faf0e0e057; xgplayer_user_id=48627996060; stream_player_status_params=%22%7B%5C%22is_auto_play%5C%22%3A0%2C%5C%22is_full_screen%5C%22%3A0%2C%5C%22is_full_webscreen%5C%22%3A1%2C%5C%22is_mute%5C%22%3A0%2C%5C%22is_speed%5C%22%3A1%2C%5C%22is_visible%5C%22%3A0%7D%22; volume_info=%7B%22isUserMute%22%3Afalse%2C%22isMute%22%3Atrue%2C%22volume%22%3A0.5%7D; download_guide=%223%2F20240826%2F0%22; pwa2=%220%7C0%7C2%7C0%22; IsDouyinActive=false; __ac_signature=_02B4Z6wo00f01ndtWKwAAIDCkG0Pu1ooc453TVwAAPsj9c; douyin.com; xg_device_score=7.292574705267544; device_web_cpu_core=6; device_web_memory_size=8; home_can_add_dy_2_desktop=%220%22; dy_swidth=1680; dy_sheight=1050; stream_recommend_feed_params=%22%7B%5C%22cookie_enabled%5C%22%3Atrue%2C%5C%22screen_width%5C%22%3A1680%2C%5C%22screen_height%5C%22%3A1050%2C%5C%22browser_online%5C%22%3Atrue%2C%5C%22cpu_core_num%5C%22%3A6%2C%5C%22device_memory%5C%22%3A8%2C%5C%22downlink%5C%22%3A10%2C%5C%22effective_type%5C%22%3A%5C%224g%5C%22%2C%5C%22round_trip_time%5C%22%3A50%7D%22; strategyABtestKey=%221724849882.935%22; csrf_session_id=106002b0ce45bc578f93006e70014f70; bd_ticket_guard_client_data=eyJiZC10aWNrZXQtZ3VhcmQtdmVyc2lvbiI6MiwiYmQtdGlja2V0LWd1YXJkLWl0ZXJhdGlvbi12ZXJzaW9uIjoxLCJiZC10aWNrZXQtZ3VhcmQtcmVlLXB1YmxpYy1rZXkiOiJCSTRkbmpDRmg3Qi9LV2ZPbi83QUlhMXQ5OGpqdENmRFJlbkhqTDFQdFRxeThIaldCRnUxVXVkZXZTb2R1Tm5DWmF6N3NIU0gzalVSM0JVN1lNQk1TMnc9IiwiYmQtdGlja2V0LWd1YXJkLXdlYi12ZXJzaW9uIjoxfQ%3D%3D");
//            connection.setRequestProperty("user-agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36");
            // 设置连接超时时间 5秒
            connection.setConnectTimeout(5000);

            // 尝试获取响应码
            int responseCode = connection.getResponseCode();

            // 200表示请求成功
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 尝试读取一部分数据来确认URL确实指向视频内容
                // 注意：这里我们只读取一小部分数据来避免下载整个视频
                inputStream = new BufferedInputStream(connection.getInputStream());
                byte[] buffer = new byte[1024];
                int bytesRead = inputStream.read(buffer);
                if (bytesRead < 0) {
                    // 如果读取到数据，则认为视频URL有效
                    log.warn("请求:{} Http数据读取不到,数据长度:{}", sUrl, bytesRead);
                }

                return true;
            }
            log.info("请求:{} \r\nHttp错误:{}", sUrl, responseCode);
        } catch (IOException e) {
            // 处理异常，如URL无效、网络问题等
            log.warn("读取：{} 失败:{}", sUrl, e.getMessage(), e);
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("关闭流异常:{}", e.getMessage(), e);
                }
            }
            if (null != connection) {
                connection.disconnect();
            }
        }
        // 如果执行到这里，说明视频URL可能无效或无法访问
        return false;
    }

    public static void main(String[] args) {
        boolean b = UrlUtil.validUrl("https://v3-web.douyinvod.com/807aaab5b1056f761cfb324f4e61e270/6783bbff/video/tos/cn/tos-cn-ve-15/oULYEr7IpigEAir3d4M9PEviQnBSB7ZQCBmIA/?a=6383&ch=26&cr=3&dr=0&lr=all&cd=0%7C0%7C0%7C3&cv=1&br=830&bt=830&cs=0&ds=4&ft=4TMWc6DnppftDFLB.s~.C_bAja-CInbmWxdc6BI6gR2NVYpHDDkuhSC~tZ_0tusZ.&mime_type=video_mp4&qs=0&rc=O2g4M2U7ZmU5aTs4aDM0PEBpamVxdHU5cmZteDMzNGkzM0AvLWNfNS4xXmMxNl81XjQ0YSMtamNpMmRrYi1gLS1kLS9zcw%3D%3D&btag=80000e00010000&cquery=100B_100x_100z_100o_100w&dy_q=1736675766&feature_id=46a7bb47b4fd1280f3d3825bf2b29388&l=202501121756050CBF8EEBC3143EF7D640");
//        boolean b1 = UrlUtil.validUrl("https://v3-web.douyinvod.com/807aaab5b1056f761cfb324f4e61e270/6783bbff/video/tos/cn/tos-cn-ve-15/oULYEr7IpigEAir3d4M9PEviQnBSB7ZQCBmIA/?a=6383&ch=26&cr=3&dr=0&lr=all&cd=0|0|0|3&cv=1&br=830&bt=830&cs=0&ds=4&ft=4TMWc6DnppftDFLB.s~.C_bAja-CInbmWxdc6BI6gR2NVYpHDDkuhSC~tZ_0tusZ.&mime_type=video_mp4&qs=0&rc=O2g4M2U7ZmU5aTs4aDM0PEBpamVxdHU5cmZteDMzNGkzM0AvLWNfNS4xXmMxNl81XjQ0YSMtamNpMmRrYi1gLS1kLS9zcw==&btag=80000e00010000&cquery=100B_100x_100z_100o_100w&dy_q=1736675766&feature_id=46a7bb47b4fd1280f3d3825bf2b29388&l=202501121756050CBF8EEBC3143EF7D640");
        System.out.println(b);
//        System.out.println(b1);


    }
}
