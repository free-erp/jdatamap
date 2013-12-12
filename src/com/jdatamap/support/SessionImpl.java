/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jdatamap.support;

import com.jdatamap.util.ObjectUtil;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 *
 * @author afa
 */
public class SessionImpl implements ISession
{
    private Connection connection;
    private boolean showSql = true;

    public SessionImpl(Connection connection)
    {
        this.connection = connection;        
    }

    public void setShowSql(boolean show)
    {
        this.showSql = show;
    }

    public Connection getConnection()
    {
        return connection;
    }

    public ITransaction beginTransaction()
    {
        checkConnectionState();
        try
        {
            this.connection.setAutoCommit(false);
            return new JTransaction(this.connection);
        }
        catch(SQLException ex)
        {
            throw new SQLRuntimeException(ex);
        }

        
    }

    public void updateEntity(Entity entity)
    {
        checkConnectionState();
        try
        {
            String sql = createUpdateSql(entity);
            if (this.showSql)
            {
                System.out.println("update statement:" + sql);
            }
            PreparedStatement statement = this.connection.prepareStatement(sql);
            int count = 1;
            for(EntityMapDes des:entity.getMaps())
            {
                if (des.fieldName.equals("id"))
                {
                    continue;
                }
                //afa added
                if (des instanceof OneToManyMapDes)
                {
                    continue;
                }
                //
                invokeStatementMethod(statement, des.classType, count, ObjectUtil.getProperty(entity, des.fieldName));//EntityInvokerUtil.getProperty(entity, des.fieldName));
                count++;
            }
            statement.execute();
            statement.close();
            //children!
            for(EntityMapDes des:entity.getMaps())
            {
                if (des instanceof OneToManyMapDes)
                {
                    //insert childs!
                    OneToManyMapDes des2 = (OneToManyMapDes)des;
                    if (Set.class.isAssignableFrom(des2.classType)) //set
                    {
                        Set<Entity> childrenSet = (Set<Entity>)ObjectUtil.getProperty(entity, des.fieldName);
                        //先删除原来的，再添加所有新的
                        String simpleClassName = des2.childClass.getSimpleName();
                        String sql2 = "delete from @" + simpleClassName + " where @" + simpleClassName  + "." + des2.childParentFieldName + "=?";
                        this.executeEntitySql(des2.childClass, sql2 , new Object[]{entity});
                        for(Entity en:childrenSet)
                        {
                            try
                            {
                                ObjectUtil.setProperty(en, des2.childParentFieldName, entity.getClass(), entity);
                                //
                                en.setId(null);//fix bugs!

                                this.saveEntity(en);
                            }
                            catch(Exception ex)
                            {
                                throw new SQLRuntimeException("Not find parent set method in " + des2.childClass + "\n oringinal exception:" + ex);
                            }
                        }
                    }
                    else //list
                    {
                        //implements later!
                    }
                    ObjectUtil.getProperty(entity, des.fieldName);
                }
             }
        }
        catch(SQLException ex)
        {
            throw new SQLRuntimeException(ex);
        }
        
    }

