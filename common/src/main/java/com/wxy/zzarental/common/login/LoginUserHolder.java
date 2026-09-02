package com.wxy.zzarental.common.login;

public class LoginUserHolder {

    public static ThreadLocal<LoginUser> threadLocal = new ThreadLocal<>();
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
