package com.intrbiz.data.db.compiler.model;

import com.intrbiz.data.db.compiler.meta.Direction;
import com.intrbiz.data.db.compiler.meta.Nulls;

public class Order
{
    private Column column;

    private Direction direction;

    private Nulls nulls;

    public Order()
    {
        super();
    }

    public Order(Column column, Direction direction, Nulls nulls)
    {
        super();
        this.column = column;
        this.direction = direction;
        this.nulls = nulls;
    }

    public Column getColumn()
    {
        return column;
    }

    public void setColumn(Column column)
    {
        this.column = column;
    }

    public Direction getDirection()
    {
        return direction;
    }

    public void setDirection(Direction direction)
    {
        this.direction = direction;
    }

    public Nulls getNulls()
    {
        return nulls;
    }

    public void setNulls(Nulls nulls)
    {
        this.nulls = nulls;
    }

}
