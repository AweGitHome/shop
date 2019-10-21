package com.leyou.item.controller;

import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("spec")
public class SpecificationController {
    @Autowired
    private SpecificationService specificationService;

    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryByCategoryId(@PathVariable("cid")Long cid){
        if(cid == null || cid < 0 ){
            return ResponseEntity.badRequest().build();
        }
        List<SpecGroup> specGroups = specificationService.queryByCategoryId(cid);
        if(CollectionUtils.isEmpty(specGroups)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(specGroups);
    }
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> queryParams(
            @RequestParam(value = "gid", required = false)Long gid,
            @RequestParam(value = "cid", required = false)Long cid,
            @RequestParam(value = "generic", required = false)Boolean generic,
            @RequestParam(value = "searching", required = false)Boolean searching
    ){
        List<SpecParam> params = specificationService.queryParams(gid,cid,generic,searching);
        if(CollectionUtils.isEmpty(params)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(params);
    }

    @PostMapping("group")
    public ResponseEntity<Void> saveSpecGroup(SpecGroup specGroup){
        if (specGroup == null){
            return ResponseEntity.badRequest().build();
        }
        this.specificationService.saveSpecGroup(specGroup);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("param")
    public ResponseEntity<Void> saveSpecParam(SpecParam specParam){
        if (specParam == null){
            return ResponseEntity.badRequest().build();
        }
        this.specificationService.saveSpecParam(specParam);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("param")
    public ResponseEntity<Void> updateSpecParam(SpecParam specParam){
        if (specParam == null){
            return ResponseEntity.badRequest().build();
        }
        this.specificationService.updateSpecParam(specParam);
        return ResponseEntity.noContent().build();
    }
}
