package com.cnj.mybatis.log.example;

import com.cnj.mybatis.log.bytebuddy.MybatisLogInstaller;
import com.cnj.mybatis.log.example.entity.User;
import com.cnj.mybatis.log.example.mapper.UserMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author czz
 * @since 2022/11/12 下午4:33
 */
@SpringBootApplication
@MapperScan(basePackages = "com.cnj.mybatis.log.example.mapper")
@RestController
public class ExampleApplication {

    @Autowired
    UserMapper userMapper;

    public static void main(String[] args) {
        MybatisLogInstaller.install();
        SpringApplication.run(ExampleApplication.class, args);
    }

    @GetMapping("/selectUserById")
    public User queryUserList(String userId) {
        return userMapper.selectById(userId);
    }

}
