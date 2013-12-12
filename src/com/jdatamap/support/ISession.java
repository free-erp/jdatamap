/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jdatamap.support;

import java.sql.Connection;
import java.util.List;

/**
 *
 * @author afa
 */
public interface ISession
{
    public ITransaction beginTransaction();

    public void updateEntity(Entity entity);

    public void saveEntity(Entity entity);

    public void saveOrUpdateEntity(Entity entity);

    public void deleteEntity(Entity entity);

    public Entity getEntity(int id, Class<?> classType);

    public List<?> findEntities(Class<?> classType, String whereStatement, Object[] args);
    /**
     * ∏Ò Ωdelete from @AbcPo where @AbcPo.name=?
     * @param classType
     * @param querySql
     * @param args
     * @return
     */
    public boolean executeEntitySql(Class<?> classType, String querySql, Object[] args);
    
    public void close();

    public boolean isClosed();

    public Connection getConnection();
}