    public void saveEntity(Entity entity)
    {
        checkConnectionState();
        try
        {
            HashMap<EntityMapDes, Object> propertyMap = new HashMap();
            Vector<EntityMapDes> v = new Vector();
            String sql = createInsertSql(entity, propertyMap, v);
            if (this.showSql)
            {
                System.out.println("insert statement:" + sql);
            }
            PreparedStatement statement = this.connection.prepareStatement(sql);
            if (entity.getKeyGeneratorType() == Entity.ASSIGNED_GENERATOR)
            {
                statement.setInt(1, entity.getId());
            }
            else if (entity.getKeyGeneratorType() == Entity.INCREMENT_GENERATOR)
            {
                statement.setInt(1, generateId(entity));
            }
            //else identity do nothing!
            int count = 2;
            if (entity.getKeyGeneratorType() == Entity.IDENTITY_GENERATOR)
            {
                count = 1;
            }

            for(EntityMapDes des:v)
            {
//                if (des.fieldName.equals("id"))
//                {
//                    continue;
//                }
//                //afa added
//                if (des instanceof OneToManyMapDes)
//                {
//                    continue;
//                }
                //
                invokeStatementMethod(statement, des.classType, count, propertyMap.get(des));
                count++;
            }
            statement.execute();
            statement.close();            
            //insert children Entity
            //children!

            if (entity.getKeyGeneratorType() == Entity.IDENTITY_GENERATOR)
            {
                entity.setId(getMaxId(entity)); //不知道该怎么解决,存在bug!
            }
            for(EntityMapDes des:entity.getMaps())
            {
                if (des instanceof OneToManyMapDes)
                {
                    //insert childs!
                    OneToManyMapDes des2 = (OneToManyMapDes)des;
                    if (Set.class.isAssignableFrom(des2.classType)) //set
                    {
                        Set<Entity> childrenSet = (Set<Entity>)ObjectUtil.getProperty(entity, des.fieldName);
                        for(Entity en:childrenSet)
                        {
                            try
                            {
                                ObjectUtil.setProperty(en, des2.childParentFieldName, entity.getClass(), entity);
                                this.saveEntity(en);
                            }
                            catch(Exception ex)
                            {
                                ex.printStackTrace();
                                throw new SQLRuntimeException("Not find parent set method in " + des2.childClass);

                            }
                        }
                    }
                    else //list
                    {
                        //implements later!
                    }
                    ObjectUtil.getProperty(entity, des.fieldName);
                }
             }
            //            
        }
        catch(SQLException ex)
        {
            throw new SQLRuntimeException(ex);
        }
    }

    private int getMaxId(Entity entity)
    {
        try
        {
            PreparedStatement statement = connection.prepareStatement("select max(" + parseFieldMapColumn(entity, "id") + ") as maxid from " + entity.getTableName());
            ResultSet rs = statement.executeQuery();
            if (rs.next())
            {
                int id = rs.getInt(1);
                return id;
            }
            return -1;
        }
        catch(Exception ex)
        {
            throw new SQLRuntimeException(ex);
        }

    }



    private static String parseFieldMapColumn(Entity entity, String fieldName)
    {
        for(EntityMapDes des:entity.getMaps())
        {
            if (des.fieldName.toLowerCase().equals(fieldName.toLowerCase()))
            {
                return des.columnName;
            }
        }
        return null;
    }

    public void deleteEntity(Entity entity)
    {
        checkConnectionState();
        try
        {
            //级联删除...
            for(EntityMapDes des:entity.getMaps())
            {
                if (des instanceof OneToManyMapDes)
                {
                    OneToManyMapDes des2 = (OneToManyMapDes)des;
                    String childClass = des2.childClass.getSimpleName();
                    this.executeEntitySql(des2.childClass, "delete from @" + childClass + " where @" + childClass + "." + des2.childParentFieldName + "=?", new Object[]{entity});
                }
            }
            //
            String sql = "delete from " + entity.getTableName() + " where " + parseFieldMapColumn(entity, "id") + " =" + entity.getId();
            PreparedStatement statement = this.connection.prepareStatement(sql);
            if (this.showSql)
            {
                System.out.println("delete statement:" + sql);
            }
            statement.execute();
            statement.close();

        }
        catch(SQLException ex)
        {
            throw new SQLRuntimeException(ex);
        }
    }

    public boolean executeEntitySql(Class<?> classType, String querySql, Object[] args)
    {
        try
        {
            Entity entity = (Entity)classType.newInstance();
            String sql = parseEntityToTableSql(entity, querySql);
            if (this.showSql)
            {
                System.out.println("execute sql:" + sql);
            }
            PreparedStatement statement = this.connection.prepareStatement(sql);
            int count = 1;
            Vector<EntityMapDes> maps = parseMapDes(entity, querySql);
            if (args != null && maps.size() != args.length)
            {
                throw new SQLRuntimeException("The arguments are not matched(size)!");
            }
            for(EntityMapDes des:maps)
            {
                //此处不需要过滤id
                Object value = args[count - 1];
                invokeStatementMethod(statement, des.classType, count, value);//EntityInvokerUtil.getProperty(entity, des.fieldName));
                count++;
            }
            boolean r = statement.execute();
            statement.close();
            return r;

        }
        catch(SQLException ex)
        {
            throw new SQLRuntimeException(ex);
        }
        catch(Exception ex)
        {
            throw new SQLRuntimeException(ex);
        }
    }

