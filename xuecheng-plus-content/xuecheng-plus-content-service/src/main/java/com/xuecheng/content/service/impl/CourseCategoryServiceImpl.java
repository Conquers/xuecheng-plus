package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        // 返回结果集
        List<CourseCategoryTreeDto> categoryTreeDtoList = new ArrayList<>();
        // 所有未分类的结果(根据id排序，这样父节点一定在子节点前面)
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectList(null).stream().map(courseCategory -> {
            CourseCategoryTreeDto courseCategoryTreeDto = new CourseCategoryTreeDto();
            BeanUtils.copyProperties(courseCategory, courseCategoryTreeDto);
            return courseCategoryTreeDto;
        }).sorted(Comparator.comparing(CourseCategory::getId)).collect(Collectors.toList());

        // 移除根节点
        courseCategoryTreeDtos.removeIf(courseCategoryTreeDto -> "1".equals(courseCategoryTreeDto.getId()));

        for (CourseCategoryTreeDto courseCategoryTreeDto : courseCategoryTreeDtos) {
            // 一级分类
            if (courseCategoryTreeDto.getIsLeaf() == 0) {
                categoryTreeDtoList.add(courseCategoryTreeDto);
            }
            // 二级分类
            else {
                // 获取当前二级分类的一级分类
                CourseCategoryTreeDto courseCategoryTreeFatherDto = new CourseCategoryTreeDto();
                for (CourseCategoryTreeDto categoryTreeDto : categoryTreeDtoList) {
                    if (categoryTreeDto.getId().equals(courseCategoryTreeDto.getParentid())) {
                        courseCategoryTreeFatherDto = categoryTreeDto;
                    }
                }
                // 获取一级分类的二级分类集合
                List<CourseCategoryTreeDto> childrenTreeNodes = courseCategoryTreeFatherDto.getChildrenTreeNodes();

                if (childrenTreeNodes == null) {
                    // 第一次的时候，一级分类还没有二级分类，拿出来的是null
                    List<CourseCategoryTreeDto> childrenTreeNodesTemp = new ArrayList<>();
                    childrenTreeNodesTemp.add(courseCategoryTreeDto);
                    courseCategoryTreeFatherDto.setChildrenTreeNodes(childrenTreeNodesTemp);
                } else {
                    childrenTreeNodes.add(courseCategoryTreeDto);
                    courseCategoryTreeFatherDto.setChildrenTreeNodes(childrenTreeNodes);
                }
            }
        }
        for (CourseCategoryTreeDto courseCategoryTreeDto : categoryTreeDtoList) {
            List<CourseCategoryTreeDto> sortedCollect = courseCategoryTreeDto.getChildrenTreeNodes().stream().sorted(Comparator.comparing(CourseCategory::getOrderby)).collect(Collectors.toList());
            courseCategoryTreeDto.setChildrenTreeNodes(sortedCollect);
        }
        return categoryTreeDtoList;
    }

}
