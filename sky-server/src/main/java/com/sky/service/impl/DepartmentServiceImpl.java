package com.sky.service.impl;

import com.sky.dto.DepartmentDTO;
import com.sky.entity.Department;
import com.sky.mapper.DepartmentMapper;
import com.sky.mapper.PositionMapper;
import com.sky.service.DepartmentService;
import com.sky.vo.DepartmentVO;
import com.sky.vo.PositionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门管理Service实现类
 */
@Service
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private PositionMapper positionMapper;

    @Override
    public List<DepartmentVO> getAllDepartments() {
        log.info("获取所有部门列表");

        List<Department> departments = departmentMapper.findAll();
        
        return departments.stream().map(dept -> {
            DepartmentVO vo = new DepartmentVO();
            BeanUtils.copyProperties(dept, vo);
            
            // 获取部门下的职位列表
            List<PositionVO> positions = positionMapper.findPositionDetailsByDeptId(dept.getId());
            vo.setPositions(positions);
            vo.setEmployeeCount(positions.stream()
                    .mapToInt(pos -> pos.getEmployeeCount() != null ? pos.getEmployeeCount() : 0)
                    .sum());
            
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public DepartmentVO getDepartmentById(Long id) {
        log.info("根据ID获取部门信息：{}", id);

        Department department = departmentMapper.findById(id);
        if (department == null) {
            throw new RuntimeException("部门不存在");
        }

        DepartmentVO vo = new DepartmentVO();
        BeanUtils.copyProperties(department, vo);

        // 获取部门下的职位列表
        List<PositionVO> positions = positionMapper.findPositionDetailsByDeptId(id);
        vo.setPositions(positions);
        vo.setEmployeeCount(positions.stream()
                .mapToInt(pos -> pos.getEmployeeCount() != null ? pos.getEmployeeCount() : 0)
                .sum());

        return vo;
    }

    @Override
    public Long createDepartment(DepartmentDTO departmentDTO) {
        log.info("创建部门：{}", departmentDTO);

        // 检查部门代码是否已存在
        Department existingDept = departmentMapper.findByDeptCode(departmentDTO.getDeptCode());
        if (existingDept != null) {
            throw new RuntimeException("部门代码已存在：" + departmentDTO.getDeptCode());
        }

        Department department = new Department();
        BeanUtils.copyProperties(departmentDTO, department);
        department.setCreatedAt(LocalDateTime.now());
        department.setUpdatedAt(LocalDateTime.now());
        
        if (department.getStatus() == null) {
            department.setStatus(1);
        }

        departmentMapper.insert(department);
        
        log.info("部门创建成功，ID：{}", department.getId());
        return department.getId();
    }

    @Override
    public void updateDepartment(DepartmentDTO departmentDTO) {
        log.info("更新部门信息：{}", departmentDTO);

        Department existingDept = departmentMapper.findById(departmentDTO.getId());
        if (existingDept == null) {
            throw new RuntimeException("部门不存在");
        }

        // 如果部门代码有变化，检查新代码是否已被占用
        if (!existingDept.getDeptCode().equals(departmentDTO.getDeptCode())) {
            Department deptWithSameCode = departmentMapper.findByDeptCode(departmentDTO.getDeptCode());
            if (deptWithSameCode != null) {
                throw new RuntimeException("部门代码已存在：" + departmentDTO.getDeptCode());
            }
        }

        Department department = new Department();
        BeanUtils.copyProperties(departmentDTO, department);
        department.setUpdatedAt(LocalDateTime.now());

        departmentMapper.update(department);
        log.info("部门信息更新成功");
    }

    @Override
    public void deleteDepartment(Long id) {
        log.info("删除部门：{}", id);

        Department department = departmentMapper.findById(id);
        if (department == null) {
            throw new RuntimeException("部门不存在");
        }

        // 检查是否有职位关联
        List<PositionVO> positions = positionMapper.findPositionDetailsByDeptId(id);
        if (!positions.isEmpty()) {
            throw new RuntimeException("该部门下还有职位，无法删除");
        }

        departmentMapper.deleteById(id);
        log.info("部门删除成功");
    }

    @Override
    public DepartmentVO getDepartmentByCode(String deptCode) {
        log.info("根据部门代码查询部门：{}", deptCode);

        Department department = departmentMapper.findByDeptCode(deptCode);
        if (department == null) {
            return null;
        }

        DepartmentVO vo = new DepartmentVO();
        BeanUtils.copyProperties(department, vo);

        // 获取部门下的职位列表
        List<PositionVO> positions = positionMapper.findPositionDetailsByDeptId(department.getId());
        vo.setPositions(positions);
        vo.setEmployeeCount(positions.stream()
                .mapToInt(pos -> pos.getEmployeeCount() != null ? pos.getEmployeeCount() : 0)
                .sum());

        return vo;
    }

    @Override
    public List<DepartmentVO> getDepartmentStatistics() {
        log.info("获取部门统计信息");

        // 这里可以调用专门的统计查询方法
        // 暂时复用现有方法
        return getAllDepartments();
    }
}


