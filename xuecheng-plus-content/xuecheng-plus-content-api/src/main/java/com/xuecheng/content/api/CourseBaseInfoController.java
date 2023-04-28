package com.xuecheng.content.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Api(value = "课程信息编辑接口",tags = "课程信息编辑接口")
@RestController
public class CourseBaseInfoController {

    @ApiOperation("课程查询接口")
    @RequestMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParams) {
        // 说明：pageParams分页参数通过url的key/value传入(地址栏)，queryCourseParams通过json数据传入(body)，使用@RequestBody注解将json转成QueryCourseParamsDto对象。
        List<CourseBase> courseBases = new ArrayList<>();
        CourseBase courseBase = new CourseBase();
        courseBase.setName("Java");
        courseBases.add(courseBase);
        return new PageResult<>(courseBases,2,1,2);

    }

}