package com.pw.player.model;

import com.pw.common.model.*;

import java.net.InetAddress;
import java.util.UUID;
import java.util.Scanner;

public class Player {
    public Team team;
    private InetAddress ipAddress;
    private int portNumber; // we can think about using InetSocketAddress from java.net
    public String playerName;
    public ActionType lastAction;
    public Position.Direction lastDirection;
    public Position position;
    public Board board;
    public boolean piece;
    public boolean tested;
    private UUID playerGuid;
    private PlayerState playerState;
    //private SimpleClient simpleClient;

    public Player(TeamColor color, TeamRole role, Position position)
    {
        Team _team = new Team();
        team.setColor(color);
        team.setRole(role);
        this.team = _team;
        this.position = position;
        piece = false;
        tested = false;
        playerState = PlayerState.INITIALIZING;
    }

    public void listen()
    {

    }

    public void makeAction()
    {
        System.out.println("Choose action:");
        System.out.println("1. TEST");
        System.out.println("2. PLACE");
        System.out.println("3. TAKE");
        System.out.println("4. DISCOVER");
        System.out.println("5. MOVE");

        int action = new Scanner(System.in).nextInt();

        switch (action)
        {
            case 1:
                if(piece == true && tested == false)
                    testPiece();
                break;
            case 2:
                if(piece==true && board.cellsGrid[position.x][position.y].getCellState()== Cell.CellState.GOAL)
                    placePiece();
                break;
            case 3:
                if(piece==false && board.cellsGrid[position.x][position.y].getCellState()==Cell.CellState.PIECE)
                    takePiece();
                break;
            case 4:
                discover();
            case 5:
                move(chooseDirection());
            default:
                makeAction();
        }
    }

    private Position.Direction chooseDirection()
    {
        System.out.println("Choose direction:");
        System.out.println("1. LEFT");
        System.out.println("2. UP");
        System.out.println("3. RIGHT");
        System.out.println("4. DOWN");
        int directionInt = new Scanner(System.in).nextInt();
        Position.Direction direction = Position.Direction.UP;
        switch (directionInt)
        {
            case 1:
                return Position.Direction.LEFT;
            case 2:
                return Position.Direction.UP;
            case 3:
                return Position.Direction.RIGHT;
            case 4:
                return Position.Direction.DOWN;
        }
        return Position.Direction.UP;
    }

    private void singleDiscover(int x, int y)
    {
        if (board.cellsGrid[x][y].getCellState() == Cell.CellState.UNKNOWN &&
                x >= 0 && x < board.boardWidth && y >= 0  && y < board.boardHeight)
        {
            board.cellsGrid[x][y].setCellState("Game Master returns");
        }
    }

    private boolean CanMove()
    {
        int x = position.x;
        int y = position.y;

        if (x < 0 && x >= board.boardWidth && y < 0  && y >= board.boardHeight)
            return false;

        if(team.color == TeamColor.BLUE)
        {
            if (y < board.taskAreaHeight)
                return false;
        }

        if(team.color == TeamColor.RED)
        {
            if (y > board.boardHeight - board.taskAreaHeight)
                return false;
        }

        return true;
    }

    private void discover()
    {
        int x = position.x;
        int y = position.y;

        if("allowed")
        {
            singleDiscover(x,y);
            singleDiscover(x-1,y);
            singleDiscover(x-1,y+1);
            singleDiscover(x-1,y-1);
            singleDiscover(x+1,y);
            singleDiscover(x+1,y-1);
            singleDiscover(x+1,y+1);
            singleDiscover(x,y+1);
            singleDiscover(x,y-1);
            //lastAction = ActionType.DISCOVER;
        }
    }

    private void move(Position.Direction direction) {
//        simpleClient.startConnection(ipAddress, portNumber);
//        simpleClient.sendMessage("message");
//        simpleClient.closeConnection();

        if("allowed")
        {
            Position oldPosition;
            oldPosition = position;
            position.changePosition(direction);

            if (CanMove())
                lastAction = ActionType.MOVE;
            else
                position = oldPosition;
        }
    }

    private void takePiece()
    {
        lastAction = ActionType.PICKUP;
        if("enabled")
        {
            board.cellsGrid[position.x][position.y].setCellState(Cell.CellState.EMPTY);
            piece = true;
        }
        else
            return;
    }

    private void testPiece()
    {
        if("enabled")
            if("sham")
            {
                lastAction = ActionType.DESTROY;
                piece = false;
                tested = false;
            }
            else
            {
                lastAction = ActionType.TEST;
                tested = true;
                return;
            }
        else return;
    }

    private void placePiece()
    {
        if("allowed")
        {
            if("goal")
            {
                lastAction = ActionType.PLACE;
                board.cellsGrid[position.x][position.y].setCellState(Cell.CellState.EMPTY);
                piece = false;
                tested = false;
            }
            else
            {
                lastAction = ActionType.PLACE;
                board.cellsGrid[position.x][position.y].setCellState(Cell.CellState.EMPTY);
                piece = false;
                tested = false;
            }
        }
        else return;
    }

    public void testAction(ActionType action, Position.Direction direction)
    {
        switch(action)
        {
            case TEST:
                testPiece();
                break;
            case MOVE:
                move(direction);
                break;
            case PICKUP:
                takePiece();
                break;
        }
    }
}
