/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jdatamap.support;

import java.util.Vector;

/**
 *
 * @author afa
 */
public interface Entity
{
    
    public int IDENTITY_GENERATOR = 2;//自动增长ID
    public int ASSIGNED_GENERATOR = 1;//手动设置ID
    public int INCREMENT_GENERATOR = 0;

    public int getKeyGeneratorType();
    
    public Integer getId();
    
    public void setId(Integer id);

    public Integer getAutoId();

    public void setAutoId(Integer autoId);

    public Vector<EntityMapDes> getMaps();
    
    public String getTableName();

    //public String getKeyColumName();
}
