package com.xtyu.toolapi.utils;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @author dylan
 */
@Slf4j
public class GDateUtil {


    public static Long toSeconds(LocalDateTime localDateTime) {
        if (ObjectUtil.isEmpty(localDateTime)) {
            return 0L;
        }

        // 转换为系统默认时区
        ZonedDateTime beijingTime = localDateTime.atZone(ZoneId.systemDefault());

        return beijingTime.toEpochSecond();
    }

    public static Long toTimeMillis(LocalDateTime localDateTime) {
        if (ObjectUtil.isEmpty(localDateTime)) {
            return 0L;
        }
        return Timestamp.valueOf(localDateTime).getTime();
    }

    public static LocalDateTime fromSeconds(Long milliseconds) {
        if (ObjectUtil.isEmpty(milliseconds)) {
            return null;
        }
        // 转换为Instant
        Instant instant = Instant.ofEpochSecond(milliseconds);
        // 获取系统默认时区
        ZoneId zoneId = ZoneId.systemDefault();

        // 转换为ZonedDateTime
        ZonedDateTime zonedDateTime = instant.atZone(zoneId);

        // 从ZonedDateTime中提取LocalDateTime
        return zonedDateTime.toLocalDateTime();

    }

    public static void main(String[] args) {
        LocalDateTime localDateTime = GDateUtil.fromSeconds(1723349448L);
        System.out.println(localDateTime);
    }
}
