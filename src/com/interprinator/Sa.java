package com.interprinator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Sa 
{
    public static final int MODE_RUN__NONE       =  0x12;
    public static final int MODE_RUN__WHILE      =  0x13;
    public static final int MODE_RUN__FUNCTION   =  0x14;
    
    private int _current_mode_run = 0;
    
    private class IfElseInfoParser
    {
        String condition, body_if, body_else;
    }
    
    public class FunctionData
    {
    	String name_function = "";
    	int count_vars = 0;
    	List<String> name_vars = new ArrayList<String>();
    	List<String> init_vars = new ArrayList<String>();
    	String body = "";
    }
    
    private List<FunctionData> _functions = new ArrayList<Sa.FunctionData>();
    
    public static final int PARSE_EXEPTION   = 1;
    public static final int PARSE_VARS       = 2;
    public static final int PARSE_FUNCTION   = 3;
    public static final int PARSE_IF         = 4;
    public static final int PARSE_WHILE      = 5;
    public static final int PARSE_BREAK      = 6;
    public static final int PARSE_CONTINUE   = 7;
    public static final int PARSE_RETURN     = 8;
    public static final int PARSE_OPERATOR   = 9;
    public static final int PARSE_ELSE       = 10;
    public static final int PARSE_COMMENT    = 11;
    public static final int PARSE_CONST      = 12;
    
    private String[] parse_is_operator      = new String[]{ ";" };
    private String[] parse_is_var           = new String[]{ "var" };
    private String[] parse_is_const         = new String[]{ "const" };
    private String[] parse_is_if            = new String[]{ "if" };
    private String[] parse_is_else          = new String[]{ "else" };
    private String[] parse_is_while         = new String[]{ "while" };
    private String[] parse_break            = new String[]{ "break" };
    private String[] parse_continue         = new String[]{ "continue" };
    private String[] parse_return           = new String[]{ "return" };
    private String[] parse_is_function      = new String[]{ "func" };
    private String[] parse_comment          = new String[]{ "/*", "*/" };
    
    private int flag_error = 0;
    private int error_value = 0;
    private int pos_error = 0;
    
    private String _curr_code = "";
    
    
    public Exeption ex = null;
    
    private String stop_tokens = "*-/+=()\\!%<>&|,";
    
    /*private class Struct_exec
    {
    	String cmd;
    	int flag_error;
    	int error_value;
    	int pos;
    	String data;
    }*/
    
    
    
    private class StructPosString
    {
    	int pos;
    	String value;
    	
    	StructPosString(int _pos, String _value)
    	{
    		pos = _pos;
    		value = _value;
    	}
    }
    
    private class IntervalValue
    {
    	int pos0;
    	int pos1;
    	
    	IntervalValue(int _p0, int _p1)
    	{
    		pos0 = _p0;
    		pos1 = _p1;
    	}
    }
    //
    
    private class Struct_find_took_2
    {
    	int type_tok, pos_start_tok, pos_end_tok;
    	String tok_name;
    	Struct_find_took_2(int type_tok, int pos_start_tok, int pos_end_tok, String tok_name)
    	{
            this.type_tok      = type_tok;
            this.pos_start_tok = pos_start_tok;
            this.pos_end_tok   = pos_end_tok;
            this.tok_name      = tok_name;
    	}
    }
    
    Sa(Map<String, Ex_Var> _global_var, List<ExternalFunction> _external_functions)
    {
    	this.ex = new Exeption(this, _functions, _global_var, _external_functions);
    }
    
    private boolean _is_function_exists(String name)
    {
    	name = name.trim().toLowerCase();
    	
    	for(int i = 0; i < _functions.size(); i++)
    	{
    		if( _functions.get(i).name_function.equalsIgnoreCase(name) )
    		{
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    private FunctionData _get_function_data(String name)
    {
    	name = name.trim().toLowerCase();
    	
    	for(int i = 0; i < _functions.size(); i++)
    	{
    		if( _functions.get(i).name_function.equalsIgnoreCase(name) )
    		{
    			return _functions.get(i);
    		}
    	}
    	
    	return null;
    }
    
    private boolean _is_name_function(String name_var)
    {
    	name_var = name_var.trim();
    	
    	if(Character.isDigit(name_var.charAt(0)))
    	{
    		return false;
    	}
    	
    	for(int i = 0, len = name_var.length(); i < len; i++)
        {
            if( stop_tokens.indexOf( name_var.charAt(i) ) != -1 )
            {
                return false;
            }
        }
    	
    	return true;
    }
    
    private boolean isNumeric(String s) 
    {
        return s.matches("[-+]?\\d*\\.?\\d+");  
    }
    
    private boolean isInteger(String s)
    {
    	return s.matches("^-?\\d+$");
    }
    
    private Struct_find_took_2 _find__took_IF(String src, int pos, int len)
    {
        for(int i = pos; i < len; i++)
    	{
            if(Character.isWhitespace( src.charAt(i) ))
            {
                continue;
            }
            else
            {
                for(int _k = 0; _k < parse_is_if.length; _k++)
                {
                    boolean tmp_f = false;

                    for(int j = i, k = 0, len_v = parse_is_if[_k].length(); j < len && k < len_v; j++, k++)
                    {
                        if(parse_is_if[_k].charAt(k) != src.charAt(j))
                        { tmp_f = true; break; }
                    }

                    if( ! tmp_f)
                    {
                        return new Struct_find_took_2(Sa.PARSE_IF, i, i + parse_is_if[_k].length(), parse_is_if[_k]);
                    }
                    else
                    {
                        return null;
                    }
                }
            }
            
        }
        
        return null;
    }
    
    private Struct_find_took_2 _find__took_ELSE(String src, int pos, int len)
    {
        for(int i = pos; i < len; i++)
    	{
            if(Character.isWhitespace( src.charAt(i) ))
            {
                continue;
            }
            else
            {
                for(int _k = 0; _k < parse_is_else.length; _k++)
                {
                    boolean tmp_f = false;

                    for(int j = i, k = 0, len_v = parse_is_else[_k].length(); j < len && k < len_v; j++, k++)
                    {
                        if(parse_is_else[_k].charAt(k) != src.charAt(j))
                        { tmp_f = true; break; }
                    }

                    if( ! tmp_f)
                    {
                        return new Struct_find_took_2(Sa.PARSE_ELSE, i, i + parse_is_else[_k].length(), parse_is_else[_k]);
                    }
                    else
                    {
                        return null;
                    }
                }
            }
            
        }
        
        return null;
    }
    
    private Struct_find_took_2 _find__took_2(String src, int pos, int len)
    {
    	boolean open_quote   = false;
    	
    	for(int i = pos; i < len; i++)
    	{
            if(i - 1 > 0 && src.charAt(i) == '"' && src.charAt(i - 1) == '\\' )
            { }
            else if( src.charAt(i) == '"' )
            {
                open_quote = ! open_quote;
                continue;
            }

            if(open_quote == true)
            {
                continue;
            }
    		
            if(Character.isWhitespace( src.charAt(i) ))
            {
                continue;
            }
            else
            {
                for(int _k = 0; _k < parse_is_var.length; _k++)
                {
                    boolean tmp_f = false;

                    for(int j = i, k = 0, len_v = parse_is_var[_k].length(); j < len && k < len_v; j++, k++)
                    {
                        if(parse_is_var[_k].charAt(k) != src.charAt(j))
                        { tmp_f = true; break; }
                    }

                    if( ! tmp_f)
                    {
                        return new Struct_find_took_2(Sa.PARSE_VARS, i, i + parse_is_var[_k].length(), parse_is_var[_k]);
                    }
                }
                
                for(int _k = 0; _k < parse_is_const.length; _k++)
                {
                    boolean tmp_f = false;

                    for(int j = i, k = 0, len_v = parse_is_const[_k].length(); j < len && k < len_v; j++, k++)
                    {
                        if(parse_is_const[_k].charAt(k) != src.charAt(j))
                        { tmp_f = true; break; }
                    }

                    if( ! tmp_f)
                    {
                        return new Struct_find_took_2(Sa.PARSE_CONST, i, i + parse_is_const[_k].length(), parse_is_const[_k]);
                    }
                }
    			
                for(int _k = 0; _k < parse_is_if.length; _k++)
                {
                    boolean tmp_f = false;

                    for(int j = i, k = 0, len_v = parse_is_if[_k].length(); j < len && k < len_v; j++, k++)
                    {
                        if(parse_is_if[_k].charAt(k) != src.charAt(j))
                        { tmp_f = true; break; }
                    }

                    if( ! tmp_f)
                    {
                        return new Struct_find_took_2(Sa.PARSE_IF, i, i + parse_is_if[_k].length(), parse_is_if[_k]);
                    }
                }

                for(int _k = 0; _k < parse_is_while.length; _k++)
                {
                    boolean tmp_f = false;

                    for(int j = i, k = 0, len_v = parse_is_while[_k].length(); j < len && k < len_v; j++, k++)
                    {
                        if(parse_is_while[_k].charAt(k) != src.charAt(j))
                        { tmp_f = true; break; }
                    }

                    if( ! tmp_f)
                    {
                        return new Struct_find_took_2(Sa.PARSE_WHILE, i, i + parse_is_while[_k].length(), parse_is_while[_k]);
                    }
                }

                for(int _k = 0; _k < parse_break.length; _k++)
                {
                    boolean tmp_f = false;

                    for(int j = i, k = 0, len_v = parse_break[_k].length(); j < len && k < len_v; j++, k++)
                    {
                        if(parse_break[_k].charAt(k) != src.charAt(j))
                        { tmp_f = true; break; }
                    }

                    if( ! tmp_f)
                    {
                        return new Struct_find_took_2(Sa.PARSE_BREAK, i, i + parse_break[_k].length(), parse_break[_k]);
                    }
                }

                for(int _k = 0; _k < parse_continue.length; _k++)
                {
                    boolean tmp_f = false;

                    for(int j = i, k = 0, len_v = parse_continue[_k].length(); j < len && k < len_v; j++, k++)
                    {
                        if(parse_continue[_k].charAt(k) != src.charAt(j))
                        { tmp_f = true; break; }
                    }

                    if( ! tmp_f)
                    {
                        return new Struct_find_took_2(Sa.PARSE_CONTINUE, i, i + parse_continue[_k].length(), parse_continue[_k]);
                    }
                }

                //for(int _k = 0; _k < _words_comment.length; _k++)
                {
                    boolean tmp_f = false;

                    for(int j = i, k = 0, len_v = parse_comment[0].length(); j < len && k < len_v; j++, k++)
                    {
                        if(parse_comment[0].charAt(k) != src.charAt(j))
                        { 
                            tmp_f = true; break; 
                        }
                    }

                    if( ! tmp_f )
                    {
                        
                        int pp           = i + parse_comment[0].length();
                        
                        while( pp < len )
                        {
                            boolean found2  = false;
                            
                            for(int j = pp, k = 0, len_v = parse_comment[1].length(); j < len && k < len_v; j++, k++)
                            {
                                if(parse_comment[1].charAt(k) != src.charAt(j))
                                { 
                                    found2 = true;
                                    break;
                                }
                            }
                            
                            if( ! found2 )
                            {
                                i = pp + parse_comment[1].length();
                                return new Struct_find_took_2(Sa.PARSE_COMMENT, i, src.length() - 1, parse_comment[1]);
                                //break;
                            }
                            
                            pp += 1;
                        }
                        
                        //
                    }
                }
    			
                for(int _k = 0; _k < parse_is_function.length; _k++)
                {
                    boolean tmp_f = false;

                    for(int j = i, k = 0, len_v = parse_is_function[_k].length(); j < len && k < len_v; j++, k++)
                    {
                        if(parse_is_function[_k].charAt(k) != src.charAt(j))
                        { tmp_f = true; break; }
                    }

                    if( ! tmp_f)
                    {
                        return new Struct_find_took_2(Sa.PARSE_FUNCTION, i, parse_is_function[_k].length(), parse_is_function[_k]);
                    }
                }

                for(int _k = 0; _k < parse_return.length; _k++)
                {
                    boolean tmp_f = false;

                    for(int j = i, k = 0, len_v = parse_return[_k].length(); j < len && k < len_v; j++, k++)
                    {
                        if(parse_return[_k].charAt(k) != src.charAt(j))
                        { tmp_f = true; break; }
                    }

                    if( ! tmp_f)
                    {
                        return new Struct_find_took_2(Sa.PARSE_RETURN, i, parse_return[_k].length(), parse_return[_k]);
                    }
                }
    			
                for(int _k = 0; _k < parse_is_operator.length; _k++)
                {
                    boolean tmp_f = false;

                    for(int j = i, k = 0, len_v = parse_is_operator[_k].length(); j < len && k < len_v; j++, k++)
                    {
                        if(parse_is_operator[_k].charAt(k) != src.charAt(j))
                        { tmp_f = true; break; }
                    }

                    if( ! tmp_f)
                    {
                        return new Struct_find_took_2(Sa.PARSE_OPERATOR, i, parse_is_operator[_k].length(), parse_is_operator[_k]);
                    }
                }
    			
            }
    	}
    	
    	return null;
    }
    
    private Ex_Var _exec(String src, int mode, int global_offset_position)
    {
        Ex_Var result = new Ex_Var(this.ex.getLastGroupTmpVars());
    	int pos = 0;
    	int new_pos = 0;
    	
    	this._current_mode_run = mode;
    	int len = src.length();
    	
    	String _return_cmd      = "";
    	String _return_data     = "";
    	String __tok_end        = "";
    	int __p                 = 0;
        boolean not_found_exec  = false;
    	
    	while(pos < len)
    	{
            pos = Utils.skipWhitespace(src, pos);
            
            String _line = Utils.getLine(src, pos);
            
            Struct_find_took_2 res = _find__took_2(src, pos, len);

            if( ! not_found_exec )
            {
                not_found_exec = res != null;
            }

            if(res != null && res.type_tok == PARSE_OPERATOR)
            {
                String openator_full_str = src.substring( Utils.skipWhitespace(src, pos), res.pos_start_tok);
                //String openator_full_str = src.substring( pos, res.pos_start_tok);

                Ex_Var __res = this.ex.exec(openator_full_str);

                if(this.ex.is_error())
                {
                    flag_error = 1;
                    //pos_error = Utils.skipWhitespace(src, pos);
                    pos_error =  res.pos_start_tok;
                    error_value = this.ex.error_value;
                    break;
                }
                
                if( __res.STATUS_EXIT )
                {
                    break;
                }

                pos = res.pos_start_tok + 1;
            }
            else if(res != null && res.type_tok == PARSE_COMMENT)
            {
                pos = res.pos_start_tok;
            }
            else if(res != null && res.type_tok == PARSE_VARS)
            {
                pos = res.pos_start_tok;
                int len_tok = res.tok_name.length();

                if( ! Character.isWhitespace( src.charAt(pos + len_tok) ) )
                {
                    flag_error = 1;
                    error_value = Errors.ERROR_PARSE;
                    break;
                }

                int pos_start = pos;

                int pos_end = Utils.find_tok(src, ";", pos_start + len_tok);

                if( pos_end == -1 )
                {
                    flag_error = 1;
                    error_value = Errors.ERROR_PARSE;
                    break;
                }

                String find_str = src.substring(pos_start + len_tok, pos_end);

                pos = pos_end + 1;

                List<String> _data = Utils.split(",", find_str, 0);

                for(int t = 0; t < _data.size(); t++)
                {
                    String[] _t = _data.get(t).split("=", 2);

                    if( _t.length != 2 )
                    {
                        flag_error = 1;
                        pos_error = pos;
                        break;
                    }
                    
                    if( Utils.isReservedWord(_t[0]) )
                    {
                        flag_error  = 1;
                        pos_error   = pos;
                        error_value = Errors.ERROR_EXEC__RESERVED_WORD;
                        break;
                    }
                    
                    if( this.ex._global_vars_is_exist(_t[0]) )
                    {
                        flag_error  = 1;
                        pos_error   = pos;
                        error_value = Errors.ERROR_EXEC__VAR_DEFINE_IN_GLOBAL;
                        break;
                    }

                    this.ex.add_new_var(_t[0], Exeption.TYPE_VAR__NONE, null, "");

                    if(_t.length > 1)
                    {
                        this.ex.exec(_data.get(t));

                        if(this.ex.is_error())
                        {
                            flag_error = 1;
                            pos_error = pos;
                            break;
                        }
                    }
                }

                if(this.flag_error == 1)
                {
                    if( error_value == 0 )
                    {
                        error_value = Errors.ERROR_VAR_DEFINE;
                    }
                    break;
                }
            }
            else if(res != null && res.type_tok == PARSE_CONST)
            {
                pos = res.pos_start_tok;
                int len_tok = res.tok_name.length();

                if( ! Character.isWhitespace( src.charAt(pos + len_tok) ) )
                {
                    flag_error = 1;
                    error_value = Errors.ERROR_PARSE;
                    break;
                }

                int pos_start = pos;

                int pos_end = Utils.find_tok(src, ";", pos_start + len_tok);

                if( pos_end == -1 )
                {
                    flag_error = 1;
                    error_value = Errors.ERROR_PARSE;
                    break;
                }

                String find_str = src.substring(pos_start + len_tok, pos_end);

                pos = pos_end + 1;

                List<String> _data = Utils.split(",", find_str, 0);

                for(int t = 0; t < _data.size(); t++)
                {
                    String[] _t = _data.get(t).split("=", 2);

                    if( _t.length != 2 )
                    {
                        flag_error = 1;
                        pos_error = pos;
                        break;
                    }
                    
                    if( Utils.isReservedWord(_t[0]) )
                    {
                        flag_error  = 1;
                        pos_error   = pos;
                        error_value = Errors.ERROR_EXEC__RESERVED_WORD;
                        break;
                    }
                    
                    if( this.ex._global_vars_is_exist(_t[0]) )
                    {
                        flag_error  = 1;
                        pos_error   = pos;
                        error_value = Errors.ERROR_EXEC__VAR_DEFINE_IN_GLOBAL;
                        break;
                    }

                    Ex_Var tmp = this.ex.add_new_var(_t[0], Exeption.TYPE_VAR__NONE, null, "");

                    if(_t.length > 1)
                    {
                        this.ex.exec(_data.get(t));

                        if(this.ex.is_error())
                        {
                            flag_error = 1;
                            pos_error = pos;
                            break;
                        }
                        
                        tmp.is_const = true;
                    }
                }

                if(this.flag_error == 1)
                {
                    if( error_value == 0 )
                    {
                        error_value = Errors.ERROR_VAR_DEFINE;
                    }
                    break;
                }
            }
            else if(res != null && res.type_tok == PARSE_IF)
            {
                pos = res.pos_start_tok;
                int len_tok = res.tok_name.length();
                
                List<IfElseInfoParser> list_ifs = new ArrayList<>();
                
                while(true)
                {
                    IfElseInfoParser __t = new IfElseInfoParser();
                    
                    Struct_find_took_2 _if = _find__took_IF(src, pos, len);
                
                    if( _if == null )
                    {
                        break;
                    }
                    else
                    {
                        FindOpenCloseTok yy = Exeption._get_str_open_close_tok(src, "(", ")", _if.pos_end_tok);

                        int kl = yy.pos_end + 1;

                        while( true )
                        {
                            if(Character.isWhitespace( src.charAt(kl) ))
                            {
                                kl += 1;
                                continue;
                            }

                            if(src.charAt(kl) == '{')
                            {
                                break;
                            }
                            else
                            {
                                // error !
                            }

                            kl += 1;
                        }
                            
                        FindOpenCloseTok yy1 = Exeption._get_str_open_close_tok(src, "{", "}", kl);

                        __t.condition = yy.value;
                        __t.body_if   = yy1.value;

                        pos = yy1.pos_end + 1;

                        Struct_find_took_2 _else = _find__took_ELSE(src, yy1.pos_end + 1, len);

                        if( _else == null )
                        {
                            list_ifs.add(__t);
                            break;
                        }
                        else
                        {
                            Struct_find_took_2 _if_next = _find__took_IF(src, _else.pos_end_tok + 1, len);

                            if( _if_next != null )
                            {
                                pos = _if_next.pos_start_tok;
                                list_ifs.add(__t);
                                continue;
                            }
                            else
                            {
                                FindOpenCloseTok yy2 = Exeption._get_str_open_close_tok(src, "{", "}", _else.pos_end_tok + 1);

                                __t.body_else = yy2.value;

                                pos = yy2.pos_end + 1;
                                list_ifs.add(__t);
                                break;
                            }
                        }
                            
                    }
                } // -- while
                
                for(int f = 0; f < list_ifs.size(); f++)
                {
                    String condition  = list_ifs.get(f).condition;
                    String body_if    = list_ifs.get(f).body_if;
                    String body_else  = list_ifs.get(f).body_else;

                    Ex_Var res_q = this.ex.exec(condition);

                    if( res_q.STATUS_EXIT )
                    {
                        break;
                    }

                    if(     isNumeric(res_q.toStringResult)
                        &&
                        (
                               ( isInteger(res_q.toStringResult) && Integer.parseInt(res_q.toStringResult) == 1)
                            || ( ! isInteger(res_q.toStringResult) && Float.parseFloat(res_q.toStringResult) == 1)
                        )
                     )
                    {
                        String group_tmp_vars = String.valueOf( System.currentTimeMillis() );
                        
                        Ex_Var __res = this.exec(body_if, _current_mode_run, res.pos_start_tok, false, group_tmp_vars );

                        this.ex.deleteTmpVarsByGroup(group_tmp_vars);
                        
                        if(this.flag_error == 1)
                        {
                            break;
                        }

                        if(_current_mode_run == Sa.MODE_RUN__WHILE)
                        {
                            if(
                                    __res.cmd.equalsIgnoreCase("break")
                                ||  __res.cmd.equalsIgnoreCase("continue")
                              )
                            {
                                _return_cmd = __res.cmd;
                                break;
                            }
                        }
                        
                        break;
                    }
                    else
                    {
                        if( body_else != null && body_else.trim().length() > 0 )
                        {
                            String group_tmp_vars = String.valueOf( System.currentTimeMillis() );
                            
                            Ex_Var __res = this.exec(body_else, _current_mode_run, res.pos_start_tok, false, group_tmp_vars);

                            this.ex.deleteTmpVarsByGroup(group_tmp_vars);
                            
                            if(this.flag_error == 1)
                            {
                                break;
                            }

                            if(_current_mode_run == Sa.MODE_RUN__WHILE)
                            {
                                if(
                                        __res.cmd.equalsIgnoreCase("break")
                                    ||  __res.cmd.equalsIgnoreCase("continue")
                                  )
                                {
                                    _return_cmd = __res.cmd;
                                    break;
                                }
                            }

                            break;
                        }
                        else
                        {
                            if( f + 1 < list_ifs.size() )
                            {
                                continue;
                            }
                            
                            break;
                        }
                    }
                }
                
            }
            else if(res != null && res.type_tok == PARSE_WHILE)
            {
                pos = res.pos_start_tok;

                int len_tok = res.tok_name.length();
                
                FindOpenCloseTok yy = Exeption._get_str_open_close_tok(src, "(", ")", pos + len_tok);

                int kl = yy.pos_end + 1;
                
                while( true )
                {
                    if(src.charAt(kl) == '{' || Character.isWhitespace( src.charAt(kl) ))
                    {
                        break;
                    }
                    else
                    {
                        // error !
                    }
                    
                    kl += 1;
                }
                
                FindOpenCloseTok yy1 = Exeption._get_str_open_close_tok(src, "{", "}", kl);

                String condition   = yy.value;
                String body_while  = yy1.value;

                while(true)
                {
                    Ex_Var res_q = this.ex.exec(condition);

                    if(this.ex.is_error())
                    {
                        flag_error = 1;
                        break;
                    }
                    
                    if( res_q.STATUS_EXIT )
                    {
                        break;
                    }

                    if(     isNumeric(res_q.toStringResult)
                        &&
                        (
                                ( isInteger(res_q.toStringResult) && Integer.parseInt(res_q.toStringResult) == 1)
                            ||  ( ! isInteger(res_q.toStringResult) && Float.parseFloat(res_q.toStringResult) == 1)
                        )
                     )
                    {
                        String group_tmp_vars = String.valueOf( System.currentTimeMillis() );
                        
                        Ex_Var _result = this.exec(body_while, Sa.MODE_RUN__WHILE, yy1.pos_start + 1, false, group_tmp_vars);

                        this.ex.deleteTmpVarsByGroup(group_tmp_vars);
                        
                        if(flag_error == 1)
                        {
                            pos_error = yy1.pos_start + 1 + result.pos;
                            break;
                        }
                        else if(_result.cmd.equalsIgnoreCase("break"))    						
                        {
                            break;
                        }
                        // continue

                    }
                    else
                    { break; }
                }

                if(this.flag_error == 1)
                {
                    break;
                }

                pos = yy1.pos_end + 1;
                not_found_exec = false;
            }
            else if(res != null && res.type_tok == PARSE_BREAK)
            {
                if(_current_mode_run != Sa.MODE_RUN__WHILE)
                {
                    flag_error = 1;
                    error_value = Errors.ERROR_BREAK_NOT_IN_WHILE;
                    break;
                }

                pos = res.pos_start_tok;

                int len_tok = res.tok_name.length();

                boolean find = false;

                for(int k = pos + len_tok; k < len; k++)
                {
                    if( Character.isWhitespace( src.charAt(k) ) )
                    { continue; }
                    else if( src.charAt(k) == ';' )
                    {
                        pos = k + 1;
                        find = true;
                        break;
                    }
                    else
                    { break; }
                }

                if(find == false)
                {
                    flag_error = 1;
                    error_value = Errors.ERROR_PARSE;
                    break;
                }

                _return_cmd = "break";
                break;
            }
            else if(res != null && res.type_tok == PARSE_CONTINUE)
            {
                if(_current_mode_run != Sa.MODE_RUN__WHILE)
                {
                    flag_error = 1;
                    error_value = Errors.ERROR_BREAK_NOT_IN_WHILE;
                    break;
                }

                pos = res.pos_start_tok;

                int len_tok = res.tok_name.length();

                boolean find = false;

                for(int k = pos + len_tok; k < len; k++)
                {
                    if( Character.isWhitespace( src.charAt(k) ) )
                    { continue; }
                    else if( src.charAt(k) == ';' )
                    {
                        pos = k + 1;
                        find = true;
                        break;
                    }
                    else
                    { break; }
                }

                if(find == false)
                {
                    flag_error = 1;
                    error_value = Errors.ERROR_PARSE;
                    break;
                }

                _return_cmd = "continue";
                break;
            }
            /*else if(res != null && res.type_tok == PARSE_COMMENT)
            {
                pos = res.pos_start_tok;

                int len_tok = res.tok_name.length();

                boolean find = false;

                for(int k = pos + len_tok; k < len; k++)
                {
                    if(src.charAt(k) == '\n')
                    {
                        pos = k + 1;
                        find = true;
                        break;
                    }
                }

                if(! find)
                {
                    break;
                }
            }*/
            else if(res != null && res.type_tok == PARSE_FUNCTION)
            {
                pos = res.pos_start_tok;

                int len_tok = res.tok_name.length();

                if( ! Character.isWhitespace( src.charAt(pos + len_tok) ))
                {
                    flag_error = 1;
                    error_value = Errors.ERROR_PARSE;
                    break;
                }
                
                int kl = pos + len_tok + 1;
                
                while( true )
                {
                    if(src.charAt(kl) == '(' || Character.isWhitespace( src.charAt(kl) ))
                    {
                        break;
                    }
                    else
                    {
                        // error !
                    }
                    
                    kl += 1;
                }
                
                String name_funcion = src.substring(pos + len_tok, kl).trim();
                
                FindOpenCloseTok yy = Exeption._get_str_open_close_tok(src, "(", ")", kl);
                
                kl = yy.pos_end + 1;
                
                while( true )
                {
                    if(src.charAt(kl) == '{' || Character.isWhitespace( src.charAt(kl) ))
                    {
                        break;
                    }
                    else
                    {
                        // error !
                    }
                    
                    kl += 1;
                }
                
                FindOpenCloseTok yy1 = Exeption._get_str_open_close_tok(src, "{", "}", kl);
                
                pos = yy1.pos_end + 1;
                
                String header_function   = yy.value;
                String body_function     = yy1.value;

                List<String> _data = new ArrayList<String>();

                int p = Utils.find_tok(header_function, ",", 0);

                if(p == -1)
                {
                    _data.add( header_function );
                }
                else
                {
                    new_pos = 0;
                    List<Integer> _pos = new ArrayList<Integer>();

                    while(p >= 0)
                    {
                        _pos.add(p);

                        p = Utils.find_tok(header_function, ",", p + 1 );
                    }

                    __p = 0;
                    String pos_copy_str = "";
                    for(int t = 0; t < _pos.size(); t++)
                    {
                        _data.add( header_function.substring(__p, _pos.get(t) ) );
                        __p = _pos.get(t) + 1;
                    }

                    String _s_last = header_function.substring(__p).trim();

                    if(_s_last.length() > 0)
                    {
                        _data.add( _s_last );
                    }
                }

                List<String> _name_vars = new ArrayList<String>();
                List<String> _init_vars = new ArrayList<String>();

                for(int t = 0; t < _data.size(); t++)
                {
                    String[] _t = _data.get(t).split("=", 2);

                    if( _name_vars.indexOf( _t[0].trim() ) != -1 )
                    {
                        flag_error = 1;
                        error_value = Errors.ERROR_NAME_FUNCTION_PARAM_DOUBLE_INIT;
                        break;
                    }

                    _name_vars.add( _t[0].trim() );

                    if(_t.length > 1)
                    {
                        _init_vars.add( _data.get(t) );
                    }
                }

                if(flag_error == 1)
                {
                    break;
                }

                FunctionData save_data_item = new FunctionData();

                save_data_item.name_function  =  name_funcion;
                save_data_item.count_vars     =  _name_vars.size();
                save_data_item.name_vars      =  _name_vars;
                save_data_item.init_vars      =  _init_vars;
                save_data_item.body           =  body_function;

                if(this._is_function_exists(name_funcion))
                {
                    flag_error = 1;
                    error_value = Errors.ERROR_NAME_FUNCTION_EXISTS;
                    break;
                }

                if( this.ex.isVar(name_funcion) != null )
                {
                    flag_error = 1;
                    error_value = Errors.ERROR_NAME_FUNCTION_NAME_SET_AS_VAR;
                    break;
                }

                this._functions.add(save_data_item);
            }
            else if(res != null && res.type_tok == PARSE_RETURN)
            {
                if(_current_mode_run != Sa.MODE_RUN__FUNCTION)
                {
                    flag_error = 1;
                    error_value = Errors.ERROR_RETURN_NOT_IN_FUNCTION;
                    break;
                }

                pos = res.pos_start_tok;

                int len_tok = res.tok_name.length();

                if( ! Character.isWhitespace( src.charAt(pos + len_tok) ) )
                {
                    flag_error = 1;
                    error_value = Errors.ERROR_PARSE;
                    break;
                }

                int pos_start = pos;

                int pos_end = Utils.find_tok(src, ";", pos_start + len_tok);

                if(pos_end == -1)
                {
                    flag_error = 1;
                    error_value = Errors.ERROR_PARSE;
                    break;
                }

                String find_str = src.substring( pos_start + len_tok, pos_end );

                pos = pos_end + 1;

                if(find_str.trim().length() > 0)
                {
                    Ex_Var res_q = this.ex.exec(find_str);

                    if( res_q.STATUS_EXIT )
                    {
                        break;
                    }

                    if(this.ex.is_error())
                    {
                        flag_error = 1;
                        break;
                    }

                    result = res_q;
                    _return_data = res_q.toStringResult;
                }

                _return_cmd = "return";
                break;
            }
            else
            {
                if( pos >= len )
                {
                    break;
                }

                __p = Utils.find_tok(src, ";", pos);

                if(__p != -1)
                {
                    String exec = src.substring(pos, __p);

                    this.ex.exec(exec);

                    if(this.ex.is_error())
                    {
                        flag_error = 1;
                        break;
                    }

                    pos = __p + 1;
                }
                else
                {
                    if( flag_error == 0 )
                    {
                        pos_error = pos;

                        while( pos_error + 1 < src.length() && Character.isWhitespace( src.charAt(pos_error) ) )
                        {
                            pos_error += 1;
                        }
                    }

                    flag_error = ! not_found_exec ? 1 : 0;
                    break;
                }
            }
    		
    	} // while
    	
    	
    	
    	result.cmd = _return_cmd;
    	result.flag_error = this.flag_error;
    	result.error_value = this.error_value;
    	result.pos = pos;
    	result.data = _return_data;
    	
    	return result;
    }

    public Ex_Var exec(String src, int mode, int pos_offset, boolean save_code, String group_tm_vars) 
    {
        if( save_code )
        {
            _curr_code      = src;
        }
        
        this.ex.addLastGroupTmpVars( group_tm_vars );
        
        return this._exec(src, mode, pos_offset);
    }
    
    private int[] get_line_number_from_pos(int pos)
    {
        int line = 1;
        int pos_in_line = 0;
        
        for(int i = 0, len = _curr_code.length(); i < len  &&  i < pos; i++)
        {
            if( _curr_code.charAt(i) == '\n' )
            {
                line += 1;
                pos_in_line = 0;
            }
            else
            {
            	pos_in_line += 1;
            }
        }
        
        return new int[]{ line, pos_in_line };
    }
    		
    public void print_error()
    {
        if(flag_error == 1)
        {
            int[] _pos = get_line_number_from_pos(pos_error);
        	
            System.out.printf("Sa: %s\n", Errors.get_str_error_value(error_value) );
            System.out.printf("line num: %d %d %d\n",  _pos[0], _pos[1], pos_error );
        }
        
        if(this.ex.is_error())
        {
            System.out.printf( "Ex: %s\n", Errors.get_str_error_value( this.ex.error_value ) );
        }
    }
}
