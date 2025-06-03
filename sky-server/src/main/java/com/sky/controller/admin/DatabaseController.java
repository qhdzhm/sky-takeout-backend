package com.sky.controller.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.sky.result.Result;

/**
 * 临时数据库修改控制器
 */
@RestController
@RequestMapping("/admin/database")
@Slf4j
public class DatabaseController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/fix-relationships")
    public Result fixTableRelationships() {
        try {
            log.info("开始修复数据库表关系...");
            
            // 1. 检查employee_id字段是否已存在
            try {
                jdbcTemplate.execute("ALTER TABLE guides ADD COLUMN employee_id BIGINT DEFAULT NULL COMMENT '关联员工ID'");
                log.info("添加employee_id字段成功");
            } catch (Exception e) {
                log.info("employee_id字段可能已存在: " + e.getMessage());
            }
            
            // 2. 添加外键约束
            try {
                jdbcTemplate.execute("ALTER TABLE guides ADD CONSTRAINT fk_guides_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE SET NULL");
                log.info("添加外键约束成功");
            } catch (Exception e) {
                log.info("外键约束可能已存在: " + e.getMessage());
            }
            
            // 3. 更新现有导游数据
            jdbcTemplate.update("UPDATE guides SET employee_id = 1 WHERE guide_id = 1 AND name = '张导游'");
            jdbcTemplate.update("UPDATE guides SET employee_id = 2 WHERE guide_id = 2 AND name = '李导游'");
            log.info("更新现有导游关联成功");
            
            // 4. 添加新导游（如果不存在）
            try {
                jdbcTemplate.update(
                    "INSERT INTO guides (employee_id, name, phone, email, experience_years, languages, hourly_rate, daily_rate, status, max_groups) " +
                    "VALUES (3, '王导游', '13800138003', 'wang@example.com', 8, '中文,英文,法文', 50.00, 300.00, 1, 2)"
                );
                log.info("添加王导游成功");
            } catch (Exception e) {
                log.info("王导游可能已存在: " + e.getMessage());
            }
            
            try {
                jdbcTemplate.update(
                    "INSERT INTO guides (employee_id, name, phone, email, experience_years, languages, hourly_rate, daily_rate, status, max_groups) " +
                    "VALUES (4, '赵导游', '13800138004', 'zhao@example.com', 6, '中文,英文', 45.00, 280.00, 1, 1)"
                );
                log.info("添加赵导游成功");
            } catch (Exception e) {
                log.info("赵导游可能已存在: " + e.getMessage());
            }
            
            // 5. 添加导游可用性数据
            String[] guideAvailability = {
                "INSERT IGNORE INTO guide_availability (guide_id, date, available_start_time, available_end_time, status) VALUES (1, '2025-05-31', '08:00:00', '18:00:00', 'available')",
                "INSERT IGNORE INTO guide_availability (guide_id, date, available_start_time, available_end_time, status) VALUES (2, '2025-05-31', '08:30:00', '17:30:00', 'available')",
                "INSERT IGNORE INTO guide_availability (guide_id, date, available_start_time, available_end_time, status) VALUES (3, '2025-05-31', '07:30:00', '19:00:00', 'available')",
                "INSERT IGNORE INTO guide_availability (guide_id, date, available_start_time, available_end_time, status) VALUES (4, '2025-05-31', '08:00:00', '17:00:00', 'available')"
            };
            
            for (String sql : guideAvailability) {
                try {
                    jdbcTemplate.update(sql);
                } catch (Exception e) {
                    log.info("导游可用性数据可能已存在: " + e.getMessage());
                }
            }
            log.info("添加导游可用性数据成功");
            
            // 6. 添加车辆可用性数据
            String[] vehicleAvailability = {
                "INSERT IGNORE INTO vehicle_availability (vehicle_id, available_date, start_time, end_time, status) VALUES (1, '2025-05-31', '08:00:00', '18:00:00', 'available')",
                "INSERT IGNORE INTO vehicle_availability (vehicle_id, available_date, start_time, end_time, status) VALUES (2, '2025-05-31', '08:00:00', '18:00:00', 'available')",
                "INSERT IGNORE INTO vehicle_availability (vehicle_id, available_date, start_time, end_time, status) VALUES (3, '2025-05-31', '08:00:00', '18:00:00', 'available')"
            };
            
            for (String sql : vehicleAvailability) {
                try {
                    jdbcTemplate.update(sql);
                } catch (Exception e) {
                    log.info("车辆可用性数据可能已存在: " + e.getMessage());
                }
            }
            log.info("添加车辆可用性数据成功");
            
            // 7. 创建视图
            try {
                jdbcTemplate.execute("DROP VIEW IF EXISTS v_guide_employee");
                jdbcTemplate.execute(
                    "CREATE VIEW v_guide_employee AS " +
                    "SELECT g.guide_id, g.name as guide_name, g.languages, g.experience_years, " +
                    "g.hourly_rate, g.daily_rate, g.max_groups, g.status as guide_status, " +
                    "e.id as employee_id, e.name as employee_name, e.phone as employee_phone, " +
                    "e.status as employee_status, e.role as employee_role, e.work_status " +
                    "FROM guides g LEFT JOIN employees e ON g.employee_id = e.id"
                );
                log.info("创建导游员工关联视图成功");
            } catch (Exception e) {
                log.error("创建视图失败: " + e.getMessage());
            }
            
            log.info("数据库表关系修复完成！");
            return Result.success("数据库表关系修复成功");
            
        } catch (Exception e) {
            log.error("修复数据库表关系失败", e);
            return Result.error("修复失败: " + e.getMessage());
        }
    }
} 