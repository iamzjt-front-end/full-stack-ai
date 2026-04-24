package com.waylau.java.demo.net;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * BlockingEchoClient 阻塞IO Echo客户端
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/25
 **/
public class BlockingEchoClient {
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

        // 创建Socket
        try (var socket = new java.net.Socket(host, port)) {
            System.out.println("BlockingEchoClient启动，连接：" + host + ":" + port);
            var input = socket.getInputStream();
            var output = socket.getOutputStream();
            var reader = new java.util.Scanner(System.in);
            while (true) {
                System.out.print("请输入：");
                var line = reader.nextLine();
                output.write(line.getBytes());
                output.flush();
                var buffer = new byte[1024];

                var bytesRead = input.read(buffer);
                if (bytesRead == -1) {
                    break;
                }

                System.out.println("Echo：" + new String(buffer, 0, bytesRead));
                System.out.println("--------------------------------------------------------------------------------");
                System.out.println("等待下一个输入...");
                System.out.println("--------------------------------------------------------------------------------");
            }
        } catch (UnknownHostException e) {
            System.out.println("BlockingEchoClient异常：" + e.getMessage());
            System.out.println("请检查服务名是否正确！");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("IO异常：" + e.getMessage());
            System.exit(1);
        }
    }
}
