#! /bin/zsh
input=./hello.txt
time gzip <$input >gzip.gz
# time pigz <$input >pigz.gz
time java Pigzj <$input >Pigzj.gz
# time ./pigzj <$input >pigzj.gz
# ls -l gzip.gz pigz.gz Pigzj.gz pigzj.gz

# This checks Pigzj's and pigzj's output.
gzip -d <Pigzj.gz | cmp - $input
# gzip -d <pigzj.gz | cmp - $input
# gzip -d <pigzj.gz | cmp - $input