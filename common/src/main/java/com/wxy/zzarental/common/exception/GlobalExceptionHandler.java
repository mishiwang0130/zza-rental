package com.wxy.zzarental.common.exception;

import com.wxy.zzarental.common.result.Result;
import com.wxy.zzarental.common.result.ResultCodeEnum;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * MVC 接口统一异常处理。保留现有 HTTP 200 + Result 业务码的返回约定。
 * 参数错误只返回字段和校验提示；系统错误的内部细节仅记录到服务端日志。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ZZAException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(ZZAException e) {
        log.warn("业务异常，code: {}，message: {}", e.getCode(), e.getMessage());
        return json(Result.fail(e.getCode(), e.getMessage()));
    }

    /** @Valid / @Validated 请求体校验，以及查询参数、表单对象的绑定和校验。 */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<Result<Void>> handleValidationException(BindException e) {
        String message = joinMessages(e.getBindingResult().getAllErrors().stream()
                .map(this::validationMessage));
        return requestFailure(e, ResultCodeEnum.PARAM_ERROR, message);
    }

    /** 类上启用 @Validated 后的方法参数约束校验；返回值校验失败属于服务端错误。 */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        boolean returnValueViolation = e.getConstraintViolations().stream()
                .anyMatch(violation -> StreamSupport.stream(violation.getPropertyPath().spliterator(), false)
                        .anyMatch(node -> node.getKind() == ElementKind.RETURN_VALUE));
        if (returnValueViolation) {
            return serverFailure(e, "服务返回数据异常，请稍后重试");
        }
        String message = joinMessages(e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage()));
        return requestFailure(e, ResultCodeEnum.PARAM_ERROR, message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<Void>> handleMissingParameterException(MissingServletRequestParameterException e) {
        return requestFailure(e, ResultCodeEnum.PARAM_ERROR, "缺少必填参数: " + e.getParameterName());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Result<Void>> handleMissingHeaderException(MissingRequestHeaderException e) {
        return requestFailure(e, ResultCodeEnum.PARAM_ERROR, "缺少必填请求头: " + e.getHeaderName());
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Result<Void>> handleMissingPartException(MissingServletRequestPartException e) {
        return requestFailure(e, ResultCodeEnum.PARAM_ERROR, "缺少上传文件或请求部分: " + e.getRequestPartName());
    }

    /** 路由模板与 @PathVariable 不匹配通常是代码配置问题，不应归因于客户端。 */
    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<Result<Void>> handleMissingPathVariableException(MissingPathVariableException e) {
        return serverFailure(e, "接口路径配置异常，请联系管理员");
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<Result<Void>> handleRequestBindingException(ServletRequestBindingException e) {
        return requestFailure(e, ResultCodeEnum.PARAM_ERROR, "请求参数绑定失败，请检查参数和请求头");
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<Result<Void>> handleTypeMismatchException(TypeMismatchException e) {
        String name = e instanceof MethodArgumentTypeMismatchException mismatch
                ? mismatch.getName() : e.getPropertyName();
        String message = StringUtils.hasText(name) ? "参数 " + name + " 类型或格式不正确" : "参数类型或格式不正确";
        return requestFailure(e, ResultCodeEnum.PARAM_ERROR, message);
    }

    @ExceptionHandler(ConversionNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleConversionNotSupportedException(ConversionNotSupportedException e) {
        return serverFailure(e, "参数转换配置异常，请联系管理员");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleMessageNotReadableException(HttpMessageNotReadableException e) {
        return requestFailure(e, ResultCodeEnum.PARAM_ERROR,
                "请求体不能为空且必须是合法 JSON，请检查字段类型和日期格式");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        String[] supportedMethods = e.getSupportedMethods();
        String message = "不支持该请求方式";
        if (supportedMethods != null && supportedMethods.length > 0) {
            message += "，请使用 " + String.join("、", supportedMethods);
        }
        return requestFailure(e, ResultCodeEnum.ILLEGAL_REQUEST, message);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        String message = "不支持该请求 Content-Type";
        if (!e.getSupportedMediaTypes().isEmpty()) {
            message += "，支持的类型: " + e.getSupportedMediaTypes().stream()
                    .map(MediaType::toString).collect(Collectors.joining("、"));
        }
        return requestFailure(e, ResultCodeEnum.ILLEGAL_REQUEST, message);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<Result<Void>> handleMediaTypeNotAcceptableException(HttpMediaTypeNotAcceptableException e) {
        return requestFailure(e, ResultCodeEnum.ILLEGAL_REQUEST, "不支持该 Accept 类型，请使用 application/json");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Result<Void>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        return requestFailure(e, ResultCodeEnum.PARAM_ERROR, "上传文件大小超出限制");
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Result<Void>> handleMultipartException(MultipartException e) {
        return requestFailure(e, ResultCodeEnum.PARAM_ERROR, "文件上传请求解析失败，请使用正确的 multipart/form-data 格式");
    }

    /** 仅处理 DispatcherServlet 已抛出的异常；不修改静态资源和 404 路由配置。 */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Result<Void>> handleNoHandlerFoundException(NoHandlerFoundException e) {
        return requestFailure(e, ResultCodeEnum.ILLEGAL_REQUEST, "请求的接口不存在");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Result<Void>> handleDuplicateKeyException(DuplicateKeyException e) {
        return requestFailure(e, ResultCodeEnum.DATA_ERROR, "数据已存在，请勿重复提交");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Result<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("数据完整性约束异常", e);
        return json(Result.fail(ResultCodeEnum.DATA_ERROR.getCode(), "数据不符合约束或存在关联数据，无法完成操作"));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Result<Void>> handleDataAccessException(DataAccessException e) {
        return serverFailure(e, "数据服务暂时不可用，请稍后重试");
    }

    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ResponseEntity<Result<Void>> handleMessageNotWritableException(HttpMessageNotWritableException e) {
        return serverFailure(e, "响应数据处理失败，请稍后重试");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        log.error("未处理的系统异常", e);
        return json(Result.fail());
    }

    private String validationMessage(ObjectError error) {
        String message = StringUtils.hasText(error.getDefaultMessage())
                ? error.getDefaultMessage() : ResultCodeEnum.PARAM_ERROR.getMessage();
        if (error instanceof FieldError fieldError) {
            // Spring 默认的类型转换错误消息可能包含 rejectedValue，不直接返回给前端。
            if (fieldError.isBindingFailure()) {
                message = "类型或格式不正确";
            }
            return fieldError.getField() + ": " + message;
        }
        return message;
    }

    private String joinMessages(Stream<String> messages) {
        String message = messages.filter(StringUtils::hasText).distinct().sorted()
                .collect(Collectors.joining("；"));
        return StringUtils.hasText(message) ? message : ResultCodeEnum.PARAM_ERROR.getMessage();
    }

    private ResponseEntity<Result<Void>> requestFailure(Exception e, ResultCodeEnum code, String message) {
        log.warn("请求处理失败 [{}]: {}", e.getClass().getSimpleName(), message);
        return json(Result.fail(code.getCode(), message));
    }

    private ResponseEntity<Result<Void>> serverFailure(Exception e, String message) {
        log.error("服务处理失败 [{}]", e.getClass().getSimpleName(), e);
        return json(Result.fail(ResultCodeEnum.SERVICE_ERROR.getCode(), message));
    }

    private ResponseEntity<Result<Void>> json(Result<Void> result) {
        // 所有错误都显式返回 JSON，避免错误的 Accept 让异常处理本身再次触发内容协商异常。
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result);
    }
}
