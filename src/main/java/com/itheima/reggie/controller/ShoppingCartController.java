package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;



    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
//        String key="shoppingCart_"+BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> lqw=new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());//BaseContext.getCurrent的是当前用户User的id
        lqw.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list=shoppingCartService.list(lqw);
        return R.success(list);
    }


    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
//        String key = "shoppingCart_"+BaseContext.getCurrentId();
        Long currentId=BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);//userId
        LambdaQueryWrapper<ShoppingCart> lqw=new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,currentId);
        Long dishId=shoppingCart.getDishId();
        if(dishId!=null) lqw.eq(ShoppingCart::getDishId,dishId);//是菜品id
        else lqw.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());//是套餐id
        ShoppingCart sc=shoppingCartService.getOne(lqw);
        if (sc != null) {//有相同数据了,那就加number
            sc.setNumber(sc.getNumber() + 1);//加一
            shoppingCartService.updateById(sc);
        } else {//没有数据，那就直接创建
            //这里不用sc的原因就是因为它一点数据都没有，如果用的话还要拷贝shoppingCart里面的信息过来
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            sc = shoppingCart;
        }
        return R.success(sc);//这里直接传sc方便一些，因为处理的都是sc
    }


    /**
     * 减少购物车某个商品的所有选择
     *
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart shoppingCart) {
//        String key = "shoppingCart_"+BaseContext.getCurrentId();
        log.info("减少购物车东西");
        //amount-1，如果等于0.去掉
//        Long setmealId = shoppingCart.getSetmealId();
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(shoppingCart.getSetmealId() != null, ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        lqw.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
//        ShoppingCart sc = shoppingCartService.getOne(lqw);
//        shoppingCartService.get
//        sc.setNumber(sc.getNumber()-1);
//        shoppingCartService.updateById(sc);
//        if(sc.getNumber()==0){
//            shoppingCartService.remove(lqw);
//        }

        shoppingCartService.remove(lqw);
        return R.success("减少成功");
    }


    @DeleteMapping("/clean")
    public R<String> delete() {
        String key = "shoppingCart_" + BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        shoppingCartService.remove(lqw);
        return R.success("删除成功");
    }


}
