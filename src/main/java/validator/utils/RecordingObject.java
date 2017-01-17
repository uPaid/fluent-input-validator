package validator.utils;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

import static java.util.Optional.ofNullable;
import static org.springframework.cglib.proxy.Enhancer.isEnhanced;

/**
 *
 */

public class RecordingObject implements MethodInterceptor {

    private static final String UNKNOWN_PROPERTY_NAME = "unknown";

    private String currentPropertyName = null;

    private RecordingObject() {
    }

    public static <T> Recorder<T> create(Class<? extends T> cls) {
        final Enhancer enhancer = new Enhancer();

        if (isEnhanced(cls)) {
            enhancer.setSuperclass(cls.getSuperclass());
        }
        else {
            enhancer.setSuperclass(cls);
        }

        final RecordingObject recordingObject = new RecordingObject();
        enhancer.setCallback(recordingObject);

        return new Recorder<>(cls.cast(enhancer.create()), recordingObject);
    }

    public Object intercept(Object o, Method method, Object[] os, MethodProxy mp) {
        if (method.getName()
                  .equals("getCurrentPropertyName")) {
            return getCurrentPropertyName();
        }
        currentPropertyName = method.getName();
        try {
            Recorder<?> currentMock = create(method.getReturnType());
            return currentMock.getObject();
        } catch (IllegalArgumentException e) {
            return DefaultValues.getDefault(method.getReturnType());
        }
    }

    public String getCurrentPropertyName() {
        return ofNullable(currentPropertyName).orElse(UNKNOWN_PROPERTY_NAME);
    }
}