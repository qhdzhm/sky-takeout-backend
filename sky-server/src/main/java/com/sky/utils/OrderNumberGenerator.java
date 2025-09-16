package com.sky.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单号生成器
 */
public class OrderNumberGenerator {
    
    // 前缀，表示Happy Tassie Travel
    private static final String PREFIX = "HT";
    
    // 日期格式
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    // 序列号，用于同一毫秒内的订单编号区分
    private static final AtomicInteger SEQUENCE = new AtomicInteger(1);
    
    // 序列号最大值
    private static final int MAX_SEQUENCE = 9999;
    
    // 随机数生成器
    private static final Random RANDOM = new Random();
    
    // 中文字符到拼音首字母的映射表（常用字符）
    private static final Map<Character, Character> PINYIN_MAP = new HashMap<>();
    
    static {
        // 初始化拼音映射表 - 添加常用汉字
        String[] pinyin = {
            "阿a", "安a", "爱a", "奥a", "八b", "白b", "北b", "本b", "比b", "宝b", "百b", "邦b", "博b", "彬b", "斌b",
            "才c", "财c", "超c", "成c", "诚c", "创c", "春c", "慈c", "聪c", "程c", "晨c", "辰c", "城c", "承c", "旅l",
            "大d", "德d", "东d", "达d", "丁d", "顶d", "鼎d", "都d", "动d", "冬d", "丹d", "但d", "道d", "登d",
            "恩e", "尔e", "二e", "峨e", "娥e", "俄e", "儿e", "耳e", "额e", "恶e", "鄂e", "饿e", "而e", "法f",
            "发f", "方f", "飞f", "风f", "福f", "富f", "峰f", "锋f", "丰f", "凤f", "芳f", "菲f", "范f", "帆f",
            "高g", "光g", "国g", "贵g", "广g", "刚g", "港g", "格g", "根g", "公g", "功g", "宫g", "冠g", "关g",
            "华h", "和h", "海h", "红h", "宏h", "弘h", "辉h", "慧h", "汇h", "会h", "火h", "华h", "花h", "豪h",
            "嘉j", "金j", "君j", "杰j", "捷j", "建j", "江j", "晶j", "静j", "净j", "聚j", "家j", "佳j", "加j",
            "科k", "可k", "康k", "凯k", "开k", "快k", "宽k", "昆k", "坤k", "空k", "客k", "控k", "看k", "库k",
            "力l", "利l", "立l", "理l", "亮l", "良l", "领l", "林l", "龙l", "隆l", "路l", "鲁l", "绿l", "蓝l",
            "美m", "明m", "民m", "敏m", "名m", "马m", "满m", "曼m", "茂m", "贸m", "迈m", "妙m", "苗m", "密m",
            "南n", "宁n", "能n", "年n", "牛n", "女n", "暖n", "内n", "纳n", "尼n", "鸟n", "农n", "念n", "宁n",
            "欧o", "偶o", "平p", "鹏p", "朋p", "品p", "普p", "萍p", "培p", "盼p", "派p", "跑p", "配p", "佩p",
            "全q", "强q", "清q", "青q", "庆q", "秋q", "奇q", "起q", "启q", "千q", "前q", "钱q", "谦q", "勤q",
            "人r", "仁r", "日r", "如r", "瑞r", "锐r", "睿r", "然r", "热r", "容r", "荣r", "融r", "柔r", "肉r",
            "胜s", "圣s", "盛s", "生s", "声s", "升s", "世s", "士s", "市s", "事s", "是s", "实s", "时s", "室s",
            "天t", "田t", "通t", "同t", "童t", "统t", "图t", "土t", "太t", "泰t", "台t", "特t", "腾t", "滕t",
            "万w", "威w", "伟w", "为w", "文w", "温w", "王w", "望w", "旺w", "维w", "微w", "唯w", "伟w", "卫w",
            "新x", "信x", "兴x", "行x", "星x", "祥x", "详x", "向x", "想x", "象x", "西x", "希x", "喜x", "协x",
            "业y", "远y", "源y", "园y", "圆y", "元y", "原y", "运y", "云y", "友y", "有y", "优y", "游y", "盈y",
            "正z", "政z", "志z", "智z", "知z", "中z", "众z", "重z", "州z", "洲z", "主z", "助z", "专z", "庄z"
        };
        
        for (String py : pinyin) {
            if (py.length() >= 2) {
                char chinese = py.charAt(0);
                char initial = py.charAt(py.length() - 1);
                PINYIN_MAP.put(chinese, initial);
            }
        }
    }
    
