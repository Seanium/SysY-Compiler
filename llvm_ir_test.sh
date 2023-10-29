# 单独运行 llvm_ir.txt
#lli llvm_ir.txt
#echo $?

# 链接 llvm_ir.txt 与 lib.ll 并运行
llvm-link llvm_ir.txt lib.ll -o out.ll
lli out.ll