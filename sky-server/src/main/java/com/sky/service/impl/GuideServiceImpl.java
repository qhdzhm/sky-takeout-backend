package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.GuidePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.entity.Guide;
import com.sky.mapper.EmployeeMapper;
import com.sky.mapper.GuideMapper;
import com.sky.result.PageResult;
import com.sky.service.GuideService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class GuideServiceImpl implements GuideService {

    @Autowired
    private GuideMapper guideMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 导游分页查询
     */
    @Override
    public PageResult pageQuery(GuidePageQueryDTO guidePageQueryDTO) {
        PageHelper.startPage(guidePageQueryDTO.getPage(), guidePageQueryDTO.getPageSize());
        
        Page<Guide> page = guideMapper.pageQuery(guidePageQueryDTO);
        
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 通过员工ID获取导游信息
     */
    @Override
    public Guide getGuideByEmployeeId(Long employeeId) {
        return guideMapper.getGuideByEmployeeId(employeeId);
    }

    /**
     * 修复导游和员工的关联关系
     */
    @Override
    public void fixEmployeeRelation() {
        // 更新张导游的员工关联
        guideMapper.updateGuideEmployeeId(1, 1L); // guide_id=1, employee_id=1
        
        // 更新李导游的员工关联  
        guideMapper.updateGuideEmployeeId(2, 9L); // guide_id=2, employee_id=9
        
        log.info("导游和员工关联关系修复完成");
    }

    /**
     * 同步导游表数据到员工表
     */
    @Override
    @Transactional
    public void syncGuidesToEmployees() {
        // 获取所有没有关联员工ID的导游
        List<Guide> guidesWithoutEmployee = guideMapper.getGuidesWithoutEmployee();
        
        for (Guide guide : guidesWithoutEmployee) {
            // 为每个导游创建对应的员工记录
            Employee employee = Employee.builder()
                    .username(generateUsername(guide.getName()))
                    .name(guide.getName())
                    .password(DigestUtils.md5DigestAsHex("123456".getBytes()))
                    .phone(guide.getPhone())
                    .sex("男") // 默认值，可以后续修改
                    .idNumber("000000000000000000") // 默认值，可以后续修改
                    .role("导游员工") // 导游角色
                    .workStatus(0) // 空闲状态
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .createUser(1L)
                    .updateUser(1L)
                    .build();
            
            // 插入员工记录
            employeeMapper.insert(employee);
            
            // 更新导游记录的员工关联
            guideMapper.updateGuideEmployeeId(guide.getGuideId(), employee.getId());
            
            log.info("为导游 {} 创建了对应的员工记录，员工ID: {}", guide.getName(), employee.getId());
        }
        
        log.info("导游同步到员工表完成，共处理 {} 条记录", guidesWithoutEmployee.size());
    }

    /**
     * 生成用户名
     */
    private String generateUsername(String name) {
        // 简单的用户名生成逻辑，可以根据需要调整
        return "guide_" + name.toLowerCase().replaceAll("\\s+", "");
    }
} 