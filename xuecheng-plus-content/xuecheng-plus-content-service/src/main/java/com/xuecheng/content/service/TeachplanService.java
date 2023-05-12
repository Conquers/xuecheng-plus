package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.TeachplanMedia;

import java.util.List;

public interface TeachplanService {
    List<TeachplanDto> findTeachplanTree(long courseId);

    void saveTeachplan(SaveTeachplanDto teachplanDto);

    void delTeachplan(long teachplanId);

    void moveTeachplan(String moveType, long teachplanId);

    /**
     * @param *bindTeachplanMediaDto*
     * @return com.xuecheng.content.model.po.TeachplanMedia
     * @description 教学计划绑定媒资
     * @author Mr.M
     * @date 2022/9/14 22:20
     */
    TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

    void unbindMedia(Long teachPlanId, String mediaId);
}
