/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.interprinator;

public class Ex_Var
{
    public String group = "";
    public int  type = 0;
    public Object value;
    public String toStringResult;

    public boolean STATUS_EXIT = false;

    public String cmd;
    public int flag_error;
    public int error_value;
    public int pos;
    public String data;
    
    public boolean is_const    = false;
    public boolean is_var      = false;
    public boolean is_array    = false;
    public String  const_name  = "";
    public String  var_name    = "";
    public Object  array_data  = null;
    
    //public int array_index = -1;

    Ex_Var(String _group, int _type, Object _value)
    {
        group = _group;
        type  = _type;
        value = _value;
    }
    
    Ex_Var(String _group, int _type, Object _value, boolean _is_const)
    {
        group     = _group;
        type      = _type;
        value     = _value;
        is_const  = _is_const;
    }
    
    Ex_Var(String _group, int _type, Object _value, boolean _is_const, boolean _is_var)
    {
        group     = _group;
        type      = _type;
        value     = _value;
        is_const  = _is_const;
        is_var    = _is_var;
    }

    /*Ex_Var(int _type, Object _value)
    {
        type  = _type;
        value = _value;
    }*/

    Ex_Var(String _group)
    {
        type  = Exeption.TYPE_VAR__NONE;
        group = _group;
        value = null;
    }
}
