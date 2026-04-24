package com.waylau.java.demo.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;

/**
 * NonBlokingEchoServer 非阻塞IO Echo服务器
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/25
 **/
public class NonBlockingEchoServer {

    private static final int DEFAULT_PORT = 7;

    public static void main(String[] args) {
        // 解析传参进来的端口号
        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            port = DEFAULT_PORT;
        }

        // 创建ServerSocketChannel、Selector
        try (var serverSocketChannel = ServerSocketChannel.open();
             var selector = Selector.open()) {
            serverSocketChannel.bind(new java.net.InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            // 接收客户端连接
            while (true) {
                selector.select();
                var selectedKeys = selector.selectedKeys();
                for (var key : selectedKeys) {
                    selectedKeys.remove(key);
                    try {
                        // 可连接
                        if (key.isAcceptable()) {
                            var socketChannel = serverSocketChannel.accept();
                            socketChannel.configureBlocking(false);
                            socketChannel.register(selector, SelectionKey.OP_READ);
                            System.out.println("接收到客户端连接：" + socketChannel.getRemoteAddress());
                            System.out.println("等待客户端发送信息...");
                            System.out.println("--------------------------------------------------------------------------------");
                            System.out.println("等待下一个客户端连接...");
                            System.out.println("--------------------------------------------------------------------------------");

                        } else if (key.isReadable()) {
                            var socketChannel = (SocketChannel) key.channel();
                            var buffer = ByteBuffer.allocate(1024);
                            int bytesRead = socketChannel.read(buffer);

                            if (bytesRead > 0) {
                                buffer.flip();
                                byte[] data = new byte[buffer.remaining()];
                                buffer.get(data);
                                System.out.println("Echo：" + new String(data));
                                System.out.println("--------------------------------------------------------------------------------");
                                System.out.println("Echo完毕");
                                System.out.println("--------------------------------------------------------------------------------");
                                socketChannel.write(ByteBuffer.wrap(data));
                                // 保持连接打开，继续监听读事件
                                key.interestOps(SelectionKey.OP_READ);
                            } else if (bytesRead == -1) {
                                // 客户端关闭连接
                                socketChannel.close();
                                key.cancel();
                                System.out.println("客户端连接已关闭");
                                System.out.println("--------------------------------------------------------------------------------");
                                System.out.println("等待下一个客户端连接...");
                                System.out.println("--------------------------------------------------------------------------------");
                            }
                        }
                    } catch (ClosedChannelException e) {
                        System.out.println("客户端已关闭");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
