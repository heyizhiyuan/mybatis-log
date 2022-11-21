package com.cnj.mybatis.log.example.mapper;

import com.cnj.mybatis.log.example.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author czz
 * @since 2022/11/12 下午12:44
 */
@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE id=#{id}")
    User selectById(@Param("id") String id);

    @Select("SELECT * FROM user WHERE id=${id}")
    User selectById2(@Param("id") String id);

    @Select("SELECT * FROM user WHERE sex=#{sex} AND user_name=#{userName}")
    List<User> selectList(User user);

    @Insert(
            "INSERT INTO user(id, user_name, sex, create_date, created_by, last_update_date, last_updated_by) " +
                    "VALUES (#{id}, #{userName}, #{sex}, #{createDate}, #{createdBy}, #{lastUpdateDate}, #{lastUpdatedBy})"
    )
    int insert(User user);

}
