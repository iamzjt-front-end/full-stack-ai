package com.waylau.java.demo.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;

/**
 * AsyncEchoClient 异步IO Echo客户端
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/25
 **/
public class AsyncEchoClient {
    private static final int DEFAULT_PORT = 7;
    public static final String DEFAULT_HOST = "localhost";

    public static void main(String[] args) {
        // 解析传参进来的服务名、端口号
        String host;
        int port;
        try {
            host = args[0];
            port = Integer.parseInt(args[1]);
        } catch (Exception e) {
            host = DEFAULT_HOST;
            port = DEFAULT_PORT;
        }

        // 创建AsynchronousSocketChannel
        try (var channel = java.nio.channels.AsynchronousSocketChannel.open()) {
            System.out.println("AsyncEchoClient启动，连接：" + host + ":" + port);

            // 等待连接完成
            var connectFuture = channel.connect(new InetSocketAddress(host, port));
            // 等待连接建立完成
            connectFuture.get();

            var scanner = new java.util.Scanner(System.in);

            while (true) {
                System.out.print("请输入：");
                var line = scanner.nextLine();

                if (line == null || line.isEmpty()) {
                    continue;
                }

                // 写入数据
                var writeFuture = channel.write(java.nio.ByteBuffer.wrap(line.getBytes()));
                // 等待写入完成
                writeFuture.get();

                // 读取响应
                var buffer = java.nio.ByteBuffer.allocate(1024);
                var readFuture = channel.read(buffer);
                // 等待读取完成
                int bytesRead = readFuture.get();

                if (bytesRead > 0) {
                    buffer.flip();
                    byte[] data = new byte[buffer.remaining()];
                    buffer.get(data);
                    System.out.println("Echo：" + new String(data));
                    System.out.println("--------------------------------------------------------------------------------");
                    System.out.println("Echo完毕");
                    System.out.println("--------------------------------------------------------------------------------");
                } else if (bytesRead == -1) {
                    System.out.println("服务器连接已关闭");
                    break;
                }

                System.out.println("等待下一个输入...");
                System.out.println("--------------------------------------------------------------------------------");
            }
        } catch (ExecutionException e) {
            System.out.println("操作执行异常: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("操作被中断: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO异常: " + e.getMessage());
        }
    }
}
