package com.leyou.item.mapper;

import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand> {
    @Select("select b.* from tb_brand b inner join tb_category_brand cb on b.id = cb.brand_id where cb.category_id = #{cid}")
    List<Brand> selectByCid(Long cid);
}
