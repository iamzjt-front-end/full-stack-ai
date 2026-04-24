package com.waylau.java.demo.aio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * AsyncEchoServer 异步IO Echo服务器
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/25
 **/
public class AsyncEchoServer {
    private static final int DEFAULT_PORT = 7;

    public static void main(String[] args) {
        // 解析传参进来的端口号
        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            port = DEFAULT_PORT;
        }

        // 创建AsynchronousServerSocketChannel实例
        try (var asyncServerSocketChannel = AsynchronousServerSocketChannel.open()) {
            System.out.println("AsyncEchoServer启动，监听：" + port);
            asyncServerSocketChannel.bind(new java.net.InetSocketAddress(port));

            // 设置参数
            asyncServerSocketChannel.setOption(java.net.StandardSocketOptions.SO_REUSEADDR, true);
            asyncServerSocketChannel.setOption(java.net.StandardSocketOptions.SO_RCVBUF, 1024);

            // 使用CompletionHandler处理异步连接
            acceptConnection(asyncServerSocketChannel);

            // 保持主线程运行
            Thread.currentThread().join();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 递归接受连接的辅助方法
    private static void acceptConnection(AsynchronousServerSocketChannel serverChannel) {
        serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel clientChannel, Void attachment) {
                try {
                    System.out.println("接收到客户端连接：" + clientChannel.getRemoteAddress());
                    System.out.println("等待客户端发送信息...");
                    System.out.println("--------------------------------------------------------------------------------");

                    // 继续接受下一个连接（真正实现并发）
                    acceptConnection(serverChannel);

                    // 为当前客户端开始读取数据
                    handleClient(clientChannel);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                System.out.println("接受客户端连接失败: " + exc.getMessage());
            }
        });
    }

    // 处理客户端数据的辅助方法
    private static void handleClient(AsynchronousSocketChannel clientChannel) {
        readClientData(clientChannel, ByteBuffer.allocate(1024));
    }

    private static void readClientData(AsynchronousSocketChannel clientChannel, ByteBuffer buffer) {
        clientChannel.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                if (result > 0) {
                    attachment.flip();
                    byte[] data = new byte[attachment.remaining()];
                    attachment.get(data);

                    System.out.println("Echo：" + new String(data));

                    // 回写数据，将attachment作为附件传递（而不是null）
                    clientChannel.write(ByteBuffer.wrap(data), attachment, new CompletionHandler<Integer, ByteBuffer>() {
                        @Override
                        public void completed(Integer result, ByteBuffer attachment) {
                            System.out.println("--------------------------------------------------------------------------------");
                            System.out.println("Echo完毕");
                            System.out.println("--------------------------------------------------------------------------------");
                            System.out.println("等待客户端发送信息...");
                            System.out.println("--------------------------------------------------------------------------------");

                            // 继续读取更多数据
                            attachment.clear();
                            readClientData(clientChannel, attachment);
                        }

                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment) {
                            System.out.println("写入数据失败: " + exc.getMessage());
                            try {
                                clientChannel.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else if (result == -1) {
                    try {
                        clientChannel.close();
                        System.out.println("客户端已关闭");
                        System.out.println("--------------------------------------------------------------------------------");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                System.out.println("读取数据失败: " + exc.getMessage());
                try {
                    clientChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
