package rpc;

import lombok.Data;

import java.io.Serializable;

/**
 * RPC 响应对象
 */
@Data
public class RpcResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private String requestId;
    private Throwable exception;
    private Object result;

    // getters and setters
}