    /**
     * 写法: select * from user where userName=? and pass=?"
     * 注意按照sql的写法写，只要将po属性替换成column能正常运行
     */
    public List<?> findEntities(Class<?> classType, String querySql, Object[] args) {
        try
        {
            Entity entity = (Entity)classType.newInstance();
            String sql = parseEntityToTableSql(entity, querySql);
            if (this.showSql)
            {
                System.out.println("query sql:" + sql);
            }
            PreparedStatement statement = this.connection.prepareStatement(sql);
            int count = 1;
            Vector<EntityMapDes> maps = parseMapDes(entity, querySql);
            if (args != null && maps.size() != args.length)
            {
                throw new SQLRuntimeException("The arguments are not matched(size)!");
            }
            if (args != null && maps.size() > 0)
            {
                for(EntityMapDes des:maps)
                {
                    //此处不需要过滤id
                    Object value = args[count - 1];
                    invokeStatementMethod(statement, des.classType, count, value);//EntityInvokerUtil.getProperty(entity, des.fieldName));
                    count++;
                }
            }
            ResultSet set = statement.executeQuery();
            List<Entity> list = new ArrayList();
            while(set.next())
            {
                entity = (Entity)classType.newInstance();
                entity.setId(set.getInt(parseFieldMapColumn(entity, "id")));
                for(EntityMapDes des:entity.getMaps())
                {
                    if (des.fieldName.equals("id"))
                    {
                        continue;
                    }
                    if (des instanceof OneToManyMapDes)
                    {
                        setOneToManyEntities(entity, (OneToManyMapDes)des);
                     }
                    else
                    {
//                        System.out.println("column:" + des.columnName + "  type:" + des.classType + "  name:" + des.fieldName);
//                        if (des.columnName.equals("saleFormId"))
//                        {
//                            int a;
//                            a = 0;
//                        }
                        ObjectUtil.setProperty(entity, des.fieldName, des.classType, parseObjectValue(des, set.getObject(des.columnName)));
                    }
                }
                list.add(entity);
            }
            set.close();
            statement.close();
            return list;
        }
        catch(SQLException ex)
        {
            ex.printStackTrace();
            throw new SQLRuntimeException(ex);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            throw new SQLRuntimeException(ex);
        }
    }

    public void setOneToManyEntities(Entity parentEntity, OneToManyMapDes des)
    {
        //lazy = false 暂时不做
        String childClassName = des.childClass.getSimpleName();
        List<?> children = this.findEntities(des.childClass, "select * from @" + childClassName + " where @" + childClassName + "." + des.childParentFieldName + "=?", new Object[]{parentEntity});
        Object fieldValue = children;
        if (Set.class.isAssignableFrom(des.classType))
        {
            fieldValue = new HashSet(children.size());
            Set setValue = (Set)fieldValue;
            for(Object v:children)
            {
                setValue.add(v);
            }
        }
        else if (List.class.isAssignableFrom(des.classType))
        {
            //do nothing
        }
        ObjectUtil.setProperty(parentEntity, des.fieldName, Set.class, fieldValue);//lazy!
    }

