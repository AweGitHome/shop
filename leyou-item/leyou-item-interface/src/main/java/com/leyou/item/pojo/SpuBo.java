package com.leyou.item.pojo;

import lombok.Data;

import java.util.List;

@Data
public class SpuBo extends Spu {

    String cname;// 商品分类名称
    
    String bname;// 品牌名称

    SpuDetail spuDetail;// 商品详情

    List<Sku> skus;// sku列表
}