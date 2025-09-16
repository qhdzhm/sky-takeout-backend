package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.context.BaseContext;
import com.sky.dto.AgentCreditDTO;
import com.sky.entity.Agent;
import com.sky.entity.AgentCredit;
import com.sky.entity.AgentOperator;
import com.sky.entity.CreditTransaction;
import com.sky.entity.TourBooking;
import com.sky.exception.BusinessException;
import com.sky.mapper.AgentCreditMapper;
import com.sky.mapper.AgentMapper;
import com.sky.mapper.CreditTransactionMapper;
import com.sky.mapper.EmployeeMapper;
import com.sky.mapper.TourBookingMapper;
import com.sky.mapper.AgentOperatorMapper;
import com.sky.result.PageResult;
import com.sky.service.AdminCreditService;
import com.sky.vo.AgentCreditVO;
import com.sky.vo.CreditTransactionVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员信用额度服务实现类
 */
@Service
@Slf4j
public class AdminCreditServiceImpl implements AdminCreditService {

    @Autowired
    private AgentCreditMapper agentCreditMapper;

    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    private CreditTransactionMapper creditTransactionMapper;

    @Autowired
    private EmployeeMapper employeeMapper;
    
    @Autowired
    private TourBookingMapper tourBookingMapper;
    
    @Autowired
    private AgentOperatorMapper agentOperatorMapper;

