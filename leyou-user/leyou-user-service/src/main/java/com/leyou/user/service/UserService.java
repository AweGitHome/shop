package com.leyou.user.service;

import com.leyou.common.utils.CodecUtils;
import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.AddressMapper;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.Address;
import com.leyou.user.pojo.User;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {
    private final UserMapper userMapper;

    private final AddressMapper addressMapper;

    private final StringRedisTemplate redisTemplate;

    private final AmqpTemplate amqpTemplate;

    private static final String KEY_PREFIX = "user:code:email:";

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public UserService(UserMapper userMapper, StringRedisTemplate redisTemplate, AmqpTemplate amqpTemplate, AddressMapper addressMapper) {
        this.userMapper = userMapper;
        this.addressMapper = addressMapper;
        this.redisTemplate = redisTemplate;
        this.amqpTemplate = amqpTemplate;
    }

    public Boolean sendVerifyCode(String email) {
        // 生成验证码
        String code = NumberUtils.generateCode(6);
        try {
            // 发送短信
            Map<String, String> msg = new HashMap<>();
            msg.put("email", email);
            msg.put("code", code);
            this.amqpTemplate.convertAndSend("awe.email.exchange", "email.verify.code", msg);
            // 将code存入redis
            this.redisTemplate.opsForValue().set(KEY_PREFIX + email, code, 5, TimeUnit.MINUTES);
            return true;
        } catch (Exception e) {
            logger.error("发送短信失败。email：{}， code：{}", email, code);
            return false;
        }
    }

    public Boolean checkData(String data, Integer type) {
        User record = new User();
        switch (type) {
            case 1:
                record.setUsername(data);
                break;
            case 2:
                record.setEmail(data);
                break;
            default:
                return null;
        }
        return this.userMapper.selectCount(record) == 0;
    }

    public Boolean register(User user, String code) {
        // 校验短信验证码
        String cacheCode = this.redisTemplate.opsForValue().get(KEY_PREFIX + user.getEmail());
        if (!StringUtils.equals(code, cacheCode)) {
            return false;
        }

        // 生成盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);

        // 对密码加密
        user.setPassword(CodecUtils.md5Hex(user.getPassword(), salt));

        // 强制设置不能指定的参数为null
        user.setId(null);
        user.setCreated(new Date());
        // 添加到数据库
        boolean b = this.userMapper.insertSelective(user) == 1;

        if(b){
            // 注册成功，删除redis中的记录
            this.redisTemplate.delete(KEY_PREFIX + user.getEmail());
        }
        return b;

    }

    public User queryUser(String username, String password) {
        // 查询
        User record = new User();
        record.setUsername(username);
        User user = this.userMapper.selectOne(record);
        // 校验用户名
        if (user == null) {
            return null;
        }
        // 校验密码
        if (!user.getPassword().equals(CodecUtils.md5Hex(password, user.getSalt()))) {
            return null;
        }
        // 用户名密码都正确
        return user;
    }

    public List<Address> getAddressListByUserId(Long uid) {
        Address address = new Address();
        address.setUser_id(uid);
        return this.addressMapper.select(address);
    }
}
