package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {

    @Autowired
    CourseTeacherMapper courseTeacherMapper;

    @Override
    public List<CourseTeacher> getTeachersByCourseId(Long courseId) {
        QueryWrapper<CourseTeacher> courseTeacherQueryWrapper = new QueryWrapper<>();
        courseTeacherQueryWrapper.eq("course_id", courseId);
        return courseTeacherMapper.selectList(courseTeacherQueryWrapper);
    }

    @Override
    public CourseTeacher saveCourseTeacher(CourseTeacher courseTeacher) {
        if (courseTeacher.getId() == null) {
            courseTeacherMapper.insert(courseTeacher);
            return courseTeacher;
        } else {
            courseTeacherMapper.updateById(courseTeacher);
            return courseTeacher;
        }
    }

    @Override
    public void deleteTeacher(Long courseId, Long courseTeacherId) {
        QueryWrapper<CourseTeacher> courseTeacherQueryWrapper = new QueryWrapper<>();
        courseTeacherQueryWrapper.eq("course_id", courseId)
                .eq("id", courseTeacherId);

        courseTeacherMapper.delete(courseTeacherQueryWrapper);
    }
}
