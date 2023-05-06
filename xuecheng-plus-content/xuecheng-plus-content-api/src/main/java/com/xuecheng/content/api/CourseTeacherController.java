package com.xuecheng.content.api;

import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "教师信息编辑接口", tags = "教师信息编辑接口")
@RestController
public class CourseTeacherController {
    @Autowired
    CourseTeacherService courseTeacherService;

    @ApiOperation("查询教师信息接口")
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacher> list(@PathVariable Long courseId) {
        return courseTeacherService.getTeachersByCourseId(courseId);
    }

    @ApiOperation("添加/修改教师信息接口")
    @PostMapping("/courseTeacher")
    public CourseTeacher saveCourseTeacher(@RequestBody CourseTeacher courseTeacher) {
        return courseTeacherService.saveCourseTeacher(courseTeacher);
    }


    @ApiOperation("删除教师信息接口")
    @DeleteMapping("/courseTeacher/course/{courseId}/{courseTeacherId}")
    public void deleteTeacher(@PathVariable Long courseId, @PathVariable Long courseTeacherId) {
        courseTeacherService.deleteTeacher(courseId, courseTeacherId);
    }
}