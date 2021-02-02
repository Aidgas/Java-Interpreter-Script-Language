package com.interprinator;

import java.util.ArrayList;
import java.util.List;

public class Utils 
{
    public static List<String> split(String q, String src, int start_pos)
    {
        List<String> _data = new ArrayList<String>();

        int new_pos, __p;
        int p = Utils.find_tok(src, ",", start_pos);

        if(p == -1)
        {
            _data.add( src.trim() );
        }
        else
        {
            new_pos = 0;
            List<Integer> _pos = new ArrayList<Integer>();

            while(p >= 0)
            {
                _pos.add(p);
                p = Utils.find_tok(src, ",", p + 1);
            }

            __p = 0;
            String pos_copy_str = "";
            for(int t = 0; t < _pos.size(); t++)
            {
                _data.add( src.substring(__p, _pos.get(t)).trim() );
                __p = _pos.get(t) + 1;
            }

            String _s_last = src.substring(__p).trim();

            if(_s_last.length() > 0)
            {
                _data.add(_s_last);
            }
        }
        
        return _data;
    }
    
    public static int skip_str(String src, int len, int i)
    {
        if( src.charAt(i) == '"' )
        {
            for(int j = i + 1; j < len; j++)
            {
                if(j - 1 > 0 && src.charAt(j) == '"' && src.charAt(j - 1) == '\\' )
                {
                    continue;
                }
                else if( src.charAt(j) == '"' )
                {
                    return j;
                }
            }
        }
        
        return 0;
    }
    
    public static int find_tok(String src, String token, int start_pos)
    {
    	int j = 0, k = 0;
        
    	for(int i = start_pos, len = src.length(); i < len; i++)
    	{
            if(i - 1 > 0 && src.charAt(i) == '"' && src.charAt(i - 1) == '\\' )
            { }
            else if( src.charAt(i) == '"' )
            {
                int g = Utils.skip_str(src, len, i);
                
                if( g == 0 )
                {
                    //flag_error = 1;
                    return 0;
                }
                else
                {
                    i = g;
                }
                
                continue;
            }
            else if( src.charAt(i) == '(' )
            {
                for(j = i + 1; j < len; j++)
                {
                    if( src.charAt(j) == '"' )
                    {
                        int g = Utils.skip_str(src, len, j);
                
                        if( g == 0 )
                        {
                            //flag_error = 1;
                            return 0;
                        }
                        else
                        {
                            j = g;
                        }
                    }
                    else if( src.charAt(j) == ')' )
                    {
                        i = j;
                        break;
                    }
                }
                continue;
            }
            else if( src.charAt(i) == '[' )
            {
                for(j = i + 1; j < len; j++)
                {
                    if( src.charAt(j) == '"' )
                    {
                        int g = Utils.skip_str(src, len, j);
                
                        if( g == 0 )
                        {
                            //flag_error = 1;
                            return 0;
                        }
                        else
                        {
                            j = g;
                        }
                    }
                    else if( src.charAt(j) == ']' )
                    {
                        i = j;
                        break;
                    }
                }
                continue;
            }
    		
            boolean find = true;

            for(int _k = 0, _len = token.length(); _k < _len; _k++ )
            {
                if( src.charAt(i + _k) != token.charAt(_k))
                {
                    find = false;
                    break;
                }
            }

            if(find)
            {
                return i;
            }
    		
    	} // for
    	
    	return -1;
    }
    
    public static int skipWhitespace(String str, int start_pos)
    {
        int i = 0, len;
        
        for(i = start_pos, len = str.length(); i < len; i++)
        {
            if(Character.isWhitespace( str.charAt(i) ))
            {
                continue;
            }
            else
            {
                break;
            }
        }
        
        return i;
    }
    
    public static String getLine(String src, int pos)
    {
        int i = 0, len = 0;
    
        for(i = pos, len = src.length(); i < len; i++)
        {
            if( src.charAt(i) == '\n' )
            {
                break;
            }
        }
        
        return src.substring(pos, i);
    }
    
    public static String setPrecision(double amt, int precision)
    {
        return String.format("%." + precision + "f", amt);
    }
    
    public static boolean isReservedWord(String word)
    {
        switch(word.trim().toLowerCase())
        {
            case "while":
            case "if":
            case "else":
            case "break":
            case "continue":
            case "func":
            case "return":
            case "var":
            case "const":
                
                return true;
                   
            default:
                
                return false;
        }
    }
    
}
