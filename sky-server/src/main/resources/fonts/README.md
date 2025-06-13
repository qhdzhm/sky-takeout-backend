# 中文字体配置

## 说明
此目录用于存放PDF生成所需的中文字体文件，解决PDF中文乱码问题。

## 字体文件放置
请将以下中文字体文件复制到此目录：

### Windows系统字体路径
从以下路径复制字体文件到此目录：
- `C:/Windows/Fonts/simhei.ttf` → `simhei.ttf` (黑体)
- `C:/Windows/Fonts/simsun.ttc` → `simsun.ttf` (宋体)
- `C:/Windows/Fonts/msyh.ttc` → `microsoftyahei.ttf` (微软雅黑)

### 推荐字体
1. **simhei.ttf** - 黑体字体，适合显示中文
2. **simsun.ttf** - 宋体字体，经典中文字体
3. **microsoftyahei.ttf** - 微软雅黑，现代化中文字体

## 复制命令示例 (Windows)
```bash
# 复制微软雅黑字体
copy "C:\Windows\Fonts\msyh.ttc" "sky-server\src\main\resources\fonts\microsoftyahei.ttf"

# 复制黑体字体  
copy "C:\Windows\Fonts\simhei.ttf" "sky-server\src\main\resources\fonts\simhei.ttf"

# 复制宋体字体
copy "C:\Windows\Fonts\simsun.ttc" "sky-server\src\main\resources\fonts\simsun.ttf"
```

## 注意事项
1. 字体文件大小通常在几MB到几十MB之间
2. 建议至少添加一个中文字体文件
3. 系统已配置自动检测和加载这些字体文件
4. 如果系统字体加载失败，会自动尝试加载此目录下的字体文件

## 测试
重启应用后，生成PDF确认单时会自动使用配置的中文字体，解决乱码问题。 