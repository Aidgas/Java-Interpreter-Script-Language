package com.interprinator;

public class ExternalFunction
{
    public static int NOT_LIMIT_PARAMETERS  = -1;
    
    public String name_class, name_method;
    public int count_params = NOT_LIMIT_PARAMETERS;

    public ExternalFunction(String name_class, String name_method, int count_params)
    {
        this.name_class   = name_class;
        this.name_method  = name_method;
        this.count_params = count_params;
    }
}
