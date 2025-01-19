package com.xtyu.toolapi.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xtyu.toolapi.mapper.ParsingInfoMapper;
import com.xtyu.toolapi.model.entity.ParsingInfo;
import com.xtyu.toolapi.model.support.BaseResponse;
import com.xtyu.toolapi.service.VideoService;
import com.xtyu.toolapi.service.impl.VideoServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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
    public void dyRedirect(@PathVariable String vid, HttpServletResponse response, HttpServletRequest request) throws IOException {
        String redirectByVideoId = videoService.getRedirectByVideoId(vid);
        if (StringUtils.isBlank(redirectByVideoId)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getOutputStream().close();
            return;
        }

//        RestTemplate restTemplate = new RestTemplate();
//
//        String range = request.getHeader("Range");
//        log.info("Range:" + range);
//        if (range != null && range.length() > 7) {
//            log.info("该请求符合断点续传");
//            range = range.substring(6);
//            String[] arr = range.split("-");
//            long lenStart = Long.parseLong(arr[0]);
//            long end=0;
//            if (arr.length > 1) {
//                end = Long.parseLong(arr[1]) ;
//            }
//            long contentLength=end>0?(end-(lenStart-1)):(file.length()-(lenStart>0?lenStart-1:0));//如果指定范围，就返回范围数据长度，如果没有就返回剩余全部长度
//            response.setHeader("Content-Length", String.valueOf(contentLength));
////            response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");//加上会报错，不能用中文
//            response.setHeader("Content-Range", "bytes " + lenStart + "-" + (end>0?end:(file.length() - 1)) + "/" + file.length());
//            response.setContentType("video/mp4");
//            response.setHeader("Accept-Ranges", "bytes");
//            response.setStatus(HttpStatus.PARTIAL_CONTENT.value());//响应码206表示响应内容为部分数据，需要多次请求
//            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
//            randomAccessFile.seek(lenStart);//设置读取的开始字节数
//            if(end>0){//客户端指定了范围的数据，那就只给范围数据
//                int size= (int) (end-lenStart+1);
//                byte[] buffer = new byte[size];
//                int len = randomAccessFile.read(buffer);
//                if (len != -1) {
//                    response.getOutputStream().write(buffer, 0, len);
//                }
//            }else{//没有指定范围
//                //视频每次返回一兆数据
//                int size = 1048576;//1MB
//                byte[] buffer = new byte[size];
//                int len ;
//                while ((len = randomAccessFile.read(buffer)) != -1) {
//                    response.getOutputStream().write(buffer, 0, len);
//                }
//            }
//            randomAccessFile.close();
//        }else{
//            log.info("该请求不符合断点续传");
//            response.setHeader("Content-Disposition", "attachment; filename=\"" +System.currentTimeMillis()+".mp4" + "\"");//不能用中文
//            response.setHeader("Content-Length", String.valueOf(file.length()-1));
//            response.setHeader("Content-Range", "" + (file.length()-1));
//            response.setHeader("Accept-Ranges", "bytes");
//            InputStream inStream=new FileInputStream(file);
//            byte[] buffer = new byte[1024];
//            int len;
//            while ((len = inStream.read(buffer)) != -1) {
//                response.getOutputStream().write(buffer, 0, len);
//            }
//            inStream.close();
//        }
//        response.getOutputStream().flush();
//        response.getOutputStream().close();

//        // 设置请求头
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Referer", redirectByVideoId);
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//        // 发起请求
//        return restTemplate.exchange(
//                redirectByVideoId,
//                HttpMethod.GET,
//                entity,
//                byte[].class);

        try (OutputStream outputStream = response.getOutputStream()) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Referer", redirectByVideoId);
            headers.set("range", request.getHeader("range"));
            headers.set("Sec-Fetch-Dest", request.getHeader("Sec-Fetch-Dest"));
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> responseEntity = restTemplate.exchange(redirectByVideoId, HttpMethod.GET, entity, byte[].class);

            log.info("resp headers:{}", JSONUtil.toJsonStr(responseEntity.getHeaders()));
            byte[] videoBytes = responseEntity.getBody();
            response.setContentType("video/mp4");
            HttpHeaders headers1 = responseEntity.getHeaders();
            headers1.forEach((key, value) -> {
                response.setHeader(key, value.get(0));
            });
            if (response.isCommitted() && !response.getOutputStream().isReady()) {
                // 客户端已断开连接，停止传输
                return;
            }
            outputStream.write(videoBytes);
            outputStream.flush();
        } catch (IOException e) {
            log.error("读取视频时发生错误: {}", e.getMessage(), e);
            // 可以根据需要返回特定的HTTP状态码或消息给客户端
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
//            response.getWriter().write("视频传输失败，请稍后再试。");
        } catch (Exception e) {
            log.error("读取视频时发生未知错误: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            response.getWriter().write("服务器内部错误，请联系管理员。");
        }

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
