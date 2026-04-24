package com.waylau.java.demo.nio;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * NonBlockingEchoClient 非阻塞IO 客户端 
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/25
**/
public class NonBlockingEchoClient {
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

        // 创建非阻塞IO客户端SocketChannel
        try (var client = SocketChannel.open()) {
            // 设置为非阻塞模式
            client.configureBlocking(false);
            System.out.println("NonBlockingEchoClient启动，连接：" + host + ":" + port);

            var address = new java.net.InetSocketAddress(host, port);
            // 非阻塞连接需要处理连接过程
            client.connect(address);

            // 等待连接完成
            while (!client.finishConnect()) {
                // 可以做一些其他工作
                Thread.sleep(10);
            }

            while (true) {
                System.out.print("请输入：");
                var reader = new java.util.Scanner(System.in);
                var line = reader.nextLine();
                client.write(java.nio.ByteBuffer.wrap(line.getBytes()));

                var buffer = java.nio.ByteBuffer.allocate(1024);

                // 对于非阻塞模式，应该循环读取直到没有数据可读
                while (true) {
                    int bytesRead = client.read(buffer);
                    if (bytesRead > 0) {
                        buffer.flip();
                        byte[] data = new byte[buffer.remaining()];
                        buffer.get(data);
                        System.out.println("Echo：" + new String(data));
                        buffer.clear();
                        System.out.println("--------------------------------------------------------------------------------");
                        System.out.println("Echo完毕");
                        break; // 读取到数据后退出循环
                    } else if (bytesRead == 0) {
                        // 暂时没有数据可读，可以短暂等待
                        Thread.sleep(10);
                    } else {
                        // 连接已关闭
                        System.out.println("连接已关闭");
                        break;
                    }
                }
                System.out.println("--------------------------------------------------------------------------------");
                System.out.println("等待下一条输入...");
                System.out.println("--------------------------------------------------------------------------------");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
