import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

public class Pigzj {

    private static int threadCount = 4;
    public static final int BLOCK_SIZE = 131072;
    private static ExecutorService executor = Executors.newFixedThreadPool(threadCount);

    public static void writeToStdOut(byte[] data) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(System.out);
        DataOutputStream dos = new DataOutputStream(bos);
        dos.write(data);
        dos.flush();
        bos.flush();
    }

    public static byte[] computeHeader() throws IOException {
        ByteArrayOutputStream headerBytes = new ByteArrayOutputStream();
        GZIPOutputStream headerStream = new GZIPOutputStream(headerBytes);
        headerStream.write(Deflater.BEST_COMPRESSION);
        headerStream.write(0); // Compression method (always 8 for deflate)
        headerStream.write(0); // Flags (none set)
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt((int) (System.currentTimeMillis() / 1000L));
        headerStream.write(buffer.array()); // Modification time
        headerStream.write(0); // Extra flags (none set)
        headerStream.write(255); // Operating system (unknown)
        return headerBytes.toByteArray();
    }
    

    public static byte[] computeTrailer(long crcValue, long dataLength) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // add CRC32 checksum
        baos.write((int) (crcValue & 0xff));
        baos.write((int) ((crcValue >> 8) & 0xff));
        baos.write((int) ((crcValue >> 16) & 0xff));
        baos.write((int) ((crcValue >> 24) & 0xff));
        // add uncompressed data length
        baos.write((int) (dataLength & 0xff));
        baos.write((int) (dataLength >> 8) & 0xff);
        baos.write((int) (dataLength >> 16) & 0xff);
        baos.write((int) (dataLength >> 24) & 0xff);
        return baos.toByteArray();
    }

    public static void setThreadCount(String[] args) {
        if (args.length > 0) {
            try {
                if (args[0].equals("-p")) {
                    threadCount = Integer.parseInt(args[1]);
                } else {
                    System.err.println("Invalid argument: " + args[0]);
                    System.exit(1);
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid thread count: " + args[0]);
                System.exit(1);
            }
        }
        
    }

    private static Future<byte[]> compress(byte[] input) {        
        return executor.submit(() -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
            DeflaterOutputStream dos = new DeflaterOutputStream(baos, deflater);
    
            dos.write(input);
            dos.finish();
            dos.close();
    
            return baos.toByteArray();
        });
    }

    public static void main(String[] args) throws Exception {
        setThreadCount(args);
        InputStream input = System.in;
        ByteArrayOutputStream fullFile = new ByteArrayOutputStream();
        byte[] inputBuff = new byte[BLOCK_SIZE * threadCount];

        // Compute and write header
        byte[] header = computeHeader();
        writeToStdOut(header);
        
        boolean fileDone = false;
        for ( ; !fileDone; ) {
            // Read bytes into inputBuff
            int nBytes = input.readNBytes(inputBuff, 0, inputBuff.length);
            int nChunks = nBytes / BLOCK_SIZE;
            int nBytesLastChunk = nBytes % BLOCK_SIZE;
            if (nBytesLastChunk != 0) {
                nChunks++;
                fileDone = true;
            } else if (nBytes == 0) {
                fileDone = true;
            }

            // Update fullFile with inputBuff
            fullFile.write(inputBuff, 0, nBytes);

            // Create threads to compress chunks
            int nFutures = Math.min(nChunks, threadCount);
            Future<byte[]>[] chunkTasks = new Future[nFutures];

            // Compress chunks by running threads
            for (int i = 0; i < nFutures; i++) {
                // Ensure last chunk is correct size
                int chunkSize = BLOCK_SIZE;
                if (i == nChunks - 1 && nBytesLastChunk != 0) {
                    chunkSize = nBytesLastChunk;
                }

                // Make chunk, copy bytes from inputBuff into it
                byte[] chunk = new byte[chunkSize];
                System.arraycopy(inputBuff, i * BLOCK_SIZE, chunk, 0, chunkSize);

                // Compress chunk
                chunkTasks[i] = compress(chunk);
            }

            // Retrieve bytes from future tasks
            for (int i = 0; i < nFutures; i++) {
                writeToStdOut(chunkTasks[i].get());
            }
        }

        // compute CRC32 checksum and trailer, write to output
        CRC32 crc32 = new CRC32();
        crc32.update(fullFile.toByteArray());
        long crcValue = crc32.getValue();
        byte[] trailer = computeTrailer(crcValue, fullFile.toByteArray().length);
        writeToStdOut(trailer);
        
        // Clean up executor
        executor.shutdown();
    }
}