# SysY-Compiler

- 使用 Java 开发的 **[SysY](https://gitlab.eduxiji.net/nscscc/compiler2021/-/blob/master/SysY语言定义.pdf) to MIPS** 编译器。

- 中间代码采用 **LLVM IR**。

## 编译器总体设计

### 总体结构

本编译器的总体结构分为**前端、中端、后端**三个部分。

- **前端**：词法分析，语法分析，错误处理。
- **中端**：中间代码生成，中端优化，寄存器分配。
  - **中端优化**：基本块简化，支配信息分析，Mem2Reg，函数内联，函数副作用分析，死代码删除，循环深度分析，全局值标号（GVN），常量折叠，全局代码移动（GCM），Phi指令消除，活跃变量分析，寄存器分配。

- **后端**：目标代码生成，后端优化。
  - **后端优化**：窥孔优化，乘除法优化。


### 文件组织

```
src		# 编译器
├── Compiler.java	# 编译器主程序
├── frontend		# 前端
│   ├── error			# 错误处理
│   ├── node			# 语法树结点
│   ├── symbol			# 前端符号表
│   └── token			# 词法单元
├── midend			# 中端
│   ├── ir				# 中间代码生成（LLVM IR）
│   │   ├── inst			# 指令
│   │   ├── symbol			# 中端符号表
│   │   └── type			# 类型
│   └── pass			# 中端优化
├── backend			# 后端
│   ├── mips			# 目标代码生成（MIPS）
│   │   ├── data			# MIPS data段
│   │   ├── record			# 内存管理与分配
│   │   └── text			# MIPS text段
│   └── pass			# 后端优化
├── utils			# 工具（IO和配置）
│
test	# 测试脚本
├── check_ir.sh		# 检查中间代码
├── check_mips.sh	# 检查目标代码
├── run_clang.sh	# 执行 Clang 导出的 LLVM IR
└── run_my.sh		# 执行编译器生成的 LLVM IR
```

### 接口设计

介绍为编译器主程序 `Compiler.main()` 设计的接口，按照编译阶段分类。

#### 词法分析

``` 
Lexer.getInstance()		// 获得词法分析器单例
lexer.tokenize()		// 进行一遍完整的词法分析，获得token列表
lexer.tokensToString()	// token列表转字符串，用于输出
```

#### 语法分析

```
Parser.getInstance()	// 获得语法分析器单例
parser.parseCompUnit()	// 解析编译单元，生成语法分析树
compUnitNode.toString()	// 语法分析树转字符串，用于输出
```

#### 错误处理

```
ErrorList.getInstance()	// 获得错误表单例
errorList.addError()	// 添加错误项
```

#### 中间代码生成

```
IRGenerator.getInstance()		// 获得中间代码生成器单例
irGenerator.visitCompUnitNode()	// 解析编译单元，生成中间代码结构
module.toString()				// 中间代码结构转字符串，用于输出
```

#### 中端优化

```
IROptimizer.getInstance()	// 获得中端优化器单例
irOptimizer.runPasses()		// 进行中端优化
```

#### 目标代码生成

```
mipsGenerator.visitModule()		// 解析中间代码结构，生成目标代码结构
mipsGenerator.getCurMIPSFile()	// 获得目标代码结构
mipsFile.toString()				// 目标代码结构转字符串，用于输出
```

#### 后端优化

```
mipsOptimizer.runPasses()	// 进行后端优化
```

#### 工具

```
Config.setMode(Mode mode)						// 设置 DEBUG/RELEASE 模式
FileIO.read(String filename)					// 输入文件名，输出对应文件内容的字符串形式
FileIO.write(String filename, String content)	// 将指定字符串写入文件名对应的文件
```

