package com.wxy.zzarental.common.exception;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wxy.zzarental.common.result.Result;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.annotation.Validated;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationInterceptor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private static final String PRIVATE_DETAIL = "private-db-password-and-stack-detail";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private LocalValidatorFactoryBean validator;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        ProxyFactory proxyFactory = new ProxyFactory(new MethodConstraintsController());
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addAdvice(new MethodValidationInterceptor((jakarta.validation.Validator) validator));

        mvc = MockMvcBuilders.standaloneSetup(new FixtureController(), proxyFactory.getProxy())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }

    @Test
    void invalidJsonBodyUsesTheParameterErrorEnvelope() throws Exception {
        JsonNode body = assertError(mvc.perform(post("/body").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\",\"age\":20}")), 202);
        assertEquals("name: 姓名不能为空", body.get("message").asText());
    }

    @Test
    void validatedQueryBeanUsesTheParameterErrorEnvelope() throws Exception {
        JsonNode body = assertError(mvc.perform(get("/binding").param("name", "")), 202);
        assertEquals("name: 姓名不能为空", body.get("message").asText());
    }

    @Test
    void queryBeanBindingErrorIdentifiesTheFieldWithoutExposingTheRawValue() throws Exception {
        JsonNode body = assertError(mvc.perform(get("/binding").param("name", "user")
                .param("age", PRIVATE_DETAIL)), 202);
        assertEquals("age: 类型或格式不正确", body.get("message").asText());
    }

    @Test
    void objectLevelValidationWithoutFieldErrorsIsHandled() throws Exception {
        JsonNode body = assertError(mvc.perform(post("/range").contentType(MediaType.APPLICATION_JSON)
                .content("{\"start\":3,\"end\":1}")), 202);
        assertEquals("结束值不得小于开始值", body.get("message").asText());
    }

    @Test
    void proxiedMethodConstraintViolationIsHandled() throws Exception {
        JsonNode body = assertError(mvc.perform(get("/method").param("count", "0")), 202);
        assertTrue(body.get("message").asText().contains("数量至少为1"));
    }

    @Test
    void proxiedReturnValueConstraintIsAServiceErrorWithoutValidationDetails() throws Exception {
        assertError(mvc.perform(get("/method-return")), 203);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/number", "/header"})
    void missingRequiredQueryOrHeaderIsHandled(String path) throws Exception {
        assertError(mvc.perform(get(path)), 202);
    }

    @Test
    void missingMultipartPartIsHandled() throws Exception {
        assertError(mvc.perform(multipart("/upload")), 202);
    }

    @Test
    void nonNumericQueryParameterIsHandledWithoutItsRawValue() throws Exception {
        assertError(mvc.perform(get("/number").param("id", PRIVATE_DETAIL)), 202);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "{", "{\"name\":\"user\",\"age\":\"private-db-password-and-stack-detail\"}"})
    void emptyMalformedOrWronglyTypedJsonIsHandled(String body) throws Exception {
        assertError(mvc.perform(post("/body").contentType(MediaType.APPLICATION_JSON)
                .content(body)), 202);
    }

    @Test
    void unsupportedHttpMethodUsesTheIllegalRequestEnvelope() throws Exception {
        assertError(mvc.perform(get("/body")), 205);
    }

    @Test
    void unsupportedContentTypeUsesTheIllegalRequestEnvelope() throws Exception {
        assertError(mvc.perform(post("/body").contentType(MediaType.TEXT_PLAIN)
                .content(PRIVATE_DETAIL)), 205);
    }

    @Test
    void incompatibleAcceptHeaderStillReceivesTheJsonErrorEnvelope() throws Exception {
        assertError(mvc.perform(get("/number").param("id", "1").accept(MediaType.TEXT_PLAIN)), 205);
    }

    @Test
    void parameterErrorWithIncompatibleAcceptStillReceivesTheJsonErrorEnvelope() throws Exception {
        assertError(mvc.perform(get("/number").param("id", PRIVATE_DETAIL)
                .accept(MediaType.TEXT_PLAIN)), 202);
    }

    @ParameterizedTest
    @CsvSource({"duplicate,204", "integrity,204", "access,203", "multipart,202",
            "oversized,202", "type-mismatch,202", "conversion-not-supported,203", "unknown,201"})
    void infrastructureExceptionsDoNotExposeInternalDetails(String kind, int code) throws Exception {
        assertError(mvc.perform(get("/throw/" + kind)), code);
    }

    @Test
    void businessExceptionKeepsItsExistingCodeAndMessage() throws Exception {
        JsonNode body = assertError(mvc.perform(get("/throw/business")), 208);
        assertEquals("公寓id不存在", body.get("message").asText());
    }

    @Test
    void responseSerializationFailureUsesTheServiceErrorEnvelope() throws Exception {
        assertError(mvc.perform(get("/broken-json")), 203);
    }

    @Test
    void missingMappedPathVariableUsesTheServiceErrorEnvelope() throws Exception {
        assertError(mvc.perform(get("/path/1")), 203);
    }

    @Test
    void explicitlyEnabledNoHandlerExceptionUsesTheIllegalRequestEnvelope() throws Exception {
        MockMvc noHandlerMvc = MockMvcBuilders.standaloneSetup(new FixtureController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .addDispatcherServletCustomizer(servlet -> servlet.setThrowExceptionIfNoHandlerFound(true))
                .build();

        assertError(noHandlerMvc.perform(get("/no-such-interface")), 205);
    }

    private JsonNode assertError(ResultActions action, int code) throws Exception {
        String response = action.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode body = objectMapper.readTree(response);
        Set<String> fields = new HashSet<>();
        body.fieldNames().forEachRemaining(fields::add);
        assertEquals(Set.of("code", "message", "data"), fields);
        assertEquals(code, body.get("code").asInt());
        assertTrue(body.get("message").isTextual());
        assertFalse(body.get("message").asText().isBlank());
        assertTrue(body.get("data").isNull());
        assertFalse(response.contains(PRIVATE_DETAIL));
        return body;
    }

    @RestController
    public static class FixtureController {

        @PostMapping(value = "/body", consumes = MediaType.APPLICATION_JSON_VALUE)
        public Result<Void> body(@Valid @RequestBody BodyRequest request) {
            return Result.ok();
        }

        @GetMapping("/binding")
        public Result<Void> binding(@Validated BodyRequest request) {
            return Result.ok();
        }

        @PostMapping(value = "/range", consumes = MediaType.APPLICATION_JSON_VALUE)
        public Result<Void> range(@Valid @RequestBody RangeRequest request) {
            return Result.ok();
        }

        @GetMapping("/number")
        public Result<Void> number(@RequestParam("id") Long id) {
            return Result.ok();
        }

        @GetMapping("/header")
        public Result<Void> header(@RequestHeader("X-Client") String client) {
            return Result.ok();
        }

        @PostMapping("/upload")
        public Result<Void> upload(@RequestPart("file") MultipartFile file) {
            return Result.ok();
        }

        @GetMapping("/path/{value}")
        public Result<Void> path(@PathVariable("missing") String missing) {
            return Result.ok();
        }

        @GetMapping("/broken-json")
        public BrokenJson brokenJson() {
            return new BrokenJson();
        }

        @GetMapping("/throw/{kind}")
        public Result<Void> fail(@PathVariable("kind") String kind) {
            throw switch (kind) {
                case "duplicate" -> new DuplicateKeyException(PRIVATE_DETAIL);
                case "integrity" -> new DataIntegrityViolationException(PRIVATE_DETAIL);
                case "access" -> new DataAccessResourceFailureException(PRIVATE_DETAIL);
                case "multipart" -> new MultipartException(PRIVATE_DETAIL);
                case "oversized" -> new MaxUploadSizeExceededException(1024, new IOException(PRIVATE_DETAIL));
                case "type-mismatch" -> new TypeMismatchException(PRIVATE_DETAIL, Long.class);
                case "conversion-not-supported" -> new ConversionNotSupportedException(
                        PRIVATE_DETAIL, Long.class, new IllegalStateException(PRIVATE_DETAIL));
                case "business" -> new ZZAException(208, "公寓id不存在");
                default -> new IllegalStateException(PRIVATE_DETAIL);
            };
        }
    }

    @RestController
    @Validated
    public static class MethodConstraintsController {

        @GetMapping("/method")
        public Result<Void> method(@RequestParam("count") @Min(value = 1, message = "数量至少为1") int count) {
            return Result.ok();
        }

        @GetMapping("/method-return")
        @NotNull(message = PRIVATE_DETAIL)
        public String methodReturn() {
            return null;
        }
    }

    public static class BodyRequest {

        @NotBlank(message = "姓名不能为空")
        private String name;
        private Integer age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }
    }

    @ValidRange
    public static class RangeRequest {

        private int start;
        private int end;

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = RangeValidator.class)
    public @interface ValidRange {

        String message() default "结束值不得小于开始值";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }

    public static class RangeValidator implements ConstraintValidator<ValidRange, RangeRequest> {

        @Override
        public boolean isValid(RangeRequest value, ConstraintValidatorContext context) {
            return value == null || value.getEnd() >= value.getStart();
        }
    }

    @JsonSerialize(using = BrokenJsonSerializer.class)
    public static class BrokenJson {
    }

    public static class BrokenJsonSerializer extends JsonSerializer<BrokenJson> {

        @Override
        public void serialize(BrokenJson value, JsonGenerator generator, SerializerProvider serializers)
                throws IOException {
            throw JsonMappingException.from(generator, PRIVATE_DETAIL);
        }
    }
}
