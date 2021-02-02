/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.interprinator;

/**
 *
 * @author sk
 */
public class Errors 
{
    public static final int ERROR_PARSE__CONDITION                                  = 0x15;
    public static final int ERROR_PARSE__EXEC                                       = 0x16;
    public static final int ERROR_BREAK_NOT_IN_WHILE                                = 0x17;
    public static final int ERROR_PARSE                                             = 0x18;
    public static final int ERROR_CONTINUE_NOT_IN_WHILE                             = 0x19;
    public static final int ERROR_PARSE_HEADER_FUNCTION                             = 0x20;
    public static final int ERROR_NAME_FUNCTION_NOT_CORRECT                         = 0x21;
    public static final int ERROR_NAME_FUNCTION_PARAM_DOUBLE_INIT                   = 0x22;
    public static final int ERROR_NAME_FUNCTION_EXISTS                              = 0x23;
    public static final int ERROR_NAME_FUNCTION_NAME_SET_AS_VAR                     = 0x24;
    public static final int ERROR_RETURN_NOT_IN_FUNCTION                            = 0x25;
    
    public static final int ERROR_IF_NOT_FOUND_END                                  = 0x26;
    public static final int ERROR_ELSE_ERROR                                        = 0x27;
    public static final int ERROR_VAR_DEFINE                                        = 0x28;
    
    public static final int ERROR_EXEC__VAR_NOT_FOUND                               = 0x30;
    public static final int ERROR_EXEC__VAR_DEVISION_BY_ZERO                        = 0x31;
    public static final int ERROR_EXEC__UNKNOWN                                     = 0x32;
    public static final int ERROR_EXEC__ERROR_OPERATION                             = 0x33;
    public static final int ERROR_EXEC__ERROR_PARAMETERS_MANY                       = 0x34;
    public static final int ERROR_EXEC__ERROR_PARAMETERS_LOW                        = 0x35;
    public static final int ERROR_EXEC__ERROR_NOT_FORMAT_PARAMETERS_FUNCTION        = 0x36;
    public static final int ERROR_EXEC__ERROR_NOT_FORMAT_ARRAY       		    = 0x37;
    public static final int ERROR_EXEC__ERROR_NOT_FORMAT_ARRAY_TYPE_IN_LPOS         = 0x38;
    public static final int ERROR_EXEC__ERROR_ARRAY_ERROR_ACCESS                    = 0x39;
    public static final int ERROR_EXEC__ERROR_ARRAY_SET_VALUE                       = 0x40;

    public static final int ERROR_EXEC__ERROR_FUNCTION_NOT_FOUND                    = 0x41;
    public static final int ERROR_EXEC__ERROR_NOT_CONVERT_VALUE_TO_STRING           = 0x42;
    public static final int ERROR_EXEC__ERROR_VAR_NAME_NOT_CORRECT                  = 0x43;
    public static final int ERROR_EXEC__VAR_DEFINED_EROOR                           = 0x44;
    public static final int ERROR_EXEC__OUT_OF_INDEX                                = 0x45;
    public static final int ERROR_EXEC__RESERVED_WORD                               = 0x46;
    public static final int ERROR_EXEC__VAR_DEFINE_IN_GLOBAL                        = 0x47;
    public static final int ERROR_EXEC__VAR_IS_CONST                                = 0x48;
    
