package com.waylau.java.demo.net;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * BlockingEchoServer 阻塞IO Echo服务器
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/25
 **/
public class BlockingEchoServer {

    private static final int DEFAULT_PORT = 7;

    public static void main(String[] args) {
        // 解析传参进来的端口号
        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            port = DEFAULT_PORT;
        }

        // 创建ServerSocket
        try (var serverSocket = new ServerSocket(port)) {
            System.out.println("BlockingEchoServer启动，监听端口：" + port);

            // 接受客户端连接，生成Socket实例
            while (true) {
                try (var clientSocket = serverSocket.accept()) {

                    System.out.println("接收到客户端连接：" + clientSocket.getInetAddress());
                    while (true) {
                        var input = clientSocket.getInputStream();
                        var output = clientSocket.getOutputStream();
                        var buffer = new byte[1024];

                        // 读取客户端的信息
                        var bytesRead = input.read(buffer);
                        if (bytesRead == -1) {
                            break;
                        }

                        // 发送信息给客户端
                        output.write(buffer, 0, bytesRead);

                        System.out.println("Echo：" + new String(buffer, 0, bytesRead));
                        System.out.println("Echo完毕");
                        System.out.println("--------------------------------------------------------------------------------");
                        System.out.println("等待下一个客户端连接...");
                        System.out.println("--------------------------------------------------------------------------------");
                    }
                } catch (IOException e) {
                    System.out.println("BlockingEchoServer异常：" + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("BlockingEchoServer启动异常，监听端口：" + port);
            System.out.println(e.getMessage());
        }
    }
}
