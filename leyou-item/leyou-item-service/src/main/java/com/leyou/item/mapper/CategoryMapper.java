package com.leyou.item.mapper;

import com.leyou.item.pojo.Category;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CategoryMapper extends Mapper<Category> {


    @Insert("insert into tb_category_brand(category_id, brand_id) values(#{cid},#{bid})")
    void insertCategoryAndBrand(Long cid,Long bid);

    @Select("SELECT * FROM tb_category WHERE id IN (SELECT category_id FROM tb_category_brand WHERE brand_id = #{bid})")
    List<Category> queryByBrandId(Long bid);

    @Update("update tb_category_brand set category_id = #{cid} where brand_id = #{bid}")
    void updateCategoryByNewCid(Long cid,Long bid);
}
