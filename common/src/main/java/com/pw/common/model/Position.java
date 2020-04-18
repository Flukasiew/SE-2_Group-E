package com.pw.common.model;

public class Position {
    public int x;
    public int y;

    public Position() {

    }

    public Position(int i, int i1) {
        this.x = i;
        this.y = i1;
    }

    public enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    public void changePosition(Direction direction) {
        switch(direction) {
            case UP:
                y += 1;
                break;
            case DOWN:
                y -= 1;
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
