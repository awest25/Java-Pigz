# Java-Pigz - Multithreaded Gzip Compression in Java

**Language**: Java <br/>
**Date**: Feb 2023 <br/>
**Repository**: [github.com/awest25/Java-Pigz](https://github.com/awest25/Java-Pigz)

## Overview

Pigzj is a Java program inspired by the C `pigz` implementation. It performs multithreaded compression, aiming to improve wall-clock performance. By breaking the input into fixed-size blocks and using multiple threads for compression, Pigzj ensures that compressed output blocks are generated in the order of their uncompressed input. This project provides a stripped-down version that focuses solely on `pigz`-style multithreaded compression.

## Key Features

- **Multithreaded Compression**: Uses multiple threads for compressing input data blocks, each of size 128 KiB.
- **Priming Compression Dictionary**: Utilizes the last 32 KiB of the previous block to prime the compression dictionary, resulting in enhanced compression for subsequent blocks.
- **GZIP Standard Compliance**: Ensures that the output strictly follows the GZIP file format standard as per Internet RFC 1952.
- **Performance Comparison**: Built to compare performance between native Java and GraalVM's `native-image`.

## Simplifications from `pigz`

- Compression-only: Pigzj solely focuses on compression, omitting decompression.
- Limited option support: Supports only the `-p processes` option from `pigz`.
- Streamlined I/O: Always reads from standard input and writes to standard output. No filename arguments are allowed.
- Error Handling: Custom error messages and handling, without strictly mimicking `pigz`.

## Technical Stack

- **Environment**: Recommended development and testing on Seasnet Linux server.
- **Java Version**: Runs under GraalVM 22.3.1 with OpenJDK 17.0.6.
- **GZIP Compliance**: Adheres to the GZIP file format standard (RFC 1952).

## Getting Started

1. Clone the repo: `git clone https://github.com/awest25/Pigzj.git`
2. Compile the program.
3. Run Pigzj, redirecting input and output appropriately.
4. For performance comparisons, also build using GraalVM's `native-image` and compare timings.

## Building

```bash
# Compiling the standard program.
jar xf hw3.jar <br/>
javac Pigzj.java <br/>

# The following will generate an optimized version.
native-image -o pigzj Pigzj
```

## Sample Usage

```bash
input=/usr/local/cs/graalvm-ce-java17-22.3.1/lib/modules
time gzip <$input >gzip.gz
time pigz <$input >pigz.gz
time java Pigzj <$input >Pigzj.gz
time ./pigzj <$input >pigzj.gz
ls -l gzip.gz pigz.gz Pigzj.gz pigzj.gz

# Checking Pigzj's and pigzj's output.
gzip -d <Pigzj.gz | cmp - $input
gzip -d <pigzj.gz | cmp - $input
```

## Further Notes

For a complete assessment, refer to the after-action report provided within the repository. This comprehensive report evaluates the performance, potential scalability issues, and general observations based on various trials.
