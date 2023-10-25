#!/bin/zsh
rm -f output Pigzj.class output.compressed
javac Pigzj.java
java Pigzj <hello.txt >output.gz
cp output.gz output.compressed
gzip -d output.gz