    /**
     * 为代理商充值信用额度
     * @param agentId 代理商ID
     * @param amount 金额
     * @param remark 备注
     * @return 处理结果
     */
    @Override
    @Transactional
    public boolean topupCredit(Long agentId, BigDecimal amount, String remark) {
        // 验证参数
        if (agentId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("参数错误");
        }

        // 查询代理商信用额度信息
        AgentCredit agentCredit = agentCreditMapper.getByAgentId(agentId);
        
        // 如果代理商信用额度记录不存在，直接返回错误
        if (agentCredit == null) {
            throw new BusinessException("代理商信用额度记录不存在");
        }
        
        // 实现"信用卡"式的充值逻辑:
        // 如果代理商有未使用的信用额度，优先恢复信用额度（减少usedCredit）
        // 如果充值金额大于已用信用额度，剩余部分增加到预存余额
        
        BigDecimal usedCredit = agentCredit.getUsedCredit(); // 已用信用额度
        
        if (usedCredit.compareTo(BigDecimal.ZERO) > 0) {
            // 有已用信用额度，优先恢复
            if (amount.compareTo(usedCredit) <= 0) {
                // 充值金额小于等于已用信用额度，全部用于减少已用额度
                agentCredit.setUsedCredit(usedCredit.subtract(amount));
                // 预存余额不变
            } else {
                // 充值金额大于已用信用额度，先将已用额度清零，剩余部分增加预存余额
                BigDecimal remaining = amount.subtract(usedCredit);
            agentCredit.setUsedCredit(BigDecimal.ZERO);
                
                // 增加预存余额
                BigDecimal depositBalance = agentCredit.getDepositBalance() != null ? 
                    agentCredit.getDepositBalance() : BigDecimal.ZERO;
                agentCredit.setDepositBalance(depositBalance.add(remaining));
            }
        } else {
            // 没有已用信用额度，全部增加到预存余额
            BigDecimal depositBalance = agentCredit.getDepositBalance() != null ? 
                agentCredit.getDepositBalance() : BigDecimal.ZERO;
            agentCredit.setDepositBalance(depositBalance.add(amount));
        }
        
        // 重新计算可用额度 = 总额度 - 已用额度 + 预存余额
        BigDecimal depositBalance = agentCredit.getDepositBalance() != null ? 
            agentCredit.getDepositBalance() : BigDecimal.ZERO;
        agentCredit.setAvailableCredit(agentCredit.getTotalCredit()
            .subtract(agentCredit.getUsedCredit())
            .add(depositBalance));
        
        // 更新最后操作时间
            agentCredit.setLastUpdated(LocalDateTime.now());
        
        // 更新数据库
            agentCreditMapper.update(agentCredit);

        // 创建信用交易记录
        CreditTransaction transaction = new CreditTransaction();
        transaction.setTransactionNo("T" + System.currentTimeMillis());
        transaction.setAgentId(agentId);
        transaction.setAmount(amount);
        transaction.setTransactionType("topup");
        
        // 计算交易前后余额
        BigDecimal balanceBefore = agentCredit.getAvailableCredit().subtract(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(agentCredit.getAvailableCredit());
        
        transaction.setNote(remark);
        transaction.setCreatedAt(LocalDateTime.now());
        
        // 获取当前登录的管理员ID并设置到交易记录
        Long currentEmpId = BaseContext.getCurrentId();
        transaction.setCreatedBy(currentEmpId);
        
        creditTransactionMapper.insert(transaction);

        return true;
    }

    /**
     * 获取所有代理商信用额度信息
     * @param agentId 代理商ID（可选）
     * @param agentName 代理商名称（可选）
     * @param page 页码
     * @param pageSize 每页记录数
     * @return 分页结果
     */
    @Override
    public PageResult getAllAgentCredits(Long agentId, String agentName, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<AgentCredit> agentCredits = agentCreditMapper.getAll(agentId, agentName, offset, pageSize);
        int total = countAllAgentCredits(agentId, agentName);
        
        // 转换为VO对象
        List<AgentCreditVO> voList = agentCredits.stream().map(credit -> {
            AgentCreditVO vo = new AgentCreditVO();
            BeanUtils.copyProperties(credit, vo);
            return vo;
        }).collect(Collectors.toList());
        
        return new PageResult(total, voList);
    }
    
    /**
     * 统计满足条件的代理商信用额度总数
     * @param agentId 代理商ID（可选）
     * @param agentName 代理商名称（可选）
     * @return 总数
     */
    @Override
    public Integer countAllAgentCredits(Long agentId, String agentName) {
        return agentCreditMapper.countAll(agentId, agentName);
    }
    
    /**
     * 根据交易记录获取操作人姓名（支持多种用户类型）
     * @param tx 交易记录
     * @return 操作人姓名
     */
    private String getOperatorNameFromTransaction(CreditTransaction tx) {
        if (tx.getCreatedBy() == null) {
            return "未知操作人";
        }
        
        Long operatorId = tx.getCreatedBy();
        
        // 根据交易类型判断操作人类型
        if ("payment".equals(tx.getTransactionType())) {
            // 支付类型的交易通常是代理商或操作员发起的
            // 如果交易有agentId，说明和代理商相关
            if (tx.getAgentId() != null) {
                
                // 1. 首先检查是否是代理商本人操作
                if (tx.getAgentId().equals(operatorId)) {
                    Agent agent = agentMapper.getById(operatorId);
                    if (agent != null) {
                        return agent.getContactPerson() + " (" + agent.getCompanyName() + ")";
                    }
                }
                
                // 2. 检查是否是该代理商的操作员
                AgentOperator agentOperator = agentOperatorMapper.getById(operatorId);
                if (agentOperator != null && agentOperator.getAgentId().equals(tx.getAgentId().intValue())) {
                    Agent agent = agentMapper.getById(Long.valueOf(agentOperator.getAgentId()));
                    String operatorName = agentOperator.getName() != null && !agentOperator.getName().trim().isEmpty() 
                        ? agentOperator.getName() 
                        : agentOperator.getUsername();
                    String companyName = agent != null ? agent.getCompanyName() : "未知公司";
                    return operatorName + " (操作员-" + companyName + ")";
                }
                
                // 3. 如果都不匹配，可能是其他代理商
                Agent agent = agentMapper.getById(operatorId);
                if (agent != null && agent.getContactPerson() != null && !agent.getContactPerson().trim().isEmpty()) {
                    return agent.getContactPerson() + " (" + agent.getCompanyName() + ")";
                }
            }
        }
        
        // 对于充值等管理员操作，优先查找员工表
        if ("topup".equals(tx.getTransactionType()) || "adjustment".equals(tx.getTransactionType())) {
            String empName = employeeMapper.getNameById(operatorId);
            if (empName != null && !empName.trim().isEmpty()) {
                return empName + " (管理员)";
            }
        }
        
        // 通用查找：按优先级查询不同表
        // 1. 先查操作员表
        AgentOperator agentOperator = agentOperatorMapper.getById(operatorId);
        if (agentOperator != null) {
            Agent agent = agentMapper.getById(Long.valueOf(agentOperator.getAgentId()));
            String operatorName = agentOperator.getName() != null && !agentOperator.getName().trim().isEmpty() 
                ? agentOperator.getName() 
                : agentOperator.getUsername();
            String companyName = agent != null ? agent.getCompanyName() : "未知公司";
            return operatorName + " (操作员-" + companyName + ")";
        }
        
        // 2. 查员工表
        String empName = employeeMapper.getNameById(operatorId);
        if (empName != null && !empName.trim().isEmpty()) {
            return empName + " (员工)";
        }
        
        // 3. 查代理商表
        Agent agent = agentMapper.getById(operatorId);
        if (agent != null && agent.getContactPerson() != null && !agent.getContactPerson().trim().isEmpty()) {
            return agent.getContactPerson() + " (" + agent.getCompanyName() + ")";
        }
        
        // 最后fallback
        return "操作人ID:" + operatorId;
    }

    /**
     * 获取代理商信用额度详情
     * @param agentId 代理商ID
     * @return 信用额度详情
     */
    @Override
    public AgentCreditVO getAgentCreditDetail(Long agentId) {
        AgentCredit agentCredit = agentCreditMapper.getByAgentId(agentId);
        if (agentCredit == null) {
            throw new BusinessException("代理商信用额度信息不存在");
        }
        
        AgentCreditVO vo = new AgentCreditVO();
        BeanUtils.copyProperties(agentCredit, vo);
        return vo;
    }

    /**
     * 获取信用交易记录
     * @param agentId 代理商ID（可选）
     * @param transactionType 交易类型（可选）
     * @param transactionNo 交易编号（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param page 页码
     * @param pageSize 每页记录数
     * @return 分页结果
     */
    @Override
    public PageResult getCreditTransactions(Long agentId, String transactionType, String transactionNo, 
                                            LocalDate startDate, LocalDate endDate, 
                                            Integer page, Integer pageSize) {
        // 转换日期为DateTime
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.plusDays(1).atStartOfDay() : null;
        
        int offset = (page - 1) * pageSize;
        List<CreditTransaction> transactions = creditTransactionMapper.getAll(agentId, transactionType, transactionNo, 
                                                                          startDateTime, endDateTime, offset, pageSize);
        int total = countCreditTransactions(agentId, transactionType, transactionNo, startDate, endDate);
        
        // 转换为VO对象
        List<CreditTransactionVO> voList = transactions.stream().map(tx -> {
            CreditTransactionVO vo = new CreditTransactionVO();
            BeanUtils.copyProperties(tx, vo);
            // 处理字段名称不同的情况
            vo.setDescription(tx.getNote());
            
            // 设置代理商名称
            if (tx.getAgentId() != null) {
                Agent agent = agentMapper.getById(tx.getAgentId());
                if (agent != null) {
                    vo.setAgentName(agent.getCompanyName());
                }
            }
            
            // 设置操作人姓名 - 根据交易类型和上下文智能判断
            String operatorName = getOperatorNameFromTransaction(tx);
            vo.setCreatedByName(operatorName);
            
            // 设置订单号（而不是订单ID）
            if (tx.getBookingId() != null) {
                TourBooking booking = tourBookingMapper.getById(tx.getBookingId().intValue());
                if (booking != null && booking.getOrderNumber() != null) {
                    vo.setOrderNumber(booking.getOrderNumber());
                }
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        return new PageResult(total, voList);
    }
    
    /**
     * 统计满足条件的信用交易记录总数
     * @param agentId 代理商ID（可选）
     * @param transactionType 交易类型（可选）
     * @param transactionNo 交易编号（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 总数
     */
    @Override
    public Integer countCreditTransactions(Long agentId, String transactionType, String transactionNo, 
                                          LocalDate startDate, LocalDate endDate) {
        // 转换日期为DateTime
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.plusDays(1).atStartOfDay() : null;
        
        return creditTransactionMapper.countAll(agentId, transactionType, transactionNo, startDateTime, endDateTime);
    }

    /**
     * 获取信用交易统计数据
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param groupBy 分组依据: day, month, year
     * @return 统计数据
     */
    @Override
    public Map<String, Object> getCreditTransactionStats(LocalDate startDate, LocalDate endDate, String groupBy) {
        // 默认分组方式为day
        if (StringUtils.isBlank(groupBy)) {
            groupBy = "day";
        }

        // 验证分组方式
        if (!Arrays.asList("day", "month", "year").contains(groupBy.toLowerCase())) {
            throw new BusinessException("不支持的分组方式");
        }

        // 如果未指定日期范围，默认查询近30天
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        // 转换为DateTime
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        // 获取原始数据 - 使用通用的查询方法代替特定方法
        List<CreditTransaction> transactions = creditTransactionMapper.getAll(null, null, null, startDateTime, endDateTime, 0, Integer.MAX_VALUE);
        
        // 手动分组处理
        Map<String, List<CreditTransaction>> groupedData = new HashMap<>();
        DateTimeFormatter formatter;
        
        if ("day".equalsIgnoreCase(groupBy)) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        } else if ("month".equalsIgnoreCase(groupBy)) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        } else {
            formatter = DateTimeFormatter.ofPattern("yyyy");
        }
        
        // 按日期分组
        for (CreditTransaction tx : transactions) {
            String dateKey = tx.getCreatedAt().format(formatter);
            if (!groupedData.containsKey(dateKey)) {
                groupedData.put(dateKey, new ArrayList<>());
            }
            groupedData.get(dateKey).add(tx);
        }
        
        // 处理分组结果
        List<Map<String, Object>> stats = new ArrayList<>();
        for (Map.Entry<String, List<CreditTransaction>> entry : groupedData.entrySet()) {
            String dateKey = entry.getKey();
            List<CreditTransaction> dateTransactions = entry.getValue();
            
            // 按交易类型再次分组
            Map<String, List<CreditTransaction>> typeGroups = new HashMap<>();
            for (CreditTransaction tx : dateTransactions) {
                String type = tx.getTransactionType();
                if (!typeGroups.containsKey(type)) {
                    typeGroups.put(type, new ArrayList<>());
                }
                typeGroups.get(type).add(tx);
            }
            
            // 对每种交易类型进行统计
            for (Map.Entry<String, List<CreditTransaction>> typeEntry : typeGroups.entrySet()) {
                String type = typeEntry.getKey();
                List<CreditTransaction> typeTxs = typeEntry.getValue();
                
                BigDecimal amount = BigDecimal.ZERO;
                for (CreditTransaction tx : typeTxs) {
                    amount = amount.add(tx.getAmount());
                }
                
                Map<String, Object> statItem = new HashMap<>();
                statItem.put("date", dateKey);
                statItem.put("type", type);
                statItem.put("amount", amount);
                statItem.put("count", typeTxs.size());
                
                stats.add(statItem);
            }
        }
        
        // 处理结果
        Map<String, Object> result = new HashMap<>();
        result.put("startDate", startDate.toString());
        result.put("endDate", endDate.toString());
        result.put("groupBy", groupBy);
        result.put("stats", stats);
        
        // 计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalCount = 0;
        Map<String, BigDecimal> typeAmounts = new HashMap<>();
        
        for (Map<String, Object> stat : stats) {
            BigDecimal amount = (BigDecimal) stat.get("amount");
            Integer count = (Integer) stat.get("count");
            String type = (String) stat.get("type");
            
            totalAmount = totalAmount.add(amount);
            totalCount += count;
            
            // 按交易类型统计
            if (typeAmounts.containsKey(type)) {
                typeAmounts.put(type, typeAmounts.get(type).add(amount));
            } else {
                typeAmounts.put(type, amount);
            }
        }
        
        result.put("totalAmount", totalAmount);
        result.put("totalCount", totalCount);
        result.put("typeAmounts", typeAmounts);
        
        // 添加代理商信用额度汇总统计
        // 获取所有代理商信用额度记录
        List<AgentCredit> allAgentCredits = agentCreditMapper.getAll(null, null, 0, Integer.MAX_VALUE);
        
        // 初始化统计变量
        BigDecimal totalCreditAmount = BigDecimal.ZERO;
        BigDecimal totalUsedAmount = BigDecimal.ZERO;
        BigDecimal totalAvailableAmount = BigDecimal.ZERO;
        BigDecimal totalDepositBalance = BigDecimal.ZERO;
        int agentCount = allAgentCredits.size();
        
        // 累加各项数据
        for (AgentCredit credit : allAgentCredits) {
            if (credit.getTotalCredit() != null) {
                totalCreditAmount = totalCreditAmount.add(credit.getTotalCredit());
            }
            if (credit.getUsedCredit() != null) {
                totalUsedAmount = totalUsedAmount.add(credit.getUsedCredit());
            }
            if (credit.getAvailableCredit() != null) {
                totalAvailableAmount = totalAvailableAmount.add(credit.getAvailableCredit());
            }
            if (credit.getDepositBalance() != null) {
                totalDepositBalance = totalDepositBalance.add(credit.getDepositBalance());
            }
        }
        
        // 添加到结果中
        result.put("totalCreditAmount", totalCreditAmount);
        result.put("totalUsedAmount", totalUsedAmount);
        result.put("totalAvailableAmount", totalAvailableAmount);
        result.put("totalDepositBalance", totalDepositBalance);
        result.put("agentCount", agentCount);
        
        return result;
    }

    /**
     * 导出信用交易记录
     * @param response HTTP响应对象
     * @param agentId 代理商ID（可选）
     * @param transactionType 交易类型（可选）
     * @param transactionNo 交易编号（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     */
    @Override
    public void exportCreditTransactions(HttpServletResponse response, Long agentId, String transactionType, 
                                         String transactionNo, LocalDate startDate, LocalDate endDate) {
        try {
            // 转换日期为DateTime
            LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
            LocalDateTime endDateTime = endDate != null ? endDate.plusDays(1).atStartOfDay() : null;
            
            log.info("导出信用交易记录, 参数: agentId={}, transactionType={}, transactionNo={}, startDate={}, endDate={}",
                    agentId, transactionType, transactionNo, startDateTime, endDateTime);
            
            // 使用专门的导出方法获取所有符合条件的记录
            List<CreditTransaction> transactions = creditTransactionMapper.getAllForExport(
                agentId, transactionType, transactionNo, startDateTime, endDateTime);
                
            log.info("找到{}条交易记录准备导出", transactions.size());
            
            // 创建工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("信用交易记录");
            
            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"交易编号", "代理商ID", "代理商名称", "交易类型", "金额", "交易前余额", "交易后余额", "交易时间", "备注"};
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 20 * 256); // 设置列宽
            }
            
            // 填充数据
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < transactions.size(); i++) {
                CreditTransaction transaction = transactions.get(i);
                Row row = sheet.createRow(i + 1);
                
                row.createCell(0).setCellValue(transaction.getTransactionNo());
                row.createCell(1).setCellValue(transaction.getAgentId());
                
                // 添加代理商名称
                String agentName = "-";
                if (transaction.getAgentId() != null) {
                    Agent agent = agentMapper.getById(transaction.getAgentId());
                    if (agent != null) {
                        agentName = agent.getCompanyName();
                    }
                }
                row.createCell(2).setCellValue(agentName);
                
                // 将交易类型转换为中文
                String typeText = transaction.getTransactionType();
                if ("topup".equals(typeText)) {
                    typeText = "充值";
                } else if ("payment".equals(typeText)) {
                    typeText = "支付";
                } else if ("refund".equals(typeText)) {
                    typeText = "退款";
                } else if ("adjustment".equals(typeText)) {
                    typeText = "调整";
                }
                row.createCell(3).setCellValue(typeText);
                
                row.createCell(4).setCellValue(transaction.getAmount().doubleValue());
                row.createCell(5).setCellValue(transaction.getBalanceBefore().doubleValue());
                row.createCell(6).setCellValue(transaction.getBalanceAfter().doubleValue());
                row.createCell(7).setCellValue(transaction.getCreatedAt().format(formatter));
                row.createCell(8).setCellValue(transaction.getNote() != null ? transaction.getNote() : "");
            }
            
