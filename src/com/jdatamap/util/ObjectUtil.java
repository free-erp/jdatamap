/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jdatamap.util;

import java.lang.reflect.Method;

/**
 *
 * @author afa
 */
public class ObjectUtil {
    public static Object getProperty(Object obj, String fieldName)
    {
        try
        {
            String f = fieldName.substring(0, 1);
            String methodName = "get" + f.toUpperCase() + fieldName.substring(1);
            Method method =  obj.getClass().getMethod(methodName.trim(), null);
            if (method == null)
            {
                return null;
            }
            return method.invoke(obj, null);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }
    public static void setProperty(Object obj, String fieldName, Class argClass, Object value)
    {
        try
        {
            String f = fieldName.substring(0, 1);
            String methodName = "set" + f.toUpperCase() + fieldName.substring(1);
            Method method =  obj.getClass().getMethod(methodName.trim(), argClass);
            if (method == null)
            {
                return;
            }
            method.invoke(obj, new Object[]{value});
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
