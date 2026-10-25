# Research: 010

## 词频字段

- **Decision**: 用 ECDICT `frq`（当代语料库名次），取 `1 ≤ frq ≤ 60000`。
- **Rationale**: 用户要求当代高频，非 BNC 历史名著向；名次越小越常见。
- **Alternatives**: 仅 `bnc`——偏传统；`frq|bnc` 并集——会混入策略不清。

## 体积

- **Decision**: 先明文 TSV 检入；去掉英文 definition。
- **Rationale**: 6 万条约数 MB–10MB+ 未压缩，jar DEFLATE 后通常更小；实现简单。
- **Alternatives**: `.tsv.gz` 运行时解压——可作后续优化。

## 源获取

- **Decision**: 构建脚本支持本地路径或从 GitHub raw/release 下载 `ecdict.csv`。
- **Rationale**: 66MB 不宜每次 CI 必下；开发者本机生成后检入产物。
