package com.cnj.mybatis.log;

import com.cnj.mybatis.log.entity.User;
import com.cnj.mybatis.log.mapper.UserMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;


/**
 * @author czz
 * @since 2022/11/12 下午12:19
 */
@SpringBootTest(classes = ExampleApplication.class)
public class QueryTest {

    private static final Logger LOG = LoggerFactory.getLogger(QueryTest.class);

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Test
    public void insertUser() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession();) {
            UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
            User user = new User();
            user.setId("1");
            user.setUserName("张三");
            user.setSex(0);
            user.setCreateDate(new Date());
            user.setCreatedBy("admin");
            user.setLastUpdateDate(new Date());
            user.setLastUpdatedBy("admin");
            int result = userMapper.insert(user);
            System.out.println("result:" + result);
            System.out.println("user:" + user);
            sqlSession.commit();
        }
    }

    @Test
    public void selectById() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession();) {
            UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
            User user = userMapper.selectById("1123132ds");
            System.out.println("user:" + user);
        }
    }

    @Test
    public void selectById2() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession();) {
            UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
            User user = userMapper.selectById2("1");
            System.out.println("user:" + user);
        }
    }

    @Test
    public void selectById3() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession();) {
            UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
            User user = userMapper.selectById2("1");
            System.out.println("user:" + user);
        }
    }

    @Test
    public void selectList() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession();) {
            UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
            User user = new User();
            user.setSex(0);
            user.setUserName("张三");
            List<User> users = userMapper.selectList(user);
            System.out.println("users:" + users);
        }
    }

}
