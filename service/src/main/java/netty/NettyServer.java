package netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import rpc.MyRpcService;
import rpc.RpcRequest;
import rpc.RpcResponse;
import service.impl.MyRpcServiceImpl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Netty RPC 服务端
 */
public class NettyServer {
    private final int port;
    private final Map<String, Object> serviceMap = new HashMap<>();

    public NettyServer(int port) {
        this.port = port;
    }

    // 服务端启动代码
    public static void main(String[] args) throws Exception {
        NettyServer server = new NettyServer(8080);
        server.registerService(MyRpcService.class.getName(), new MyRpcServiceImpl());
        server.start();
    }

    // 注册服务实现
    public void registerService(String serviceName, Object service) {
        serviceMap.put(serviceName, service);
    }

    public void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(
                                    // 消息拆包/粘包处理器
                                    new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4),
                                    new LengthFieldPrepender(4),
                                    // 对象编解码器
                                    new ObjectEncoder(),
                                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                    // RPC 请求处理器
                                    new RpcServerHandler(serviceMap)
                            );
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // 绑定端口，开始接收连接
            ChannelFuture f = b.bind(port).sync();
            System.out.println("RPC Server started on port " + port);
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}

/**
 * RPC 请求处理器
 */
class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private final Map<String, Object> serviceMap;

    public RpcServerHandler(Map<String, Object> serviceMap) {
        this.serviceMap = serviceMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        RpcResponse response = new RpcResponse();
        response.setRequestId(request.getRequestId());

        try {
            Object result = handleRequest(request);
            response.setResult(result);
        } catch (Exception e) {
            response.setException(e);
        }

        ctx.writeAndFlush(response);
    }

    private Object handleRequest(RpcRequest request) throws Exception {
        String serviceName = request.getInterfaceName();
        Object service = serviceMap.get(serviceName);

        if (service == null) {
            throw new Exception("Service not found: " + serviceName);
        }

        Class<?> serviceClass = service.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        // 使用反射调用服务方法
        java.lang.reflect.Method method = serviceClass.getMethod(methodName, parameterTypes);
        return method.invoke(service, parameters);
    }
}


