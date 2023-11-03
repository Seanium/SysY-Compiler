# 单独运行 llvm_ir.txt
#lli ../llvm_ir.txt
#echo $?

# 链接 llvm_ir.txt 与 libsysy.ll 并运行
llvm-link ../llvm_ir.txt libsysy/libsysy.ll -o out.ll
lli out.ll