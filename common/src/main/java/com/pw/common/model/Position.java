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
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    public void changePosition(Direction direction) {
        switch(direction) {
            case UP:
                y -= 1;
                break;
            case DOWN:
                y += 1;
                break;
            case LEFT:
                x-=1;
                break;
            case RIGHT:
                x+=1;
                break;
        }
    }

}
