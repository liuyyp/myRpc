package rpc;

import lombok.Data;

import java.io.Serializable;

/**
 * RPC 请求对象
 */
@Data
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String requestId;
    private String interfaceName;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;

    // getters and setters
}