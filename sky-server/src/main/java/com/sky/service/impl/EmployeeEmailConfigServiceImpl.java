package com.sky.service.impl;

import com.sky.dto.EmployeeEmailConfigDTO;
import com.sky.entity.Employee;
import com.sky.mapper.EmployeeMapper;
import com.sky.service.EmployeeEmailConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Properties;

/**
 * 员工邮箱配置服务实现类
 */
@Service
@Slf4j
public class EmployeeEmailConfigServiceImpl implements EmployeeEmailConfigService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Value("${sky.email.encryption-key:SkyTravelEmailKey2024}")
    private String encryptionKey;

    @Override
    public void configEmployeeEmail(EmployeeEmailConfigDTO configDTO) {
        log.info("配置员工邮箱：employeeId={}, email={}", configDTO.getEmployeeId(), configDTO.getEmail());

        // 查找员工
        Employee employee = employeeMapper.getById(configDTO.getEmployeeId().intValue());
        if (employee == null) {
            throw new RuntimeException("员工不存在");
        }

        // 加密密码
        String encryptedPassword = encryptPassword(configDTO.getEmailPassword());

        // 更新员工邮箱配置
        Employee updateEmployee = Employee.builder()
                .id(configDTO.getEmployeeId())
                .email(configDTO.getEmail())
                .emailPassword(encryptedPassword)
                .emailHost(configDTO.getEmailHost())
                .emailPort(configDTO.getEmailPort())
                .emailEnabled(configDTO.getEmailEnabled())
                .emailSslEnabled(configDTO.getEmailSslEnabled())
                .emailAuthEnabled(configDTO.getEmailAuthEnabled())
                .updateTime(LocalDateTime.now())
                .build();

        employeeMapper.update(updateEmployee);
        log.info("员工邮箱配置成功：employeeId={}", configDTO.getEmployeeId());
    }

    @Override
    public Employee getEmployeeEmailConfig(Long employeeId) {
        log.info("获取员工邮箱配置：employeeId={}", employeeId);
        return employeeMapper.getById(employeeId.intValue());
    }

    @Override
    public boolean testEmployeeEmailConnection(Long employeeId) {
        log.info("测试员工邮箱连接：employeeId={}", employeeId);

        Employee employee = employeeMapper.getById(employeeId.intValue());
        if (employee == null || !Boolean.TRUE.equals(employee.getEmailEnabled())) {
            log.warn("员工不存在或未启用邮箱发送：employeeId={}", employeeId);
            return false;
        }

        try {
            // 创建JavaMailSender实例进行测试
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost(employee.getEmailHost());
            mailSender.setPort(employee.getEmailPort());
            mailSender.setUsername(employee.getEmail());
            mailSender.setPassword(decryptPassword(employee.getEmailPassword()));

            // 配置邮件属性
            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.auth", employee.getEmailAuthEnabled());
            props.put("mail.smtp.starttls.enable", employee.getEmailSslEnabled());
            props.put("mail.debug", "false");

            // 测试连接
            mailSender.testConnection();
            log.info("员工邮箱连接测试成功：employeeId={}", employeeId);
            return true;

        } catch (Exception e) {
            log.error("员工邮箱连接测试失败：employeeId={}, error={}", employeeId, e.getMessage());
            return false;
        }
    }

    @Override
    public void updateEmailEnabled(Long employeeId, Boolean enabled) {
        log.info("更新员工邮箱启用状态：employeeId={}, enabled={}", employeeId, enabled);

        Employee updateEmployee = Employee.builder()
                .id(employeeId)
                .emailEnabled(enabled)
                .updateTime(LocalDateTime.now())
                .build();

        employeeMapper.update(updateEmployee);
        log.info("员工邮箱启用状态更新成功：employeeId={}, enabled={}", employeeId, enabled);
    }

    @Override
    public String encryptPassword(String password) {
        try {
            // 确保密钥长度为32字节（256位）
            byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
            byte[] key = new byte[32]; // 256位密钥
            
            if (keyBytes.length >= 32) {
                System.arraycopy(keyBytes, 0, key, 0, 32);
            } else {
                System.arraycopy(keyBytes, 0, key, 0, keyBytes.length);
                // 用0填充剩余字节
                for (int i = keyBytes.length; i < 32; i++) {
                    key[i] = 0;
                }
            }
            
            // 使用AES加密
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("邮箱密码加密失败", e);
            throw new RuntimeException("邮箱密码加密失败");
        }
    }

    @Override
    public String decryptPassword(String encryptedPassword) {
        try {
            // 确保密钥长度为32字节（256位）- 与加密方法保持一致
            byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
            byte[] key = new byte[32]; // 256位密钥
            
            if (keyBytes.length >= 32) {
                System.arraycopy(keyBytes, 0, key, 0, 32);
            } else {
                System.arraycopy(keyBytes, 0, key, 0, keyBytes.length);
                // 用0填充剩余字节
                for (int i = keyBytes.length; i < 32; i++) {
                    key[i] = 0;
                }
            }
            
            // 使用AES解密
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedPassword));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("邮箱密码解密失败", e);
            throw new RuntimeException("邮箱密码解密失败");
        }
    }
}
