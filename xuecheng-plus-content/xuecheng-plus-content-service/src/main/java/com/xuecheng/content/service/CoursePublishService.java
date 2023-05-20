package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CoursePreviewDto;

/**
 * @author Mr.M
 * @version 1.0
 * @description 课程预览、发布接口
 * @date 2022/9/16 14:59
 */
public interface CoursePublishService {

    /**
     * @param courseId 课程id
     * @return com.xuecheng.content.model.dto.CoursePreviewDto
     * @description 获取课程预览信息
     * @author Mr.M
     * @date 2022/9/16 15:36
     */
    CoursePreviewDto getCoursePreviewInfo(Long courseId);


    /**
     * @param courseId 课程id
     * @return void
     * @description 提交审核
     * @author Mr.M
     * @date 2022/9/18 10:31
     */
    void commitAudit(Long companyId, Long courseId);

    /**
     * @param companyId 机构id
     * @param courseId  课程id
     * @return void
     * @description 课程发布接口
     * @author Mr.M
     * @date 2022/9/20 16:23
     */
    void publish(Long companyId, Long courseId);
}
