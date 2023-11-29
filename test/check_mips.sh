#!/bin/bash

#example_dir="2022-test-1105/full/A/"
#my_mips_output_dir="my_mips_output_2022/A/"
example_dir="2023代码生成辅助库/A/"
my_mips_output_dir="my_mips_output/A/"
file_index_begin=1
file_index_end=15 #A
#file_index_end=18 #B,C

mars_file="mars.jar"
jar_file="../out/artifacts/compiler_jar/compiler.jar"

for i in $(seq "$file_index_begin" "$file_index_end")
do
  test_file="${example_dir}testfile${i}.txt"
  input_file="${example_dir}input${i}.txt"
  output_file="${example_dir}output${i}.txt"
  my_mips_file="${my_mips_output_dir}my_mips${i}.txt"
  my_mips_output_file="${my_mips_output_dir}my_mips${i}_output.txt"
  my_mips_score_file="${my_mips_output_dir}my_mips${i}_score.txt"

  echo "testing $test_file"

  # 生成mips代码
  cp "$test_file" testfile.txt
  java -jar "$jar_file"
  mv mips.txt "$my_mips_file"
  # mars执行mips代码，获取输出
  java -jar "$mars_file" "$my_mips_file" < "$input_file" > "$my_mips_output_file"

  # 删除前两行无效信息
  sed -i '1,2d' "$my_mips_output_file"
  # 比较输出差异(去除行尾cr后比较）
  diff_output=$(diff --strip-trailing-cr "$my_mips_output_file" "$output_file")
  if [ $? -eq 0 ]; then
    echo "Accept"
  else
    echo "Wrong Answer"
    echo "$diff_output"
  fi

  # 保存目标代码统计信息
  mv InstructionStatistics.txt "$my_mips_score_file"

done