package code.model;

public class Agent {
    public final String id;
    public Position pos;

    public Agent(String id, Position initial){
        this.id = id;
        this.pos = initial;
    }

    @Override public String toString(){ return id + "@" + pos; }
}
