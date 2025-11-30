package code.search;

import code.model.Position;
import java.util.Objects;

public class State {
    public final Position pos;
    public final Position goal;
    public State(Position pos, Position goal){ this.pos = pos; this.goal = goal; }
    @Override public boolean equals(Object o){
        if(this==o) return true;
        if(!(o instanceof State)) return false;
        State s = (State)o;
        return pos.equals(s.pos) && goal.equals(s.goal);
    }
    @Override public int hashCode(){ return Objects.hash(pos, goal); }
}
