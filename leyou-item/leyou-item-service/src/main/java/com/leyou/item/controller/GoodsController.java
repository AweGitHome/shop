package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class GoodsController {
    @Autowired
    private GoodsService goodsService;

    @GetMapping("spu/page")
    public ResponseEntity<PageResult<SpuBo>> queryGoodsByPage(
            @RequestParam(name = "key",required = false) String key,
            @RequestParam(value = "saleable", required = false)Boolean saleable,
            @RequestParam(value = "page", defaultValue = "1")Integer page,
            @RequestParam(value = "rows", defaultValue = "5")Integer rows
    ){
        PageResult<SpuBo> result = goodsService.queryGoodsByPage(key,saleable,page,rows);
        if (CollectionUtils.isEmpty(result.getItems())) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("goods")
    public ResponseEntity<Void> saveGood(@RequestBody SpuBo spuBo){
        if (spuBo == null ){
            return ResponseEntity.badRequest().build();
        }
        this.goodsService.saveGood(spuBo);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("goods")
    public ResponseEntity<Void> updateGood(@RequestBody SpuBo spuBo){
        if (spuBo == null ){
            return ResponseEntity.badRequest().build();
        }
        this.goodsService.updateGood(spuBo);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("spu/detail/{spid}")
    public ResponseEntity<SpuDetail> querySpuDetailBySpId(@PathVariable("spid") Long spid){
        if (spid == null || spid == 0){
            return ResponseEntity.badRequest().build();
        }
        SpuDetail spuDetail = this.goodsService.querySpuDetailBySpid(spid);
        if (spuDetail == null){
            return ResponseEntity.notFound().build();
        }else{
            return ResponseEntity.ok(spuDetail);
        }
    }

    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkusBySpId(@RequestParam Long id){
        if (id == null || id == 0){
            return ResponseEntity.badRequest().build();
        }
        List<Sku> skus = this.goodsService.querySkusBySpid(id);
        if (CollectionUtils.isEmpty(skus)){
            return ResponseEntity.notFound().build();
        }else{
            return ResponseEntity.ok(skus);
        }
    }


}
