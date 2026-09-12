package com.wxy.aicustomer.tool;

import com.wxy.aicustomer.client.ApartmentClient;
import com.wxy.aicustomer.client.dto.RoomDetail;
import com.wxy.aicustomer.client.dto.RoomSearchQuery;
import com.wxy.aicustomer.client.dto.RoomSummary;
import com.wxy.zzarental.common.exception.ZZAException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 房源类 Function Tool。每个 Tool 只调用公寓系统预先定义的内部接口。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoomTools {

    private static final int MAX_LIMIT = 10;
    private static final int MAX_RENT = 100_000;
    private static final int MAX_AREA = 1_000;

    private final ApartmentClient apartmentClient;

    @Tool(name = "searchAvailableRooms",
            description = "查询当前可租的空房列表，可按城市、预算、户型、面积筛选。"
                    + "当访客询问“有哪些房子、多少钱、几室几厅、还有没有空房”时调用。")
    public String searchAvailableRooms(
            @ToolParam(required = false, description = "城市编码，例如 310100，不知道时留空") String cityCode,
            @ToolParam(required = false, description = "月租金下限，单位元") Integer minRent,
            @ToolParam(required = false, description = "月租金上限，单位元") Integer maxRent,
            @ToolParam(required = false, description = "户型室数，2 表示两室") Integer roomCount,
            @ToolParam(required = false, description = "面积下限，单位平方米") Integer minArea,
            @ToolParam(required = false, description = "最多返回条数，默认 5，最大 10") Integer limit) {
        String invalid = validate(minRent, maxRent, roomCount, minArea, limit);
        if (invalid != null) {
            return invalid;
        }
        int size = limit == null ? 5 : Math.min(limit, MAX_LIMIT);
        RoomSearchQuery query = new RoomSearchQuery(
                StringUtils.hasText(cityCode) ? cityCode.trim() : null,
                toAmount(minRent),
                toAmount(maxRent),
                roomCount,
                minArea,
                null,
                size);
        try {
            List<RoomSummary> rooms = apartmentClient.searchAvailableRooms(query);
            if (rooms == null || rooms.isEmpty()) {
                return "当前没有符合条件的空房，可以建议访客放宽预算、户型或区域条件。";
            }
            return rooms.stream().map(this::formatRoom).collect(Collectors.joining("\n"));
        } catch (ZZAException ex) {
            log.warn("查询空房失败：{}", ex.getMessage());
            return "暂时无法查询房源：" + ex.getMessage();
        } catch (Exception ex) {
            log.error("查询空房异常", ex);
            return "暂时无法查询房源，请稍后再试。";
        }
    }

    @Tool(name = "getRoomDetail",
            description = "根据房源 ID 查询某套房的详细信息，包括租金、面积、朝向、配套和描述。"
                    + "房源 ID 必须来自 searchAvailableRooms 的结果。")
    public String getRoomDetail(
            @ToolParam(description = "房源 ID，来自空房查询结果") String roomId) {
        if (!StringUtils.hasText(roomId) || roomId.length() > 64) {
            return "房源 ID 不合法，请先使用 searchAvailableRooms 获取房源列表。";
        }
        try {
            RoomDetail detail = apartmentClient.getRoomDetail(roomId.trim());
            if (detail == null) {
                return "没有查询到该房源的详细信息。";
            }
            return """
                    房源 ID：%s
                    所属公寓：%s
                    房号：%s
                    楼层：%s
                    户型：%s室
                    租金：%s 元/月
                    面积：%s 平方米
                    朝向：%s
                    配套：%s
                    描述：%s
                    """.formatted(
                    detail.roomId(),
                    nullToDash(detail.apartmentName()),
                    nullToDash(detail.roomNumber()),
                    nullToDash(detail.floor()),
                    detail.roomCount(),
                    detail.rent(),
                    detail.area(),
                    nullToDash(detail.orientation()),
                    detail.facilities() == null ? "-" : String.join("、", detail.facilities()),
                    nullToDash(detail.description()));
        } catch (ZZAException ex) {
            log.warn("查询房源详情失败：{}", ex.getMessage());
            return "暂时无法查询该房源：" + ex.getMessage();
        } catch (Exception ex) {
            log.error("查询房源详情异常", ex);
            return "暂时无法查询该房源，请稍后再试。";
        }
    }

    private String validate(Integer minRent, Integer maxRent, Integer roomCount, Integer minArea, Integer limit) {
        if (minRent != null && (minRent < 0 || minRent > MAX_RENT)) {
            return "月租金下限超出可查询范围（0~" + MAX_RENT + " 元）。";
        }
        if (maxRent != null && (maxRent < 0 || maxRent > MAX_RENT)) {
            return "月租金上限超出可查询范围（0~" + MAX_RENT + " 元）。";
        }
        if (minRent != null && maxRent != null && minRent > maxRent) {
            return "月租金下限不能大于上限，请与访客确认预算区间。";
        }
        if (roomCount != null && (roomCount < 1 || roomCount > 9)) {
            return "户型室数只支持 1~9 室。";
        }
        if (minArea != null && (minArea < 1 || minArea > MAX_AREA)) {
            return "面积超出可查询范围（1~" + MAX_AREA + " 平方米）。";
        }
        if (limit != null && (limit < 1 || limit > MAX_LIMIT)) {
            return "返回条数只支持 1~" + MAX_LIMIT + "。";
        }
        return null;
    }

    private BigDecimal toAmount(Integer amount) {
        return amount == null ? null : BigDecimal.valueOf(amount);
    }

    private String formatRoom(RoomSummary room) {
        return "- 房源 ID %s：%s %s，%s室，租金 %s 元/月，面积 %s 平方米%s".formatted(
                room.roomId(),
                nullToDash(room.apartmentName()),
                nullToDash(room.roomNumber()),
                room.roomCount(),
                room.rent(),
                room.area(),
                room.labels() == null || room.labels().isEmpty() ? "" : "，标签：" + String.join("、", room.labels()));
    }

    private String nullToDash(String value) {
        return StringUtils.hasText(value) ? value : "-";
    }
}
