# Feature Specification: ECDICT 词频前 6 万取词表

**Feature Branch**: `010-ecdict-freq-60k`

**Created**: 2026-10-25

**Status**: Code complete — client verify pending

**Input**: 保持 ECDICT 优先、Ollama 其次；将捆绑词表扩至按当代词频（ECDICT `frq`）排序的前约 6 万 lemma，避免随机/生僻堆砌。

## Clarifications

- 排序键：ECDICT **`frq`**（当代语料库词频名次，数值越小越常见）。
- 选取：`0 < frq ≤ 60000` 且有中文 `translation` 的词头；不足 6 万则以有 `frq` 的全部为准并记录实际条数。
- 不使用商业 COCA；许可 MIT，附 NOTICE。
- 查找顺序不变：本地词表 → Ollama。

## User Scenarios & Testing

### User Story 1 - 高频词离线可查 (P1)

玩家定格取词悬停当代高频英文词（含常见变形经 exchange/启发式），多数立即出现词典卡片，无需等待 Ollama。

**Acceptance**:

1. 捆绑表按 `frq` 截取，约 6 万 lemma（或文档标明实际 N）。
2. 常见词如 `into` / `claim` / `tucked`（经还原）可离线命中。
3. 加载失败时仍可降级为空表 + Ollama（不崩溃）。

### User Story 2 - 构建可重复 (P2)

维护者可从官方 ECDICT CSV 重新生成 TSV，步骤写在 `tools/dictionary/README.md`。

## Requirements

- **FR-001**: MUST 从 ECDICT 衍生，按 `frq` 选取高频前约 60000 lemma，MUST NOT 随机抽样。
- **FR-002**: 输出 MUST 兼容现有 rich TSV（word/phonetic/pos/exchange/translation）。
- **FR-003**: MUST 保留 MIT/ECDICT 许可声明。
- **FR-004**: 取词路径 MUST 仍先查本地再 Ollama。
- **FR-005**: 构建脚本 MUST 可重复运行；生成物可检入或由文档说明生成方式。

## Success Criteria

- **SC-001**: 表内 lemma 数 ≥ 50000（目标 60000；若源数据 `frq` 不足则接近上限并文档说明）。
- **SC-002**: 抽样高频词离线命中；`frq` 很大的生僻词不因「凑满 6 万」被优先选入。
- **SC-003**: 客户端能加载词表且定格取词不因体积崩溃。

## Assumptions

- 源：`https://github.com/skywind3000/ECDICT` 的 `ecdict.csv`（或等价 release）。
- 英文 `definition` 不打进模组以控体积；保留中文释义与 exchange。
- jar 内明文 TSV 可接受（约数 MB–十余 MB 未压缩级）；可选后续再 gzip。
