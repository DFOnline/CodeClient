package dev.dfonline.codeclient.dev.Debug;

import java.util.ArrayList;
import java.util.Objects;

public class Variables {
    public ArrayList<Variable> variables = new ArrayList<>();
    public Variables() {}

    public void addOrUpdate(Variable variable) {
        for (int i = 0; i < variables.size(); i++) {
            if(Objects.equals(variables.get(i).name, variable.name)) {
                variables.set(i, variable);
                return;
            }
        }
        variables.add(variable);
    }

    public void clear() {
        variables.clear();
    }
}
