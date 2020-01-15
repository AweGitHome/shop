package com.leyou.user.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Pattern;
import java.util.Date;

@Data
@Table(name = "tb_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Length(min = 4, max = 30, message = "用户名只能在4~30位之间")
    private String username;// 用户名

    @JsonIgnore
    @Length(min = 4, max = 30, message = "密码只能在4~30位之间")
    private String password;// 密码

    @Pattern(regexp = "^[A-Za-z0-9]+([_\\.][A-Za-z0-9]+)*@([A-Za-z0-9\\-]+\\.)+[A-Za-z]{2,6}$", message = "邮箱格式不正确")
    private String email;// 电话

    private Date created;// 创建时间

    @JsonIgnore
    private String salt;// 密码的盐值
}