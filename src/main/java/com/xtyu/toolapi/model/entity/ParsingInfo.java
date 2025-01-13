package com.xtyu.toolapi.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xtyu.toolapi.config.ArrayListHandler;
//import com.xtyu.toolapi.config.LongDataTimeHandler;
import com.xtyu.toolapi.utils.UrlUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author: 小熊
 * @date: 2021/6/15 15:33
 * @description:phone 17521111022
 */
@Data
@TableName(value = "parsing_info", autoResultMap = true)
@Slf4j
public class ParsingInfo {
    @TableId("id")
    private Long id;
    /**
     * 源地址的 url 列表
     */
    @TableField(value = "origin_urls", typeHandler = ArrayListHandler.class, jdbcType = JdbcType.LONGVARBINARY)
    private List<String> originUrls;

    private String originUri;
    private String sourceId;
    private String userOpenId;
    private String downloadUrl;
    private String title;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    private String author;//视频作者
    private String cover;//视频封面地址
    /**
     * 作者头像
     */
    private String avatar;

    /**
     * 获取能够播放的视频地址
     *
     * @return
     */
    public String getRedirectUrl() {
        String returnUrl = "";
        List<String> originUrls = getOriginUrls();
        //取播放地址，如果没有或者不能播放则调用抖音的接口
        returnUrl = UrlUtil.pickUrl(originUrls);
        //取备用地址
        if (StringUtils.isNotBlank(returnUrl)) {
            log.info("视频id:{}\t返回播放地址:{}", getId(), returnUrl);
            return returnUrl;
        }
        //取默认播放接口
        if (StringUtils.isNotBlank(getSourceId())) {
            returnUrl = "https://www.douyin.com/aweme/v1/play/?video_id=" + getSourceId();
            if (UrlUtil.validUrl(returnUrl)) {
                return returnUrl;
            }
            log.warn("请求OriginUri:{}无效，尝试取备用地址", returnUrl);
        }

        return returnUrl;
    }


}
