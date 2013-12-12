/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jdatamap.support;

/**
 *
 * @author afa
 */
public class ForeignKeyMapDes extends EntityMapDes
{
    public boolean lazy = true;
    public boolean notNull = false;
    //add other infos!
    public ForeignKeyMapDes(String fieldName, Class<?> classType, String columnName)
    {
        super(fieldName, classType, columnName);
    }

    public ForeignKeyMapDes(String fieldName, Class<?> classType, String columnName, boolean lazy)
    {
        super(fieldName, classType, columnName);
        this.lazy = lazy;
    }

    public ForeignKeyMapDes(String fieldName, Class<?> classType, String columnName, boolean lazy, boolean notNull)
    {
        super(fieldName, classType, columnName);
        this.lazy = lazy;
        this.notNull = notNull;
    }
}
