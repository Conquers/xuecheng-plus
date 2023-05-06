package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeachplanServiceImpl implements TeachplanService {

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public List<TeachplanDto> findTeachplanTree(long courseId) {
        // 所有课程id = courseId的集合（没有分树形结构）
        QueryWrapper<Teachplan> teachplanQueryWrapper = new QueryWrapper<>();
        teachplanQueryWrapper.eq("course_id", courseId);
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectList(teachplanQueryWrapper).stream().map(teachplan -> {
            TeachplanDto teachplanDto = new TeachplanDto();
            BeanUtils.copyProperties(teachplan, teachplanDto);
            return teachplanDto;
        }).collect(Collectors.toList());

        // 根据等级排序，保证一级类别在前面
        List<TeachplanDto> teachplanDtosList = teachplanDtos.stream().sorted(Comparator.comparing(Teachplan::getGrade)).collect(Collectors.toList());

        // 处理后的数据
        List<TeachplanDto> teachplanDtoList = new ArrayList<>();

        for (TeachplanDto teachplanDto : teachplanDtosList) {
            // 如果是父类别
            if (teachplanDto.getParentid() == 0) {
                teachplanDtoList.add(teachplanDto);
            }
            // 如果是子类别
            else {
                // 如果是子类别，赋值课程计划
                QueryWrapper<TeachplanMedia> teachplanMediaQueryWrapper = new QueryWrapper<>();
                TeachplanMedia teachplanMedia = teachplanMediaMapper.selectOne(teachplanMediaQueryWrapper.eq("teachplan_id",teachplanDto.getId()));
                if(teachplanMedia != null) {
                    teachplanDto.setTeachplanMedia(teachplanMedia);
                }
                for (TeachplanDto dto : teachplanDtoList) {
                    // 找到当前子类别的父类别
                    if (teachplanDto.getParentid().equals(dto.getId())) {
                        // 获取父类别的子类别
                        List<TeachplanDto> teachPlanTreeNodes = dto.getTeachPlanTreeNodes();
                        if (teachPlanTreeNodes == null) {
                            List<TeachplanDto> sun = new ArrayList<>();
                            sun.add(teachplanDto);
                            dto.setTeachPlanTreeNodes(sun);
                        } else {
                            teachPlanTreeNodes.add(teachplanDto);
                        }
                    }
                }
            }
        }
        // 父类别排序
        teachplanDtoList.sort(Comparator.comparing(Teachplan::getOrderby));
        for (TeachplanDto teachplanDto : teachplanDtoList) {
            // 子类别排序
            if (teachplanDto.getTeachPlanTreeNodes() != null) {
                teachplanDto.getTeachPlanTreeNodes().sort(Comparator.comparing(Teachplan::getOrderby));
            }
        }

        return teachplanDtoList;
    }
}
