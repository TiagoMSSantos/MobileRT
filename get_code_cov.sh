#!/bin/bash
for filename in `find ./app -not -path "*/third_party/*" -not -path "*/.cxx/*" -not -path "*/*build*/*" | egrep '.cpp'`;
do
  gcov -n -o ./build_Release $filename > /dev/null;
done
