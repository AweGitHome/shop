package com.leyou.user.pojo;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "tb_address")
@Data
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String phone;
    private String state;
    private String city;
    private String district;
    private String zipcode;
    private String address;
    private Long user_id;
}
