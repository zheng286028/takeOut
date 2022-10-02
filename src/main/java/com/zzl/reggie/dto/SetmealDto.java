package com.zzl.reggie.dto;
import com.zzl.reggie.pojo.Setmeal;
import com.zzl.reggie.pojo.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
