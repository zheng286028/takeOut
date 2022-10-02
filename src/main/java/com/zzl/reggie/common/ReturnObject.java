package com.zzl.reggie.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用返回结果，服务端响应的数据最终都会封装成此对象
 * @param <T>
 */
@Data
@ApiModel(value = "返回响应信息")
public class ReturnObject<T> implements Serializable {

    @ApiModelProperty(value = "表示成功")
    private Integer code; //编码：1成功，0和其它数字为失败
    @ApiModelProperty(value = "错误信息")
    private String msg; //错误信息
    @ApiModelProperty(value = "携带的数据")
    private T data; //数据
    @ApiModelProperty(value = "动态数据")
    private Map map = new HashMap(); //动态数据

    public static <T> ReturnObject<T> success(T object) {
        ReturnObject<T> returnObject = new ReturnObject<T>();
        returnObject.data = object;
        returnObject.code = 1;
        return returnObject;
    }

    public static <T> ReturnObject<T> error(String msg) {
        ReturnObject returnObject = new ReturnObject();
        returnObject.msg = msg;
        returnObject.code = 0;
        return returnObject;
    }

    public ReturnObject<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

}
