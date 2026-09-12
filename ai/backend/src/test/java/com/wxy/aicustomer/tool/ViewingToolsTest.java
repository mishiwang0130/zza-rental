package com.wxy.aicustomer.tool;

import com.wxy.aicustomer.client.ApartmentClient;
import com.wxy.aicustomer.client.dto.ViewingRequestCommand;
import com.wxy.aicustomer.client.dto.ViewingResult;
import com.wxy.zzarental.common.exception.ZZAException;
import com.wxy.zzarental.common.result.ResultCodeEnum;
import com.wxy.zzarental.common.util.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 看房预约 Tool 测试：入参校验、幂等提交、下游异常兜底。
 */
class ViewingToolsTest {

    private final ApartmentClient apartmentClient = mock(ApartmentClient.class);
    private final Map<String, String> cache = new HashMap<>();
    private ViewingTools viewingTools;

    @BeforeEach
    void setUp() {
        cache.clear();
        RedisUtil redisUtil = mock(RedisUtil.class);
        when(redisUtil.get(anyString())).thenAnswer(invocation -> cache.get(invocation.getArgument(0)));
        doAnswer(invocation -> {
            cache.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(redisUtil).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

        @SuppressWarnings("unchecked")
        ObjectProvider<RedisUtil> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(redisUtil);
        viewingTools = new ViewingTools(apartmentClient, provider);
    }

    @Test
    void shouldRejectInvalidPhone() {
        String result = viewingTools.createViewingRequest("R1", "visitor-1", "张三", "123",
                futureTime(), null);

        assertThat(result).contains("11 位中国大陆手机号");
    }

    @Test
    void shouldRejectPastViewingTime() {
        String result = viewingTools.createViewingRequest("R1", "visitor-1", "张三", "13800138000",
                LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")), null);

        assertThat(result).contains("不能早于当前时间");
    }

    @Test
    void shouldRejectWrongTimeFormat() {
        String result = viewingTools.createViewingRequest("R1", "visitor-1", "张三", "13800138000",
                "明天下午", null);

        assertThat(result).contains("yyyy-MM-ddTHH:mm");
    }

    @Test
    void shouldSubmitAndThenReuseIdempotentResult() {
        when(apartmentClient.createViewingRequest(any(ViewingRequestCommand.class)))
                .thenReturn(new ViewingResult("req-1", "AP2026001", "WAITING", "已受理"));
        String time = futureTime();

        String first = viewingTools.createViewingRequest("R1", "visitor-1", "张三", "13800138000", time, null);
        String second = viewingTools.createViewingRequest("R1", "visitor-1", "张三", "13800138000", time, null);

        assertThat(first).contains("AP2026001");
        assertThat(second).contains("已提交过");
    }

    @Test
    void shouldReturnFriendlyMessageWhenApartmentFails() {
        when(apartmentClient.createViewingRequest(any(ViewingRequestCommand.class)))
                .thenThrow(new ZZAException(ResultCodeEnum.SERVICE_ERROR.getCode(), "公寓系统未开启"));

        String result = viewingTools.createViewingRequest("R1", "visitor-2", "李四", "13900139000",
                futureTime(), null);

        assertThat(result).contains("暂时无法提交看房预约");
    }

    private String futureTime() {
        return LocalDateTime.now().plusDays(3).withSecond(0).withNano(0)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    }
}
