package com.intrbiz.data.db.compiler.function;

import static com.intrbiz.data.db.compiler.DatabaseAdapterCompiler.escapeString;

import com.intrbiz.data.db.compiler.DatabaseAdapterCompiler;
import com.intrbiz.data.db.compiler.model.Argument;
import com.intrbiz.data.db.compiler.model.Function;
import com.intrbiz.data.db.compiler.util.TextUtil;
import com.intrbiz.util.compiler.model.JavaMethod;

public class SetterCompiler implements FunctionCompiler
{

    @Override
    public void compileFunctionBinding(DatabaseAdapterCompiler compiler, JavaMethod method, Function function)
    {
        StringBuilder s = method.getCode();
        //
        s.append("this.connection.use(new DatabaseCall<Object>() {\r\n");
        s.append("  public Object run(final Connection with) throws SQLException, DataException {\r\n");
        //
        s.append("    try (PreparedStatement stmt = with.prepareStatement(\"").append(escapeString(compiler.getDialect().getFunctionCallQuery(function).toString())).append("\"))\r\n");
        s.append("    {\r\n");
        // bind params
        int idx = 0;
        for (Argument arg : function.getArguments())
        {

            s.append(arg.getType().setBinding("      ", idx + 1, "p0.get" + TextUtil.ucFirst(arg.getShadowOf().getDefinition().getName()) + "()"));
            idx++;
        }
        // execute
        s.append("      stmt.execute();\r\n");
        s.append("    }\r\n");
        s.append("    return null;\r\n");
        //
        s.append("  }\r\n");
        s.append("});\r\n");
    }

}
