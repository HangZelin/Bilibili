package com.imooc.bilibili.domain;

public class JsonResponse<T> {

    private String code;

    private String msg;

    private T data;

    public JsonResponse(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public JsonResponse(T data) {
        this.data = data;
        msg = "success";
        code = "0";
    }

    // 一些不需要返回给前端，但是请求成功的情况。
    public static JsonResponse<String> success() {
        return new JsonResponse<>(null);
    }

    // 需要给前端返回一些参数，而且是字符串类型。
    public static JsonResponse<String> success(String data) {
        return new JsonResponse<>(data);
    }

    // 请求发送失败的情况。
    public static JsonResponse<String> fail() {
        return new JsonResponse<>("1", "fail");
    }

    // 需要返回特定状态码和提示信息的情况。
    public static JsonResponse<String> fail(String code, String msg) {
        return new JsonResponse<>(code, msg);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
