# testfile_clang.txt 需要引入库函数
cp ../testfile.txt testfile_clang.txt
sed -i '1 i #include "libsysy/libsysy.h"' testfile_clang.txt
# 使用 clang 导出 LLVM IR
clang -x c -emit-llvm -S testfile_clang.txt -o llvm_ir_clang.txt
# 链接 llvm_ir_clang.txt 与 libsysy.ll 并运行
llvm-link llvm_ir_clang.txt libsysy/libsysy.ll -o out_clang.ll
lli out_clang.ll
rm -f out_clang.ll