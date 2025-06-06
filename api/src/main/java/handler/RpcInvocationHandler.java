package handler;

import io.netty.channel.ChannelFuture;
import rpc.RpcRequest;
import rpc.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

public class RpcInvocationHandler implements InvocationHandler {
    private final Class<?> serviceInterface;
    private final RpcClient client;

    public RpcInvocationHandler(Class<?> serviceInterface, RpcClient client) {
        this.serviceInterface = serviceInterface;
        this.client = client;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setInterfaceName(serviceInterface.getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);
        try {
            client.connect();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        RpcFuture channelFuture = client.sendRequest(request);

        RpcResponse response = (RpcResponse) channelFuture.get(); // 同步等待结果
        System.out.println("Received response for request");
        client.close();

        if (response.getException() != null) {
            throw response.getException();
        }

        return response.getResult();
    }
}
