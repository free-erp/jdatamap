/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jdatamap.support;

/**
 *
 * @author afa
 */
public interface ITransaction
{
    public void commit();
    public void rollback();
}
