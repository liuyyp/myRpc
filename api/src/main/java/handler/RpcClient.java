package handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.util.concurrent.Future;
import rpc.RpcRequest;
import rpc.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

/**
 * Netty RPC 客户端
 */
public class RpcClient {
    private final String host;
    private final int port;
    private Channel channel;
    private EventLoopGroup group;
//    private final ConcurrentMap<String, RpcFuture> pendingRequests = new ConcurrentHashMap<>();

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }


    public void connect() throws Exception {
        group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(
                                    new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4),
                                    new LengthFieldPrepender(4),
                                    new ObjectEncoder(),
                                    new ObjectDecoder(ClassResolvers.cacheDisabled(null))
//                                    new RpcClientHandler(pendingRequests)
                            );
                        }
                    });

            ChannelFuture future = b.connect(host, port).sync();
            channel = future.channel();
        } catch (Exception e) {
            group.shutdownGracefully();
            throw e;
        }
    }

    public void close() {
        if (channel != null) {
            channel.close();
        }
        if (group != null) {
            group.shutdownGracefully();
        }
    }

    // 创建服务代理
    @SuppressWarnings("unchecked")
    public <T> T createProxy(Class<T> serviceInterface) {
        return (T) Proxy.newProxyInstance(
                serviceInterface.getClassLoader(),
                new Class<?>[]{serviceInterface},
                new RpcInvocationHandler(serviceInterface, this)
        );
    }

    // 发送请求
    public ChannelFuture sendRequest(RpcRequest request) {
//        RpcFuture future = new RpcFuture(request);
//        pendingRequests.put(request.getRequestId(), future);

        try {
            return channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        System.err.println("Failed to send request: " + future.cause().getMessage());
                        // 处理发送失败的情况
                    }
                }
            });
        } catch (Exception e) {
//            pendingRequests.remove(request.getRequestId());
        }

        return null;
    }
}

///**
// * RPC 客户端处理器
// */
//class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
//    private final ConcurrentMap<String, RpcFuture> pendingRequests;
//
//    public RpcClientHandler(ConcurrentMap<String, RpcFuture> pendingRequests) {
//        this.pendingRequests = pendingRequests;
//    }
//
//    @Override
//    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
//        String requestId = response.getRequestId();
//        RpcFuture future = pendingRequests.remove(requestId);
//        if (future != null) {
//            future.setSuccess(response);
//        }
//    }
//
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        System.err.println("Client exception: " + cause.getMessage());
//        ctx.close();
//    }
//}