    public Entity getInnerEntity(int id, Class<?> classType) {
        try
        {
            Entity entity = (Entity)classType.newInstance();
            entity.setId(id);
            PreparedStatement statement = this.connection.prepareStatement("select * from " + entity.getTableName() + " where " + this.parseFieldMapColumn(entity, "id") + " =" + id);
            ResultSet set = statement.executeQuery();
            if(set.next())
            {
                for(EntityMapDes des:entity.getMaps())
                {
                    //find.....

                    if (des instanceof ForeignKeyMapDes)
                    {
                        continue;
                    }
                    //
                    if (des.fieldName.equals("id"))
                    {
                        continue;
                    }
                    ObjectUtil.setProperty(entity, des.fieldName, des.classType, parseObjectValue(des, set.getObject(des.columnName)));
                }
            }
            set.close();
            statement.close();
            return entity;
        }
        catch(SQLException ex)
        {
            throw new SQLRuntimeException(ex);
        }
        catch(Exception ex)
        {
            throw new SQLRuntimeException(ex);
        }

    }

    public Entity getEntity(int id, Class<?> classType) {
        String name = classType.getSimpleName();
        List<?> values = this.findEntities(classType, "select * from @" + name + " where @" + name + ".id =" + id, null);
        if (values != null && values.size() > 0)
        {
            return (Entity)values.get(0);
        }
        return null;
        /*
        try
        {
            Entity entity = (Entity)classType.newInstance();
            entity.setId(id);
            PreparedStatement statement = this.connection.prepareStatement("select * from " + entity.getTableName() + " where " + this.parseFieldMapColumn(entity, "id") + " =" + id);
            ResultSet set = statement.executeQuery();
            if(set.next())
            {
                for(EntityMapDes des:entity.getMaps())
                {
                    //find.....


                    //

                    if (des.fieldName.equals("id"))
                    {
                        continue;
                    }
                    ObjectUtil.setProperty(entity, des.fieldName, des.classType, parseObjectValue(des, set.getObject(des.columnName)));
                }
            }
            set.close();
            statement.close();
            return entity;
        }
        catch(SQLException ex)
        {
            throw new SQLRuntimeException(ex);
        }
        catch(Exception ex)
        {
            throw new SQLRuntimeException(ex);
        }
         *
         */
    }

    public void close()
    {
        try
        {

            this.connection.close();
        }
        catch(SQLException ex)
        {
            throw new SQLRuntimeException(ex);
        }

    }

    public void checkConnectionState()
    {
//        try
//        {
////            if (this.connection.isClosed() || this.connection.isValid(0))
////            {
////                throw new SQLRuntimeException("The session is not valid!");
////            }
//        }
//        catch(Exception ex)
//        {
//            ex.printStackTrace();
//            //throw new SQLRuntimeException("The session is not valid!");
//        }
    }

    private static String createInsertSql(Entity entity, HashMap<EntityMapDes, Object> propertyMap, java.util.Vector<EntityMapDes> v)
    {
        if (entity.getKeyGeneratorType() == Entity.IDENTITY_GENERATOR)
        {
            String sql = "insert into " + entity.getTableName() + "(";
            int j = 0;
            String valueSql = ") values(";
            for(EntityMapDes des:entity.getMaps())
            {

                if (des.fieldName.equals("id"))
                {
                    continue;
                }
                //afa added
                if (des instanceof OneToManyMapDes)
                {
                    continue;
                }
                Object obj = ObjectUtil.getProperty(entity, des.fieldName);
                if (obj == null || "".equals(obj.toString().trim()))
                {
                    continue;
                }
                propertyMap.put(des, obj);
                v.add(des);
                 //
                if (j > 0)
                {
                    sql += ",";
                    valueSql += ",";
                }
                sql += des.columnName;
                valueSql += "?";
                j++;
            }
            valueSql += ")";

            sql += valueSql;//") values(" ;
            /*
            for(int i = 0; i < entity.getMaps().size() - 1; i++)
            {
                EntityMapDes des = entity.getMaps().get(i);
                if (des instanceof OneToManyMapDes)// || des.fieldName.equals("id"))
                {
                    continue;
                }
                System.out.println("number:" + i + " field:" + des.fieldName);
                if (i > 0)
                {
                    sql += ",";
                }
                sql += "?";
            }
            sql += ")";
             */
            return sql;
        }
        else
        {
            String sql = "insert into " + entity.getTableName() + "(" + parseFieldMapColumn(entity, "id");
            for(EntityMapDes des:entity.getMaps())
            {
                if (des.fieldName.equals("id"))
                {
                    continue;
                }
                //afa added
                if (des instanceof OneToManyMapDes)
                {
                    continue;
                }
                 //
                sql += "," + des.columnName;
            }
            sql += ") values(?" ;
            for(int i = 0; i < entity.getMaps().size() - 1; i++)
            {
                //
                if (entity.getMaps().get(i) instanceof OneToManyMapDes)
                {
                    continue;
                }
                //
                sql += ",?";
            }
            sql += ")";
            return sql;
        }
    }

