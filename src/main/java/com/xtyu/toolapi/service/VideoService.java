package com.xtyu.toolapi.service;

import com.xtyu.toolapi.model.dto.VideoInfoDto;
import com.xtyu.toolapi.model.entity.ParsingInfo;
import org.springframework.http.HttpHeaders;

import java.io.IOException;


public interface VideoService {

    /**
     * 根据数据路里的video Id 获取他的重定向地址
     *
     * @param videoId
     * @return
     */
    String getRedirectByVideoId(String videoId);

    /**
     * 解析视频并保存解析信息
     *
     * @param openid
     * @param url
     * @return
     */
    VideoInfoDto getVideoInfo(String openid, String url);


    /**
     * PHP服务解析（暂用）
     *
     * @param url 分享链接
     * @return
     */
    VideoInfoDto phpParsingVideoInfo(String url);
}
