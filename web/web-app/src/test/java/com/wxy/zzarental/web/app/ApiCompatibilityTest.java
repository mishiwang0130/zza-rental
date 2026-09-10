package com.wxy.zzarental.web.app;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wxy.zzarental.web.app.controller.login.LoginController;
import com.wxy.zzarental.web.app.controller.room.RoomController;
import com.wxy.zzarental.web.app.service.LoginService;
import com.wxy.zzarental.web.app.service.RoomInfoService;
import com.wxy.zzarental.web.app.vo.apartment.ApartmentItemRespVO;
import com.wxy.zzarental.web.app.vo.appointment.AppointmentDetailRespVO;
import com.wxy.zzarental.web.app.vo.appointment.AppointmentSaveReqVO;
import com.wxy.zzarental.web.app.vo.graph.GraphRespVO;
import com.wxy.zzarental.web.app.vo.history.HistoryItemRespVO;
import com.wxy.zzarental.web.app.vo.room.RoomDetailRespVO;
import com.wxy.zzarental.web.app.vo.room.RoomItemRespVO;
import com.wxy.zzarental.web.app.vo.room.RoomPageReqVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiCompatibilityTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .setTimeZone(TimeZone.getTimeZone("UTC"));
    private LoginService loginService;
    private RoomInfoService roomInfoService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        loginService = mock(LoginService.class);
        roomInfoService = mock(RoomInfoService.class);
        LoginController loginController = new LoginController();
        RoomController roomController = new RoomController();
        ReflectionTestUtils.setField(loginController, "loginService", loginService);
        ReflectionTestUtils.setField(roomController, "roomInfoService", roomInfoService);
        mockMvc = MockMvcBuilders.standaloneSetup(loginController, roomController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void loginKeepsOriginalBodyFieldsAndBareToken() throws Exception {
        when(loginService.login(any())).thenReturn("original-token");

        mockMvc.perform(post("/app/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13800000000\",\"code\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("original-token"));

        verify(loginService).login(argThat(request ->
                "13800000000".equals(request.getPhone())
                        && "123456".equals(request.getCode())));
    }

    @Test
    void phoneAndIdRemainRequiredScalarQueryParameters() throws Exception {
        RoomDetailRespVO room = new RoomDetailRespVO();
        room.setId(7L);
        when(roomInfoService.getDetailById(7L)).thenReturn(room);

        mockMvc.perform(get("/app/login/getCode").param("phone", "13800000000"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/app/room/getDetailById").param("id", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(7));
        mockMvc.perform(get("/app/login/getCode"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/app/room/getDetailById"))
                .andExpect(status().isBadRequest());

        verify(loginService).sendSmsCode("13800000000");
        verify(roomInfoService).getDetailById(7L);
    }

    @Test
    void roomPageKeepsOriginalQueryParametersAndCompletePageMetadata() throws Exception {
        RoomItemRespVO room = new RoomItemRespVO();
        room.setId(7L);
        Page<RoomItemRespVO> result = new Page<>(2, 20, 31);
        result.setRecords(List.of(room));
        result.addOrder(OrderItem.asc("rent"));
        result.setOptimizeCountSql(false);
        result.setSearchCount(false);
        result.setMaxLimit(100L);
        result.setCountId("roomCount");
        when(roomInfoService.pageItem(any(), any())).thenAnswer(invocation -> {
            Page<RoomItemRespVO> page = invocation.getArgument(0);
            RoomPageReqVO query = invocation.getArgument(1);
            assertEquals(2, page.getCurrent());
            assertEquals(20, page.getSize());
            assertEquals(11L, query.getProvinceId());
            assertEquals("asc", query.getOrderType());
            return result;
        });

        String response = mockMvc.perform(get("/app/room/pageItem")
                        .param("current", "2").param("size", "20")
                        .param("provinceId", "11").param("orderType", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].id").value(7))
                .andReturn().getResponse().getContentAsString();

        ObjectNode metadata = (ObjectNode) objectMapper.readTree(response).get("data");
        metadata.remove("records");
        assertEquals(objectMapper.readTree("""
                {"current":2,"size":20,"total":31,"pages":2,
                 "orders":[{"column":"rent","asc":true}],
                 "optimizeCountSql":false,"searchCount":false,
                 "maxLimit":100,"countId":"roomCount"}
                """), metadata);
        mockMvc.perform(get("/app/room/pageItem").param("size", "20"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/app/room/pageItem").param("current", "2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void appointmentRequestAcceptsOriginalDateFormatAndTimeZone() throws Exception {
        AppointmentSaveReqVO request = objectMapper.readValue(
                "{\"appointmentTime\":\"2026-09-10 10:30:00\"}", AppointmentSaveReqVO.class);

        assertEquals(Instant.parse("2026-09-10T02:30:00Z"), request.getAppointmentTime().toInstant());
    }

    @Test
    void appointmentAndHistoryResponsesKeepOriginalDateFormats() {
        Date date = Date.from(Instant.parse("2026-09-10T02:30:00Z"));
        AppointmentDetailRespVO appointment = new AppointmentDetailRespVO();
        appointment.setAppointmentTime(date);
        HistoryItemRespVO history = new HistoryItemRespVO();
        history.setBrowseTime(date);

        JsonNode appointmentJson = objectMapper.valueToTree(appointment);
        JsonNode historyJson = objectMapper.valueToTree(history);
        assertEquals("2026-09-10 10:30:00", appointmentJson.get("appointmentTime").asText());
        assertEquals("2026-09-10 02:30:00", historyJson.get("browseTime").asText());
    }

    @Test
    void nestedResponsePropertiesKeepTheirExistingNames() {
        GraphRespVO graph = new GraphRespVO("cover", "https://example.test/cover.jpg");
        HistoryItemRespVO history = new HistoryItemRespVO();
        history.setRoomGraphVoList(List.of(graph));
        ApartmentItemRespVO apartment = new ApartmentItemRespVO();
        apartment.setId(5L);
        RoomDetailRespVO room = new RoomDetailRespVO();
        room.setApartmentItemVo(apartment);
        room.setGraphVoList(List.of(graph));

        JsonNode historyJson = objectMapper.valueToTree(history);
        JsonNode roomJson = objectMapper.valueToTree(room);
        assertEquals("cover", historyJson.at("/roomGraphVoList/0/name").asText());
        assertEquals(5, roomJson.at("/apartmentItemVo/id").asLong());
        assertEquals("https://example.test/cover.jpg", roomJson.at("/graphVoList/0/url").asText());
        assertFalse(historyJson.has("roomGraphRespVOList"));
        assertFalse(roomJson.has("apartmentItemRespVO"));
    }
}
