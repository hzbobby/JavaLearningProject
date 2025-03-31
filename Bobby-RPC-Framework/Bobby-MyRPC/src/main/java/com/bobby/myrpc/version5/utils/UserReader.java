package com.bobby.myrpc.version5.utils;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.bobby.myrpc.version5.domain.User;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author: Bobby
 * @email: vividbobby@163.com
 * @date: 2025/3/28
 */
public class UserReader implements ObjectReader<User> {
    @Override
    public User readObject(JSONReader jsonReader, Type type, Object o, long l) {
        if (jsonReader.nextIfNull()) {
            return null;
        }
        // readObject方法：将json字符串转化成K-V形式
        Map<String, Object> objectMap = jsonReader.readObject();
        // 新建一个目标类A，作为返回的结果
//        User user = BeanUtil.fillBeanWithMap(objectMap, User.class, true);
        User user = User.builder().build();

        return user;
    }
}
