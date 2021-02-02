package com.interprinator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class main 
{
    public static void main(String[] args) 
    {
        Map<String, Ex_Var> global_var = new HashMap<String, Ex_Var>();
        List<ExternalFunction> external_functions = new ArrayList<ExternalFunction>();
        
        global_var.put("pi3", new Ex_Var("", Exeption.TYPE_VAR__FLOAT, Math.PI / 3, true, true));
        
        

        external_functions.add(new ExternalFunction(  "com.interprinator.Functions"
                                                         , "printInt"
                                                         , ExternalFunction.NOT_LIMIT_PARAMETERS) );
        
        external_functions.add(new ExternalFunction(  "com.interprinator.Functions"
                                                         , "printList"
                                                         , ExternalFunction.NOT_LIMIT_PARAMETERS) );

        
        Sa _sa = new Sa(global_var, external_functions);
        
        try 
        {
            String content = new String(Files.readAllBytes(Paths.get("/home/sk/__RETURN__/Java/EX/4.s")));

            Ex_Var res = _sa.exec(content, Sa.MODE_RUN__NONE, 0, true, "");

            System.out.println( res.data );
        } 
        catch (IOException e) 
        { e.printStackTrace(); }

        _sa.print_error();
    } 
	
}