            // 设置响应头
            String fileName = URLEncoder.encode("信用交易记录", StandardCharsets.UTF_8.toString());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            
            // 写入响应输出流
            workbook.write(response.getOutputStream());
            workbook.close();
        } catch (IOException e) {
            log.error("导出信用交易记录失败", e);
            throw new BusinessException("导出失败");
        }
    }

    /**
     * 生成交易编号
     * @return 交易编号
     */
    private String generateTransactionNo() {
        return "T" + System.currentTimeMillis() + String.format("%04d", new Random().nextInt(10000));
    }

    /**
     * 更新代理商信用额度信息
     * @param creditDTO 信用额度更新数据
     * @return 是否成功
     */
    @Override
    @Transactional
    public boolean updateAgentCredit(AgentCreditDTO creditDTO) {
        // 参数校验
        if (creditDTO == null || creditDTO.getAgentId() == null) {
            throw new BusinessException("参数错误");
        }
        
        // 获取代理商信用额度记录
        AgentCredit agentCredit = agentCreditMapper.getByAgentId(creditDTO.getAgentId());
        if (agentCredit == null) {
            throw new BusinessException("代理商信用额度信息不存在");
        }
        
        // 计算信用额度变化
        BigDecimal totalCreditBefore = agentCredit.getTotalCredit();
        BigDecimal totalCreditAfter = creditDTO.getTotalCredit();
        BigDecimal totalCreditChange = totalCreditAfter.subtract(totalCreditBefore);
        
        // 更新信用额度信息
        agentCredit.setTotalCredit(totalCreditAfter);
        
        // 更新其他信息
        agentCredit.setCreditRating(creditDTO.getCreditRating());
        agentCredit.setInterestRate(creditDTO.getInterestRate());
        agentCredit.setBillingCycleDay(creditDTO.getBillingCycleDay());
        agentCredit.setLastSettlementDate(creditDTO.getLastSettlementDate());
        agentCredit.setOverdraftCount(creditDTO.getOverdraftCount());
        agentCredit.setIsFrozen(creditDTO.getIsFrozen());
        
        // 重新计算可用额度 = 总额度 - 已用额度 + 预存余额
        BigDecimal depositBalance = agentCredit.getDepositBalance() != null ? 
            agentCredit.getDepositBalance() : BigDecimal.ZERO;
        agentCredit.setAvailableCredit(agentCredit.getTotalCredit()
            .subtract(agentCredit.getUsedCredit())
            .add(depositBalance));
        
        // 更新时间
        agentCredit.setLastUpdated(LocalDateTime.now());
        
        // 更新数据库
        agentCreditMapper.update(agentCredit);
        
        // 如果总额度发生变化，记录交易日志
        if (totalCreditChange.compareTo(BigDecimal.ZERO) != 0) {
            CreditTransaction transaction = new CreditTransaction();
            transaction.setTransactionNo("T" + System.currentTimeMillis());
            transaction.setAgentId(creditDTO.getAgentId());
            transaction.setAmount(totalCreditChange);
            transaction.setTransactionType("adjustment");
            transaction.setBalanceBefore(totalCreditBefore);
            transaction.setBalanceAfter(totalCreditAfter);
            transaction.setNote(creditDTO.getNote() != null ? creditDTO.getNote() : "管理员调整总额度");
            transaction.setCreatedAt(LocalDateTime.now());
            
            // 获取当前登录的管理员ID并设置到交易记录
            Long currentEmpId = BaseContext.getCurrentId();
            transaction.setCreatedBy(currentEmpId);
            
            creditTransactionMapper.insert(transaction);
        }
        
        return true;
    }
    
    /**
     * 为没有信用额度记录的现有代理商初始化信用额度
     * @param defaultCredit 默认信用额度
     * @return 初始化成功的代理商数量
     */
    @Override
    @Transactional
    public int initializeCreditForExistingAgents(BigDecimal defaultCredit) {
        log.info("开始为现有代理商初始化信用额度，默认额度: {}", defaultCredit);
        
        // 1. 查询所有代理商
        List<Agent> allAgents = agentMapper.list();
        if (allAgents == null || allAgents.isEmpty()) {
            log.info("没有找到代理商，无需初始化信用额度");
            return 0;
        }
        
        int successCount = 0;
        int skipCount = 0;
        
        // 2. 为每个没有信用额度记录的代理商创建记录
        for (Agent agent : allAgents) {
            try {
                // 检查代理商是否已有信用额度记录
                AgentCredit existingCredit = agentCreditMapper.getByAgentId(agent.getId());
                if (existingCredit != null) {
                    log.debug("代理商{}(ID:{})已有信用额度记录，跳过", agent.getCompanyName(), agent.getId());
                    skipCount++;
                    continue;
                }
                
                // 创建信用额度记录
                int result = agentCreditMapper.createCreditRecord(agent.getId().intValue(), defaultCredit);
                if (result > 0) {
                    log.info("为代理商{}(ID:{})创建信用额度记录成功，默认额度: {}", 
                            agent.getCompanyName(), agent.getId(), defaultCredit);
                    successCount++;
                } else {
                    log.warn("为代理商{}(ID:{})创建信用额度记录失败", agent.getCompanyName(), agent.getId());
                }
            } catch (Exception e) {
                log.error("为代理商{}(ID:{})创建信用额度记录时发生异常: {}", 
                        agent.getCompanyName(), agent.getId(), e.getMessage());
            }
        }
        
        log.info("信用额度初始化完成，总代理商数: {}, 成功创建: {}, 跳过(已有记录): {}", 
                allAgents.size(), successCount, skipCount);
        
        return successCount;
    }
} 