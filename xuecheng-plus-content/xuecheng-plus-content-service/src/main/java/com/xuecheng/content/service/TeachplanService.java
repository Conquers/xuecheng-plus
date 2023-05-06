package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;

import java.util.List;

public interface TeachplanService {
    List<TeachplanDto> findTeachplanTree(long courseId);

    void saveTeachplan(SaveTeachplanDto teachplanDto);

    void delTeachplan(long teachplanId);
}
