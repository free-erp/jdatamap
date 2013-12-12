/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jdatamap.support;

/**
 *
 * @author afa
 */
public class EntityMapDes
{
    public String fieldName;
    public String columnName;
    public Class<?> classType;

    public EntityMapDes(String fieldName, Class<?> classType, String columnName)
    {
        this.fieldName = fieldName;
        this.classType = classType;
        this.columnName = columnName;
    }

}
