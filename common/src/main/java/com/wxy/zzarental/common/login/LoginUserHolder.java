package com.wxy.zzarental.common.login;

public class LoginUserHolder {

    private static final ThreadLocal<LoginUser> threadLocal = new ThreadLocal<>();
    public static void setThreadLocal(LoginUser loginUser){
        threadLocal.set(loginUser);
    }
    public static LoginUser getLoginUser(){
        return threadLocal.get();
    }
    public static void clear(){
        threadLocal.remove();
    }

}
