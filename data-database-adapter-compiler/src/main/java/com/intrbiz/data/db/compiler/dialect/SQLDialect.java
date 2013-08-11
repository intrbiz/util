package com.intrbiz.data.db.compiler.dialect;

import java.io.IOException;
import java.lang.annotation.Annotation;

import com.intrbiz.data.db.compiler.dialect.function.SQLFunctionGenerator;
import com.intrbiz.data.db.compiler.dialect.type.SQLType;
import com.intrbiz.data.db.compiler.model.Function;
import com.intrbiz.data.db.compiler.model.Schema;
import com.intrbiz.data.db.compiler.model.Table;
import com.intrbiz.data.db.compiler.model.Type;
import com.intrbiz.data.db.compiler.util.SQLWriter;

public interface SQLDialect
{
    String getDialectName();
    
    //
    
    void registerFunctionGenerator(Class<? extends Annotation> type, SQLFunctionGenerator generator);
    
    //
    
    String getOwner();
    
    void setOwner(String owner);
    
    //
    
    SQLType getType(Class<?> javaClass);
    
    //
    
    void writeCreateSchema(SQLWriter to, Schema schema) throws IOException;
    
    void writeCreateTable(SQLWriter to, Table table) throws IOException;
    
    void writeCreateType(SQLWriter to, Type type) throws IOException;
    
    void writeCreateFunction(SQLWriter to, Function function) throws IOException;
    
    String functionBindingSQL(Function function);
}
