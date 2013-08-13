package com.intrbiz.data.db.compiler.function;

import com.intrbiz.data.db.compiler.DatabaseAdapterCompiler;
import com.intrbiz.data.db.compiler.model.Argument;
import com.intrbiz.data.db.compiler.model.Column;
import com.intrbiz.data.db.compiler.model.Function;
import com.intrbiz.data.db.compiler.model.function.GetterInfo;
import com.intrbiz.util.compiler.model.JavaMethod;
import com.intrbiz.util.compiler.util.JavaUtil;

public class GetterCompiler implements FunctionCompiler
{

    @Override
    public void compileFunctionBinding(DatabaseAdapterCompiler compiler, JavaMethod method, Function function)
    {
        GetterInfo info = (GetterInfo) function.getIntrospectionInformation();
        //
        StringBuilder s = method.getCode();
        //
        String objType = function.getReturnType().getDefaultJavaType().getSimpleName();
        method.getJavaClass().addImport(function.getReturnType().getDefaultJavaType().getCanonicalName());
        //
        s.append("return this.connection.use(new DatabaseCall<").append(method.getReturnType()).append(">() {\r\n");
        s.append("  public ").append(method.getReturnType()).append(" run(final Connection with) throws SQLException, DataException {\r\n");
        //
        if (function.isReturnsList()) s.append("    List<").append(objType).append("> ret = new LinkedList<").append(objType).append(">();\r\n");
        //
        s.append("    try (PreparedStatement stmt = with.prepareStatement(\"").append(JavaUtil.escapeString(compiler.getDialect().getFunctionCallQuery(function).toString())).append("\"))\r\n");
        s.append("    {\r\n");
        // bind params
        int idx = 0;
        for (Argument arg : function.getArguments())
        {
            arg.getType().addImports(method.getJavaClass().getImports());
            s.append("      ").append(arg.getType().setBinding(idx + 1, "p" + idx)).append(";\r\n");
            idx++;
        }
        // execute
        s.append("      try (ResultSet rs = stmt.executeQuery())\r\n");
        s.append("      {\r\n");
        // move next
        if (function.isReturnsList()) s.append("        while (rs.next())\r\n");
        else s.append("        if (rs.next())\r\n");
        s.append("        {\r\n");
        // bind the result
        s.append("          ").append(objType).append(" obj = new ").append(objType).append("();\r\n");
        // bind cols
        idx = 0;
        for (Column col : info.getTable().getColumns())
        {
            col.getType().addImports(method.getJavaClass().getImports());
            s.append("          obj.").append(JavaUtil.setterName(col.getDefinition())).append("(").append(col.getType().getBinding(idx + 1)).append(");\r\n");
            idx++;
        }
        // return
        if (function.isReturnsList()) s.append("          ret.add(obj);\r\n");
        else s.append("          return obj;\r\n");
        s.append("        }\r\n");
        s.append("      }\r\n");
        s.append("    }\r\n");
        //
        if (function.isReturnsList()) s.append("    return ret;\r\n");
        else s.append("    return null;\r\n");
        //
        s.append("  }\r\n");
        s.append("});\r\n");
    }

}
