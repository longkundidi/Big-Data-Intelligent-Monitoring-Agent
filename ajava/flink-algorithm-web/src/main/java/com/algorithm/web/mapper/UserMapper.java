// package com.algorithm.web.mapper;
//
// import com.algorithm.web.model.entity.User;
// import com.algorithm.web.model.page.PageParam;
// import com.github.pagehelper.Page;
//
// import org.apache.ibatis.annotations.Param;
// import org.springframework.stereotype.Repository;
//
// import java.util.List;
//
// @Repository
// public interface UserMapper {
//
// User selectByUsername(@Param("username") String username);
//
// User selectByUserId(@Param("userid") Integer userid);
//
// int insert(User user);
//
// int updateByPrimaryKeySelective(User user);
//
// int updateByUserIdSelective(User user);
//
// List<User> findAll();
//
// Page<User> queryAllByPage(PageParam pageparam);
//
// }
