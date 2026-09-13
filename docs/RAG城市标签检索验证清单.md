# RAG 城市标签检索验证清单

配套文档：`武汉_/广州_/深圳_` 各两份（管理规定 + 费用与结算细则），以及 `通用_平台服务通用条款.docx`。
城市标签的 metadata 字段名为 `city`，取值为 `武汉` / `广州` / `深圳` / `通用`。

## 0. 前置准备

1. 启动中间件（Redis / Qdrant / MinIO）与 AI 服务：

   ```powershell
   docker compose -f ai/docker-compose.yml up -d
   $env:DASHSCOPE_API_KEY = '<你的百炼 Key>'
   .\mvnw.cmd -pl ai/backend -am spring-boot:run
   ```

2. 给 `city` 字段建 keyword payload 索引（只需一次，过滤才走索引而不是全量扫描）：

   ```powershell
   Invoke-RestMethod -Method Put -Uri 'http://192.168.205.128:6333/collections/zza_knowledge/index' `
     -ContentType 'application/json' `
     -Body '{"field_name":"city","field_schema":"keyword"}'
   ```

3. 在 Swagger（<http://localhost:8083/doc.html>）按「知识库 → 上传文档」逐份上传，`category` 与 `city` 按下表填写：

   | 文件 | category | city |
   | --- | --- | --- |
   | 武汉_公寓租房管理规定.docx | 租赁规定 | 武汉 |
   | 武汉_费用与结算细则.docx | 费用说明 | 武汉 |
   | 广州_公寓租房管理规定.docx | 租赁规定 | 广州 |
   | 广州_费用与结算细则.docx | 费用说明 | 广州 |
   | 深圳_公寓租房管理规定.docx | 租赁规定 | 深圳 |
   | 深圳_费用与结算细则.docx | 费用说明 | 深圳 |
   | 通用_平台服务通用条款.docx | 平台通用 | 通用 |

   `city` 留空时默认按 `通用` 入库，因此平台级文档也可以直接不填。

## 1. 切片验证

上传后用 `GET /api/knowledge/documents` 逐条核对 `chunkCount` 与 `status=INDEXED`：

| 文档 | 正文字数（约） | 期望切片数 |
| --- | --- | --- |
| 各城市_公寓租房管理规定.docx | 3700 | 4～7 |
| 各城市_费用与结算细则.docx | 2400 | 3～5 |
| 通用_平台服务通用条款.docx | 1850 | 2～4 |

检查点：

- 7 份文档合计切片数应落在 24～36；明显偏少说明存在超长切片被合并，偏多说明有碎片片段。
- 数字完整性：`押金 1 个月 / 1.5 个月 / 2 个月`、`退租提前 30/30/15 天`、`报修上门 48/24/72 小时`、`押金退还 7/5/10 个工作日` 这些关键数字应完整落在同一个切片内，不能被切在两个 chunk 之间。
- 用 `POST /api/knowledge/search` 传 `{"query":"押金要交几个月？"}`（不传 city），确认每个城市的条款都能被检索到，说明切片携带了正文内容。

## 2. 三城过滤对照实验

每组做两次检索：一次不传 `city`（基线，预期出现多城条款混杂），一次传 `city`（预期只出现该城 + 通用）。

| 问题 | 不选城市（基线） | 选武汉 | 选广州 | 选深圳 |
| --- | --- | --- | --- | --- |
| 押金要交几个月？ | 三城条款混杂 | 1 个月租金 | 1.5 个月租金 | 2 个月租金 |
| 退租要提前多久申请？ | 混杂 | 30 天 | 30 天 | 15 天 |
| 提前退租违约金怎么算？ | 混杂 | 1 个月租金 | 1 个月租金 | 半个月租金 |
| 报修多久上门？ | 混杂 | 24 小时响应 / 48 小时上门 | 12 小时 / 24 小时 | 24 小时 / 72 小时 |
| 押金多久退还？ | 混杂 | 7 个工作日 | 5 个工作日 | 10 个工作日 |
| 服务费怎么收？ | 混杂 | 租金 8% | 租金 10% | 租金 6% |
| 可以养宠物吗？ | 混杂 | 允许已登记猫与小型犬 | 不接受宠物 | 允许猫，宠物押金 500 元 |
| 可以转租吗？ | 混杂 | 可转租，半个月租金手续费 | 不接受转租，可换房 | 可转租，半个月租金手续费 |
| 逾期滞纳金怎么算？ | 混杂 | 第 3 日起每日万分之五 | 第 5 日起每日万分之三 | 第 3 日起每日千分之一 |
| 水电燃气单价多少？ | 混杂 | 0.58 / 3.20 / 2.60 | 0.63 / 3.50 / 3.10 | 0.68 / 4.00 / 3.45 |

