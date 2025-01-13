package com.xtyu.toolapi.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.xtyu.toolapi.exception.Asserts;
import com.xtyu.toolapi.mapper.ParsingInfoMapper;
import com.xtyu.toolapi.model.dto.VideoInfoDto;
import com.xtyu.toolapi.model.entity.ParsingInfo;
import com.xtyu.toolapi.model.entity.WxUser;
import com.xtyu.toolapi.service.VideoService;
import com.xtyu.toolapi.service.WxUserService;
import com.xtyu.toolapi.utils.RestTemplateUtil;
import com.xtyu.toolapi.utils.UrlUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: 小熊
 * @date: 2021/8/27 14:33
 * @description:phone 17521111022
 */
@Service
@Slf4j
public class VideoServiceImpl implements VideoService {
    @Resource(name = "wxUserService")
    private WxUserService wxUserService;


    @Value("${py_dy.host}")
    String pyDyHist;

    @Value("${host}")
    String host;
    @Resource
    private RestTemplate restTemplate;

    @Resource
    private RestTemplateUtil restTemplateUtil;

    @Resource
    private ParsingInfoMapper parsingInfoMapper;
    private static final Pattern GLOBAL_DOUYIN_PATTERN = Pattern.compile("/share/video/([\\d]*)[/|?]");

    /**
     * 根据数据路里的video Id 获取他的重定向地址
     *
     * @param videoId
     * @return
     */
    @Override
    public String getRedirectByVideoId(String videoId) {
        ParsingInfo parsingInfo = parsingInfoMapper.selectById(videoId);
        return parsingInfo.getRedirectUrl();
    }

    @Override
    public VideoInfoDto getVideoInfo(String openid, String url) {
        WxUser wxUser = wxUserService.getUserInfoByOpenId(openid);
        if (wxUser == null) {
            Asserts.wxInfoFail("未查到用户信息");
        } else if (wxUser.getVideoNumber() < 1) {
            Asserts.wxInfoFail("解析次数已用完");
        }
        isContainsStrings(url);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("User-Agent", "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Mobile Safari/537.36");
        httpHeaders.set("Referer", url);

        //抖音快手Java解析其余短视频平台Java版本暂时没时间写先用php
        if (!url.contains("douyin")) {
            Asserts.urlParsingFail("不支持的链接");
        }
        ParsingInfo parsingInfo = parsingDyVideoInfo(url, httpHeaders);
        wxUser.setVideoNumber(wxUser.getVideoNumber() - 1);
        wxUser.setLastParsingTime(new Date());
        wxUserService.updateById(wxUser);
        parsingInfo.setUserOpenId(openid);
        parsingInfoMapper.insert(parsingInfo);

        return VideoInfoDto.builder().author(parsingInfo.getAuthor()).avatar(parsingInfo.getAvatar())
                .title(parsingInfo.getTitle()).cover(parsingInfo.getCover())
                .id(Long.toString(parsingInfo.getId()))
                .time(parsingInfo.getCreateTime().toString())
                .url(host + "/api/video/video/" + parsingInfo.getId()).build();
    }

    public ParsingInfo parsingDyVideoInfo(String url, HttpHeaders httpHeaders) {
        //获取重定向后的地址
        URI location = restTemplate.headForHeaders(url).getLocation();
        if (location != null) {
            url = location.toString();
        }
        Matcher matcher = GLOBAL_DOUYIN_PATTERN.matcher(url);
        if (!matcher.find()) {
            Asserts.urlParsingFail("解析链接ID:" + url + "异常");
        }
        //获取链接ID
        String id = matcher.group(1);
        String api = pyDyHist + "/api/douyin/web/fetch_one_video?aweme_id=" + id;
        String content = restTemplateUtil.getForObject(api, httpHeaders, String.class);
        JSONObject videoInfo = JSON.parseObject(content).getJSONObject("data").getJSONObject("aweme_detail");
        ParsingInfo videoInfoDto = new ParsingInfo();
        JSONObject videoJson = videoInfo.getJSONObject("video");
        videoInfoDto.setSourceId(id);
        videoInfoDto.setOriginUri(videoJson.getJSONObject("play_addr").getString("uri"));
        videoInfoDto.setCover(videoJson.getJSONObject("origin_cover").getJSONArray("url_list").getString(1));
        JSONArray jsonArray = videoJson.getJSONObject("play_addr").getJSONArray("url_list");
        videoInfoDto.setOriginUrls(jsonArray.toJavaList(String.class));
        log.info("videoUrls:{}", jsonArray);
        videoInfoDto.setTitle(videoInfo.getString("desc"));
        JSONObject author = videoInfo.getJSONObject("author");
        videoInfoDto.setAuthor(author.getString("nickname"));
        if (author.containsKey("avatar_larger")) {
            videoInfoDto.setAvatar(videoInfo.getJSONObject("author").getJSONObject("avatar_larger").getJSONArray("url_list").getString(0));
        } else if (author.containsKey("avatar_thumb")) {
            videoInfoDto.setAvatar(videoInfo.getJSONObject("author").getJSONObject("avatar_thumb").getJSONArray("url_list").getString(0));
        }
        return videoInfoDto;
    }

