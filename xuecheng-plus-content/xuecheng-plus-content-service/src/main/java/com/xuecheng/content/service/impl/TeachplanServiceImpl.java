package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveTeachplan(SaveTeachplanDto teachplanDto) {

        //课程计划id
        Long id = teachplanDto.getId();
        //修改课程计划
        if (id != null) {
            Teachplan teachplan = teachplanMapper.selectById(id);
            BeanUtils.copyProperties(teachplanDto, teachplan);
            teachplanMapper.updateById(teachplan);
        } else {
            //取出同父同级别的课程计划最大的orderBy
            int count = getTeachplanMaxOrderBy(teachplanDto.getCourseId(), teachplanDto.getParentid());
            Teachplan teachplanNew = new Teachplan();
            //设置排序号
            teachplanNew.setOrderby(count + 1);
            BeanUtils.copyProperties(teachplanDto, teachplanNew);

            teachplanMapper.insert(teachplanNew);

        }

    }

    @Transactional
    @Override
    public void delTeachplan(long teachplanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        // 二级目录
        if(teachplan.getGrade() == 2){
            teachplanMapper.deleteById(teachplanId);
            QueryWrapper<TeachplanMedia> teachplanMediaQueryWrapper = new QueryWrapper<>();
            teachplanMediaQueryWrapper.eq("teachplan_id",teachplanId);
            teachplanMediaMapper.delete(teachplanMediaQueryWrapper);
        }
        // 一级目录
        else{
            QueryWrapper<Teachplan> teachplanQueryWrapper = new QueryWrapper<>();
            teachplanQueryWrapper.eq("parentid", teachplanId);
            List<Teachplan> teachplans = teachplanMapper.selectList(teachplanQueryWrapper);
            if(teachplans.isEmpty()){
                teachplanMapper.deleteById(teachplanId);
            }
            else{
                throw new XueChengPlusException("课程计划信息还有子级信息，无法操作");
            }
        }
    }

    private int getTeachplanMaxOrderBy(long courseId, long parentId) {
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId);
        queryWrapper.eq(Teachplan::getParentid, parentId);
        List<Teachplan> teachplans = teachplanMapper.selectList(queryWrapper);
        Optional<Teachplan> max = teachplans.stream().max(Comparator.comparing(Teachplan::getOrderby));
        if (max.isPresent()) {
            return max.get().getOrderby();
        }
        return 0;
    }
}
