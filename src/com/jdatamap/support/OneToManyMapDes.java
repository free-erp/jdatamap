/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jdatamap.support;

/**
 *
 * @author afa
 */
public class OneToManyMapDes extends ForeignKeyMapDes
{
    public Class<?> childClass;
    public String childParentFieldName;
    public OneToManyMapDes(String fieldName, Class<?> collectionClassType, Class<?> childClassType, String childParentFieldName)
    {
        super(fieldName, collectionClassType, null);
        this.childClass = childClassType;
        this.childParentFieldName = childParentFieldName;
    }

    public OneToManyMapDes(String fieldName, Class<?> collectionClassType, Class<Entity> childClassType, String childParentFieldName, boolean lazy)
    {
        super(fieldName, collectionClassType, null);
        this.childClass = childClassType;
        this.childParentFieldName = childParentFieldName;
        this.lazy = lazy;
    }

}
