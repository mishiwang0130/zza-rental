package com.wxy.zzarental.common.exception;

import com.wxy.zzarental.common.result.ResultCodeEnum;
import lombok.Data;

@Data
public class ZZAException extends RuntimeException {
    private Integer code;


    public ZZAException(Integer code,String message){
        super(message);
        this.code=code;
    }
    public ZZAException(ResultCodeEnum en){
        super(en.getMessage());
        this.code = en.getCode();
    }
}
