package com.wxy.aicustomer.tool;

import com.wxy.aicustomer.client.ApartmentClient;
import com.wxy.aicustomer.client.dto.ViewingRequestCommand;
import com.wxy.aicustomer.client.dto.ViewingResult;
import com.wxy.zzarental.common.exception.ZZAException;
import com.wxy.zzarental.common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;

/**
 * 看房预约类 Function Tool（写操作）。
 *
 * <p>写操作必须幂等：requestId 由“房源 + 访客 + 期望时间”派生，公寓系统据此去重；
 * 本地再用 Redis 缓存一次结果，避免模型重复调用产生重复预约。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ViewingTools {

    private static final String IDEMPOTENT_KEY_PREFIX = "ai:tool:viewing:";
    private static final Duration IDEMPOTENT_TTL = Duration.ofHours(2);
    private static final String PHONE_PATTERN = "^1[3-9]\\d{9}$";
    private static final int MAX_TEXT_LENGTH = 200;

    private final ApartmentClient apartmentClient;
    private final ObjectProvider<RedisUtil> redisUtilProvider;

    @Tool(name = "createViewingRequest",
            description = "帮访客提交看房预约意向。必须先与访客确认房源、联系人和看房时间，"
                    + "再用本工具提交；同一访客、同一房源、同一时间重复提交不会产生重复预约。")
    public String createViewingRequest(
            @ToolParam(description = "房源 ID，来自空房查询结果") String roomId,
            @ToolParam(description = "访客标识，使用当前会话的 visitorId") String visitorId,
            @ToolParam(description = "联系人称呼") String contactName,
            @ToolParam(description = "联系人手机号，11 位中国大陆手机号") String contactPhone,
            @ToolParam(description = "期望看房时间，格式 yyyy-MM-ddTHH:mm，例如 2026-09-20T15:00") String expectedTime,
            @ToolParam(required = false, description = "备注，可留空") String remark) {
        String invalid = validate(roomId, visitorId, contactName, contactPhone, expectedTime, remark);
        if (invalid != null) {
            return invalid;
        }
        String normalizedRoomId = roomId.trim();
        String normalizedVisitorId = visitorId.trim();
        LocalDateTime viewingTime = LocalDateTime.parse(expectedTime.trim());
        if (viewingTime.isBefore(LocalDateTime.now())) {
            return "看房时间不能早于当前时间，请与访客重新确认。";
        }
        String requestId = buildRequestId(normalizedRoomId, normalizedVisitorId, viewingTime);

        String cached = readCache(requestId);
        if (cached != null) {
            return "该预约此前已提交过，无需重复提交。" + cached;
        }
        try {
            ViewingResult result = apartmentClient.createViewingRequest(new ViewingRequestCommand(
                    requestId,
                    normalizedRoomId,
                    normalizedVisitorId,
                    contactName.trim(),
                    contactPhone.trim(),
                    viewingTime,
                    StringUtils.hasText(remark) ? remark.trim() : null));
            String summary = result == null
                    ? "预约已提交。"
                    : "预约已提交，预约单号：%s，状态：%s。".formatted(
                    result.appointmentNo() == null ? "待公寓系统返回" : result.appointmentNo(),
                    result.status() == null ? "待确认" : result.status());
            writeCache(requestId, summary);
            return summary;
        } catch (ZZAException ex) {
            log.warn("提交看房预约失败：{}", ex.getMessage());
            return "暂时无法提交看房预约：" + ex.getMessage();
        } catch (Exception ex) {
            log.error("提交看房预约异常", ex);
            return "暂时无法提交看房预约，请稍后再试。";
        }
    }

    private String validate(String roomId, String visitorId, String contactName, String contactPhone,
                            String expectedTime, String remark) {
        if (!StringUtils.hasText(roomId) || roomId.length() > 64) {
            return "房源 ID 不合法，请先查询房源。";
        }
        if (!StringUtils.hasText(visitorId) || visitorId.length() > 64) {
            return "访客标识不合法，无法提交预约。";
        }
        if (!StringUtils.hasText(contactName) || contactName.length() > 50) {
            return "请提供 1~50 字的联系人称呼。";
        }
        if (!StringUtils.hasText(contactPhone) || !contactPhone.trim().matches(PHONE_PATTERN)) {
            return "请提供 11 位中国大陆手机号，用于公寓管家联系确认。";
        }
        if (!StringUtils.hasText(expectedTime)) {
            return "请提供期望看房时间，格式 yyyy-MM-ddTHH:mm。";
        }
        try {
            LocalDateTime.parse(expectedTime.trim());
        } catch (DateTimeParseException ex) {
            return "看房时间格式不正确，请使用 yyyy-MM-ddTHH:mm。";
        }
        if (remark != null && remark.length() > MAX_TEXT_LENGTH) {
            return "备注过长，请控制在 " + MAX_TEXT_LENGTH + " 字以内。";
        }
        return null;
    }

    private String buildRequestId(String roomId, String visitorId, LocalDateTime viewingTime) {
        String raw = roomId + "|" + visitorId + "|" + viewingTime;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 32);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("幂等键生成失败", e);
        }
    }

    private String readCache(String requestId) {
        RedisUtil redisUtil = redisUtilProvider.getIfAvailable();
        if (redisUtil == null) {
            return null;
        }
        try {
            return redisUtil.get(IDEMPOTENT_KEY_PREFIX + requestId);
        } catch (Exception ex) {
            log.warn("读取幂等缓存失败：{}", ex.getMessage());
            return null;
        }
    }

    private void writeCache(String requestId, String summary) {
        RedisUtil redisUtil = redisUtilProvider.getIfAvailable();
        if (redisUtil == null) {
            return;
        }
        try {
            redisUtil.set(IDEMPOTENT_KEY_PREFIX + requestId, summary, IDEMPOTENT_TTL.toMillis(),
                    TimeUnit.MILLISECONDS);
        } catch (Exception ex) {
            log.warn("写入幂等缓存失败：{}", ex.getMessage());
        }
    }
}
