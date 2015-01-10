package com.intrbiz.data.db.compiler;

import static org.junit.Assert.*;

import org.junit.Test;

import com.intrbiz.data.db.compiler.dialect.type.SQLSimpleType;
import com.intrbiz.data.db.compiler.dialect.type.SQLType;
import com.intrbiz.data.db.compiler.model.Column;
import com.intrbiz.data.db.compiler.model.Table;
import com.intrbiz.data.db.compiler.model.Version;

public class TestColumnOrdering
{

    /**
     * Test that columns get ordered by: version, class, index
     */
    @Test
    public void testColumnOrdering()
    {
        SQLType type = new SQLSimpleType("TEXT", "String", String.class);
        //
        Table table = new Table("test");
        table.addColumn(new Column(0, 1, "col_1",  type, new Version(1, 0, 0)));
        table.addColumn(new Column(0, 2, "col_2",  type, new Version(1, 0, 0)));
        table.addColumn(new Column(1, 1, "inter_col_1", type, new Version(1, 0, 0)));
        table.addColumn(new Column(1, 2, "inter_col_2", type, new Version(1, 2, 0)));
        table.addColumn(new Column(1, 1, "inter_col_3", type, new Version(1, 3, 0)));
        table.addColumn(new Column(2, 1, "parent_col_1", type, new Version(1, 0, 0)));
        table.addColumn(new Column(2, 2, "parent_col_2", type, new Version(1, 0, 0)));
        table.addColumn(new Column(2, 3, "parent_col_3", type, new Version(1, 1, 0)));
        table.finish();
        //
        System.out.println(table);
        //
        assertTrue("parent_col_1".equals(table.getColumns().get(0).getName()));
        assertTrue("parent_col_2".equals(table.getColumns().get(1).getName()));
        assertTrue("inter_col_1".equals(table.getColumns().get(2).getName()));
        assertTrue("col_1".equals(table.getColumns().get(3).getName()));
        assertTrue("col_2".equals(table.getColumns().get(4).getName()));
        assertTrue("parent_col_3".equals(table.getColumns().get(5).getName()));
        assertTrue("inter_col_2".equals(table.getColumns().get(6).getName()));
        assertTrue("inter_col_3".equals(table.getColumns().get(7).getName()));
    }

}
