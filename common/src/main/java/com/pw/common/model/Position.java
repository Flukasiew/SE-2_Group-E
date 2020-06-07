package com.pw.common.model;
import lombok.Getter;
import lombok.Setter;

public class Position {
    public int x;
    public int y;

    public Position() {

    }

    public Position(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public int getX(){ return x; }
    public void setX(int x){ this.x  = x; }

    public int getY(){ return y; }
    public void setY(int y){ this.y  = y; }

    public enum Direction {
        Up,
        Down,
        Left,
        Right
    }

    public void changePosition(Direction direction) {
        switch(direction) {
            case Up:
                y -= 1;
                break;
            case Down:
                y += 1;
                break;
            case Left:
                x-=1;
                break;
            case Right:
                x+=1;
                break;
        }
    }

}
