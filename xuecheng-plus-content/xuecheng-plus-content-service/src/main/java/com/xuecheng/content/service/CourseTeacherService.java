package com.xuecheng.content.service;

import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

public interface CourseTeacherService {
    List<CourseTeacher> getTeachersByCourseId(Long courseId);

    CourseTeacher saveCourseTeacher(CourseTeacher courseTeacher);

    void deleteTeacher(Long courseId, Long courseTeacherId);
}
