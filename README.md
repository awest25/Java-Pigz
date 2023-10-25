Configuration:
pigz v 2.4
gzip v 1.9
javac 1.8.0_312
GraalVM v 22.3.1

To build both versions:
jar xf hw3.jar
javac Pigzj.java
native-image -o pigzj Pigzj

Analysis
In order to evaluate the performance of gzip, pigz, OpenJDK Pigzj, and GraalVM
 pigzj, several tests were run. In the first test I executed each method 
 without specifying the -p flag. The results showed that gzip was the slowest,
  taking over 10 seconds, while pigz performed the best, taking only about 
  3.6 seconds. Subsequent tests were conducted on pigz, Pigzj, and pigzj using
   a range of processor counts. Pigz peaked around 1 second at 4 processors, 
   Pigzj peaked around 1 second at 4 processors, and pigzj peaked around 1.4 
   seconds at 4 processors. Thus, Pigzj emerged as the clear winner in terms 
   of increased processor count.

Strace: The system call traces generated using strace were used to compare 
and contrast the four programs. The majority syscall used by each program 
revealed that gzip mainly uses write, pigz mainly uses read, and both Pigzj 
and pigzj mainly use futex. Futex is a syscall that is often used for blocking
 in multi-threaded applications. In my implementation, an ExecutorService 
 object was used to read a bounded block of input and send that block for 
 asynchronous execution. After all input was processed, the result of each 
 thread was output by accessing the associated Future object. Since the main
  thread may wait for the first block of data to compress, the program mainly
   uses futex. Despite this, it is much more efficient than singly-threaded 
   programs like gzip.

As the file size increases, the multi-threaded programs are expected to be 
more efficient compared to sequential programs. This was observed in the 
trials, where gzip performed worse than all other implementations. A 
limitation is that there may be a few bottlenecks such as reading from 
input and creating the trialer with the giant file in memory. This could
 be addressed with changes to the implimentation.

Increasing the number of processors could potentially be counterproductive.
 Increasing the number of threads used may have overhead greater than the 
 amount of work done in each thread. Since blocking threads is already the 
 main syscall used in Pigzj and pigzj, more threads would mean more blocking,
  which may hurt performance if increased past the "sweet-spot". But in 
  general, performance with increased threads scales well.



Some outputs of the timing script:

Processors = 1 (Default)
gzip:
real    0m11.787s   0m11.482s   0m11.644s
user    0m10.081s   0m10.307s   0m10.102s
sys     0m0.117s    0m0.121s    0m0.114s

pigz:
real    0m3.663s    0m3.793s    0m3.858s
user    0m10.458s   0m10.404s   0m10.531s
sys     0m0.098s    0m0.081s    0m0.072s

OpenJDK Pigzj:
real    0m3.826s    0m3.739s    0m3.878s
user    0m10.715s   0m10.115s   0m10.329s
sys     0m0.500s    0m0.514s    0m0.488s

GraalVM pigzj:
real    0m4.014s    0m4.052s    0m4.122s
user    0m10.807s   0m10.846s   0m10.887s
sys     0m0.734s    0m0.736s    0m0.691s


Processors = 2
gzip:
real    0m5.894s    0m6.033s    0m6.157s
user    0m10.535s   0m10.562s   0m10.593s
sys     0m0.130s    0m0.140s    0m0.129s

pigz:
real    0m1.831s    0m1.876s    0m1.929s
user    0m10.874s   0m10.917s   0m10.996s
sys     0m0.193s    0m0.186s    0m0.162s

OpenJDK Pigzj:
real    0m1.852s    0m1.795s    0m1.844s
user    0m10.468s   0m10.461s   0m10.473s
sys     0m0.998s    0m0.970s    0m0.918s

GraalVM pigzj:
real    0m1.912s    0m1.981s    0m1.935s
user    0m10.984s   0m11.024s   0m11.078s
sys     0m0.888s    0m0.904s    0m0.865s

Processors = 4
gzip:
real    0m3.579s    0m4.857s    0m3.743s
user    0m10.535s   0m10.562s   0m10.593s
sys     0m0.130s    0m0.140s    0m0.129s

pigz:
real    0m0.915s    0m1.042s    0m1.041s
user    0m10.874s   0m10.917s   0m10.996s
sys     0m0.193s    0m0.186s    0m0.162s

OpenJDK Pigzj:
real    0m1.068s    0m1.025s    0m1.038s
user    0m10.468s   0m10.461s   0m10.473s
sys     0m0.998s    0m0.970s    0m0.918s

GraalVM pigzj:
real    0m1.496s    0m1.445s    0m1.470s
user    0m10.984s   0m11.024s   0m11.078s
sys     0m0.888s    0m0.904s    0m0.865s

Processors = 8
gzip:
real    0m4.400s    0m5.986s    0m4.607s
user    0m12.836s   0m12.863s   0m12.893s
sys     0m0.156s    0m0.168s    0m0.154s

pigz:
real    0m1.117s    0m1.271s    0m1.271s
user    0m13.320s   0m13.357s   0m13.434s
sys     0m0.238s    0m0.232s    0m0.202s

OpenJDK Pigzj:
real    0m1.292s    0m1.237s    0m1.252s
user    0m12.822s   0m12.815s   0m12.827s
sys     0m1.197s    0m1.161s    0m1.098s

GraalVM pigzj:
real    0m1.872s    0m1.809s    0m1.838s
user    0m13.672s   0m13.716s   0m13.770s
sys     0m1.182s    0m1.261s    0m1.295s