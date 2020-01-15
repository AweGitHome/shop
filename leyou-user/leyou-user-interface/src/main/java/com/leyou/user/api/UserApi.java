package com.leyou.user.api;

import com.leyou.user.pojo.Address;
import com.leyou.user.pojo.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface UserApi {

    @GetMapping("query")
    User queryUser(
            @RequestParam("username") String username,
            @RequestParam("password") String password);

    @GetMapping("addressList/{uid}")
    List<Address> getAddressList(@PathVariable("uid") Long uid);
}