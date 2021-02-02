package com.interprinator;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Exeption 
{
    public static final int TYPE_VAR__NONE       = 0;
    public static final int TYPE_VAR__INT        = 1;
    public static final int TYPE_VAR__FLOAT      = 2;
    public static final int TYPE_VAR__STR        = 3;
    public static final int TYPE_VAR__ARRAY      = 4;
    public static final int TYPE_VAR__USER_DATA  = 5;

    public static final int ANALIZ_BEGIN_END     = 0x10;
    public static final int ANALIZ_RIGHT         = 0x11;
    public static final int ANALIZ_LEFT          = 0x12;
    public static final int ANALIZ_LEFT_RIGHT    = 0x13;
    
    private String stop_tokens = "^*-/+=()\\!%<>&|,";

    private int flag_error  = 0;
    public int error_value = 0;
    private int error_pos_in_file = 0;
    private int _index_tmp_vars   = 0;

    public boolean STATUS_EXIT = false;
    public Ex_Var  EXIT_PARAMETERS;
    private Sa _sa;

    private List<ExternalFunction> external_functions = null;

    private class PriorityItem
    {
        public int type;
        public ArrayList<String[]> data = new ArrayList<String[]>();

    }
	
    private List<PriorityItem> priority = new ArrayList<PriorityItem>();

    private Map<String, Ex_Var> local_vars     = new HashMap<String, Ex_Var>();
    private Map<String, Ex_Var> local_tmp_vars = new HashMap<String, Ex_Var>();
    private Map<String, Ex_Var> global_var     = new HashMap<String, Ex_Var>();
    private List<String> _group_tmp_vars       = new ArrayList<>();
    private List<Sa.FunctionData> functions;
    
    public void addLastGroupTmpVars(String v)
    {
        _group_tmp_vars.add( v );
    }
    
    public String getLastGroupTmpVars()
    {
        if( _group_tmp_vars.size() == 0 )
        {
            return "";
        }
        
        return _group_tmp_vars.get( _group_tmp_vars.size() - 1 );
    }
    
    public void deleteTmpVarsByGroup(String timestamp_index)
    {
        ListIterator<String> iter = _group_tmp_vars.listIterator();
        
        while(iter.hasNext())
        {
            if(iter.next().equals(timestamp_index))
            {
                iter.remove();
            }
        }
        
        for(Iterator<Map.Entry<String, Ex_Var>> it = local_tmp_vars.entrySet().iterator(); it.hasNext(); ) 
        {
            Map.Entry<String, Ex_Var> entry = it.next();
            
            if(entry.getValue().group.equals(timestamp_index)) 
            {
                it.remove();
            }
        }
    }
	
    Exeption(Sa sa, List<Sa.FunctionData> _functions, Map<String, Ex_Var> _global_var, List<ExternalFunction> _external_functions)
    {
        functions    = _functions;
        _sa          = sa;
        global_var   = _global_var;
        external_functions = _external_functions;

        /// ---- init prioroty ----  //////////////////////////////////////////////////
        PriorityItem tmp = null;

        tmp = new PriorityItem();
        tmp.type = Exeption.ANALIZ_BEGIN_END;
        tmp.data.add( new String[]{ "(", ")" } );

        priority.add(tmp);

        tmp = new PriorityItem();
        tmp.type = Exeption.ANALIZ_BEGIN_END;
        tmp.data.add( new String[]{ "[", "]" } );

        priority.add(tmp);

        tmp = new PriorityItem();
        tmp.type = Exeption.ANALIZ_RIGHT;
        tmp.data.add( new String[]{ "!" } );

        priority.add(tmp);

        tmp = new PriorityItem();
        tmp.type = Exeption.ANALIZ_LEFT_RIGHT;
        tmp.data.add( new String[]{ "^" } );
        tmp.data.add( new String[]{ "*" } );
        tmp.data.add( new String[]{ "/" } );
        tmp.data.add( new String[]{ "%" } );

        priority.add(tmp);
        
        tmp = new PriorityItem();
        tmp.type = Exeption.ANALIZ_LEFT_RIGHT;
        tmp.data.add( new String[]{ "+=" } );

        priority.add(tmp);

        tmp = new PriorityItem();
        tmp.type = Exeption.ANALIZ_LEFT_RIGHT;
        tmp.data.add( new String[]{ "+" } );
        tmp.data.add( new String[]{ "-" } );

        priority.add(tmp);

        tmp = new PriorityItem();
        tmp.type = Exeption.ANALIZ_LEFT_RIGHT;
        tmp.data.add( new String[]{ ">=" } );
        tmp.data.add( new String[]{ "<=" } );
        tmp.data.add( new String[]{ "==" } );
        tmp.data.add( new String[]{ "!=" } );
        tmp.data.add( new String[]{ ">" } );
        tmp.data.add( new String[]{ "<" } );

        priority.add(tmp);

        tmp = new PriorityItem();
        tmp.type = Exeption.ANALIZ_LEFT_RIGHT;
        tmp.data.add( new String[]{ "&&" } );

        priority.add(tmp);

        tmp = new PriorityItem();
        tmp.type = Exeption.ANALIZ_LEFT_RIGHT;
        tmp.data.add( new String[]{ "||" } );

        priority.add(tmp);
        
        tmp = new PriorityItem();
        tmp.type = Exeption.ANALIZ_LEFT_RIGHT;
        tmp.data.add( new String[]{ "=" } );

        priority.add(tmp);

        ////////////////////////////////////////////////////////////////////
    }
	
    private boolean _is_string( String v )
    {
        v = v.trim();
        
        if(v.length() == 0)
        {
            return false;
        }
        
        if(    v.charAt( 0 ) == '"' 
            && v.charAt( v.length() - 1 ) == '"' )
        {
            return true;
        }
        
        return false;
    }
    
    private boolean _is_correct_name_var( String name_var )
    {
        name_var = name_var.trim();
        
        if( Character.isDigit( name_var.charAt( 0 ) ) )
        { return false; }
        
        for(int i = 0, len = name_var.length(); i < len; i++)
        {
            if( stop_tokens.indexOf( name_var.charAt(i) ) != -1 )
            {
                return false;
            }
        }
        
        return true;
    }
    
    public boolean _global_vars_is_exist(String name)
    {
        name = name.toLowerCase().trim();
    	
    	if( global_var.get(name) != null )
    	{
            return true;
    	}
    	
    	return false;
    }
    
    private boolean functionExists(String name_f)
    {
        name_f = name_f.trim();
        
        for(int i = 0; i < functions.size(); i++)
        {
            if( functions.get(i).name_function.equalsIgnoreCase(name_f) )
            {
                return true;
            }
        }
        
        if( functionExistsInternal(name_f) )
        {
            return true;
        }
        
        return false;
    }
    
    private void _local_tmp_vars_add_new(String name, Ex_Var v)
    {
    	name = name.toLowerCase().trim();
    	
    	if( ! this._is_correct_name_var(name) )
    	{
            flag_error = 1;
            error_value = Errors.ERROR_EXEC__ERROR_VAR_NAME_NOT_CORRECT;

            return;
    	}
    	
    	if( local_vars.get(name) != null ) /// проверка во внешней среде
    	{
            return;
    	}
    	
    	if(    v.value instanceof String
    	    && ((String)(v.value)).equalsIgnoreCase("none") )
    	{
            v.value = null;
    	}
    	
    	if(v.type == Exeption.TYPE_VAR__ARRAY && v.value == null)
    	{
            v.value = new HashMap<String, Object>();
    	}
    	else if(v.type == Exeption.TYPE_VAR__NONE)
    	{
            v.value = null;
    	}
    	
    	local_tmp_vars.put(name, v);	
        
        this._index_tmp_vars += 1;
    }
    
    private boolean _local_tmp_vars_is_var_exists( String name )
    {
    	name = name.toLowerCase().trim();
    	
    	if( local_tmp_vars.get(name) != null )
    	{
            return true;
    	}
    	
    	return false;
    }
    
    private Ex_Var _local_tmp_vars_get_value( String name )
    {
    	name = name.toLowerCase().trim();
    	
    	if( local_tmp_vars.get(name) != null )
    	{
            return local_tmp_vars.get(name);
    	}
    	
    	return null;
    }
    
    private void _local_tmp_vars_remove_all()
    {
    	this.local_tmp_vars.clear();
    }
    ///-------------------------------------------------------------------------
    
    private boolean _local_vars_add_new( String name, Ex_Var v )
    {
    	name = name.toLowerCase().trim();
    	
    	if( ! this._is_correct_name_var(name) )
    	{
            flag_error = 1;
            error_value = Errors.ERROR_EXEC__ERROR_VAR_NAME_NOT_CORRECT;

            return false;
    	}
    	
    	if( local_vars.get(name) != null ) /// проверка во внешней среде
    	{
            return false;
    	}
    	
    	if( v.value instanceof String && ((String)(v.value)).equalsIgnoreCase("none") )
    	{
            v.value = null;
    	}
    	
    	if(v.type == Exeption.TYPE_VAR__ARRAY && v.value == null)
    	{
            v.value = new HashMap<String, Object>();
    	}
    	else if(v.type == Exeption.TYPE_VAR__NONE)
    	{
            v.value = null;
    	}
    	
    	local_vars.put(name, v);
    	
    	return true;
    }
    
    private void _local_vars_remove_from_name(String name)
    {
    	name = name.toLowerCase().trim();
    	
    	if( local_vars.get(name) != null )
    	{
            local_vars.remove(name);
    	}
    }
    
    private void _local_vars_remove_by_group(String name_group)
    {
    	Map<String, Ex_Var> new_local_vars = new HashMap<String, Ex_Var>();
    	
    	for(Entry<String, Ex_Var> entry : local_vars.entrySet()) 
    	{
    	    String key = entry.getKey();
    	    Ex_Var value = entry.getValue();
    	    
    	    if( ! value.group.equalsIgnoreCase(name_group) )
    	    {
    	    	new_local_vars.put(key, value);
    	    }
    	}
    	
    	local_vars = new_local_vars;
    }
    
    private boolean _local_vars_is_var_exists( String name )
    {
    	name = name.toLowerCase().trim();
    	
    	if( local_vars.get(name) != null )
    	{
            return true;
    	}
    	
    	return false;
    }
    
    private void _local_vars_set_new_value( String name, Object value )
    {
    	name = name.toLowerCase().trim();
        
        if(global_var.get(name) != null)
    	{
            global_var.get(name).value = value;
    	}
        else if(local_tmp_vars.get(name) != null)
    	{
            local_tmp_vars.get(name).value = value;
    	}
        else if(local_vars.get(name) != null)
    	{
            local_vars.get(name).value = value;
    	}
    }
    
    private void _local_vars_set_new_value_and_type(String name, int type, Object value)
    {
    	name = name.toLowerCase().trim();
    	
        if(global_var.get(name) != null)
    	{
            global_var.get(name).type  = type;
            global_var.get(name).value = value;
        }
        else if(local_tmp_vars.get(name) != null)
    	{
            local_tmp_vars.get(name).type  = type;
            local_tmp_vars.get(name).value = value;
        }
        else if(local_vars.get(name) != null)
    	{
            local_vars.get(name).type  = type;
            local_vars.get(name).value = value;
    	}
    }
    
    private Ex_Var _local_vars_get_value( String name )
    {
    	name = name.toLowerCase().trim();
    	
        if(global_var.get(name) != null)
    	{
            return global_var.get(name);
    	}
        else if(local_tmp_vars.get(name) != null)
    	{
            return local_tmp_vars.get(name);
    	}
        else if(local_vars.get(name) != null)
    	{
            return local_vars.get(name);
    	}
    	
    	return null;
    }
    
    private boolean _is_error_in_count_parameters(int need_count, int current)
    {
    	if( current < need_count )
    	{
            flag_error = 1;
            error_value = Errors.ERROR_EXEC__ERROR_PARAMETERS_LOW;
            return true;
    	}
    	
    	if( current > need_count )
    	{
            flag_error = 1;
            error_value = Errors.ERROR_EXEC__ERROR_PARAMETERS_MANY;
            return true;
    	}
    	
    	return false;
    }
    
    private boolean functionExistsInternal(String name_f)
    {
        name_f = name_f.trim();
        
        switch(name_f.toLowerCase())
        {
            case "exit":
            case "toradians":
            case "todegrees":
            case "currenttimemillis":
            case "rand01":
            case "sqrt":
            case "toint":
            case "tofloat":
            case "sin":
            case "cos":
            case "abs":
            case "ceil":
            case "floor":
            case "tan":
            case "print":
                return true;
                
        }
        
        for(int i = 0; i < external_functions.size(); i++)
        {
            if( external_functions.get(i).name_method.equalsIgnoreCase(name_f) )
            {
                return true;
            }
        }
        
        return false;
    }
    
    private Ex_Var _run_functions(String name, List<Ex_Var> params)
    {
    	/*for(int i = 0; i < params.size(); i++)
    	{
    		Ex_Var v = params.get(i);
    		
    		if( 
                        v.type == Exeption.TYPE_VAR__FLOAT
                    ||  v.type == Exeption.TYPE_VAR__INT
    		  )
    		{
    			//params.get(i).value = params.get(i).value.
    		}
    	}*/
    	
    	name = name.trim();
    	
    	Ex_Var res = null;
    	Ex_Var tmp_v;
        
        switch(name.toLowerCase())
    	{
            case "exit":
                    res = new Ex_Var(getLastGroupTmpVars());

                    res.type  = Exeption.TYPE_VAR__ARRAY;
                    res.value =  params;

                    STATUS_EXIT     = true;
                    EXIT_PARAMETERS = new Ex_Var(getLastGroupTmpVars(), Exeption.TYPE_VAR__ARRAY, params);
                    EXIT_PARAMETERS.STATUS_EXIT = true;

                    return res;
                    
            case "toradians":
                    //tmp_v = __get_current_type_and_value((String)params.get(0).value);

                    res = new Ex_Var(getLastGroupTmpVars());

                    res.type  = Exeption.TYPE_VAR__FLOAT;
                    
                    if( params.get(0).type == TYPE_VAR__INT )
                    {
                        res.value =  Math.toRadians(((Long) params.get(0).value).doubleValue() );
                    }
                    else if( params.get(0).type == TYPE_VAR__FLOAT )
                    {
                        res.value =  Math.toRadians((Double) params.get(0).value );
                    }

                    return res;
                        
            case "todegrees":
                    res = new Ex_Var(getLastGroupTmpVars());

                    res.type  = Exeption.TYPE_VAR__FLOAT;
                    
                    if( params.get(0).type == TYPE_VAR__INT )
                    {
                        res.value =  Math.toDegrees(((Long) params.get(0).value).doubleValue() );
                    }
                    else if( params.get(0).type == TYPE_VAR__FLOAT )
                    {
                        res.value =  Math.toDegrees((Double) params.get(0).value );
                    }

                    return res;
                        
            case "currenttimemillis":
                
                    res = new Ex_Var(getLastGroupTmpVars());

                    res.type  = Exeption.TYPE_VAR__INT;
                    res.value = System.currentTimeMillis();

                    return res;
                    
            case "rand01": 
                    res = new Ex_Var(getLastGroupTmpVars());

                    res.type  = Exeption.TYPE_VAR__FLOAT;
                    res.value =  Math.random();

                    return res;
    						
            case "sqrt": 
                    //tmp_v = __get_current_type_and_value((String)params.get(0).value);
                    tmp_v = params.get(0);

                    res = new Ex_Var(getLastGroupTmpVars());

                    res.type  = Exeption.TYPE_VAR__FLOAT;

                    if(tmp_v.type == Exeption.TYPE_VAR__FLOAT)
                    {
                        res.value =  Math.sqrt( (Double) tmp_v.value );
                    }
                    else
                    {
                        res.value =  Math.sqrt( (Integer) tmp_v.value );
                    }

                    return res;
                    
            case "toint": 
                    tmp_v = params.get(0);

                    res = new Ex_Var(getLastGroupTmpVars());

                    res.type  = Exeption.TYPE_VAR__INT;

                    if(tmp_v.type == Exeption.TYPE_VAR__FLOAT)
                    {
                        res.value =  Math.round((Double) tmp_v.value );
                    }
                    else
                    {
                        res.value =  (Integer) tmp_v.value;
                    }

                    return res;
                    
            case "tofloat": 
                    tmp_v = params.get(0);

                    res = new Ex_Var(getLastGroupTmpVars());

                    res.type  = Exeption.TYPE_VAR__FLOAT;

                    if(tmp_v.type == Exeption.TYPE_VAR__FLOAT)
                    {
                        res.value =  ((Double) tmp_v.value );
                    }
                    else
                    {
                        res.value =  (double)((Integer) tmp_v.value);
                    }

                    return res;
							
            case "sin": 
                    res = new Ex_Var(getLastGroupTmpVars());
                    res.type  = Exeption.TYPE_VAR__FLOAT;
                    
                    if( params.get(0).type == TYPE_VAR__INT )
                    {
                        res.value =  Math.sin(((Long) params.get(0).value).doubleValue() );
                    }
                    else if( params.get(0).type == TYPE_VAR__FLOAT )
                    {
                        res.value =  Math.sin((Double) params.get(0).value );
                    }

                    return res;
							
            case "cos": 
                    res = new Ex_Var(getLastGroupTmpVars());

                    res.type  = Exeption.TYPE_VAR__FLOAT;
                    
                    if( params.get(0).type == TYPE_VAR__INT )
                    {
                        res.value =  Math.cos(((Long) params.get(0).value).doubleValue() );
                    }
                    else if( params.get(0).type == TYPE_VAR__FLOAT )
                    {
                        res.value =  Math.cos((Double) params.get(0).value );
                    }

                    return res;
                                
            case "abs": 
                    res = new Ex_Var(getLastGroupTmpVars());

                    res.type  = Exeption.TYPE_VAR__FLOAT;
                    
                    if( params.get(0).type == TYPE_VAR__INT )
                    {
                        res.value =  Math.abs(((Long) params.get(0).value).doubleValue() );
                    }
                    else if( params.get(0).type == TYPE_VAR__FLOAT )
                    {
                        res.value =  Math.abs((Double) params.get(0).value );
                    }

                    return res;
                                
            case "ceil": 
                    res = new Ex_Var(getLastGroupTmpVars());
                    res.type  = Exeption.TYPE_VAR__FLOAT;
                    
                    if( params.get(0).type == TYPE_VAR__INT )
                    {
                        res.value =  Math.ceil(((Long) params.get(0).value).doubleValue() );
                    }
                    else if( params.get(0).type == TYPE_VAR__FLOAT )
                    {
                        res.value =  Math.ceil((Double) params.get(0).value );
                    }

                    return res;
                                
            case "floor": 
                    res = new Ex_Var(getLastGroupTmpVars());
                    res.type  = Exeption.TYPE_VAR__FLOAT;
                    
                    if( params.get(0).type == TYPE_VAR__INT )
                    {
                        res.value =  Math.floor(((Long) params.get(0).value).doubleValue() );
                    }
                    else if( params.get(0).type == TYPE_VAR__FLOAT )
                    {
                        res.value =  Math.floor((Double) params.get(0).value );
                    }

                    return res;

            case "tan": 
                    res = new Ex_Var(getLastGroupTmpVars());
                    res.type  = Exeption.TYPE_VAR__FLOAT;
                    
                    if( params.get(0).type == TYPE_VAR__INT )
                    {
                        res.value =  Math.tan(((Long) params.get(0).value).doubleValue() );
                    }
                    else if( params.get(0).type == TYPE_VAR__FLOAT )
                    {
                        res.value =  Math.tan((Double) params.get(0).value );
                    }

                    return res;
							
            case "print":  
                    //tmp_v = __get_current_type_and_value((String)params.get(0).res);
                    //String rr = String.valueOf( params.get(0).value ).replace("~", "-");

                    //System.out.printf("print: %s\n", rr );
                    
                    if( params.get(0).type == Exeption.TYPE_VAR__STR )
                    {
                        System.out.printf("\"%s\"\n", String.valueOf( params.get(0).value ) );
                    }
                    else
                    {
                        System.out.printf("%s\n", String.valueOf( params.get(0).value ) );
                    }

                    res = new Ex_Var(getLastGroupTmpVars());

                    res.type  = Exeption.TYPE_VAR__NONE;
                    res.value =  null;

                    return res;

            /*case "array": 
                    res = new Ex_Var();

                    res.type  = Exeption.TYPE_VAR__ARRAY;
                    res.value =  Math.tan( (Double) args[1] );

                    return res;*/
    	}
    	
    	Class[] _param = new Class[1];	
    	_param[0] = List.class;
    	
    	for(int j = 0, len = this.external_functions.size(); j < len; j++)
    	{
            ExternalFunction item = this.external_functions.get(j);

            if( item.name_method.trim().equalsIgnoreCase(name) )
            {
                try
                {
                    Class cls = Class.forName(item.name_class.trim());
                    Object obj = cls.newInstance();

                    Method method = cls.getDeclaredMethod(item.name_method.trim(), _param);

                    Ex_Var result_exec_f = (Ex_Var) method.invoke(obj, params);

                    if(result_exec_f == null)
                    {
                        res = new Ex_Var(getLastGroupTmpVars());

                        res.type  = Exeption.TYPE_VAR__NONE;
                        res.value =  null;

                        return res;
                    }

                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }
            }
    	}
    	
    	return null;
    }
    
    /*private void _is_array_parse(String src)
    {
    	src = src.trim();
    	
    	int p = -1;
    	boolean open_quote = false;
    	
    	for(int i = 0, len = src.length(); i < len; i++)
    	{
    		if( src.charAt(i) == '"' )
    		{
    			open_quote = ! open_quote;
    			continue;
    		}
    		
    		if(open_quote == true)
    		{
    			continue;
    		}
    		
    		if(src.charAt(i) == '[')
    		{
    			p = i; break;
    		}
    	}
    	
    	if(p != -1)
    	{
    		String name_array = src.substring(0, p).trim();
    		
    		if(name_array.length() == 0)
    		{
    			flag_error = 1;
    			error_value = Exeption.ERROR_EXEC__UNKNOWN;
    			
    			return null;
    		}
    		else if( ! _local_vars_is_var_exists(name_array))
    		{
    			flag_error = 1;
    			error_value = Exeption.ERROR_EXEC__VAR_NOT_FOUND;
    			
    			return null;
    		}
    		
    		int count_open  = 0;
    		int count_close = 0;
    		
    		open_quote = false;
    		
    		for(int i = 0, len = src.length(); i < len; i++)
    		{
    			if( src.charAt(i) == '"' )
    			{
    				open_quote = ! open_quote;
    				continue;
    			}
    			
    			if(open_quote == true)
    			{
    				continue;
    			}
    			
    			if(src.charAt(i) == '[')
    			{ count_open += 1; }
    			
    			if(src.charAt(i) == ']')
    			{ count_close += 1; }
    		}
    		
    		if(count_open != count_close)
    		{
    			flag_error = 1;
    			error_value = Exeption.ERROR_EXEC__ERROR_NOT_FORMAT_ARRAY;
    		}
    		
    		// get all '[]'*
            int offset = p;
            List<Ex_Var> params = new ArrayList<Ex_Var>();
            
            while(true)
            {
            	FindOpenCloseTok find = _get_str_open_close_tok(src, "[", "]", offset);
            	
            	if(find == null)
            	{
            		break;
            	}
    			
            	offset += find.value.length() + 2;
            	
            	if( isNumeric(src) )
            	{
            		if( isInteger(src) )
            		{
            			params.add( new Ex_Var("", Exeption.TYPE_VAR__INT, (Object)(Integer.parseInt(find.value)) ) );
            		}
            		else
            		{
            			params.add( new Ex_Var("", Exeption.TYPE_VAR__FLOAT, (Object)(Integer.parseInt(find.value)) ) );
            		}
            	}
            	else if(this._is_string(src))
            	{
        			params.add( new Ex_Var("", Exeption.TYPE_VAR__STR, (Object)(find.value) ) );
            	}
            }
            
            
    	}
    }*/
    
    public Ex_Var isVar(String name)
    {
    	name = name.toLowerCase().trim();
        
        if(global_var.get(name) != null)
    	{
            return global_var.get(name);
    	}
    	
    	if(local_tmp_vars.get(name) != null)
    	{
            return local_tmp_vars.get(name);
    	}
    	
    	if(local_vars.get(name) != null)
    	{
            return local_vars.get(name);
    	}
    	
    	return null;
    }
    
    private Ex_Var _is_const(String name)
    {
    	name = name.toLowerCase().trim();
    	
    	switch(name)
    	{
            case "none":   return new Ex_Var("", Exeption.TYPE_VAR__NONE, null);
            case "true":   return new Ex_Var("", Exeption.TYPE_VAR__INT, 1);
            case "false":  return new Ex_Var("", Exeption.TYPE_VAR__INT, 0);
            case "pi":     return new Ex_Var("", Exeption.TYPE_VAR__FLOAT, Math.PI);
            case "pi2":    return new Ex_Var("", Exeption.TYPE_VAR__FLOAT, Math.PI / 2);
    	}
    	
    	return null;
    }
    
    public static FindOpenCloseTok _get_str_open_close_tok(String src, String tok_open, String tok_close, int pos)
    {
    	int len = src.length();
    	int open_t  = 0;
    	int close_t = 0;
    	
    	boolean open_quote = false;
    	
    	for(int i = pos; i < len; i++)
    	{
            if(src.charAt(i) == '"')
            {
                open_quote = ! open_quote;
                continue;
            }

            if(open_quote == true)
            {
                continue;
            }
    		
            boolean find_open = true;

            for(int _k = 0, _len = tok_open.length(); _k < _len; _k++)
            {
                if( src.charAt( i + _k) != tok_open.charAt(_k) )
                {
                    find_open = false; break;
                }
            }

            if(find_open)
            {
                open_t += 1;

                if(open_t == 1)
                {
                    pos = i;
                }
            }

            boolean find_close = true;

            for(int _k = 0, _len = tok_close.length(); _k < _len; _k++)
            {
                if( src.charAt( i + _k) != tok_close.charAt(_k) )
                {
                    find_close = false; break;
                }
            }

            if(find_close)
            {
                close_t += 1;
            }

            if( open_t > 0 && (open_t == close_t ) )
            {
                FindOpenCloseTok res = new FindOpenCloseTok();

                int start_pos = pos + tok_open.length();
                int rlen = i - pos - tok_close.length();

                res.value = src.substring(start_pos, start_pos + rlen);
                res.pos = pos;
                
                res.pos_start = pos - tok_close.length() + 1;
                res.pos_end   = i + tok_close.length() - 1;

                return res;
            }
    	}
    	
    	return null;
    }
    
    private List<String> parse_parameters(String src)
    {
    	List<String> res = new ArrayList<String>();
    	
    	if( src.trim().length() == 0 )
    	{
    		return res;
    	}
    	
    	String s = "";
    	
    	for(int i = 0, len = src.length(); i < len; i++)
    	{
    		if(src.charAt(i) == ',')
    		{
                    res.add( s );
                    s = "";
    		}
    		else if( src.charAt(i) == '"' )
    		{
                    int pos1 = i;
                    int pos2 = 0;
                    for(int j = i + 1; j < len; j++)
                    {
                        if( src.charAt(j) == '"' )
                        {
                            pos2 = j + 1;
                            break;
                        }
                    }

                    s = s + src.substring(pos1, pos2);

                    i = pos2  - 1;
    		}
                else if( src.charAt(i) == '(' )
    		{
                    int pos1 = i;
                    int pos2 = 0;
                    for(int j = i + 1; j < len; j++)
                    {
                        if( src.charAt(j) == ')' )
                        {
                            pos2 = j + 1;
                            break;
                        }
                    }

                    s = s + src.substring(pos1, pos2);

                    i = pos2  - 1;
    		}
    		else
    		{
                    s = s + src.charAt(i);
    		}
    	}
    	
    	res.add( s.trim() );
    	return res;
    }
    
    /**
     * return int pos start token
     */
    private int _find_tok(String src, String tok, int start_pos)
    {
    	boolean open_quote = false;
    	
    	for(int i = start_pos, len = src.length(); i < len; i++)
    	{
    		if(    i - 1 > 0
                    && src.charAt(i) == '"'
                    && src.charAt(i - 1) == '\\')
    		{ }
    		else if( src.charAt(i) == '"' )
    		{
                    open_quote = ! open_quote;
                    continue;
    		}
    		
    		if( open_quote )
    		{
                    continue;
    		}
    		
    		boolean find = true;
    		
    		for(int _k = 0, _len = tok.length(); _k < _len && i + _k < len; _k++)
    		{
                    if( src.charAt(i + _k) != tok.charAt(_k) )
                    {
                        find = false;
                        break;
                    }
    		}
    		
    		if( find )
    		{
                    return i;
    		}
    	}
    	
    	return -1;
    }
    
    private int _find_tok_left(String src, int pos_s)
    {
    	int result = pos_s;
    	boolean open_quote = false;
    	
    	for(int i = pos_s; i >= 0; i--)
    	{
    		if(i == 0)
    		{
                    return i;
    		}
    		
    		if( 
    		   	   i - 1 > 0
    			&& src.charAt(i) == '"'
    			&& src.charAt(i - 1) == '\\'
    		  )
    		{ }
    		
    		if( src.charAt(i) == '"' )
    		{
                    open_quote = ! open_quote;
                    continue;
    		}
    		
    		if(open_quote == true)
    		{
                    continue;
    		}
    		
    		//System.out.printf("%c\n", src.charAt(i) );
    		
    		if( stop_tokens.indexOf( src.charAt(i) ) != -1 )
    		{
                    if( 
                            src.charAt(i) == '='
                        &&  i - 1 >= 0
                        &&  ( src.charAt(i - 1) == '<' || src.charAt(i - 1) == '>' )
                      )
                    {
                        i -= 1;
                        continue;
                    }
                    else
                    {
                        return i + 1;
                    }
    		}
    	}
    	
    	return result;
    }
    
    private int _find_tok_right(String src, int pos_s)
    {
    	boolean open_quote = false;
        int i = 0, len = 0;
    	
    	for(i = pos_s, len = src.length(); i < len; i++)
    	{
            if( 
                        i - 1 > 0
                    &&  src.charAt(i) == '"'
                    &&  src.charAt(i - 1) == '\\'
              )
            { }
            if( src.charAt(i) == '"' )
            {
                open_quote = ! open_quote;
                continue;
            }

            if(open_quote == true)
            {
                continue;
            }

            if( stop_tokens.indexOf( src.charAt(i) ) != -1 )
            {
                if( 
                        (src.charAt(i) == '>'
                     ||  src.charAt(i) == '<')
                     && i + 1 < len
                     && src.charAt(i + 1) == '='
                  )
                {
                    i += 1;
                }
                else
                {
                    return i;
                }
            }
    	}
    	
    	return i;
    }
    
    public String replace(String str, int index, char replace)
    { 
        if(str==null)
        {
            return str;
        }
        else if(index<0 || index>=str.length())
        {
            return str;
        }
        
        char[] chars = str.toCharArray();
        chars[index] = replace;
        return String.valueOf(chars);       
    }
    
    private String _minus_replace(String src)
    {
    	String result = src;
    	
    	while(true)
    	{
    		boolean find = false;
    		boolean open_quote = false;
    		
    		for(int i = 0, len = result.length(); i < len; i++)
    		{
                    if(
                                    i - 1 > 0
                            &&  src.charAt(i) == '"'
                            &&  src.charAt(i - 1) == '\\'
                      )
                    { }
    			
                    if( src.charAt(i) == '"' )
                    {
                            open_quote = ! open_quote;
                            continue;
                    }
    			
                    if(open_quote == true)
                    {
                            continue;
                    }
    			
                    if(result.charAt(i) == '-')
                    {
                        int pos = i;
                        int pos_left  = this._find_tok_left(src, pos - 1);
                        //int pos_right = this._find_tok_right(src, pos + 1 );

                        if(pos_left < 0) // not found
                        {
                            result = replace(result, i, '~');

                            find = true;
                            break;
                        }
                        else
                        {
                                String str_left = src.substring(pos_left, pos);

                                if( str_left.trim().length() == 0 )
                                {
                                        result = replace(result, i, '~');

                                        find = true;
                                        break;
                                }
                        }
                    }
    		}
    		
    		if(! find)
            {
                break;
            }
    	}
    	
    	return src;
    }
    
    private boolean isNumeric(String s) 
    {
        try 
        {
            Integer.parseInt(s);
        } 
        catch (NumberFormatException | NullPointerException nfe) 
        {
            return false;
        }
        return true;
    }
    
    private boolean isFloat(String s) 
    {
        try 
        {
            Float.parseFloat(s);
        } 
        catch (NumberFormatException | NullPointerException nfe) 
        {
            return false;
        }
        return true;
    }  
    
    private boolean isInteger(String s)
    {
    	return s.matches("^-?\\d+$");
    }
    
    private String _convert_res_type_to_str(Object value, int type, String var_name)
    {
    	String str = "";
    	
    	if(type == Exeption.TYPE_VAR__INT)
    	{
            long v = (Long)value;
            String _s = String.valueOf(v);

            _s = _s.replace("-", "~");

            return _s;
    	}
    	else if(type == Exeption.TYPE_VAR__FLOAT)
    	{
            Double v = (Double)value;
            String _s = Utils.setPrecision(v, 9);

            _s = _s.replace("-", "~");

            return _s;
    	}
    	else if(type == Exeption.TYPE_VAR__STR)
    	{
            return (String) value;
    	}
    	else if(type == Exeption.TYPE_VAR__ARRAY)
    	{
            return var_name;
    	}
    	else if(type == Exeption.TYPE_VAR__USER_DATA)
    	{
            return var_name;
    	}
    	else if(type == Exeption.TYPE_VAR__NONE)
    	{
            return "none";
    	}
    	else
    	{
            flag_error = 1;
            error_value = Errors.ERROR_EXEC__ERROR_NOT_CONVERT_VALUE_TO_STRING;
    	}
    	
    	return "";
    }
    
    /*public boolean var_is_var_exists(String name)
    {
    	return this._local_vars_is_var_exists(name);
    }*/
    
    private Ex_Var __get_current_type_and_value(String src)
    {
    	Ex_Var res = null;

    	int is_minus = 1;
    	src = src.trim();
    	
    	if(src.length() == 0)
    	{
            return res;
    	}
    	
    	if(src.charAt(0) == '-')
    	{
            src = replace(src, 0, '~');
    	}
    	
    	if(src.charAt(0) == '~')
    	{
            is_minus = -1;
    	}
    	
    	src = src.replace("~", "").trim();
    	
    	
        if( isNumeric(src) )
        {
            res = new Ex_Var(getLastGroupTmpVars());
            res.type = Exeption.TYPE_VAR__INT;
            res.value  = (Object)(Long.parseLong(src) * is_minus);
        }
        else if( isFloat(src) )
        {
            res = new Ex_Var(getLastGroupTmpVars());
            res.type = Exeption.TYPE_VAR__FLOAT;
            res.value  = (Object)(Double.parseDouble(src) * is_minus);
        }
    	else if(this._is_string(src))
    	{
            res = new Ex_Var(getLastGroupTmpVars());
            res.type = Exeption.TYPE_VAR__STR;
            res.value  = (Object)( src.substring(1, src.length() - 1) );
    	}
        else if(src == "none")
        {
            res = new Ex_Var(getLastGroupTmpVars());
            res.type  = Exeption.TYPE_VAR__NONE;
            res.value = null;
        }
    	//else array
    	else
    	{
            if(flag_error == 1)
            {
                return res;
            }

            Ex_Var v = this._is_const(src); 

            if( v != null )
            {
                res = new Ex_Var(getLastGroupTmpVars());
                res.type = v.type;
                res.value  = v.value;
                res.const_name = src;
                res.is_const   = true;
            }
            else
            {
                v = this.isVar(src);

                if(v == null)
                {
                    // Проверка на МАССИВ
                    int _pos = this._find_tok(src, "[", 0);
                    
                    if(_pos != -1)
                    {
                        int pos_right = this._find_tok_left(src, _pos - 1 );

                        String var_name = src.substring(pos_right,  _pos);

                        Ex_Var _res1 = exec(var_name);

                        if( _res1 != null )
                        {
                            FindOpenCloseTok yy = Exeption._get_str_open_close_tok(src, "[", "]", 0);

                            //String var_name = src.substring(0, yy.pos_start);

                            //Ex_Var _res1 = exec(var_name);
                            
                            
                            if( src.substring(yy.pos_end + 1).trim().length() > 0 )
                            {
                                return null;
                            }
                            
                            Ex_Var _res2 = exec(yy.value);

                            if( _res1.type == TYPE_VAR__ARRAY && ( _res2.type == TYPE_VAR__INT || _res2.type == TYPE_VAR__FLOAT ) )
                            {
                                List<Ex_Var> array = (List<Ex_Var>)_res1.value;

                                int index = 0;

                                if(_res2.type == TYPE_VAR__INT)
                                {
                                    index = ( Math.toIntExact((Long)_res2.value));
                                }
                                else
                                {
                                    index = ( Math.toIntExact(Math.round((Double)_res2.value)));
                                }

                                //_res1.array_index = index;

                                if(index >= 0 && index < array.size())
                                {
                                    return array.get( index );
                                }
                                else
                                {
                                   flag_error = 1;
                                   error_value = Errors.ERROR_EXEC__OUT_OF_INDEX;
                                   //res = new Ex_Var();
                                   return _res1;
                                }
                            }


                            return null;
                        }
                        else if( var_name.trim().length() > 0 )
                        {
                            flag_error = 1;
                            error_value = Errors.ERROR_EXEC__VAR_NOT_FOUND;
                            return null;
                        }
                    }
                    
                    return null;
                    
                }
                else
                {
                    /*res = new Ex_Var(getLastGroupTmpVars());
                    res.type = v.type;
                    res.value  = v.value;
                    res.var_name = src;
                    res.is_var   = true;*/
                    
                    v.var_name = src;
                    res = v;
                }
            }	
    	}
    	
    	return res;
    }
    
    private Ex_Var _exec(String src)
    {
    	//System.out.println("_exec_: "  + src);
    	
    	src = src.trim();
    	
    	src = this._minus_replace(src).trim();
    	
    	if(src.length() == 0)
    	{
            return null;
    	}
    	
    	if(src.charAt(0) == '-' || src.charAt(0) == '+')
    	{
            src = "0" + src;
    	}
        
        Ex_Var analize_first = __get_current_type_and_value(src);
        
        if( flag_error == 1 )
        {
            return null;
        }
        
        if( analize_first != null )
        {
            return analize_first;
        }
    	
    	for(int i = 0; i < priority.size(); i++)
    	{
            PriorityItem item = priority.get(i);

            if(item.type == Exeption.ANALIZ_BEGIN_END)
            {
                src = this.__ANALIZ_BEGIN_END(src, item);
            }
            else if(item.type == Exeption.ANALIZ_LEFT_RIGHT)
            {
                src = this.__ANALIZ_LEFT_RIGHT(src, item);
            }
            else if(item.type == Exeption.ANALIZ_LEFT_RIGHT)
            {
                src = this.__ANALIZ_RIGHT(src, item);
            }
            
            if( STATUS_EXIT )
            {
                return EXIT_PARAMETERS;
            }

            if(flag_error == 1)
            {
                return null;
            }
    	}
    	
    	//System.out.printf("%s\n", src);
    	
    	Ex_Var  _r = __get_current_type_and_value(src);
    	
    	if(this.flag_error == 1)
    	{
            return null;
    	}
    	
    	/*Ex_Var _return = new Ex_Var();
    	_return.type = _r.type;
    	
    	if(
    	        _r.type == Exeption.TYPE_VAR__ARRAY
             || _r.type == Exeption.TYPE_VAR__USER_DATA
           )
        {
    		_return.value  = _r.value;
        }
    	else
    	{
            _return.value = _convert_res_type_to_str(_r.value, _r.type, "");
            _return.type = TYPE_VAR__STR;
    	}*/
    	
    	return _r;
    }
    
    private String __ANALIZ_RIGHT(String src, PriorityItem pitem)
    {
    	while(true)
    	{
            List<String[]> pos_mass = new ArrayList<String[]>();
            List<Integer>  f_pos_list    = new ArrayList<Integer>();
            String[] struct = null;
            boolean find_token = false;

            for(int p = 0; p < pitem.data.size(); p++)
            {
                int f_pos = this._find_tok(src, pitem.data.get(p)[0], 0);

                if(f_pos == -1)
                {
                        continue;
                }

                find_token = true;
                pos_mass.add( pitem.data.get(p) );
                f_pos_list.add( f_pos );
            }
    		
            //System.out.println(pitem.data.get(p)[0]);

            int min = src.length();
            for(int i = 0, len = pos_mass.size(); i < len; i++)
            {
                if(min > f_pos_list.get(i))
                {
                    min    = f_pos_list.get(i);
                    struct = pos_mass.get(i);
                }
            }
    		
            int pos = min;

            if(pos == -1 || ! find_token)
            {
                break;
            }
    		
            int pos_right = this._find_tok_left(src, pos + struct[0].length() );

            String str_right = src.substring(pos + struct[0].length(), pos_right - pos - struct[0].length());

            Ex_Var r2 = this.__get_current_type_and_value(str_right);

            if(flag_error == 1)
            {
                return "";
            }
    		
            Object right_res = r2.value;

            int type_right = r2.type;

            str_right = str_right.trim();
    		
            if(
                (type_right == Exeption.TYPE_VAR__INT)
             || (type_right == Exeption.TYPE_VAR__FLOAT)
            )
            {
                if(struct[0].equalsIgnoreCase("!"))
                {
                    double v1 = (type_right == Exeption.TYPE_VAR__INT)? (Long) right_res: (Float) right_res ;

                    if( v1 != 0)
                    {
                        src = src.substring(0, pos) + "1" + src.substring(pos_right + 1);
                    }
                    else
                    {
                        src = src.substring(0, pos) + "0" + src.substring(pos_right + 1);
                    }
                }
                else
                {
                    flag_error = 1;
                    error_value = Errors.ERROR_EXEC__ERROR_OPERATION;
                    return "";
                }
            }
    	}
    	
    	return src;
    }
    
    private String __ANALIZ_LEFT_RIGHT(String src, PriorityItem pitem)
    {
    	while(true)
    	{
            List<String[]> pos_mass     = new ArrayList<String[]>();
            List<Integer>  f_pos_list   = new ArrayList<Integer>();
            String[] struct             = null;
            boolean find_token          = false;

            for(int p = 0; p < pitem.data.size(); p++)
            {
                int f_pos = this._find_tok(src, pitem.data.get(p)[0], 0);

                if(f_pos == -1)
                {
                    continue;
                }

                find_token = true;
                pos_mass.add( pitem.data.get(p) );
                f_pos_list.add( f_pos );
            }
    	    
            int min = src.length();
            for(int i = 0, len = pos_mass.size(); i < len; i++)
            {
                if(min > f_pos_list.get(i))
                {
                    min    = f_pos_list.get(i);
                    struct = pos_mass.get(i);
                }
            }

            int pos = min;

            if(pos == -1 || ! find_token)
            {
                break;
            }

            //System.out.printf("pos: %d %d\n", pos, struct[0].length() );

            int pos_left  = this._find_tok_left(src, pos - 1);
            int pos_right = this._find_tok_right(src, pos + struct[0].length() );

            if( pos_left < 0 )
            {
                return src;
            }

            String str_left  = src.substring(pos_left, pos);
            String str_right = src.substring(pos + struct[0].length(), pos_right);

            if( str_left.trim().length() == 0 )
            {
                return src;
            }

            Ex_Var r1 = this.__get_current_type_and_value(str_left);
            Ex_Var r2 = this.__get_current_type_and_value(str_right);
            
            if( r1 == null )
            {
                flag_error = 1;
            }
            
            if( r2 == null )
            {
                flag_error = 1;
            }

            if(flag_error == 1)
            {
                return "";
            }

            Object left_res  = r1.value;
            Object right_res = r2.value;

            int type_left = r1.type;
            int type_right = r2.type;

            /*System.out.printf("%s [%s] %s [%s] %d\n"
            , this.get_str_type_value(type_left)
            , str_left
            , this.get_str_type_value(type_right)
            , str_right
            , pos_right );*/

            str_left  = str_left.trim();
            str_right = str_right.trim();

            if(
                    ( type_left == Exeption.TYPE_VAR__INT && type_right == Exeption.TYPE_VAR__INT )
              )
            {
                long v1 = (Long) left_res;
                long v2 = (Long) right_res;

                long _res = 0;

                switch(struct[0])
                {
                    case "+": _res = v1 + v2; break;
                    case "*": _res = v1 * v2; break;
                    case "^": _res = (int) Math.pow((double)v1, (double)v2); break;
                    case "/": 
                                    
                            double _d_res = (double) v1 / (double) v2;

                            if(_d_res < 0)
                            {
                                String s_res = (_d_res == 0)?"0": Utils.setPrecision(_d_res, 9);
                                s_res = s_res.replace("-", "~");

                                src = src.substring(0, pos_left) + s_res + src.substring(pos_right);
                            }
                            else
                            {
                                String s_res = (_d_res == 0)?"0": Utils.setPrecision(_d_res, 9);

                                src = src.substring(0, pos_left) + s_res + src.substring(pos_right);

                                //System.out.println(src);
                            }


                            continue;
                                    
                    case "-": _res = v1 - v2; break;
                    case "%": _res = v1 % v2; break;
                    case "==": _res = v1 == v2?1:0; break;
                    case "!=": _res = v1 != v2?1:0; break;
                    case ">": _res = v1 > v2?1:0; break;
                    case "<": _res = v1 < v2?1:0; break;
                    case "<=": _res = v1 <= v2?1:0; break;
                    case ">=": _res = v1 >= v2?1:0; break;
                    case "||": _res = (v1 == 0?false:true) || (v2 == 0?false:true) ?1:0; break;
                    case "&&": _res = (v1 == 0?false:true) && (v2 == 0?false:true) ?1:0; break;
                    case "=":   
                                    if( r1.is_var == true && r1.is_const )
                                    {
                                        flag_error = 1;
                                        error_value = Errors.ERROR_EXEC__VAR_IS_CONST;
                                        return "";
                                    }
                        
                                    if( type_left == type_right )
                                    {
                                        if(r1.is_var == true && r2.is_var == false)
                                        {
                                            this._local_vars_set_new_value(str_left, right_res);

                                            _res = v2;
                                        }
                                        else if(r1.is_var == true && r2.is_var == true)
                                        {
                                            this._local_vars_set_new_value(str_left, right_res);

                                            _res = v2;
                                        }
                                    }
                                    else
                                    {
                                        this._local_vars_set_new_value_and_type(str_left, type_right, right_res);
                                    }

                                    break;
                                    
                    case "+=": 
                                if( r1.is_var == true && r1.is_const )
                                {
                                    flag_error = 1;
                                    error_value = Errors.ERROR_EXEC__VAR_IS_CONST;
                                    return "";
                                }
                                
                                if( type_left == type_right )
                                {
                                    if(r1.is_var == true && r2.is_var == false)
                                    {
                                        this._local_vars_set_new_value(str_left, (Long) left_res + (Long) right_res);

                                        _res = v2;
                                    }
                                    else if(r1.is_var == true && r2.is_var == true)
                                    {
                                        this._local_vars_set_new_value(str_left, (Long) left_res + (Long) right_res);

                                        _res = v2;
                                    }
                                }
                                else
                                {
                                    this._local_vars_set_new_value_and_type(str_left, type_right, right_res);
                                }

                                break;
    							
                    default:
                        flag_error = 1;
                        error_value = Errors.ERROR_EXEC__ERROR_OPERATION;
                        return "";
                    }

                    if(_res < 0)
                    {
                        String s_res = (_res == 0)?"0": String.valueOf(_res);
                        s_res = s_res.replace("-", "~");

                        src = src.substring(0, pos_left) + s_res + src.substring(pos_right);
                    }
                    else
                    {
                        String s_res = (_res == 0)?"0": String.valueOf(_res);

                        src = src.substring(0, pos_left) + s_res + src.substring(pos_right);

                        //System.out.println(src);
                    }
            }
            else if(
                    ( type_left == Exeption.TYPE_VAR__INT || type_left == Exeption.TYPE_VAR__FLOAT )
                 && ( type_right == Exeption.TYPE_VAR__INT || type_right == Exeption.TYPE_VAR__FLOAT )
                )
            {
                    double v1 = 0;

                    if(type_left == Exeption.TYPE_VAR__INT)
                    {
                        if( left_res instanceof Double )
                        {
                            v1 = ( (Double) left_res).intValue();
                        }
                        else
                        {
                            v1 = (Long) left_res;
                        }
                    }
                    else
                    {
                        v1 = (Double) left_res;
                    }
    			
                    double v2 = 0;

                    if(type_right == Exeption.TYPE_VAR__INT)
                    {
                        if( right_res instanceof Double )
                        {
                            v2 = ( (Double) right_res).intValue();
                        }
                        else
                        {
                            v2 = (Long) right_res;
                        }
                    }
                    else
                    {
                        v2 = (Double) right_res;
                    }
    			
                    //(type_right == Exeption.TYPE_VAR__INT)? (int) right_res: (Double) right_res ;

                    double _res = 0;

                    switch(struct[0])
                    {
                        case "+": _res = v1 + v2; break;
                        case "*": _res = v1 * v2; break;
                        case "^": _res = Math.pow((double)v1, (double)v2); break;
                        case "/": _res = v1 / v2; break;
                        case "-": _res = v1 - v2; break;
                        case "%": _res = v1 % v2; break;
                        case "==": _res = v1 == v2?1:0; break;
                        case "!=": _res = v1 != v2?1:0; break;
                        case ">": _res = v1 > v2?1:0; break;
                        case "<": _res = v1 < v2?1:0; break;
                        case "<=": _res = v1 <= v2?1:0; break;
                        case ">=": _res = v1 >= v2?1:0; break;
                        case "||": _res = (v1 == 0?false:true) || (v2 == 0?false:true) ?1:0; break;
                        case "&&": _res = (v1 == 0?false:true) && (v2 == 0?false:true) ?1:0; break;
                        case "=":
                                
                                if( r1.is_var == true && r1.is_const )
                                {
                                    flag_error = 1;
                                    error_value = Errors.ERROR_EXEC__VAR_IS_CONST;
                                    return "";
                                }
                            
                                if( type_left == type_right )
                                {
                                    if(r1.is_var == true && r2.is_var == false)
                                    {
                                        this._local_vars_set_new_value(str_left, right_res);

                                        _res = v2;
                                    }
                                    else if(r1.is_var == true && r2.is_var == true)
                                    {
                                        this._local_vars_set_new_value(str_left, right_res);

                                        _res = v2;
                                    }
                                }
                                else
                                {
                                    this._local_vars_set_new_value_and_type(str_left, type_right, right_res);
                                }

                                break;
    							
                        default:
                            flag_error = 1;
                            error_value = Errors.ERROR_EXEC__ERROR_OPERATION;
                            return "";
                    }
    			
                    if(_res < 0)
                    {
                        String s_res = (_res == 0)?"0": Utils.setPrecision(_res, 9);
                        s_res = s_res.replace("-", "~");

                        src = src.substring(0, pos_left) + s_res + src.substring(pos_right);
                    }
                    else
                    {
                        String s_res = (_res == 0)?"0": Utils.setPrecision(_res, 9);

                        src = src.substring(0, pos_left) + s_res + src.substring(pos_right);

                        //System.out.println(src);
                    }
            }
            else if(
                        r1.is_var == true
                    &&  struct[0].equalsIgnoreCase("+=")
                    &&  type_left == Exeption.TYPE_VAR__STR
                    &&  type_right == Exeption.TYPE_VAR__STR
                )
            {
                if( r1.is_const )
                {
                    flag_error = 1;
                    error_value = Errors.ERROR_EXEC__VAR_IS_CONST;
                    return "";
                }
                
                String s1 = (String) left_res;
                String s2 = (String) right_res;
                
                this._local_vars_set_new_value(r1.var_name, s1 + s2);

                String s_res = this._convert_res_type_to_str(s1 + s2, type_left, "");

                src = src.substring(0, pos_left) + "\"" + s_res + "\"" + src.substring(pos_right);
                
                return src;
            }
            else if(
                        r1.is_var == true
                    &&  struct[0].equalsIgnoreCase("+=")
                    &&  type_left == Exeption.TYPE_VAR__INT
                    &&  type_right == Exeption.TYPE_VAR__INT
                )
            {
                if( r1.is_const )
                {
                    flag_error = 1;
                    error_value = Errors.ERROR_EXEC__VAR_IS_CONST;
                    return "";
                }
                
                Integer s1 = (Integer) left_res;
                Integer s2 = (Integer) right_res;
                
                this._local_vars_set_new_value(r1.var_name, s1 + s2);

                String s_res = this._convert_res_type_to_str(s1 + s2, type_left, "");

                src = src.substring(0, pos_left) + "\"" + s_res + "\"" + src.substring(pos_right);
                
                return src;
            }
            else if(
                        r1.is_var == true
                    &&  struct[0].equalsIgnoreCase("=")
                    &&  r2.is_var == false
                    &&  r2.is_array == false
                )
            {
                    if( r1.is_const )
                    {
                        flag_error = 1;
                        error_value = Errors.ERROR_EXEC__VAR_IS_CONST;
                        return "";
                    }
                    
                
                    if(type_right == Exeption.TYPE_VAR__INT || type_right == Exeption.TYPE_VAR__FLOAT)
                    {
                        this._local_vars_set_new_value_and_type(r1.var_name, type_right, right_res);

                        String s_res = this._convert_res_type_to_str(right_res, type_right, "");

                        src = src.substring(0, pos_left) + s_res + src.substring(pos_right);
                    }
                    else if(type_right == Exeption.TYPE_VAR__STR)
                    {
                        this._local_vars_set_new_value_and_type(r1.var_name, type_right, str_right.substring(1, str_right.length() - 1));

                        String s_res = this._convert_res_type_to_str(right_res, type_right, "");

                        src = src.substring(0, pos_left) + s_res + src.substring(pos_right);
                    }
                    else if(type_right == Exeption.TYPE_VAR__NONE)
                    {
                        this._local_vars_set_new_value_and_type(r1.var_name, type_right, null);

                        String s_res = this._convert_res_type_to_str(right_res, type_right, "");

                        src = src.substring(0, pos_left) + s_res + src.substring(pos_right);
                    }
            }
            else if(
                        r1.is_var == true
                    &&  struct[0].equalsIgnoreCase("=")
                    &&  r2.is_var == true
                )
            {
                    if( r1.is_const )
                    {
                        flag_error = 1;
                        error_value = Errors.ERROR_EXEC__VAR_IS_CONST;
                        return "";
                    }
                
                    //Ex_Var r = _local_vars_get_value(r2.var_name);

                    //Object res = r2.value;

                    _local_vars_set_new_value_and_type(r1.var_name, r2.type, r2.value);

                    String s_res = r1.var_name; //this._convert_res_type_to_str(r2.value, r2.type, "");

                    src = src.substring(0, pos_left) + s_res + src.substring(pos_right);
            }
            else if(
                (
                       type_left  == Exeption.TYPE_VAR__INT 
                    || type_left == Exeption.TYPE_VAR__FLOAT
                )
                &&
                    type_right == Exeption.TYPE_VAR__STR
              )
            {
                String ss = str_right.substring(1, str_right.length() - 1);

                switch(struct[0])
                {
                    case ">":
                    case "<":
                    case "<=":
                    case ">=":
                    case "-":	
                    case "+":
                                    
                            if( isNumeric(ss) )
                            {
                                        if( isInteger(ss) )
                                        {
                                            Integer _t_int = Integer.parseInt(ss);

                                            if(type_left  == Exeption.TYPE_VAR__INT)
                                            {
                                                long res = 0;

                                                switch(struct[0])
                                                {
                                                    case ">":   res = ((Long)left_res > _t_int)?1:0; break;
                                                    case "<":   res = ((Long)left_res < _t_int)?1:0; break;
                                                    case "<=":  res = ((Long)left_res <= _t_int)?1:0; break;
                                                    case ">=":  res = ((Long)left_res >= _t_int)?1:0; break;
                                                    case "-":	res = (Long)left_res - _t_int; break;
                                                    case "+":   res = (Long)left_res + _t_int; break;
                                                    default:    flag_error = 1;
                                                }

                                                src =     src.substring(0, pos_left) 
                                                        + String.valueOf(res).replace("-", "~") 
                                                        + src.substring(pos_right );
                                            }
                                            else
                                            {
                                                Double res = 0.0;

                                                switch(struct[0])
                                                {
                                                    case ">":   res = ((Double)left_res > _t_int)?1.0:0.0; break;
                                                    case "<":   res = ((Double)left_res < _t_int)?1.0:0.0; break;
                                                    case "<=":  res = ((Double)left_res <= _t_int)?1.0:0.0; break;
                                                    case ">=":  res = ((Double)left_res >= _t_int)?1.0:0.0; break;
                                                    case "-":	res = (Double)left_res - _t_int; break;
                                                    
                                                    /*case "+":   src =  src.substring(0, pos_left) 
                                                         + "\"" + ss + String.valueOf(right_res) + "\""
                                                         + src.substring(pos_right );
                                                    
                                                        break;*/
                                                        
                                                    case "+":   src =  "\"" + ss + String.valueOf(right_res) + "\"";
                                                    
                                                        break;
                                                    
                                                    default:    flag_error = 1;
                                                }

                                                src =     src.substring(0, pos_left) 
                                                        + String.valueOf(res).replace("-", "~")
                                                        + src.substring(pos_right );
                                            }
                                        }
                                        else
                                        {
                                            Double _t_d = Double.parseDouble(ss);

                                            if(type_left  == Exeption.TYPE_VAR__INT)
                                            {
                                                Double res = 0.0;

                                                switch(struct[0])
                                                {
                                                    case ">":   res = ((Long)left_res > _t_d)?1.0:0.0; break;
                                                    case "<":   res = ((Long)left_res < _t_d)?1.0:0.0; break;
                                                    case "<=":  res = ((Long)left_res <= _t_d)?1.0:0.0; break;
                                                    case ">=":  res = ((Long)left_res >= _t_d)?1.0:0.0; break;
                                                    case "-":	res = (Long)left_res - _t_d; break;
                                                    case "+":   res = (Long)left_res + _t_d; break;
                                                    default:    flag_error = 1;
                                                }

                                                src =     src.substring(0, pos_left) 
                                                        + String.valueOf(res).replace("-", "~")
                                                        + src.substring(pos_right );
                                            }
                                            else
                                            {
                                                Double res = 0.0;

                                                switch(struct[0])
                                                {
                                                    case ">":   res = ((Double)left_res > _t_d)?1.0:0.0; break;
                                                    case "<":   res = ((Double)left_res < _t_d)?1.0:0.0; break;
                                                    case "<=":  res = ((Double)left_res <= _t_d)?1.0:0.0; break;
                                                    case ">=":  res = ((Double)left_res >= _t_d)?1.0:0.0; break;
                                                    case "-":	res = (Double)left_res - _t_d; break;
                                                    case "+":   res = (Double)left_res + _t_d; break;
                                                    default:    flag_error = 1;
                                                }

                                                src =     src.substring(0, pos_left) 
                                                        + String.valueOf(res).replace("-", "~")
                                                        + src.substring(pos_right );
                                            }
                                        }
                            }
                            else
                            {
                                    Integer _t_int = ss.length();

                                    if(type_left  == Exeption.TYPE_VAR__INT)
                                    {
                                        long res = 0;

                                        switch(struct[0])
                                        {
                                            case ">":   res = ((Long)left_res > _t_int)?1:0; break;
                                            case "<":   res = ((Long)left_res < _t_int)?1:0; break;
                                            case "<=":  res = ((Long)left_res <= _t_int)?1:0; break;
                                            case ">=":  res = ((Long)left_res >= _t_int)?1:0; break;
                                            case "-":   res = (Long)left_res - _t_int; break;
                                            case "+":   res = (Long)left_res + _t_int; break;
                                            default:    flag_error = 1;
                                        }

                                        src =     src.substring(0, pos_left) 
                                                + String.valueOf(res).replace("-", "~")
                                                + src.substring(pos_right );
                                    }
                                    else
                                    {
                                        Double res = 0.0;

                                        switch(struct[0])
                                        {
                                            case ">":   res = ((Double)left_res > _t_int)?1.0:0.0; break;
                                            case "<":   res = ((Double)left_res < _t_int)?1.0:0.0; break;
                                            case "<=":  res = ((Double)left_res <= _t_int)?1.0:0.0; break;
                                            case ">=":  res = ((Double)left_res >= _t_int)?1.0:0.0; break;
                                            case "-":	res = (Double)left_res - _t_int; break;
                                            
                                            case "+":   src =  src.substring(0, pos_left) 
                                                         + "\"" + String.valueOf(left_res) + ss + "\""
                                                         + src.substring(pos_right );
                                                    
                                                        return src;
                                            
                                            default:    flag_error = 1;
                                        }

                                        src =     src.substring(0, pos_left) 
                                                + String.valueOf(res).replace("-", "~")
                                                + src.substring(pos_right );
                                    }
                            }

                            break;
                                    
                    default:
                        flag_error = 1;
                        error_value = Errors.ERROR_EXEC__ERROR_OPERATION;
                        return "";
                }
            }
            else if(
                        type_left == Exeption.TYPE_VAR__STR
                    &&  (  
                            type_right == Exeption.TYPE_VAR__INT
                         || type_right == Exeption.TYPE_VAR__FLOAT
                        )
                    )
            {
                String ss = str_left.substring(1, str_left.length() - 1);

                switch(struct[0])
                {
                    case ">":
                    case "<":
                    case "<=":
                    case ">=":
                    case "-":	
                    case "+":	
                            
                            if( isNumeric(ss) )
                            {
                                if( isInteger(ss) )
                                {
                                    Integer _t_int = Integer.parseInt(ss);

                                    if(type_right  == Exeption.TYPE_VAR__INT)
                                    {
                                        long res = 0;

                                        switch(struct[0])
                                        {
                                            case ">":   res = (_t_int > (Long)right_res )?1:0; break;
                                            case "<":   res = (_t_int < (Long)right_res)?1:0; break;
                                            case "<=":  res = (_t_int <= (Long)right_res)?1:0; break;
                                            case ">=":  res = (_t_int >= (Long)right_res)?1:0; break;
                                            case "-":	res = (Long)right_res - _t_int; break;
                                            case "+":   res = (Long)right_res + _t_int; break;
                                            default:    flag_error = 1;
                                        }

                                        src =     src.substring(0, pos_left) 
                                                + String.valueOf(res).replace("-", "~")
                                                + src.substring(pos_right );
                                    }
                                    else
                                    {
                                        Double res = 0.0;

                                        switch(struct[0])
                                        {
                                            case ">":   res = (_t_int > (Double)right_res)?1.0:0.0; break;
                                            case "<":   res = (_t_int < (Double)right_res)?1.0:0.0; break;
                                            case "<=":  res = (_t_int <= (Double)right_res)?1.0:0.0; break;
                                            case ">=":  res = (_t_int >= (Double)right_res)?1.0:0.0; break;
                                            case "-":	res = (Double)right_res - _t_int; break;
                                            case "+":   res = (Double)right_res + _t_int; break;
                                            default:    flag_error = 1;
                                        }

                                        src =     src.substring(0, pos_left) 
                                            + String.valueOf(res).replace("-", "~")
                                            + src.substring(pos_right );
                                    }
                                }
                                else
                                {
                                    Double _t_d = Double.parseDouble(ss);

                                    if(type_right  == Exeption.TYPE_VAR__INT)
                                    {
                                        Double res = 0.0;

                                        switch(struct[0])
                                        {
                                            case ">":   res = (_t_d > (Long)right_res)?1.0:0.0; break;
                                            case "<":   res = (_t_d < (Long)right_res)?1.0:0.0; break;
                                            case "<=":  res = (_t_d <= (Long)right_res)?1.0:0.0; break;
                                            case ">=":  res = (_t_d >= (Long)right_res)?1.0:0.0; break;
                                            case "-":	res = (Long)right_res - _t_d; break;
                                            case "+":   res = (Long)right_res + _t_d; break;
                                            default:    flag_error = 1;
                                        }
				    	    				
                                        src =     src.substring(0, pos_left) 
                                                + String.valueOf(res).replace("-", "~")
                                                + src.substring(pos_right );
                                    }
                                    else
                                    {
                                        Double res = 0.0;

                                        switch(struct[0])
                                        {
                                            case ">":   res = (_t_d > (Double)right_res)?1.0:0.0; break;
                                            case "<":   res = (_t_d < (Double)right_res)?1.0:0.0; break;
                                            case "<=":  res = (_t_d <= (Double)right_res)?1.0:0.0; break;
                                            case ">=":  res = (_t_d >= (Double)right_res)?1.0:0.0; break;
                                            case "-":	res = (Double)right_res - _t_d; break;
                                            case "+":   res = (Double)right_res + _t_d; break;
                                            default:    flag_error = 1;
                                        }
				    	    				
                                        src =   src.substring(0, pos_left) 
                                              + String.valueOf(res).replace("-", "~")
                                              + src.substring(pos_right );
                                    }
                                }
                            }
                            else
                            {
                                Integer _t_int = ss.length();

                                if(type_right  == Exeption.TYPE_VAR__INT)
                                {
                                    long res = 0;
				    					
                                    switch(struct[0])
                                    {
                                        case ">":   res = (_t_int > (Long)right_res)?1:0; break;
                                        case "<":   res = (_t_int < (Long)right_res)?1:0; break;
                                        case "<=":  res = (_t_int <= (Long)right_res)?1:0; break;
                                        case ">=":  res = (_t_int >= (Long)right_res)?1:0; break;
                                        case "-":   res = (Long)right_res - _t_int; ;
                                                    src =  src.substring(0, pos_left) 
                                                         + String.valueOf(res).replace("-", "~")
                                                         + src.substring(pos_right );
                                                    
                                                    break;
                                                    
                                        case "+":   src =  src.substring(0, pos_left) 
                                                         + "\"" + ss + String.valueOf(right_res) + "\""
                                                         + src.substring(pos_right );
                                    }
			    	    
                                }
                                else
                                {
                                    Double res = 0.0;
			    	    				
                                    switch(struct[0])
                                    {
                                        case ">":   res = (_t_int > (Double)right_res)?1.0:0.0; break;
                                        case "<":   res = (_t_int < (Double)right_res)?1.0:0.0; break;
                                        case "<=":  res = (_t_int <= (Double)right_res)?1.0:0.0; break;
                                        case ">=":  res = (_t_int >= (Double)right_res)?1.0:0.0; break;
                                        case "-":	res = (Double)right_res - _t_int; break;
                                        case "+":   res = (Double)right_res + _t_int; break;
                                    }
			    	    				
                                    src =     src.substring(0, pos_left) 
                                            + String.valueOf(res).replace("-", "~")
                                            + src.substring(pos_right );
                                }
                            }
	    						
                            break;
                            
                    default:
                            flag_error = 1;
                            error_value = Errors.ERROR_EXEC__ERROR_OPERATION;
                            return "";
                }
            }
            else if(
                        (
                            (type_left  == Exeption.TYPE_VAR__STR)
                        )
                        &&
                        (
                            (type_right == Exeption.TYPE_VAR__STR)
                        )
                   )
            {
                String res = "";

                switch(struct[0])
                {
                    case "+":
                        res =     '"' 
                                + str_left.substring(1, str_left.length() - 1) 
                                + str_right.substring(1, str_right.length() - 1) 
                                + '"';

                        src =     src.substring(0, pos_left) 
                                + res + src.substring(pos_right );
                        break;

                    case "==": 
                        res = "0";

                        if( str_left.substring(1, str_left.length() - 1).equalsIgnoreCase( str_right.substring(1, str_right.length() - 1) )  )
                        {
                            res = "1";
                        }

                        src =     src.substring(0, pos_left) 
                                + res + src.substring(pos_right );
                        break;

                    default:
                        flag_error = 1;
                        error_value = Errors.ERROR_EXEC__ERROR_OPERATION;
                        return "";
                }
            }
            else
            {
                flag_error  = 1;
                error_value = Errors.ERROR_EXEC__VAR_DEFINED_EROOR;
                return src;
            }
    	}
    	
    	return src;
    }
    
    private String __ANALIZ_BEGIN_END(String src, PriorityItem pitem)
    {
    	while(true)
    	{
            List<String[]> pos_mass = new ArrayList<String[]>();
            List<Integer>  f_pos_list    = new ArrayList<Integer>();
            String[] struct = null;
            boolean find_token = false;

            for(int p = 0; p < pitem.data.size(); p++)
            {
                int f_pos = this._find_tok(src, pitem.data.get(p)[0], 0);

                if(f_pos == -1)
                {
                    continue;
                }

                find_token = true;
                pos_mass.add( pitem.data.get(p) );
                f_pos_list.add( f_pos );
            }
    		
            //System.out.println(pitem.data.get(p)[0]);

            int min = src.length();
            for(int i = 0, len = pos_mass.size(); i < len; i++)
            {
                if(min > f_pos_list.get(i))
                {
                    min    = f_pos_list.get(i);
                    struct = pos_mass.get(i);
                }
            }
    		
            int pos = min;

            if(pos == -1 || ! find_token)
            {
                break;
            }
    		
            /// IS FUNCTION ?
            if( 
                    src.charAt(pos) == '('
                &&  pos - 1 >= 0
                /*&&
                    (
                            Character.isAlphabetic( src.charAt(pos - 1) )
                        ||  Character.isDigit( src.charAt(pos - 1) )
                    )*/
              )
            {
                int left_pos_start_function = this._find_tok_left(src, pos - 1);

                String fun_name = src.substring(left_pos_start_function, pos).trim();
                
                if( fun_name.length() == 0 )
                {
                    int start_pos = pos;

                    FindOpenCloseTok params = Exeption._get_str_open_close_tok(src, struct[0], struct[1], start_pos);

                    Ex_Var r = this._exec(params.value);

                    src =     src.substring(0, start_pos)
                            + String.valueOf( r.value )
                            + src.substring( start_pos + params.value.length() + 2 );

                    if(flag_error == 1)
                    {
                        return "";
                    }
                    
                    continue;
                }
                
                if( ! functionExists(fun_name) )
                {
                    flag_error = 1;
                    error_value = Errors.ERROR_EXEC__ERROR_FUNCTION_NOT_FOUND;
                    return "";
                }

                int start_pos = pos;

                FindOpenCloseTok params = Exeption._get_str_open_close_tok(src, struct[0], struct[1], start_pos);

                start_pos = params.pos;

                List<String> r  = parse_parameters( params.value );
                List<Ex_Var> r1 = new ArrayList<Ex_Var>();

                for(int _k = 0; _k < r.size(); _k++)
                {
                    r1.add( this._exec( r.get( _k ) ) );

                    if( this.flag_error == 1 )
                    {
                        return "";
                    }
                }
                
                Ex_Var rf = null;
                boolean found = false;
                
                for( int k = 0; k < functions.size(); k++ )
                {
                    if( functions.get(k).name_function.equals(fun_name) )
                    {
                        String __group_tmp_vars = String.valueOf( System.currentTimeMillis() );
                        
                        Sa _sa = new Sa( global_var, external_functions );
                        for(int _k = 0; _k < functions.get(k).name_vars.size(); _k++)
                        {
                            Ex_Var vv = new Ex_Var(__group_tmp_vars);
                            vv.is_var = true;
                            
                            _sa.ex._local_vars_add_new(functions.get(k).name_vars.get( _k ), vv);
                        }
                        
                        for(int _k = 0; _k < functions.get(k).init_vars.size(); _k++)
                        {
                            _sa.ex.exec( functions.get(k).init_vars.get( _k ));
                        }
                        
                        for(  int _k = 0
                            ; _k < functions.get(k).name_vars.size() && _k < r1.size()
                            ; _k++)
                        {
                            _sa.ex._local_vars_set_new_value_and_type(
                                      functions.get(k).name_vars.get( _k )
                                    , r1.get(_k).type
                                    , r1.get(_k).value );
                        }
                        
                        rf = _sa.exec(functions.get(k).body, Sa.MODE_RUN__FUNCTION, 0, true, __group_tmp_vars);
                        
                        found = true;
                        break;
                    }
                }

                if( ! found )
                {
                    rf = _run_functions(fun_name, r1);
                }
                
                String str_rf = "";

                if( STATUS_EXIT )
                {
                    return "";
                }
    			
                if(
                        rf.type == Exeption.TYPE_VAR__ARRAY
                     || rf.type == Exeption.TYPE_VAR__FLOAT
                     || rf.type == Exeption.TYPE_VAR__INT
                     || rf.type == Exeption.TYPE_VAR__STR
                     || rf.type == Exeption.TYPE_VAR__USER_DATA
                     || rf.type == Exeption.TYPE_VAR__NONE                        
                   )
                {
                    String _new_var_name = "_" + String.valueOf(this._index_tmp_vars) + "_" + String.valueOf( System.currentTimeMillis() );

                    Ex_Var tmp_var = new Ex_Var(getLastGroupTmpVars(), rf.type, rf.value);
                    tmp_var.is_var = true;

                    this._local_tmp_vars_add_new(_new_var_name, tmp_var);

                    str_rf = _new_var_name;
                }
                else
                {
                    str_rf = _convert_res_type_to_str(rf.value, rf.type, "");
                }
    			
                if(flag_error == 1)
                {
                    return "";
                }

                src =     src.substring(0, left_pos_start_function) 
                        + str_rf 
                        + src.substring(start_pos + params.value.length() + 2);
            }
            ///-----------------------------------------------------------------------------
            else if( src.charAt(pos) == '[' )
            {
                int start_pos = pos;
                
                int pos_right = this._find_tok_left(src, pos - 1 );

                String var_name = src.substring(pos_right,  pos);
                
                Ex_Var _res1 = null;
                
                if( var_name.trim().length() > 0 && isVar(var_name.trim()) != null )
                {
                    _res1 = exec(var_name);
                }
                
                if( _res1 != null )
                {
                        FindOpenCloseTok yy = Exeption._get_str_open_close_tok(src, "[", "]", pos);
                        
                        Ex_Var _res2 = exec(yy.value);
                        
                        if( _res1.type == TYPE_VAR__ARRAY && ( _res2.type == TYPE_VAR__INT || _res2.type == TYPE_VAR__FLOAT ) )
                        {
                            List<Ex_Var> array = (List<Ex_Var>)_res1.value;
                            
                            int index = 0;
                            
                            if(_res2.type == TYPE_VAR__INT)
                            {
                                index = ( Math.toIntExact((Long)_res2.value));
                            }
                            else
                            {
                                index = ( Math.toIntExact(Math.round((Double)_res2.value)));
                            }
                            
                            //_res1.array_index = index;
                            
                            if(index >= 0 && index < array.size())
                            {
                                String _new_var_name = "_" + String.valueOf(this._index_tmp_vars) + "_" + String.valueOf( System.currentTimeMillis() );
                                Ex_Var tmp_var = array.get( index );
                                tmp_var.is_var = true;
                                
                                this._local_tmp_vars_add_new(_new_var_name, tmp_var);
                                
                                src = src.substring(0, pos_right)
                                        + _new_var_name
                                        + src.substring( yy.pos_end + 1 );
                                
                                return src;
                            }
                            else
                            {
                               flag_error = 1;
                               error_value = Errors.ERROR_EXEC__OUT_OF_INDEX;
                               //res = new Ex_Var();
                               return "";
                            }
                        }
                }
                else
                {
                
                    List<Ex_Var> list_array = new ArrayList<>();

                    String _new_var_name = "_" + String.valueOf(this._index_tmp_vars) + "_" + String.valueOf( System.currentTimeMillis() );

                    Ex_Var tmp_var = new Ex_Var(getLastGroupTmpVars(), TYPE_VAR__ARRAY, list_array);
                    tmp_var.is_var = true;

                    this._local_tmp_vars_add_new(_new_var_name, tmp_var);

                    FindOpenCloseTok params = Exeption._get_str_open_close_tok(src, struct[0], struct[1], start_pos);

                    src = src.substring(0, start_pos)
                              + _new_var_name
                              + src.substring( start_pos + params.value.length() + 2  );
                
                    if(flag_error == 1)
                    {
                        return "";
                    }

                    List<String> _data = Utils.split(",", params.value, 0);

                    for(int t = 0; t < _data.size(); t++)
                    {
                        list_array.add(exec(_data.get(t)));

                        if(is_error())
                        {
                            flag_error = 1;
                            break;
                        }
                    }
                }
            }
            else
            {
                int start_pos = pos;

                FindOpenCloseTok params = Exeption._get_str_open_close_tok(src, struct[0], struct[1], start_pos);

                Ex_Var r = this._exec(params.value);

                src =     src.substring(0, start_pos)
                        + String.valueOf( r.value )
                        + src.substring( start_pos + params.value.length() + 2 );

                if(flag_error == 1)
                {
                    return "";
                }
            }
            // --------------------------------------------------------
    	}
    	
    	//src = src.replace("``{{", "[");
    	//src = src.replace("}}``", "]");
    	
    	return src;
    }
    
    public void AddExternalFunction(ExternalFunction add)
    {
    	boolean found = false;
    	
    	for(int i = 0, len = this.external_functions.size(); i < len; i++)
    	{
            ExternalFunction item = this.external_functions.get(i);

            if( 
                    item.name_class.trim().equalsIgnoreCase( add.name_class.trim() ) 
                &&  item.name_method.trim().equalsIgnoreCase( add.name_method.trim() )
              )
            {
                found = true;
                break;
            }
    	}
    	
    	if( ! found )
    	{
            this.external_functions.add( add );
    	}
    }
    
    public Ex_Var exec2(String src)
    {
    	if( src.trim().length() == 0 )
    	{
            return null;
    	}
    	
    	Ex_Var res = this._exec(src);
    	
    	if(flag_error == 1)
    	{
            return null;
    	}
    	
    	return res;
    }
    
    public Ex_Var exec(String src)
    {
    	if( src.trim().length() == 0 )
    	{
            return null;
    	}
    	
    	Ex_Var res = this._exec(src);
    	
    	if(flag_error == 1)
    	{
            return null;
    	}
    	
    	//System.out.printf("%s\n", get_str_type_value( res.type) );
    	res.toStringResult = _convert_res_type_to_str(res.value, res.type, "");
    	
    	//_res = _res.replace("~", "-");
    	return res;
    }
    
    public Ex_Var add_new_var(String name, int type, Object value, String group)
    {
        Ex_Var added = new Ex_Var(group, type, value);
        added.is_var = true;
        
    	boolean res = _local_vars_add_new(name.trim(), added);
    	
    	if(flag_error == 1 || ! res )
        {
            return null;
        }
        
        return added;
    }
    
    public boolean is_error()
    {
    	return flag_error == 1?true:false;
    }
    
    /*public String get_str_type_value(int value)
    {
        switch(value)
        {
            case Exeption.TYPE_VAR__ARRAY:              return "TYPE_VAR__ARRAY";
            case Exeption.TYPE_VAR__FLOAT:              return "TYPE_VAR__FLOAT";
            case Exeption.TYPE_VAR__INT:                return "TYPE_VAR__INT";
            case Exeption.TYPE_VAR__NONE:               return "TYPE_VAR__NONE";
            case Exeption.TYPE_VAR__STR:                return "TYPE_VAR__STR";
            case Exeption.TYPE_VAR__USER_DATA:          return "TYPE_VAR__USER_DATA";
        }
        
        return "";
    }*/
    
    
}
