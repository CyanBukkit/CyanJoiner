# CyanJoiner - 我的世界服务器匹配插件

## 1. 项目信息
- **项目名称**：CyanJoiner
- **功能描述**：带版本限制的智能服务器匹配插件，支持：
  - 多服务器分类管理
  - 动态GUI界面展示
  - 智能匹配算法（KD值/版本号过滤）
  - 实时服务器状态监控
- **适用版本**：
  - Minecraft 1.12+ 
  - BungeeCord/Waterfall服务端

## 2. 使用教程
### 安装方法
1. 将插件放入plugins文件夹
2. 重启服务器自动生成配置文件

### 指令列表
```
/cj gui <分类名>  # 打开指定分类的GUI界面
/cj quick <服务器名>  # 快速加入指定服务器
/cj list  # 查看可用分类列表
/cj reload  # 重载插件配置
/cj updateServer  # 手动更新服务器状态
```

### 配置文件要点
- `servers.yml`:
  ```yaml
  categories:
    分类名称:
      settings:
        enable: true
        kdPapi: "%player_kd%"  # KD值PAPI变量
        difference: 0.5  # 允许的KD差值
        version: ">1.16"  # 版本限制
      servers:
        服务器名称:
          ip: 127.0.0.1:25565
          name: "&a生存服务器"
  ```
- `settings.yml`:
  ```yaml
  update_delay: 5000  # 服务器状态更新间隔(ms)
  ```

## 3. 项目编译
```bash
./gradlew build  # 构建插件包
构建结果：build/libs/CyanJoiner-1.0.jar
```

## 4. 项目结构
```
src/main/kotlin/
  ├── cn/cyanbukkit/cyanjoiner/  # 核心包
  │   ├── command/  # 指令处理
  │   ├── server/   # 服务器管理
  │   └── utils/    # 工具类
  └── resources/    # 配置模板
      ├── settings.yml
      └── servers.yml
```

## 5. 其他信息
- 依赖库：ProtocolLib（用于版本检测）
- 开源协议：MIT License

## 6. 更新日志
### 2025-03-07
- 新增GUI自定义功能
- 优化服务器匹配算法

### 2025-01-27
- 新增"傻瓜式"快速加入功能
- 修复指令检索逻辑BUG

### 历史版本
- 初始版本发布