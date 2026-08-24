package com.wxy.zzarental.common.exception;

import lombok.Data;

@Data
public class ZZAException extends RuntimeException {
    private Integer code;


    public ZZAException(Integer code,String message){
        super(message);
        this.code=code;
    }
}
