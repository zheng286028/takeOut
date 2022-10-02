package com.zzl.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzl.reggie.pojo.Employee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee>{
}
