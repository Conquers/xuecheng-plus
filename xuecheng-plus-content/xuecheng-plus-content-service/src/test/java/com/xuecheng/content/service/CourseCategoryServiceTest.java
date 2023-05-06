package com.xuecheng.content.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CourseCategoryServiceTest {

    @Autowired
    CourseCategoryService courseCategoryService;


    @Test
    void queryTreeNodes() {
        courseCategoryService.queryTreeNodes("1");
    }
}