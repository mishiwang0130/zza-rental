package com.wxy.aicustomer.client;

import com.wxy.aicustomer.client.dto.ApartmentResponse;
import com.wxy.aicustomer.client.dto.RoomDetail;
import com.wxy.aicustomer.client.dto.RoomSearchQuery;
import com.wxy.aicustomer.client.dto.RoomSummary;
import com.wxy.aicustomer.client.dto.ViewingRequestCommand;
import com.wxy.aicustomer.client.dto.ViewingResult;
import com.wxy.aicustomer.config.AppProperties;
import com.wxy.zzarental.common.exception.ZZAException;
import com.wxy.zzarental.common.result.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Set;

/**
 * 公寓系统内部接口客户端。
 *
 * <p>只允许调用白名单中的 /internal/ai/** 接口，模型无法拼接任意 URL；
 * 每次请求携带固定 Service Token 供公寓系统校验调用方身份。
 */
@Slf4j
@Component
public class ApartmentClient {

    public static final String PATH_ROOM_SEARCH = "/rooms/search";
    public static final String PATH_ROOM_DETAIL = "/rooms/{roomId}";
    public static final String PATH_VIEWING_CREATE = "/viewings";

    private static final String SERVICE_TOKEN_HEADER = "X-Service-Token";
    private static final Set<String> ALLOWED_PATHS = Set.of(PATH_ROOM_SEARCH, PATH_ROOM_DETAIL, PATH_VIEWING_CREATE);

    private final RestClient restClient;
    private final AppProperties properties;
    private final String pathPrefix;

    public ApartmentClient(RestClient.Builder builder, AppProperties properties) {
        this.properties = properties;
        this.pathPrefix = properties.getApartment().getInternalPathPrefix();
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) properties.getApartment().getConnectTimeout().toMillis());
        requestFactory.setReadTimeout((int) properties.getApartment().getReadTimeout().toMillis());
        this.restClient = builder.clone()
                .baseUrl(properties.getApartment().getBaseUrl())
                .requestFactory(requestFactory)
                .defaultHeader(SERVICE_TOKEN_HEADER, properties.getApartment().getServiceToken())
                .build();
    }

    /**
     * 查询空房列表。
     */
    public List<RoomSummary> searchAvailableRooms(RoomSearchQuery query) {
        String path = assertAllowedPath(PATH_ROOM_SEARCH);
        assertEnabled();
        ApartmentResponse<List<RoomSummary>> response = restClient.post()
                .uri(pathPrefix + path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(query)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        return unwrap(response);
    }

    /**
     * 查询房源详情。
     */
    public RoomDetail getRoomDetail(String roomId) {
        String path = assertAllowedPath(PATH_ROOM_DETAIL);
        assertEnabled();
        ApartmentResponse<RoomDetail> response = restClient.get()
                .uri(pathPrefix + path, roomId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        return unwrap(response);
    }

    /**
     * 提交看房预约，requestId 作为幂等键。
     */
    public ViewingResult createViewingRequest(ViewingRequestCommand command) {
        String path = assertAllowedPath(PATH_VIEWING_CREATE);
        assertEnabled();
        ApartmentResponse<ViewingResult> response = restClient.post()
                .uri(pathPrefix + path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(command)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        return unwrap(response);
    }

    private <T> T unwrap(ApartmentResponse<T> response) {
        if (response == null) {
            throw new ZZAException(ResultCodeEnum.SERVICE_ERROR.getCode(), "公寓系统未返回数据");
        }
        if (!response.isSuccess()) {
            throw new ZZAException(ResultCodeEnum.SERVICE_ERROR.getCode(),
                    "公寓系统返回失败：" + response.message());
        }
        return response.data();
    }

    private void assertEnabled() {
        if (!properties.getApartment().isEnabled()) {
            throw new ZZAException(ResultCodeEnum.SERVICE_ERROR.getCode(),
                    "公寓系统对接未开启（app.apartment.enabled=false）");
        }
    }

    private String assertAllowedPath(String path) {
        if (!ALLOWED_PATHS.contains(path)) {
            throw new IllegalArgumentException("未授权的公寓接口：" + path);
        }
        return path;
    }
}
