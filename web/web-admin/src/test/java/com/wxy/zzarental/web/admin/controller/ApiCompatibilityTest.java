package com.wxy.zzarental.web.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxy.zzarental.model.entity.SystemPost;
import com.wxy.zzarental.web.admin.controller.apartment.FacilityController;
import com.wxy.zzarental.web.admin.controller.apartment.FileUploadController;
import com.wxy.zzarental.web.admin.controller.lease.LeaseAgreementController;
import com.wxy.zzarental.web.admin.controller.login.LoginController;
import com.wxy.zzarental.web.admin.controller.system.SystemPostController;
import com.wxy.zzarental.web.admin.service.FacilityInfoService;
import com.wxy.zzarental.web.admin.service.FileService;
import com.wxy.zzarental.web.admin.service.LeaseAgreementService;
import com.wxy.zzarental.web.admin.service.LoginService;
import com.wxy.zzarental.web.admin.service.SystemPostService;
import com.wxy.zzarental.web.admin.vo.agreement.AgreementRespVO;
import com.wxy.zzarental.web.admin.vo.agreement.AgreementSaveReqVO;
import com.wxy.zzarental.web.admin.vo.appointment.AppointmentRespVO;
import com.wxy.zzarental.web.admin.vo.attr.AttrKeyRespVO;
import com.wxy.zzarental.web.admin.vo.attr.AttrValueRespVO;
import com.wxy.zzarental.web.admin.vo.fee.FeeKeyRespVO;
import com.wxy.zzarental.web.admin.vo.fee.FeeValueRespVO;
import com.wxy.zzarental.web.admin.vo.login.LoginReqVO;
import com.wxy.zzarental.web.admin.vo.system.user.SystemPostItemRespVO;
import com.wxy.zzarental.web.admin.vo.system.user.SystemUserItemRespVO;
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
        AgreementRespVO agreement = new AgreementRespVO();
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
        when(service.login(any(LoginReqVO.class))).thenReturn("existing-token");

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

        AgreementRespVO agreement = new AgreementRespVO();
        agreement.setLeaseStartDate(request.getLeaseStartDate());
        agreement.setLeaseEndDate(request.getLeaseEndDate());
        JsonNode agreementJson = objectMapper.valueToTree(agreement);
        assertEquals("2026-09-10", agreementJson.get("leaseStartDate").asText());
        assertEquals("2027-09-09", agreementJson.get("leaseEndDate").asText());

        AppointmentRespVO appointment = new AppointmentRespVO();
        appointment.setAppointmentTime(Date.from(Instant.parse("2026-09-10T01:02:03Z")));
        JsonNode appointmentJson = objectMapper.valueToTree(appointment);
        assertEquals("2026-09-10 09:02:03", appointmentJson.get("appointmentTime").asText());
    }

    @Test
    void nestedListsDoNotIntroduceFieldsAbsentFromTheOriginalEntities() {
        AttrValueRespVO attributeValue = new AttrValueRespVO();
        attributeValue.setId(11L);
        attributeValue.setName("South");
        attributeValue.setAttrKeyName("Orientation");
        AttrKeyRespVO attribute = new AttrKeyRespVO();
        attribute.setAttrValueList(List.of(attributeValue));
        JsonNode nestedAttribute = objectMapper.valueToTree(attribute).get("attrValueList").get(0);
        assertEquals("South", nestedAttribute.get("name").asText());
        assertFalse(nestedAttribute.has("attrKeyName"));
        assertTrue(objectMapper.valueToTree(attributeValue).has("attrKeyName"));

        FeeValueRespVO feeValue = new FeeValueRespVO();
        feeValue.setFeeKeyName("Water");
        FeeKeyRespVO fee = new FeeKeyRespVO();
        fee.setFeeValueList(List.of(feeValue));
        assertFalse(objectMapper.valueToTree(fee).get("feeValueList").get(0).has("feeKeyName"));
        assertTrue(objectMapper.valueToTree(feeValue).has("feeKeyName"));

        SystemUserItemRespVO user = new SystemUserItemRespVO();
        user.setPostName("Manager");
        SystemPostItemRespVO post = new SystemPostItemRespVO();
        post.setSystemUsers(List.of(user));
        assertFalse(objectMapper.valueToTree(post).get("systemUsers").get(0).has("postName"));
        assertTrue(objectMapper.valueToTree(user).has("postName"));
    }

    private MockMvc mvc(Object controller, String serviceField, Object service) {
        ReflectionTestUtils.setField(controller, serviceField, service);
        return MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }
}