    /**
     * 生成订单号（默认前缀）
     * 格式: 前缀 + 日期 + 4位序列号 + 2位随机数
     * 例如: HT202503150001XX
     * @return 订单号
     */
    public static String generate() {
        // 获取当前日期时间
        LocalDateTime now = LocalDateTime.now();
        
        // 格式化日期
        String date = now.format(DATE_FORMATTER);
        
        // 获取序列号，并重置
        int sequence = SEQUENCE.getAndIncrement();
        if (sequence > MAX_SEQUENCE) {
            SEQUENCE.set(1);
            sequence = 1;
        }
        
        // 生成两位随机数
        int randomNum = RANDOM.nextInt(100);
        
        // 组合订单号: 前缀 + 日期 + 序列号(4位) + 随机数(2位)
        return String.format("%s%s%04d%02d", PREFIX, date, sequence, randomNum);
    }
    
    /**
     * 生成带代理商前缀的订单号
     * 格式: 代理商前缀(3位) + 日期 + 4位序列号 + 2位随机数
     * 例如: ljy202503150001XX（领君游）
     * @param agentCompanyName 代理商公司名
     * @return 订单号
     */
    public static String generateWithAgent(String agentCompanyName) {
        // 获取当前日期时间
        LocalDateTime now = LocalDateTime.now();
        
        // 格式化日期
        String date = now.format(DATE_FORMATTER);
        
        // 获取序列号，并重置
        int sequence = SEQUENCE.getAndIncrement();
        if (sequence > MAX_SEQUENCE) {
            SEQUENCE.set(1);
            sequence = 1;
        }
        
        // 生成两位随机数
        int randomNum = RANDOM.nextInt(100);
        
        // 提取代理商公司名的首字母缩写
        String agentPrefix = extractInitials(agentCompanyName);
        
        // 组合订单号: 代理商前缀 + 日期 + 序列号(4位) + 随机数(2位)
        return String.format("%s%s%04d%02d", agentPrefix, date, sequence, randomNum);
    }
    
    /**
     * 提取中文公司名的首字母缩写（最多3位）
     * @param companyName 公司名
     * @return 首字母缩写（大写）
     */
    private static String extractInitials(String companyName) {
        if (companyName == null || companyName.trim().isEmpty()) {
            return PREFIX; // 如果没有公司名，使用默认前缀
        }
        
        StringBuilder initials = new StringBuilder();
        String name = companyName.trim();
        
        // 遍历公司名的每个字符，提取前3个有效的首字母
        for (int i = 0; i < name.length() && initials.length() < 3; i++) {
            char ch = name.charAt(i);
            
            // 如果是中文字符，查找对应的拼音首字母
            if (PINYIN_MAP.containsKey(ch)) {
                initials.append(PINYIN_MAP.get(ch));
            } 
            // 如果是英文字母，直接添加
            else if (Character.isLetter(ch)) {
                initials.append(Character.toLowerCase(ch));
            }
            // 跳过数字、符号等其他字符
        }
        
        // 如果提取的首字母不足3位，用默认前缀补充
        String result = initials.toString();
        if (result.length() < 3) {
            if (result.isEmpty()) {
                result = PREFIX.toLowerCase(); // 没有提取到任何字母时使用默认前缀
            } else {
                // 不足3位时，在后面补充默认前缀的字母
                String defaultSuffix = PREFIX.toLowerCase();
                for (int i = 0; i < defaultSuffix.length() && result.length() < 3; i++) {
                    result += defaultSuffix.charAt(i);
                }
                // 如果仍然不足3位，补充'x'
                while (result.length() < 3) {
                    result += 'x';
                }
            }
        }
        
        return result.toUpperCase(); // 返回大写形式
    }
} 