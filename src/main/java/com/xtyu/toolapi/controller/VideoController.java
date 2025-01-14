package com.xtyu.toolapi.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xtyu.toolapi.mapper.ParsingInfoMapper;
import com.xtyu.toolapi.model.entity.ParsingInfo;
import com.xtyu.toolapi.model.support.BaseResponse;
import com.xtyu.toolapi.service.VideoService;
import com.xtyu.toolapi.service.impl.VideoServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;

/**
 * @author: 小熊
 * @date: 2021/6/9
 * @description:phone 17521111022
 */
@RestController
@Slf4j
@RequestMapping("/api/video")
public class VideoController {
    @Resource
    ParsingInfoMapper parsingInfoMapper;
    @Resource
    VideoService videoService;
    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/video/{vid}")
    public ResponseEntity<byte[]> dyRedirect(@PathVariable String vid, HttpServletResponse response) throws IOException {
        String redirectByVideoId = videoService.getRedirectByVideoId(vid);
        try {
            // 发送请求获取视频流
            ResponseEntity<byte[]> responseEntity = restTemplate.getForEntity(redirectByVideoId, byte[].class);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.add("Referer", redirectByVideoId);
            return new ResponseEntity<>(responseEntity.getBody(), headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

//        try (OutputStream outputStream = response.getOutputStream()) {
//            HttpHeaders headers = new HttpHeaders();
//            headers.set("Referer", redirectByVideoId);
//            HttpEntity<String> entity = new HttpEntity<>(headers);
//            ResponseEntity<byte[]> responseEntity = restTemplate.exchange(redirectByVideoId, HttpMethod.GET, entity, byte[].class);
//            byte[] videoBytes = responseEntity.getBody();
//            response.setContentType("video/mp4");
//            outputStream.write(videoBytes);
//            outputStream.flush();
//        } catch (Exception e) {
//            log.error("read video error:{}", e.getMessage(), e);
//        }




    }

    /***
     * 视频无水印链接解析
     * @param url 分享地址
     * @param openId 用户openId
     * @return
     */
    @PostMapping(value = "getVideoInfo")
    public BaseResponse getVideoInfo(@RequestParam(value = "url") String url, @RequestParam(value = "openId") String openId) {
        return BaseResponse.ok(videoService.getVideoInfo(openId, url));
    }

    /***
     * 获取解析记录
     * @param openId
     * @return
     */
    @PostMapping(value = "getParsingInfo")
    public BaseResponse<List> getVideoInfo(@RequestParam(value = "openId") String openId) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - 30);
        QueryWrapper<ParsingInfo> queryWrapper = new QueryWrapper();
        queryWrapper.lambda().eq(ParsingInfo::getUserOpenId, openId).gt(ParsingInfo::getCreateTime, calendar.getTime());
        List<ParsingInfo> parsingInfoList = parsingInfoMapper.selectList(queryWrapper);
        return BaseResponse.ok(parsingInfoList);
    }
}
