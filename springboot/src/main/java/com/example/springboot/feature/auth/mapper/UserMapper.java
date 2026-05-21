package com.example.springboot.feature.auth.mapper;

import com.example.springboot.feature.auth.entity.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {

    User selectByEmail(@Param("email") String email);

    User selectById(@Param("id") Long id);

    int insert(User user);
}
