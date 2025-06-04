package service.impl;

import rpc.MyRpcService;

public class MyRpcServiceImpl implements MyRpcService {


    @Override
    public String sayHello(String name) {
        System.out.println("myRpc say hello, name= " + name);
        return "myRpc say hello";
    }

    @Override
    public String getUserInfo(int userId) {
        System.out.println("myRpc get user info, userId= " + userId);
        return "11111111111111111111111";
    }

    @Override
    public String getRoomInfo(int roomId) {
        return null;
    }

    @Override
    public String getEventList(int roomId, int pageNo, int pageSize) {
        return null;
    }

    @Override
    public String syncEvent(int eventId, int userId, int count) {
        return null;
    }
}
