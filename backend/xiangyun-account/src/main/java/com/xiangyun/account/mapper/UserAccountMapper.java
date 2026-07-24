package com.xiangyun.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiangyun.account.entity.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface UserAccountMapper extends BaseMapper<UserAccount> {

    @Select("SELECT * FROM user_account WHERE username = #{username}")
    Optional<UserAccount> findByUsername(String username);

    @Select("SELECT * FROM user_account WHERE phone = #{phone}")
    Optional<UserAccount> findByPhone(String phone);
}
