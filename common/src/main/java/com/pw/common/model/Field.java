package com.pw.common.model;
import lombok.Data;
import org.json.simple.JSONObject;

@Data
public class Field {
    public Position position;
    public Cell cell;
    public FieldColor fieldColor;

    public enum FieldColor {
        RED,
        BLUE,
        GRAY
    }

    public Field() {

    }
    public JSONObject getJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("x", position.x);
        jsonObject.put("y", position.y);
        jsonObject.put("cell", cell.getJson());

        return jsonObject;
    }

    public Field(Position position, Cell cell){
        this.position = position;
        this.cell = cell;
        this.fieldColor=FieldColor.GRAY;
    }

    public Position getPosition(){ return this.position;}
    public void setPosition(Position position) {this.position = position;}

    public Cell getCell(){return this.cell;}
    public void setCell(Cell cell) {this.cell = cell; }

    public FieldColor getFieldColor() { return this.fieldColor;}
    public void setFieldColor(FieldColor fieldColor) { this.fieldColor = fieldColor;}

}
