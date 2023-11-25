#!/bin/bash

example_dir="2023代码生成辅助库/A/"
my_ir_output_dir="my_ir_output/A/"
file_index_begin=1
file_index_end=15 #A
#file_index_end=18 #B,C

jar_file="../out/artifacts/compiler_jar/compiler.jar"

for i in $(seq "$file_index_begin" "$file_index_end")
do
  test_file="${example_dir}testfile${i}.txt"
  input_file="${example_dir}input${i}.txt"
  output_file="${example_dir}output${i}.txt"
  my_ir_file="${my_ir_output_dir}my_ir${i}.txt"
  my_ir_output_file="${my_ir_output_dir}my_ir${i}_output.txt"

  echo "testing $test_file"

  # 生成中间代码
  cp "$test_file" testfile.txt
  java -jar "$jar_file"
  mv llvm_ir.txt "$my_ir_file"
  # 链接 llvm_ir.txt 与 libsysy.ll 并运行
  llvm-link "$my_ir_file" libsysy/libsysy.ll -o out.ll
  lli out.ll < "$input_file" > "$my_ir_output_file"
  rm -f out.ll

  # 比较输出差异(去除行尾cr后比较）
  diff_output=$(diff --strip-trailing-cr "$my_ir_output_file" "$output_file")
  if [ $? -eq 0 ]; then
    echo "Accept"
  else
    echo "Wrong Answer"
    echo "$diff_output"
  fi

done