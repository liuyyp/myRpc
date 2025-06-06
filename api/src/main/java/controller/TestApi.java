package controller;

import handler.RpcClient;
import handler.RpcInvocationHandler;
import rpc.MyRpcService;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

public class TestApi {

    private MyRpcService myRpcService = null;

    static TestApi testApi = new TestApi();

//    static {
//        RpcClient client = new RpcClient("localhost", 8080);
//
//        testApi.myRpcService = client.createProxy(MyRpcService.class);
//
////        Field[] fields = TestApi.class.getDeclaredFields();
////        for (Field field : fields) {
////            System.out.println("Field: " + field.getName() + ", Type: " + field.getType());
////            try {
////                field.set(testApi,
////                        Proxy.newProxyInstance(
////                                field.getType().getClassLoader(),
////                                new Class<?>[]{field.getType()},
////                                new RpcInvocationHandler(field.getType(), client)
////                        ));
////            } catch (IllegalAccessException e) {
////                throw new RuntimeException(e);
////            }
////        }
//    }

    public static void main(String[] args) throws Exception {
        RpcClient client = new RpcClient("localhost", 8080);

        testApi.myRpcService = client.createProxy(MyRpcService.class);

        String world = testApi.myRpcService.sayHello("World");
        System.out.println("Response from sayHello: " + world);
        String userInfo = testApi.myRpcService.getUserInfo(1);
        System.out.println("Response from getUserInfo: " + userInfo);


    }
}
