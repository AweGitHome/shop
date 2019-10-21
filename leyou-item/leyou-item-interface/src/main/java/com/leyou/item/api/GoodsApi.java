package com.leyou.item.api;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.SpuBo;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GoodsApi {

    @GetMapping("spu/page")
     PageResult<SpuBo> queryGoodsByPage(
            @RequestParam(name = "key",required = false) String key,
            @RequestParam(value = "saleable", required = false)Boolean saleable,
            @RequestParam(value = "page", defaultValue = "1")Integer page,
            @RequestParam(value = "rows", defaultValue = "5")Integer rows
    );

    @GetMapping("spu/detail/{spid}")
     SpuDetail querySpuDetailBySpId(@PathVariable("spid") Long spid);

    @GetMapping("sku/list")
     List<Sku> querySkusBySpId(@RequestParam Long id);

}
