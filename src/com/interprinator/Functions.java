package com.interprinator;

import java.util.List;

import com.interprinator.Ex_Var;

public class Functions 
{
    public Ex_Var printInt(List<Ex_Var> params)
    {
        for(int i = 0; i < params.size(); i++)
        {
            Ex_Var item = params.get(i);
            
            System.out.printf("[%s]", String.valueOf( item.value ) );
        }
		
        System.out.printf("\n" );
		
        return null;
    }
    
    public Ex_Var printList(List<Ex_Var> params)
    {
        for(int i = 0; i < params.size(); i++)
        {
            Ex_Var item = params.get(i);

            if( item.type == Exeption.TYPE_VAR__STR )
            {
                System.out.printf("\"%s\" ", String.valueOf( item.value ) );
            }
            else
            {
                System.out.printf("%s ", String.valueOf( item.value ) );
            }
        }
		
        System.out.printf("\n" );
		
        return null;
    }
}