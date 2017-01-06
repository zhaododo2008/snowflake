package com.zhaododo.core.rest.server.common;

import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;

import java.io.ByteArrayOutputStream;

/**
 * 支持FAIL_ON_UNKNOWN_PROPERTIES， 兼容以前的配置
 * @param <T>
 */
public class YHJsonInstanceSerializer<T> implements InstanceSerializer<T> {
    private final ObjectMapper mapper;
    private final Class<T> payloadClass;
    private final JavaType type;

    /**
     * @param payloadClass used to validate payloads when deserializing
     */
    public YHJsonInstanceSerializer(Class<T> payloadClass) {
        this.payloadClass = payloadClass;
        mapper = new ObjectMapper();
        mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
        type = mapper.getTypeFactory().constructType(ServiceInstance.class);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public ServiceInstance<T> deserialize(byte[] bytes) throws Exception {
        ServiceInstance rawServiceInstance = mapper.readValue(bytes, type);
        payloadClass.cast(rawServiceInstance.getPayload()); // just to verify that it's the correct type
        return (ServiceInstance<T>) rawServiceInstance;
    }

    @Override
    public byte[] serialize(ServiceInstance<T> instance) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        mapper.writeValue(out, instance);
        return out.toByteArray();
    }

}