    public VideoInfoDto parsingKsuVideoInfo(String url, HttpHeaders httpHeaders) {
        Matcher matcher = Pattern.compile("(https?://v.kuaishou.com/[\\S]*)").matcher(url);
        VideoInfoDto videoInfoDto = null;
        if (matcher.find()) {
            url = matcher.group(1);
            url = restTemplate.headForHeaders(url).getLocation().toString();
            String htmlContent = restTemplateUtil.getForObject(url, httpHeaders, String.class);
            Document document = Jsoup.parse(htmlContent);
            Elements elements = document.getElementsByTag("script");
            for (int i = 0; i < elements.size(); i++) {
                if (elements.get(i).childNodeSize() > 0) {
                    Matcher matcherForPageData = Pattern.compile("window.pageData[\\s]*=[\\s]*(.*)[\\s]*").matcher(elements.get(i).childNode(0).toString());
                    if (matcherForPageData.find()) {
                        String pageData = matcherForPageData.group(1);
                        JSONObject pageDataOb = JSONObject.parseObject(pageData);
                        videoInfoDto = VideoInfoDto.builder().build();
                        JSONObject mediaJob = pageDataOb.getJSONObject("video");
                        String photoType = mediaJob.getString("type");
                        if ("video".equals(photoType)) {
                            videoInfoDto.setUrl(mediaJob.getString("srcNoMark"));
                        } else {
                            Asserts.urlParsingFail("暂时只支持视频解析");
                        }
                        videoInfoDto.setTitle(mediaJob.getString("caption"));
                        videoInfoDto.setAuthor(pageDataOb.getJSONObject("user").getString("name"));
                        videoInfoDto.setAvatar(pageDataOb.getJSONObject("user").getString("avatar"));
                        videoInfoDto.setCover(mediaJob.getString("shareCover"));
                        videoInfoDto.setTime(pageDataOb.getJSONObject("rawPhoto").getString("timestamp"));
                    }
                }

            }
        }
        Asserts.urlInfoNotNull(videoInfoDto, "解析异常");
        return videoInfoDto;
    }

    @Override
    public VideoInfoDto phpParsingVideoInfo(String url) {
        url = "https://video.xtyu.top/?url=" + url;
        String htmlContent = restTemplateUtil.getForObject(url, null, String.class);
        Asserts.urlInfoNotNull(htmlContent, "视频解析异常");
        VideoInfoDto videoInfoDto = JSONArray.parseObject(htmlContent).getObject("data", VideoInfoDto.class);
        Asserts.urlInfoNotNull(videoInfoDto, "视频解析异常");
        return videoInfoDto;
    }


    void isContainsStrings(String url) {
        String[] strings = new String[]{"pipix", "douyin", "huoshan", "h5.weishi", "isee.weishi", "weibo.com", "oasis.weibo", "zuiyou",
                "bbq.bilibili", "kuaishou", "quanmin", "moviebase", "hanyuhl", "eyepetizer", "immomo", "vuevideo",
                "xiaokaxiu", "ippzone", "qq.com", "ixigua.com"
        };
        for (String s : strings) {
            if (url.contains(s)) {
                return;
            }
        }
        Asserts.urlParsingFail("链接格式错误");
    }
}
