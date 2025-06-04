package rpc;

public interface MyRpcService {

    String sayHello(String name);

    String getUserInfo(int userId);

    String getRoomInfo(int roomId);

    String getEventList(int roomId, int pageNo, int pageSize);

    String syncEvent(int eventId, int userId, int count);
}