    private static String createUpdateSql(Entity entity)
    {
        String sql = "update " + entity.getTableName() + " set ";
        Vector<EntityMapDes> maps = entity.getMaps();
        int count = 0;
        for(int i = 0; i <  maps.size(); i++)
        {
            EntityMapDes des = maps.get(i);
            if (des.fieldName.equals("id"))
            {
                continue;
            }
            //afa added
            if (des instanceof OneToManyMapDes)
            {
                continue;
            }
            //
            if (count > 0)
            {
                sql += ", ";
            }
            sql += des.columnName + "=?";
            count++;
        }
        sql += " where " + parseFieldMapColumn(entity, "id");
        sql += "=" + entity.getId();
        return sql;
    }


    /**
     * 格式 "name=? and age > ?"
     * 写法: select * from user where userName=? and pass=?"
     * @param entity
     * @param whereSql
     * @return
     */
    private static String parseEntityToTableSql(Entity entity, String sql)
    {
        String className = entity.getClass().getSimpleName();
        String sqlString = sql.replaceAll("@" + className, entity.getTableName());
        Vector<EntityMapDes> maps = entity.getMaps();
        for(int i = 0; i <  maps.size(); i++)
        {
            EntityMapDes des = maps.get(i);
            sqlString = sqlString.replaceAll(entity.getTableName() + "." + des.fieldName, des.columnName);
        }
        //sqlString = sqlString.replaceAll("id", parseFieldMapColumn(entity, "id"));
        return sqlString;
    }

    public static Vector<EntityMapDes> parseMapDes(Entity entity, String sql)
    {
        int sep = sql.toLowerCase().indexOf("where");
        if (sep <= 0)
        {
            return new Vector<EntityMapDes>();
        }
        Vector<EntityMapDes> maps = new Vector<EntityMapDes>();
        String afterWhere = sql.substring(sep);
        String className = entity.getClass().getSimpleName();
        //需要排序
        HashMap<Integer, EntityMapDes> sortMap = new HashMap<Integer, EntityMapDes>();
        int max = 0;
        for(EntityMapDes des:entity.getMaps())
        {
            String tempString = "@" + className + "." + des.fieldName;
            int sep2 = afterWhere.indexOf(tempString);
            while(sep2 > 0)
            {
                sortMap.put(sep2, des);
                if (max < sep2)
                {
                    max = sep2;
                }
                int t = sep2 + tempString.length();
                sep2 = afterWhere.indexOf(tempString, t);
            }
            /*
             * //old  当出现多次的时候出错
            int sep2 = afterWhere.indexOf(tempString);
            if (sep2 > 0)
            {
                sortMap.put(sep2, des);
                if (max < sep2)
                {
                    max = sep2;
                }
            }
             * //end old
             */
        }
        //sort 最简单的排序
        for(int i = 0; i <= max; i++)
        {
            EntityMapDes d = sortMap.get(i);
            if (d != null)
            {
                maps.add(d);
            }
        }
        return maps;
    }

    private int generateId(Entity entity) throws SQLException
    {
        if (entity.getAutoId() >= 0)
        {
            entity.setAutoId(entity.getAutoId() + 1);
            entity.setId(entity.getAutoId());
            return entity.getAutoId();
        }
        else
        {
            PreparedStatement statement = connection.prepareStatement("select max(" + parseFieldMapColumn(entity, "id") + ") as maxid from " + entity.getTableName());
            ResultSet rs = statement.executeQuery();
            if (rs.next())
            {
                int id = rs.getInt(1);
                entity.setAutoId(id + 1);
                rs.close();
                statement.close();
                entity.setId(entity.getAutoId());
                return entity.getAutoId();
            }
            entity.setAutoId(1);

            return entity.getAutoId();
        }
    }

