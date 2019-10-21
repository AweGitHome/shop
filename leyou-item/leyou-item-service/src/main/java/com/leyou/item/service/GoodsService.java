package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsService {
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private StockMapper stockMapper;

    /**
     *
     * @param key 根据标题搜索
     * @param saleable 上下架
     * @param page
     * @param rows
     * @return
     */
    public PageResult<SpuBo> queryGoodsByPage(String key, Boolean saleable, Integer page, Integer rows) {
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        // 搜索条件
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }
        //添加分页条件
        PageHelper.startPage(page,rows);
        //查询
        List<Spu> spus = spuMapper.selectByExample(example);
        //获取分页信息
        PageInfo<Spu> spuPageInfo = new PageInfo<>(spus);
        List<SpuBo> spuBos = new ArrayList<>();
        //遍历赋值
        spus.forEach(spu -> {
            SpuBo spuBo = new SpuBo();
            BeanUtils.copyProperties(spu,spuBo);
            List<String> names = categoryService.queryCategoriesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            spuBo.setCname(StringUtils.join(names,"/"));
            Brand brand = brandMapper.selectByPrimaryKey(spu.getBrandId());
            spuBo.setBname(brand.getName());
            spuBos.add(spuBo);
        });
        return new PageResult<>(spuPageInfo.getTotal(),spuBos);
    }

    @Transactional
    public void saveGood(SpuBo spuBo) {
        //新增spu
        spuBo.setId(null);
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setCreateTime(new Date());
        spuBo.setLastUpdateTime(spuBo.getCreateTime());
        this.spuMapper.insertSelective(spuBo);
        //新增spu_detail
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId());
        spuDetailMapper.insertSelective(spuDetail);
        //新增Sku和库存
        saveSkuAndStock(spuBo);
    }

    private void saveSkuAndStock(SpuBo spuBo) {
        spuBo.getSkus().forEach(sku -> {
            //新增SKU
            sku.setSpuId(spuBo.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            skuMapper.insertSelective(sku);
            //新增库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insertSelective(stock);
        });
    }

    public SpuDetail querySpuDetailBySpid(Long spid) {
        SpuDetail spuDetail = new SpuDetail();
        spuDetail.setSpuId(spid);
        return this.spuDetailMapper.selectOne(spuDetail);
    }

    public List<Sku> querySkusBySpid(Long id) {
        Sku sku = new Sku();
        sku.setSpuId(id);
        List<Sku> skus = this.skuMapper.select(sku);
        skus.forEach(s->{
            Stock stock = this.stockMapper.selectByPrimaryKey(s.getId());
            s.setStock(stock.getStock());
        });
        return skus;
    }

    @Transactional
    public void updateGood(SpuBo spu) {
        List<Sku> skus = this.querySkusBySpid(spu.getId());
        if (!CollectionUtils.isEmpty(skus)){
            //删除sku库存
            List<Long> ids = skus.stream().map(sku -> sku.getId()).collect(Collectors.toList());
            Example example = new Example(Stock.class);
            example.createCriteria().andIn("skuId",ids);
            this.stockMapper.deleteByExample(example);
            //删除spuid下的所有sku
            Sku sku = new Sku();
            sku.setSpuId(spu.getId());
            this.skuMapper.delete(sku);
        }

        saveSkuAndStock(spu);

        spu.setLastUpdateTime(new Date());
        spu.setCreateTime(null);
        spu.setValid(null);
        spu.setSaleable(null);
        this.spuMapper.updateByPrimaryKeySelective(spu);

        this.spuDetailMapper.updateByPrimaryKey(spu.getSpuDetail());
    }
}
