package com.wxy.aicustomer.client;

import com.sun.net.httpserver.HttpServer;
import com.wxy.aicustomer.client.dto.RoomDetail;
import com.wxy.aicustomer.client.dto.RoomSearchQuery;
import com.wxy.aicustomer.client.dto.RoomSummary;
import com.wxy.aicustomer.config.AppProperties;
import com.wxy.zzarental.common.exception.ZZAException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 公寓系统调用测试：用 JDK 内置 HttpServer 起一个假公寓系统，
 * 验证白名单路径、Service Token、响应解包与错误处理。
 */
class ApartmentClientTest {

    private final List<String> requestedPaths = new ArrayList<>();
    private final List<String> serviceTokens = new ArrayList<>();
    private HttpServer server;
    private int port;
    private String responseBody = "{\"code\":200,\"message\":\"成功\",\"data\":null}";

    @BeforeEach
    void startFakeApartment() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/internal/ai", exchange -> {
            requestedPaths.add(exchange.getRequestURI().getPath());
            serviceTokens.add(exchange.getRequestHeaders().getFirst("X-Service-Token"));
            byte[] body = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(body);
            }
        });
        server.start();
        port = server.getAddress().getPort();
    }

    @AfterEach
    void stopFakeApartment() {
        server.stop(0);
    }

    @Test
    void shouldSearchRoomsWithServiceToken() {
        responseBody = """
                {"code":200,"message":"成功","data":[
                  {"roomId":"R1","apartmentName":"阳光公寓","roomNumber":"101","roomCount":2,
                   "rent":3200,"area":58.5,"labels":["近地铁"]}
                ]}
                """;

        List<RoomSummary> rooms = client().searchAvailableRooms(
                new RoomSearchQuery(null, new BigDecimal("2000"), new BigDecimal("4000"), 2, null, null, 5));

        assertThat(rooms).hasSize(1);
        assertThat(rooms.get(0).roomId()).isEqualTo("R1");
        assertThat(requestedPaths).containsExactly("/internal/ai/rooms/search");
        assertThat(serviceTokens).containsExactly("test-service-token");
    }

    @Test
    void shouldFetchRoomDetailByPath() {
        responseBody = """
                {"code":200,"message":"成功","data":
                  {"roomId":"R1","apartmentName":"阳光公寓","roomNumber":"101","floor":"10",
                   "roomCount":2,"rent":3200,"area":58.5,"orientation":"南",
                   "facilities":["空调","洗衣机"],"description":"采光好","coverUrl":null}}
                """;

        RoomDetail detail = client().getRoomDetail("R1");

        assertThat(detail.apartmentName()).isEqualTo("阳光公寓");
        assertThat(detail.facilities()).containsExactly("空调", "洗衣机");
        assertThat(requestedPaths).containsExactly("/internal/ai/rooms/R1");
    }

    @Test
    void shouldFailWhenApartmentReturnsBusinessError() {
        responseBody = "{\"code\":203,\"message\":\"公寓服务异常\",\"data\":null}";

        ApartmentClient client = client();

        assertThatThrownBy(() -> client.getRoomDetail("R1"))
                .isInstanceOf(ZZAException.class)
                .hasMessageContaining("公寓服务异常");
    }

    @Test
    void shouldRejectCallWhenApartmentDisabled() {
        AppProperties properties = properties();
        properties.getApartment().setEnabled(false);
        ApartmentClient client = new ApartmentClient(RestClient.builder(), properties);

        assertThatThrownBy(() -> client.getRoomDetail("R1"))
                .isInstanceOf(ZZAException.class)
                .hasMessageContaining("未开启");
    }

    private ApartmentClient client() {
        return new ApartmentClient(RestClient.builder(), properties());
    }

    private AppProperties properties() {
        AppProperties properties = new AppProperties();
        properties.getApartment().setBaseUrl("http://127.0.0.1:" + port);
        properties.getApartment().setServiceToken("test-service-token");
        properties.getApartment().setEnabled(true);
        return properties;
    }
}