    private void invokeStatementMethod(PreparedStatement statement, Class<?> classType, int index, Object value) throws SQLException
    {
        int type = Types.VARCHAR;
        Object v = value;
        if (String.class.isAssignableFrom(classType))
        {
            type = Types.VARCHAR;
        }
        else if (Time.class.isAssignableFrom(classType)) //注意：Time类型必须写在Date之前
        {
            type = Types.TIME;           
        }
        else if (Timestamp.class.isAssignableFrom(classType))//注意：Timestamp类型必须写在Date之前
        {
            type = Types.TIMESTAMP;
        }
        else if (Date.class.isAssignableFrom(classType))
        {
            type = Types.DATE;
            Date date = (Date)value;
            if(date != null)
            {
                v = new java.sql.Date(date.getTime());
            }
        }
        
        else if (Integer.class.isAssignableFrom(classType) || Short.class.isAssignableFrom(classType) || Long.class.isAssignableFrom(classType))
        {
            type = Types.INTEGER;
        }
        else if (Double.class.isAssignableFrom(classType) ||  Float.class.isAssignableFrom(classType) )
        {
            type = Types.DOUBLE;
        }
        
        //待添加保留内容
        else if (Blob.class.isAssignableFrom(classType))
        {
            type = Types.BLOB;
        }
        else if (Boolean.class.isAssignableFrom(classType))
        {
            type = Types.BOOLEAN;
        }        
        else 
        {
            if (Entity.class.isAssignableFrom(classType))//外键
            {
                type = Types.INTEGER;
                if (value != null) //在此处需要增加description是否可以为空的判断
                {
                    v = ((Entity)value).getId();
                }
            }
            else
            {
                throw new SQLRuntimeException("The object value is not a entity instance!");//Jdatamap doesn't support the data type yet! ->" + classType.getSimpleName());
            }
        }
        statement.setObject(index, v, type);
        System.out.println(index+":"+v+"-"+type);
    }

    public Object parseObjectValue(EntityMapDes des, Object value)
    {
         Class<?> classType = des.classType;
         //bigDecimal temp fix bug 20091126
         if (value instanceof java.math.BigDecimal)
         {
             return ((java.math.BigDecimal)value).doubleValue();
         }
         //end fix bug
         if (Entity.class.isAssignableFrom(classType))
         {
             try
             {
                 if (des instanceof ForeignKeyMapDes && !((ForeignKeyMapDes)des).lazy)
                 {
                     ForeignKeyMapDes des2 = (ForeignKeyMapDes)des;
                     if (value == null)
                     {
                         if (des2.notNull)
                         {
                            throw new SQLRuntimeException("The foreign key field \"" + des.columnName + "\" can't be null while the value is null!");
                         }
                         else
                         {
                             return null;
                         }
                     }
                     return this.getInnerEntity((Integer)value, classType); //获取内部的不包含多层的对象
                 }
                 else
                 {
                    if (value == null)
                    {
                        return null;
                    }
                    Entity va = (Entity)classType.newInstance();
                    va.setId((Integer)value);
                    return va;
                 }
             }
             catch(Exception ex)
             {
                 throw new SQLRuntimeException(ex);
             }
         }
         else
         {
             return value;
         }
    }


    public void saveOrUpdateEntity(Entity entity)
    {
//        int id = entity.getId();
//        Entity e = this.getEntity(id, entity.getClass());
        if (entity.getId() != null)
        {
            this.updateEntity(entity);
        }
        else
        {
            this.saveEntity(entity);
        }
    }


    public boolean isClosed()
    {
        try
        {
             return this.connection.isClosed();
        }
        catch(Exception ex)
        {
            return true;
        }
    }
    
}
