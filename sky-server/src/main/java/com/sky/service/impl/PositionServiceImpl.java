package com.sky.service.impl;

import com.sky.dto.EmployeeAssignDTO;
import com.sky.dto.PositionDTO;
import com.sky.entity.Position;
import com.sky.mapper.EmployeeMapper;
import com.sky.mapper.PositionMapper;
import com.sky.service.PositionService;
import com.sky.vo.PositionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 职位管理Service实现类
 */
@Service
@Slf4j
public class PositionServiceImpl implements PositionService {

    @Autowired
    private PositionMapper positionMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    public List<PositionVO> getAllPositions() {
        log.info("获取所有职位列表");
        
        List<Position> positions = positionMapper.findAll();
        return positions.stream().map(position -> {
            PositionVO vo = new PositionVO();
            BeanUtils.copyProperties(position, vo);
            return vo;
        }).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public PositionVO getPositionById(Long id) {
        log.info("根据ID获取职位信息：{}", id);

        Position position = positionMapper.findById(id);
        if (position == null) {
            throw new RuntimeException("职位不存在");
        }

        PositionVO vo = new PositionVO();
        BeanUtils.copyProperties(position, vo);
        return vo;
    }

    @Override
    public List<PositionVO> getPositionsByDeptId(Long deptId) {
        log.info("根据部门ID获取职位列表：{}", deptId);
        
        return positionMapper.findPositionDetailsByDeptId(deptId);
    }

    @Override
    public Long createPosition(PositionDTO positionDTO) {
        log.info("创建职位：{}", positionDTO);

        // 检查职位代码是否已存在
        Position existingPosition = positionMapper.findByPositionCode(positionDTO.getPositionCode());
        if (existingPosition != null) {
            throw new RuntimeException("职位代码已存在：" + positionDTO.getPositionCode());
        }

        Position position = new Position();
        BeanUtils.copyProperties(positionDTO, position);
        position.setCreatedAt(LocalDateTime.now());
        position.setUpdatedAt(LocalDateTime.now());
        
        if (position.getStatus() == null) {
            position.setStatus(1);
        }

        positionMapper.insert(position);
        
        log.info("职位创建成功，ID：{}", position.getId());
        return position.getId();
    }

    @Override
    public void updatePosition(PositionDTO positionDTO) {
        log.info("更新职位信息：{}", positionDTO);

        Position existingPosition = positionMapper.findById(positionDTO.getId());
        if (existingPosition == null) {
            throw new RuntimeException("职位不存在");
        }

        // 如果职位代码有变化，检查新代码是否已被占用
        if (!existingPosition.getPositionCode().equals(positionDTO.getPositionCode())) {
            Position positionWithSameCode = positionMapper.findByPositionCode(positionDTO.getPositionCode());
            if (positionWithSameCode != null) {
                throw new RuntimeException("职位代码已存在：" + positionDTO.getPositionCode());
            }
        }

        Position position = new Position();
        BeanUtils.copyProperties(positionDTO, position);
        position.setUpdatedAt(LocalDateTime.now());

        positionMapper.update(position);
        log.info("职位信息更新成功");
    }

    @Override
    public void deletePosition(Long id) {
        log.info("删除职位：{}", id);

        Position position = positionMapper.findById(id);
        if (position == null) {
            throw new RuntimeException("职位不存在");
        }

        // 检查是否有员工关联
        // 这里简化处理，实际需要查询员工表
        positionMapper.deleteById(id);
        log.info("职位删除成功");
    }

    @Override
    public PositionVO getPositionByCode(String positionCode) {
        log.info("根据职位代码查询职位：{}", positionCode);

        Position position = positionMapper.findByPositionCode(positionCode);
        if (position == null) {
            return null;
        }

        PositionVO vo = new PositionVO();
        BeanUtils.copyProperties(position, vo);
        return vo;
    }

    @Override
    public List<PositionVO> getPositionDetails() {
        log.info("获取职位详细信息");
        
        return positionMapper.findPositionDetails();
    }

    @Override
    @Transactional
    public void assignEmployeeToPosition(EmployeeAssignDTO employeeAssignDTO) {
        log.info("分配员工到部门职位：{}", employeeAssignDTO);

        // 更新员工的部门职位信息
        try {
            employeeMapper.updateEmployeeDeptAndPosition(
                    employeeAssignDTO.getEmployeeId(),
                    employeeAssignDTO.getDeptId(),
                    employeeAssignDTO.getPositionId(),
                    employeeAssignDTO.getDirectSupervisorId()
            );
            log.info("员工分配成功：员工ID={}，部门ID={}，职位ID={}", 
                    employeeAssignDTO.getEmployeeId(), 
                    employeeAssignDTO.getDeptId(), 
                    employeeAssignDTO.getPositionId());
        } catch (Exception e) {
            log.error("员工分配失败", e);
            throw new RuntimeException("员工分配失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void batchAssignEmployees(List<EmployeeAssignDTO> assignList) {
        log.info("批量分配员工到部门职位，数量：{}", assignList.size());

        for (EmployeeAssignDTO assignDTO : assignList) {
            assignEmployeeToPosition(assignDTO);
        }

        log.info("批量员工分配完成");
    }

    @Override
    public List<PositionVO> getAvailablePositions(Long deptId) {
        log.info("获取可分配的职位列表，部门ID：{}", deptId);
        
        if (deptId != null) {
            return getPositionsByDeptId(deptId);
        } else {
            return getAllPositions();
        }
    }
}
