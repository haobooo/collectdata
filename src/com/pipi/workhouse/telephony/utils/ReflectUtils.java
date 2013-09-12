/**
 * 
 */

package com.pipi.workhouse.telephony.utils;

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author dingli
 */
public class ReflectUtils {

    public static final String TAG = "ReflectUtils";

    /**
     * 
     */
    public ReflectUtils() {
    }

    public static Object getFieldValue(Object obj, String fieldName) {
        return getFieldValue(obj.getClass(), obj, fieldName);
    }

    public static Object getFieldValue(Class<?> clazz, Object obj, String fieldName) {
        Object ret = null;
        try {
            ret = getFieldValueOrThrow(clazz, obj, fieldName);
        } catch (Exception e) {
            Log.w(TAG, "Exception occured when get field value", e);
        }
        return ret;
    }

    public static Object getFieldValueOrThrow(Object obj, String fieldName)
            throws NoSuchFieldException,
            SecurityException, IllegalArgumentException, IllegalAccessException {
        return getFieldValueOrThrow(obj.getClass(), obj, fieldName);
    }

    public static Object getFieldValueOrThrow(Class<?> clazz, Object obj, String fieldName)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException,
            IllegalAccessException {
        Object ret = null;
        Field field = clazz.getDeclaredField(fieldName);
        if (field != null) {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            ret = field.get(obj);
        }
        return ret;
    }

    public static Object invokeMethod(Object obj, String methodName,
            Class<?>[] parameterTypes, Object[] args) {
        return invokeMethod(obj.getClass(), obj, methodName, parameterTypes, args);
    }

    public static Object invokeMethod(Class<?> clazz, Object obj, String methodName,
            Class<?>[] parameterTypes, Object[] args) {
        Object ret = null;
        try {
            ret = invokeMethodOrThrow(clazz, obj, methodName, parameterTypes, args);
        } catch (Exception e) {
            Log.w(TAG, "Exception occured when invoke method", e);
        }
        return ret;
    }

    public static Object invokeMethodOrThrow(Object obj, String methodName,
            Class<?>[] parameterTypes, Object[] args)
            throws NoSuchMethodException,
            SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        return invokeMethodOrThrow(obj.getClass(), obj, methodName, parameterTypes, args);
    }

    public static Object invokeMethodOrThrow(Class<?> clazz, Object obj, String methodName,
            Class<?>[] parameterTypes, Object[] args)
            throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        Object ret = null;
        Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
        if (method != null) {
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            ret = method.invoke(obj, args);
        }

        return ret;
    }

}
