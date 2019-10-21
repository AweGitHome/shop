package com.leyou.search.test;

import com.leyou.LeyouSearchApplication;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuBo;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LeyouSearchApplication.class)
public class ElaticsearchTest {
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SearchService searchService;
    @Test
    public void createIndex(){
        this.elasticsearchTemplate.createIndex(Goods.class);
        this.elasticsearchTemplate.putMapping(Goods.class);
        Integer page = 1;
        Integer row = 100;

        do {
            PageResult<SpuBo> result = goodsClient.queryGoodsByPage(null, true, page, row);
            List<Goods> goodsList = result.getItems().stream().map(spuBo -> {
                try {
                    return this.searchService.buildGood(spuBo);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());
            this.goodsRepository.saveAll(goodsList);
            // 获取当前页的数据条数，如果是最后一页，没有100条
            row = result.getItems().size();
            // 每次循环页码加1
            page++;
        }while(row == 100);
    }

}
