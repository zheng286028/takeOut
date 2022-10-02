package com.zzl.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzl.reggie.pojo.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 功能描述
 *
 * @author 郑子浪
 * @date 2022/05/31  10:51
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
