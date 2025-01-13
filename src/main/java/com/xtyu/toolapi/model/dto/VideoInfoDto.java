package com.xtyu.toolapi.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author: 小熊
 * @date: 2021/8/27 14:22
 * @description:phone 17521111022
 */
@Data
@Builder
public class VideoInfoDto {
    String id ;
    private String author;//视频作者

    private String avatar;//作者头像
    private String time;
    private String title; //视频标题
    private String cover;//视频封面
    private String url;//视频无水印链接
}