    public static String get_str_error_value(int error_value)
    {
        switch(error_value)
        {
            case Errors.ERROR_EXEC__VAR_NOT_FOUND:                        return "ERROR_EXEC__VAR_NOT_FOUND";
            case Errors.ERROR_EXEC__VAR_DEVISION_BY_ZERO:                 return "ERROR_EXEC__VAR_DEVISION_BY_ZERO";
            case Errors.ERROR_EXEC__UNKNOWN:                              return "ERROR_EXEC__UNKNOWN";
            case Errors.ERROR_EXEC__ERROR_OPERATION:                      return "ERROR_EXEC__ERROR_OPERATION";
            case Errors.ERROR_EXEC__ERROR_PARAMETERS_MANY:                return "ERROR_EXEC__ERROR_PARAMETERS_MANY";
            case Errors.ERROR_EXEC__ERROR_PARAMETERS_LOW:                 return "ERROR_EXEC__ERROR_PARAMETERS_LOW";
            case Errors.ERROR_EXEC__ERROR_NOT_FORMAT_PARAMETERS_FUNCTION: return "ERROR_EXEC__ERROR_NOT_FORMAT_PARAMETERS_FUNCTION";
            case Errors.ERROR_EXEC__ERROR_NOT_FORMAT_ARRAY:               return "ERROR_EXEC__ERROR_NOT_FORMAT_ARRAY";
            case Errors.ERROR_EXEC__ERROR_NOT_FORMAT_ARRAY_TYPE_IN_LPOS:  return "ERROR_EXEC__ERROR_NOT_FORMAT_ARRAY_TYPE_IN_LPOS";
            case Errors.ERROR_EXEC__ERROR_ARRAY_ERROR_ACCESS:             return "ERROR_EXEC__ERROR_ARRAY_ERROR_ACCESS";
            case Errors.ERROR_EXEC__ERROR_ARRAY_SET_VALUE:                return "ERROR_EXEC__ERROR_ARRAY_SET_VALUE";
            case Errors.ERROR_EXEC__ERROR_FUNCTION_NOT_FOUND:             return "ERROR_EXEC__ERROR_FUNCTION_NOT_FOUND";
            case Errors.ERROR_EXEC__ERROR_NOT_CONVERT_VALUE_TO_STRING:    return "ERROR_EXEC__ERROR_NOT_CONVERT_VALUE_TO_STRING";
            case Errors.ERROR_EXEC__ERROR_VAR_NAME_NOT_CORRECT:           return "ERROR_EXEC__ERROR_VAR_NAME_NOT_CORRECT";
            
            case Errors.ERROR_PARSE__CONDITION:                           return "ERROR_PARSE__CONDITION";
            case Errors.ERROR_PARSE__EXEC:                                return "ERROR_PARSE__EXEC";
            case Errors.ERROR_BREAK_NOT_IN_WHILE:                         return "ERROR_BREAK_NOT_IN_WHILE";
            case Errors.ERROR_PARSE:                                      return "ERROR_PARSE";
            case Errors.ERROR_CONTINUE_NOT_IN_WHILE:                      return "ERROR_CONTINUE_NOT_IN_WHILE";
            case Errors.ERROR_PARSE_HEADER_FUNCTION:                      return "ERROR_PARSE_HEADER_FUNCTION";
            case Errors.ERROR_NAME_FUNCTION_NOT_CORRECT:                  return "ERROR_NAME_FUNCTION_NOT_CORRECT";
            case Errors.ERROR_NAME_FUNCTION_PARAM_DOUBLE_INIT:            return "ERROR_NAME_FUNCTION_PARAM_DOUBLE_INIT";
            case Errors.ERROR_NAME_FUNCTION_EXISTS:                       return "ERROR_NAME_FUNCTION_EXISTS";
            case Errors.ERROR_NAME_FUNCTION_NAME_SET_AS_VAR:              return "ERROR_NAME_FUNCTION_NAME_SET_AS_VAR";
            case Errors.ERROR_RETURN_NOT_IN_FUNCTION:                     return "ERROR_RETURN_NOT_IN_FUNCTION";
            case Errors.ERROR_VAR_DEFINE:                                 return "ERROR_VAR_DEFINE";
            case Errors.ERROR_EXEC__OUT_OF_INDEX:                         return "ERROR_EXEC__OUT_OF_INDEX";
            case Errors.ERROR_EXEC__RESERVED_WORD:                        return "ERROR_EXEC__RESERVED_WORD";
            case Errors.ERROR_EXEC__VAR_DEFINE_IN_GLOBAL:                 return "ERROR_EXEC__VAR_DEFINE_IN_GLOBAL";
            case Errors.ERROR_EXEC__VAR_IS_CONST:                         return "ERROR_EXEC__VAR_IS_CONST";
        }
        
        return "get_str_error_value: error";
    }
}