检索示例（Swagger 的「语义检索调试」或直接 POST）：

```json
{ "query": "押金要交几个月？", "city": "武汉", "topK": 5 }
```

判定标准：

- 传 `city=武汉` 时，返回结果里 `city` 字段只应出现 `武汉` 与 `通用`，出现广州或深圳即判定过滤未生效。
- 不传 `city` 时若只命中单一城市，说明向量区分度足够高、过滤价值看起来不明显；此时重点看「押金」「退租提前告知」这类仅数字不同的条款，它们最容易串城。
- `score` 用于判断是否被相似度阈值（默认 0.5）挡掉：若某城市条款一条都不返回，先确认是阈值问题而不是过滤问题。

## 3. 通用条款叠加验证

选任一城市后提问平台级问题（不涉及城市数字），预期命中 `通用_平台服务通用条款.docx`：

| 问题 | 期望来源 |
| --- | --- |
| 怎么预约看房？ | 通用文档第 2 章（任一城市下都应命中） |
| 投诉多久给反馈？ | 通用文档第 3 章 |
| 你们怎么保护我的个人信息？ | 通用文档第 4 章 |

若这些问题的结果里只剩城市文档、看不到通用文档，检查过滤条件是否退化成只匹配 `city == 选中城市`。

## 4. 兜底行为验证

- 用一个没有对应文档的城市标签检索（例如 `city=北京`），预期：第一次过滤无命中，服务自动回退为不带过滤的检索并返回结果，日志里能看到回退记录。
- 把 `app.rag.fallback-to-unfiltered-when-empty` 改成 `false` 重启后再试，预期返回空来源（`sources` 为空），模型基于自身知识回答。

## 5. Qdrant 数据层核查

按城市统计切片数（全部城市之和应等于各文档 `chunkCount` 之和）：

```powershell
$body = '{"filter":{"must":[{"key":"city","match":{"value":"武汉"}}]},"exact":true}'
Invoke-RestMethod -Method Post -Uri 'http://192.168.205.128:6333/collections/zza_knowledge/points/count' `
  -ContentType 'application/json' -Body $body
```

抽查切片 payload，确认每片都带 `city`、`category`、`fileName`、`chunkIndex`：

```powershell
$body = '{"filter":{"must":[{"key":"city","match":{"value":"通用"}}]},"limit":3,"with_payload":true}'
Invoke-RestMethod -Method Post -Uri 'http://192.168.205.128:6333/collections/zza_knowledge/points/scroll' `
  -ContentType 'application/json' -Body $body
```

多城市一次命中（模拟"选中城市 + 通用"的过滤条件）：

```powershell
$body = '{"filter":{"must":[{"key":"city","match":{"any":["广州","通用"]}}]},"exact":true}'
Invoke-RestMethod -Method Post -Uri 'http://192.168.205.128:6333/collections/zza_knowledge/points/count' `
  -ContentType 'application/json' -Body $body
```

## 6. 注意事项

- 城市标签在**上传时写入**：修改文档内容或城市后需要重新上传（或调用 `rebuild` 前先把 `city` 参数补齐），只改文件名不会改变切片上的标签。
- 之前已入库、没有 `city` 字段的历史切片不会被"选中城市 + 通用"的条件召回，只能在不选城市时检索到；调用 `rebuild` 重建即可自动补成 `通用`。
- `category` 与 `city` 相互独立：`category` 表示文档类别（租赁规定 / 费用说明 / 平台通用），`city` 表示适用城市，可组合使用。
- 换向量模型会改变维度，需要删掉 Qdrant 集合重新入库。
