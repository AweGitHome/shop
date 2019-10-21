package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    public PageResult<Brand> queryBrandsByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();
        if(StringUtils.isNotBlank(key)){
            criteria.andLike("name","%"+key+"%").orEqualTo("letter",key);
        }
        //设置分页
        PageHelper.startPage(page,rows);
        //设置搜索顺序
        if (StringUtils.isNotBlank(sortBy)) {
            example.setOrderByClause(sortBy + " " + (desc ? "desc" : "asc"));
        }
        List<Brand> brands = brandMapper.selectByExample(example);
        // 包装成pageInfo
        PageInfo<Brand> pageInfo = new PageInfo<>(brands);

        return new PageResult<>(pageInfo.getTotal(),pageInfo.getList());
    }

    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        brand.setId(null);
        brandMapper.insertSelective(brand);
        cids.forEach(cid -> {
            categoryMapper.insertCategoryAndBrand(cid,brand.getId());
        });
    }

    @Transactional
    public void updateBrand(Brand brand, List<Long> cids) {
        brandMapper.updateByPrimaryKey(brand);
        cids.forEach(cid -> {
            categoryMapper.updateCategoryByNewCid(cid,brand.getId());
        });
    }

    public List<Brand> queryBrandsByCid(Long cid) {
        return this.brandMapper.selectByCid(cid);
    }

    public Brand queryById(Long bid) {
        Brand brand = new Brand();
        brand.setId(bid);
        return this.brandMapper.selectByPrimaryKey(brand);
    }
}
