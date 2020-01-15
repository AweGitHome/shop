package com.leyou.auth.service;

import com.leyou.auth.client.UserClient;
import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserClient userClient;

    private final JwtProperties properties;

    @Autowired
    public AuthService(UserClient userClient, JwtProperties properties) {
        this.userClient = userClient;
        this.properties = properties;
    }

    public String authentication(String username, String password) {

        try {
            // 调用微服务，执行查询
            User user = this.userClient.queryUser(username, password);

            // 如果查询结果为null，则直接返回null
            if (user == null) {
                return null;
            }

            // 如果有查询结果，则生成token
            return JwtUtils.generateToken(new UserInfo(user.getId(), user.getUsername()),
                    properties.getPrivateKey(), properties.getExpire());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}