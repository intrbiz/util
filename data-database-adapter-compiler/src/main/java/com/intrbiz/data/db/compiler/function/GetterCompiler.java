package com.intrbiz.data.db.compiler.function;

import com.intrbiz.Util;
import com.intrbiz.data.db.compiler.DatabaseAdapterCompiler;
import com.intrbiz.data.db.compiler.model.Argument;
import com.intrbiz.data.db.compiler.model.Column;
import com.intrbiz.data.db.compiler.model.Function;
import com.intrbiz.util.compiler.model.JavaField;
import com.intrbiz.util.compiler.model.JavaMethod;
import com.intrbiz.util.compiler.util.JavaUtil;

public class GetterCompiler implements FunctionCompiler
{

    @Override
    public void compileFunctionBinding(DatabaseAdapterCompiler compiler, JavaMethod method, Function function)
    {
        // metrics
        JavaField  metricField = null;
        JavaField  cacheMissMetricField = null;
        if (compiler.isWithMetrics()) metricField = DatabaseAdapterCompiler.addMetricField(method, function);
        if (compiler.isWithMetrics() && function.isCacheable()) cacheMissMetricField = DatabaseAdapterCompiler.addCacheMissMetricField(method, function);
        //
        StringBuilder s = method.getCode();
        //
        String objType = function.getReturnType().getDefaultJavaType().getSimpleName();
        method.getJavaClass().addImport(function.getReturnType().getDefaultJavaType().getCanonicalName());
        //
        s.append("return this.use");
        // method variants
        if (compiler.isWithMetrics()) s.append("Timed");
        if (function.isCacheable()) s.append("Cached");
        if (function.isCacheable() && function.isReturnsList()) s.append("List");
        s.append("(\r\n");
        // some parameters before the call
        if (compiler.isWithMetrics()) s.append(metricField.getName()).append(",\r\n");
        // cache stuff
        if (function.isCacheable())
        {
            if (compiler.isWithMetrics()) s.append(cacheMissMetricField.getName()).append(",\r\n");
            // optimise for lookup by primary key
            if (function.isAllArgumentsPrimaryKey())
            {
                s.append("\"").append(JavaUtil.escapeString(function.getTable().getName())).append(".\"");
                boolean ns = false;
                for (Column column : function.getTable().getPrimaryKey().getColumns())
                {
                    int idx = 0;
                    for (Argument arg : function.getArguments())
                    {
                        if (column.equals(arg.getShadowOf()))
                        {
                            if (ns) s.append(" + \".\"");
                            s.append(" + p").append(idx);
                            ns = true;
                            break;
                        }
                        idx++;
                    }
                }
                s.append(",\r\n");
                // the entity key lambda
                // as we are a looking up by the primary cache key we do not need to xref
                s.append("null,\r\n");
            }
            else
            {
            // build the cache key, function_name.arg0[.argN]
            s.append("\"").append(JavaUtil.escapeString(function.getName())).append(function.getArguments().size() > 0 ? "." : "").append("\"");
            for (int i = 0; i < function.getArguments().size(); i++)
            {
                if (i > 0) s.append(" + \".\"");
                s.append(" + p" + i);
            }
            s.append(",\r\n");
            // the entity key lambda
            s.append(DatabaseAdapterCompiler.tableCacheKey(function.getTable())).append(",\r\n");
            }
        }
        //
        /* s.append("new DatabaseCall<").append(method.getReturnType()).append(">() {\r\n"); */
        s.append("(with) -> {\r\n");
        /*s.append("  public ").append(method.getReturnType()).append(" run(final Connection with) throws SQLException, DataException {\r\n");*/
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
            s.append("      ").append(arg.getType().setBinding(idx + 1, DatabaseAdapterCompiler.applyAdapter(method.getJavaClass(), Util.nullable(arg.getShadowOf(), Column::getAdapter), false, "p" + idx))).append(";\r\n");
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
        for (Column col : function.getTable().getColumns())
        {
            col.getType().addImports(method.getJavaClass().getImports());
            s.append("          obj.").append(JavaUtil.setterName(col.getDefinition())).append("(").append(DatabaseAdapterCompiler.applyAdapter(method.getJavaClass(), col.getAdapter(), true, col.getType().getBinding(idx + 1))).append(");\r\n");
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
        /*s.append("  }\r\n");*/
        // close lamdba
        s.append("}\r\n");
        // close use()
        s.append(");\r\n");
    }

}
