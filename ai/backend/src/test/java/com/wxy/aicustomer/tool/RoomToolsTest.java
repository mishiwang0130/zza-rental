package com.wxy.aicustomer.tool;

import com.wxy.aicustomer.client.ApartmentClient;
import com.wxy.aicustomer.client.dto.RoomSearchQuery;
import com.wxy.aicustomer.client.dto.RoomSummary;
import com.wxy.zzarental.common.exception.ZZAException;
import com.wxy.zzarental.common.result.ResultCodeEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 房源 Tool 测试：入参校验、结果格式化、下游异常兜底。
 */
class RoomToolsTest {

    private final ApartmentClient apartmentClient = mock(ApartmentClient.class);
    private final RoomTools roomTools = new RoomTools(apartmentClient);

    @Test
    void shouldRejectInvalidRentRange() {
        String result = roomTools.searchAvailableRooms(null, 5000, 3000, null, null, null);

        assertThat(result).contains("不能大于上限");
    }

    @Test
    void shouldRejectOutOfRangeRoomCount() {
        String result = roomTools.searchAvailableRooms(null, null, null, 12, null, null);

        assertThat(result).contains("1~9 室");
    }

    @Test
    void shouldFormatRoomsForModel() {
        when(apartmentClient.searchAvailableRooms(any(RoomSearchQuery.class))).thenReturn(List.of(
                new RoomSummary("R1", "阳光公寓", "101", 2, new BigDecimal("3200"),
                        new BigDecimal("58.5"), List.of("近地铁"))));

        String result = roomTools.searchAvailableRooms(null, 2000, 4000, 2, null, 3);

        assertThat(result).contains("R1").contains("阳光公寓").contains("3200").contains("近地铁");
    }

    @Test
    void shouldTellWhenNoRoomMatched() {
        when(apartmentClient.searchAvailableRooms(any(RoomSearchQuery.class))).thenReturn(List.of());

        String result = roomTools.searchAvailableRooms(null, null, null, null, null, null);

        assertThat(result).contains("没有符合条件的空房");
    }

    @Test
    void shouldReturnFriendlyMessageWhenApartmentFails() {
        when(apartmentClient.searchAvailableRooms(any(RoomSearchQuery.class)))
                .thenThrow(new ZZAException(ResultCodeEnum.SERVICE_ERROR.getCode(), "公寓系统未开启"));

        String result = roomTools.searchAvailableRooms(null, null, null, null, null, null);

        assertThat(result).contains("暂时无法查询房源").contains("公寓系统未开启");
    }

    @Test
    void shouldRejectInvalidRoomId() {
        String result = roomTools.getRoomDetail("  ");

        assertThat(result).contains("房源 ID 不合法");
    }
}
