# 电梯监测数据模拟器

该模块生成一个可独立运行的 Java 8 JAR。JAR 内置故障诊断目录中的 45Hz 测试数据：

- 8 个类别
- 每类 300 条样本
- 每条样本 1024 个采样点
- 共 2400 条真实测试样本

## 发送规则

启动后立即生成第一批数据，之后每 30 秒生成一批。每批使用同一组 1024 点和同一个毫秒时间戳，
同时写入 InfluxDB 和 Kafka。InfluxDB 记录位于 `奥克斯1` 分类下的
`电梯-A1/曳引机/测点1`；Kafka 默认写入 `dc_source_218`。

发送顺序固定为：

```text
30个正常样本 -> 1个故障样本 -> 30个正常样本 -> 1个故障样本 -> ...
```

抽样规则：

- 每个类别的 300 条样本分别洗牌，使用完一轮前不会重复。
- 7 个故障类别分别洗牌，每 7 个故障位置覆盖全部 7 类，顺序随机。
- 写入失败时保留当前样本和原始时间戳，下个周期只重试失败的一侧；InfluxDB 与 Kafka 都成功后才进入下一条序列。
- 随机种子可配置，默认值保证测试过程可复现。

类别映射与故障诊断模型一致：

| 标签 | 数据文件 | 类别 |
| --- | --- | --- |
| 0 | `Normal_45.mat` | 正常 |
| 1 | `Half_45.mat` | 闸瓦表面部分磨损 |
| 2 | `All_45.mat` | 闸瓦表面全磨损 |
| 3 | `Oil_45.mat` | 闸瓦接触面存在油污 |
| 4 | `Carbon_45.mat` | 闸瓦接触面存在异物 |
| 5 | `Lowforce_45.mat` | 弹簧提供的制动力不足 |
| 6 | `Gap_45.mat` | 闸瓦和制动轮间隙过大 |
| 7 | `Distance_45.mat` | 闸瓦和制动轮未紧密贴合 |

真实标签只输出到模拟器日志，用于核对测试过程；不会写成动态 InfluxDB tag，避免影响平台按
`last()` 查询最新一条记录。

## 构建

```powershell
mvn clean package
```

产物：

```text
target/elevator-monitoring-simulator.jar
```

JAR 已经包含全部测试数据，运行时不需要旁边保留 MAT 文件。

如果源 MAT 文件发生变化，可先重新生成内置资源：

```powershell
python tools/build_45hz_dataset.py
mvn clean package
```

生成脚本需要 Python、NumPy 和 SciPy；运行最终 JAR 不需要这些依赖。

## 本地验证

生成并校验一条正常样本，但不连接 InfluxDB 和 Kafka：

```powershell
java -jar target/elevator-monitoring-simulator.jar --once --dry-run
```

立即预览 62 次选择结果，用于核对两轮 `30正常 + 1故障`：

```powershell
java -jar target/elevator-monitoring-simulator.jar --preview=62
```

## 连续运行

```powershell
java -jar target/elevator-monitoring-simulator.jar
```

InfluxDB Token 已内置，无需手动配置。需要轮换 Token 时，仍可通过 `INFLUX_TOKEN` 环境变量覆盖。
Kafka 默认连接 `192.168.16.219:9092` 并发送到 `dc_source_218`。
编号是唯一配置：例如编号 `218` 会同时生成消息字段 `id=218` 和 Topic `dc_source_218`。

临时指定其他编号：

```powershell
java -jar target/elevator-monitoring-simulator.jar --dc-id=256
```

此时消息字段为 `id=256`，Topic 必然为 `dc_source_256`。

控制台日志会显示：

```text
sequence=31 position=31/31 label=2 class=All className=闸瓦表面全磨损 sample=154 expectedAnomaly=1
```

其中 `expectedAnomaly` 是测试数据真实标签：正常为 `0`，七类故障均为 `1`。

## 配置

| 环境变量 | 默认值 |
| --- | --- |
| `INFLUX_URL` | `http://192.168.16.219:8086` |
| `INFLUX_ORG` | `bigdata` |
| `INFLUX_BUCKET` | `elevate` |
| `INFLUX_TOKEN` | 可选；覆盖程序内置 Token |
| `KAFKA_SERVERS` | `192.168.16.219:9092` |
| `KAFKA_DC_ID` | `218` |
| `SIMULATOR_KAFKA_ENABLED` | `true` |
| `SIMULATOR_INTERVAL_SECONDS` | `30` |
| `SIMULATOR_MONITOR_POINT_ID` | `smart-home-aux1-a1-traction-point1` |
| `SIMULATOR_SCENE` | `智慧家园小区` |
| `SIMULATOR_PRODUCT_MODEL` | `奥克斯1` |
| `SIMULATOR_ELEVATOR_INSTANCE` | `电梯-A1` |
| `SIMULATOR_GBOM_LEVEL_2` | `曳引机` |
| `SIMULATOR_GBOM_LEVEL_3` | `测点1` |
| `SIMULATOR_MEASUREMENT` | `智慧家园小区` |
| `SIMULATOR_FIELD` | `奥克斯1` |
| `SIMULATOR_RANDOM_SEED` | `20260720` |

每个环境变量也可以用 Java 系统属性配置。例如：

```powershell
java -Dsimulator.interval.seconds=30 -jar target/elevator-monitoring-simulator.jar
```

## InfluxDB 记录

每次发送一条类似下面的 Line Protocol：

```text
智慧家园小区,monitor_point_id=smart-home-aux1-a1-traction-point1,elevator_instance=电梯-A1,gbom_level_2=曳引机,gbom_level_3=测点1 奥克斯1="[0.1,0.2,...完整1024点...]" 1784512800000
```

时间戳精度为毫秒，表示该批数据发送时间。平台可以根据 `startTime/endTime` 限定范围，按
平台按 `monitor_point_id`、时间范围和 `奥克斯1` 字段读取最新一条记录，再解析其中的
1024 个数值。

同批 Kafka 消息如下，其中 `dc_data` 与上面 InfluxDB 字段方括号内的数据完全一致，
`dc_time` 与 InfluxDB Line Protocol 末尾时间戳完全一致：

```json
{"dc_data":"0.10000000,0.20000000,...完整1024点...","dc_time":1784512800000,"id":218}
```
