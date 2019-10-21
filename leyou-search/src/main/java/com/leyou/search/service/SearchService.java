package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class SearchService {
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private SpecificationClient specificationClient;
    @Autowired
    private GoodsRepository goodsRepository;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public Goods buildGood(Spu spu) throws IOException {
        Goods goods = new Goods();
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setId(spu.getId());
        goods.setSubTitle(spu.getSubTitle());

        //根据spuid查询出所有有关sku，用于构造所需的skuMapList
        List<Sku> skus = goodsClient.querySkusBySpId(spu.getId());
        //根据spu的三个类别id查询出类别名称，用于构造搜索字段all。
        List<String> names = categoryClient.queryNameByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        //根据spuid查询出品牌名字，用于构造搜索字段all
        Brand brand = brandClient.queryById(spu.getBrandId());
        //创建价格列表，代表各个sku的具体价格
        List<Long> prices = new ArrayList<>();
        //创建skuMapList，代表各个sku商品的信息
        List<Map<String, Object>> skuMapList = new ArrayList<>();
        //遍历skus，将获取到的sku信息填入skuMapList中，并形成价格列表
        skus.forEach(sku -> {
            prices.add(sku.getPrice());
            Map<String, Object> skuMap = new HashMap<>();
            skuMap.put("id", sku.getId());
            skuMap.put("title", sku.getTitle());
            skuMap.put("price", sku.getPrice());
            skuMap.put("image", StringUtils.isNotBlank(sku.getImages()) ? StringUtils.split(sku.getImages(), ",")[0] : "");
            skuMapList.add(skuMap);
        });
        //根据spu的最小类别，查询出所有可用于搜索的参数。
        List<SpecParam> params = specificationClient.queryParams(null, spu.getCid3(), null, true);
        //根据spuid查询出该商品的详情
        SpuDetail spuDetail = goodsClient.querySpuDetailBySpId(spu.getId());
        //利用商品详情获取的通用参数键值对，构造成一个代表通用参数的Map集合。
        Map<Long,Object> genericSpecMap = MAPPER.readValue(spuDetail.getGenericSpec(),new TypeReference<Map<Long,Object>>(){});
        //同上，构造出一个代表特殊参数的Map集合。
        Map<Long, List<Object>> specialSpecMap = MAPPER.readValue(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<Object>>>(){});
        //构造一个代表真正参数键值对的Map集合。
        Map<String,Object> paramMap = new HashMap<>();
        //遍历获取到的所有属于可用于搜索的属性，给参数map赋值
        params.forEach(specParam -> {
            //判断是通用参数还是特殊参数
            if (specParam.getGeneric()){
                //通过该参数的id获取该参数对应的值。
                String value = genericSpecMap.get(specParam.getId()).toString();
                //判断是否是数字参数,若是，加上搜索区间段
                if (specParam.getNumeric()){
                    value = chooseSegment(value,specParam);
                }
                //将该通用参数名和值加入参数Map集合中
                paramMap.put(specParam.getName(),value);
            }else{
                //将该特殊参数名和值加入参数Map集合中
                paramMap.put(specParam.getName(),specialSpecMap.get(specParam.getId()));
            }
        });
        //赋值
        goods.setAll(spu.getTitle() + " " + StringUtils.join(names," ")+ " "+ brand.getName());
        goods.setPrice(prices);
        goods.setSkus(MAPPER.writeValueAsString(skuMapList));
        goods.setSpecs(paramMap);
        return goods;
    }
    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public SearchResult search(SearchRequest request) {
        String key = request.getKey();
        if (StringUtils.isBlank(key)) {
            return null;
        }
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 添加查询条件
        MatchQueryBuilder basicQuery = QueryBuilders.matchQuery("all", request.getKey()).operator(Operator.AND);
        queryBuilder.withQuery(basicQuery);
        //添加结果集过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","skus","subTitle"},null));
        //添加分页条件
        queryBuilder.withPageable(PageRequest.of(request.getPage()-1,request.getSize()));
        //添加排序条件
        queryBuilder.withSort(SortBuilders.fieldSort(request.getSortBy()).order(request.getDescending()? SortOrder.DESC:SortOrder.ASC));

        String categoryAggName = "categories";
        String brandAggName = "brand";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));
        //执行查询
        AggregatedPage<Goods> goodsPage = (AggregatedPage<Goods>)this.goodsRepository.search(queryBuilder.build());
        List<Map<String, Object>> categoryAggResult = getCategoryAggResult(goodsPage.getAggregation(categoryAggName));
        List<Brand> brandAggResult = getBrandAggResult(goodsPage.getAggregation(brandAggName));
        List<Map<String, Object>> specs = null;
        if (categoryAggResult.size() == 1){
            specs = getParamAggResult((Long)categoryAggResult.get(0).get("id"), basicQuery);
        }

        //获取总条数
        Long totalElements = goodsPage.getTotalElements();

        // 封装结果并返回
        return new SearchResult(goodsPage.getContent(),totalElements, goodsPage.getTotalPages(),categoryAggResult,brandAggResult,specs);
    }
    /**
     * 解析分类
     * @param aggregation
     * @return
     */
    private List<Map<String,Object>> getCategoryAggResult(Aggregation aggregation) {
        LongTerms longTerms = (LongTerms)aggregation;
        List<LongTerms.Bucket> buckets = longTerms.getBuckets();
        // 定义一个分类集合，搜集所有的分类对象
        List<Map<String, Object>> categories = new ArrayList<>();
        List<Long> cids = new ArrayList<>();
        //遍历桶获取cids列表
        buckets.forEach(bucket -> {
            cids.add(bucket.getKeyAsNumber().longValue());
        });
        //利用cids列表查询出对于的类别名称
        List<String> names = this.categoryClient.queryNameByIds(cids);
        //给分类集合赋值
        for (int i = 0; i < cids.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", cids.get(i));
            map.put("name", names.get(i));
            categories.add(map);
        }
        return categories;
    }
    /**
     * 解析品牌聚合结果集
     * @param aggregation
     * @return
     */
    private List<Brand> getBrandAggResult(Aggregation aggregation) {
        // 处理聚合结果集
        LongTerms terms = (LongTerms)aggregation;
        // 获取所有的品牌id桶
        List<LongTerms.Bucket> buckets = terms.getBuckets();
        // 定义一个品牌集合，搜集所有的品牌对象
        List<Brand> brands = new ArrayList<>();
        // 解析所有的id桶，查询品牌
        buckets.forEach(bucket -> {
            Brand brand = this.brandClient.queryById(bucket.getKeyAsNumber().longValue());
            brands.add(brand);
        });
        return brands;
    }
    private List<Map<String,Object>> getParamAggResult(Long id, QueryBuilder basicQuery) {

        // 创建自定义查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 基于基本的查询条件，聚合规格参数
        queryBuilder.withQuery(basicQuery);
        // 查询要聚合的规格参数
        List<SpecParam> params = this.specificationClient.queryParams(null, id, null, true);
        // 添加聚合
        params.forEach(param -> {
            queryBuilder.addAggregation(AggregationBuilders.terms(param.getName()).field("specs." + param.getName() + ".keyword"));
        });
        // 只需要聚合结果集，不需要查询结果集
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{}, null));

        // 执行聚合查询
        AggregatedPage<Goods> goodsPage = (AggregatedPage<Goods>)this.goodsRepository.search(queryBuilder.build());

        // 定义一个集合，收集聚合结果集
        List<Map<String, Object>> paramMapList = new ArrayList<>();
        // 解析聚合查询的结果集
        Map<String, Aggregation> aggregationMap = goodsPage.getAggregations().asMap();
        for (Map.Entry<String, Aggregation> entry : aggregationMap.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            // 放入规格参数名
            map.put("k", entry.getKey());
            // 收集规格参数值
            List<Object> options = new ArrayList<>();
            // 解析每个聚合
            StringTerms terms = (StringTerms)entry.getValue();
            // 遍历每个聚合中桶，把桶中key放入收集规格参数的集合中
            terms.getBuckets().forEach(bucket -> options.add(bucket.getKeyAsString()));
            map.put("options", options);
            paramMapList.add(map);
        }

        return paramMapList;
    }
}
