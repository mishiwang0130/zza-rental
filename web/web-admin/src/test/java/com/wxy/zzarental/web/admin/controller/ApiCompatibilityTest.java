package com.wxy.zzarental.web.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxy.zzarental.model.entity.ApartmentInfo;
import com.wxy.zzarental.model.entity.SystemPost;
import com.wxy.zzarental.web.admin.controller.apartment.ApartmentController;
import com.wxy.zzarental.web.admin.controller.apartment.FacilityController;
import com.wxy.zzarental.web.admin.controller.apartment.FileUploadController;
import com.wxy.zzarental.web.admin.controller.apartment.RoomController;
import com.wxy.zzarental.web.admin.controller.assembler.AdminApiAssembler;
import com.wxy.zzarental.web.admin.controller.lease.LeaseAgreementController;
import com.wxy.zzarental.web.admin.controller.login.LoginController;
import com.wxy.zzarental.web.admin.controller.system.SystemPostController;
import com.wxy.zzarental.web.admin.service.FacilityInfoService;
import com.wxy.zzarental.web.admin.service.ApartmentInfoService;
import com.wxy.zzarental.web.admin.service.FileService;
import com.wxy.zzarental.web.admin.service.LeaseAgreementService;
import com.wxy.zzarental.web.admin.service.LoginService;
import com.wxy.zzarental.web.admin.service.RoomInfoService;
import com.wxy.zzarental.web.admin.service.SystemPostService;
import com.wxy.zzarental.web.admin.service.command.LoginCommand;
import com.wxy.zzarental.web.admin.service.dto.AgreementDTO;
import com.wxy.zzarental.web.admin.service.dto.AppointmentDTO;
import com.wxy.zzarental.web.admin.service.dto.AttrKeyDTO;
import com.wxy.zzarental.web.admin.service.dto.AttrValueDTO;
import com.wxy.zzarental.web.admin.service.dto.FeeKeyDTO;
import com.wxy.zzarental.web.admin.service.dto.FeeValueDTO;
import com.wxy.zzarental.web.admin.service.dto.RoomItemDTO;
import com.wxy.zzarental.web.admin.service.dto.SystemPostItemDTO;
import com.wxy.zzarental.web.admin.service.dto.SystemUserItemDTO;
import com.wxy.zzarental.web.admin.service.query.RoomQuery;
import com.wxy.zzarental.web.admin.vo.agreement.AgreementSaveReqVO;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiCompatibilityTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getByIdAcceptsTheExistingQueryParameter() throws Exception {
        SystemPostService service = mock(SystemPostService.class);
        SystemPost post = new SystemPost();
        post.setId(7L);
        post.setPostCode("MANAGER");
        when(service.getById(7L)).thenReturn(post);

        mvc(new SystemPostController(), "systemPostService", service)
                .perform(get("/admin/system/post/getById").param("id", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(7))
                .andExpect(jsonPath("$.data.postCode").value("MANAGER"));

        verify(service).getById(7L);
    }

    @Test
    void agreementDetailKeepsTheOriginalRouteAlongsideGetById() throws Exception {
        LeaseAgreementService service = mock(LeaseAgreementService.class);
        AgreementDTO agreement = new AgreementDTO();
        agreement.setId(7L);
        when(service.getLeaseInfoById(7L)).thenReturn(agreement);
        MockMvc mockMvc = mvc(new LeaseAgreementController(), "leaseAgreementService", service);

        for (String path : List.of("/admin/agreement", "/admin/agreement/getById")) {
            mockMvc.perform(get(path).param("id", "7"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(7));
        }
    }

    @Test
    void optionalTypeCanStillBeOmitted() throws Exception {
        FacilityInfoService service = mock(FacilityInfoService.class);
        when(service.list(any())).thenReturn(List.of());

        mvc(new FacilityController(), "facilityInfoService", service)
                .perform(get("/admin/facility/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());

        verify(service).list(argThat(wrapper -> wrapper.getSqlSegment().isEmpty()));
    }

    @Test
    void loginKeepsTheExistingBodyFieldsAndStringData() throws Exception {
        LoginService service = mock(LoginService.class);
        when(service.login(any(LoginCommand.class))).thenReturn("existing-token");

        mvc(new LoginController(), "loginService", service)
                .perform(post("/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"secret",
                                 "captchaKey":"captcha-key","captchaCode":"1234"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isString())
                .andExpect(jsonPath("$.data").value("existing-token"));

        verify(service).login(argThat(request -> "admin".equals(request.getUsername())
                && "secret".equals(request.getPassword())
                && "captcha-key".equals(request.getCaptchaKey())
                && "1234".equals(request.getCaptchaCode())));
    }

    @Test
    void uploadKeepsTheMultipartFileFieldAndStringData() throws Exception {
        FileService service = mock(FileService.class);
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png",
                "image-content".getBytes(StandardCharsets.UTF_8));
        when(service.upload(any())).thenReturn("https://example.test/image.png");

        mvc(new FileUploadController(), "fileService", service)
                .perform(multipart("/admin/file/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isString())
                .andExpect(jsonPath("$.data").value("https://example.test/image.png"));

        verify(service).upload(file);
    }

    @Test
    void entityPageConversionPreservesRecordsAndAllPageMetadata() throws Exception {
        SystemPostService service = mock(SystemPostService.class);
        SystemPost post = new SystemPost();
        post.setId(7L);
        post.setPostCode("MANAGER");
        post.setName("Manager");
        Page<SystemPost> page = new Page<>(2, 5, 12);
        page.setRecords(List.of(post));
        page.addOrder(OrderItem.asc("name"));
        page.setOptimizeCountSql(false);
        JsonNode originalPageJson = objectMapper.readTree(objectMapper.writeValueAsString(page));
        when(service.page(any(IPage.class))).thenReturn(page);

        String response = mvc(new SystemPostController(), "systemPostService", service)
                .perform(get("/admin/system/post/page").param("current", "2").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].id").value(7))
                .andExpect(jsonPath("$.data.current").value(2))
                .andExpect(jsonPath("$.data.size").value(5))
                .andExpect(jsonPath("$.data.total").value(12))
                .andReturn().getResponse().getContentAsString();

        assertEquals(originalPageJson, objectMapper.readTree(response).get("data"));
        verify(service).page(argThat((IPage<SystemPost> request) ->
                request.getCurrent() == 2 && request.getSize() == 5));
    }

    @Test
    void dateFieldsKeepTheExistingNamesFormatsAndAppointmentTimezone() throws Exception {
        AgreementSaveReqVO request = objectMapper.readValue("""
                {"leaseStartDate":"2026-09-10","leaseEndDate":"2027-09-09",
                 "identificationNumber":"existing-id-number","additionalInfo":"existing-note"}
                """, AgreementSaveReqVO.class);
        assertEquals("existing-id-number", request.getIdentificationNumber());
        assertEquals("existing-note", request.getAdditionalInfo());

        AgreementDTO agreement = new AgreementDTO();
        agreement.setLeaseStartDate(request.getLeaseStartDate());
        agreement.setLeaseEndDate(request.getLeaseEndDate());
        JsonNode agreementJson = objectMapper.valueToTree(AdminApiAssembler.toResponse(agreement));
        assertEquals("2026-09-10", agreementJson.get("leaseStartDate").asText());
        assertEquals("2027-09-09", agreementJson.get("leaseEndDate").asText());

        AppointmentDTO appointment = new AppointmentDTO();
        appointment.setAppointmentTime(Date.from(Instant.parse("2026-09-10T01:02:03Z")));
        JsonNode appointmentJson = objectMapper.valueToTree(AdminApiAssembler.toResponse(appointment));
        assertEquals("2026-09-10 09:02:03", appointmentJson.get("appointmentTime").asText());
    }

    @Test
    void nestedListsDoNotIntroduceFieldsAbsentFromTheOriginalEntities() {
        AttrValueDTO attributeValue = new AttrValueDTO();
        attributeValue.setId(11L);
        attributeValue.setName("South");
        attributeValue.setAttrKeyName("Orientation");
        AttrKeyDTO attribute = new AttrKeyDTO();
        attribute.setAttrValueList(List.of(attributeValue));
        JsonNode nestedAttribute = objectMapper.valueToTree(AdminApiAssembler.toResponse(attribute))
                .get("attrValueList").get(0);
        assertEquals("South", nestedAttribute.get("name").asText());
        assertFalse(nestedAttribute.has("attrKeyName"));
        assertTrue(objectMapper.valueToTree(AdminApiAssembler.toResponse(attributeValue)).has("attrKeyName"));

        FeeValueDTO feeValue = new FeeValueDTO();
        feeValue.setFeeKeyName("Water");
        FeeKeyDTO fee = new FeeKeyDTO();
        fee.setFeeValueList(List.of(feeValue));
        assertFalse(objectMapper.valueToTree(AdminApiAssembler.toResponse(fee)).get("feeValueList").get(0).has("feeKeyName"));
        assertTrue(objectMapper.valueToTree(AdminApiAssembler.toResponse(feeValue)).has("feeKeyName"));

        SystemUserItemDTO user = new SystemUserItemDTO();
        user.setPostName("Manager");
        SystemPostItemDTO post = new SystemPostItemDTO();
        post.setSystemUsers(List.of(user));
        assertFalse(objectMapper.valueToTree(AdminApiAssembler.toResponse(post)).get("systemUsers").get(0).has("postName"));
        assertTrue(objectMapper.valueToTree(AdminApiAssembler.toResponse(user)).has("postName"));

        attribute.setAttrValueList(null);
        post.setSystemUsers(null);
        assertTrue(objectMapper.valueToTree(AdminApiAssembler.toResponse(attribute)).get("attrValueList").isNull());
        assertTrue(objectMapper.valueToTree(AdminApiAssembler.toResponse(post)).get("systemUsers").isNull());
    }

    @Test
    void apartmentSaveKeepsTheExistingGraphAndAssociationFields() throws Exception {
        ApartmentInfoService service = mock(ApartmentInfoService.class);

        mvc(new ApartmentController(), "apartmentInfoService", service)
                .perform(post("/admin/apartment/saveOrUpdate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":7,"name":"Apartment","facilityInfoIds":[11],
                                 "labelIds":[12],"feeValueIds":[13],
                                 "graphVoList":[{"name":"cover","url":"https://example.test/cover.png"}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(service).saveOrUpdateApart(argThat(command -> command.getId() == 7L
                && "Apartment".equals(command.getName())
                && List.of(11L).equals(command.getFacilityInfoIds())
                && List.of(12L).equals(command.getLabelIds())
                && List.of(13L).equals(command.getFeeValueIds())
                && command.getGraphVoList().size() == 1
                && "cover".equals(command.getGraphVoList().get(0).getName())
                && "https://example.test/cover.png".equals(command.getGraphVoList().get(0).getUrl())));
    }

    @Test
    void servicePageKeepsTheQueryNestedFieldsAndPaginationMetadata() throws Exception {
        RoomInfoService service = mock(RoomInfoService.class);
        ApartmentInfo apartment = new ApartmentInfo();
        apartment.setId(7L);
        apartment.setName("Apartment");
        RoomItemDTO room = new RoomItemDTO();
        room.setId(8L);
        room.setRoomNumber("101");
        room.setApartmentInfo(apartment);
        room.setLeaseEndDate(Date.from(Instant.parse("2027-09-09T00:00:00Z")));
        Page<RoomItemDTO> page = new Page<>(2, 5, 12);
        page.setRecords(List.of(room));
        page.addOrder(OrderItem.asc("id"));
        page.setOptimizeCountSql(false);
        JsonNode originalPageJson = objectMapper.readTree(objectMapper.writeValueAsString(page));
        when(service.pageItem(eq(2L), eq(5L), any(RoomQuery.class))).thenReturn(page);

        String response = mvc(new RoomController(), "roomInfoService", service)
                .perform(get("/admin/room/pageItem").param("current", "2").param("size", "5")
                        .param("apartmentId", "7").param("districtId", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].roomNumber").value("101"))
                .andExpect(jsonPath("$.data.records[0].apartmentInfo.name").value("Apartment"))
                .andReturn().getResponse().getContentAsString();

        assertEquals(originalPageJson, objectMapper.readTree(response).get("data"));
        verify(service).pageItem(eq(2L), eq(5L), argThat(query ->
                query.getApartmentId() == 7L && query.getDistrictId() == 9L));
    }

    private MockMvc mvc(Object controller, String serviceField, Object service) {
        ReflectionTestUtils.setField(controller, serviceField, service);
        return MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }
}
