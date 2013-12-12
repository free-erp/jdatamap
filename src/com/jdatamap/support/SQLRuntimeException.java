/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jdatamap.support;

/**
 *
 * @author afa
 */
public class SQLRuntimeException extends RuntimeException{
    public SQLRuntimeException(Throwable exception)
    {
        super(exception);
    }

    public SQLRuntimeException(String message)
    {
        super(message);
    }
}
