package businesslogic.task;

import businesslogic.menu.MenuItem;
import businesslogic.recipe.CookingProcedure;
import businesslogic.recipe.Recipe;
import businesslogic.turn.PreparationTurn;
import businesslogic.turn.Turn;
import businesslogic.user.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import persistence.BatchUpdateHandler;
import persistence.PersistenceManager;
import persistence.ResultHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Task {
    private int id;
    private String time;
    private String portions;
    private boolean completed;
    private User cook;
    private CookingProcedure procedure;
    private PreparationTurn turn;
    private boolean added;

    public Task(CookingProcedure procedure, boolean added){
        this.cook = null;
        this.turn = null;
        this.procedure = procedure;
        this.added = added;
        this.time = "";
        this.portions = "";
        id = 0;
    }


    public int getId() {
        return id;
    }

    public String toString(){
        StringBuilder s = new StringBuilder(procedure.toString());
        s.append(". time: "+time+"; portions: "+portions+"; complete: "+completed);
        s.append("; turn:"+turn+"; cook: "+ cook);
        return s.toString();
    }

    public CookingProcedure getProcedure(){ return procedure;}

    public void setTurn(PreparationTurn turn, User cook, String time, String portion) {
        this.turn = turn;
        this.cook = cook;
        this.time = time;
        this.portions = portion;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setPortions(String portions){
        this.portions = portions;
    }

    public void setComplete(boolean complete){
        this.completed = complete;
    }

    public void removeAssignment() {
        this.turn = null;
        this.cook = null;
    }

    public boolean isAdded(){ return this.added; }

    public String getTime(){ return time;}
    public String getPortions(){ return portions;}
    public User getCook(){ return cook;}
    public boolean getComplete() {
        return this.completed;
    }
    public PreparationTurn getTurn(){ return turn;}

    public void setProcedure(CookingProcedure procedure) {
        this.procedure = procedure;
    }
    public void setAdded(boolean added) {
        this.added = added;
    }



    // STATIC METHODS FOR PERSISTENCE

    public static ObservableList<Task> loadTaskFor(int id) {
        ObservableList<Task> result = FXCollections.observableArrayList();
        String query = "SELECT * FROM Tasks WHERE sheet_id = " + id + " ORDER BY position";
        PersistenceManager.executeQuery(query, new ResultHandler() {
            @Override
            public void handle(ResultSet rs) throws SQLException {
                CookingProcedure ck = CookingProcedure.loadCookingProcedureById(rs.getInt("procedure_id"));
                boolean added = rs.getBoolean("added");
                Task t = new Task(ck,added);
                t.time = rs.getString("time");
                t.portions = rs.getString("portions");
                t.id = rs.getInt("id");
                t.completed = rs.getBoolean("completed");
                int cook = rs.getInt("cook_id");
                if (cook != 0)
                    t.cook = User.loadUserById(rs.getInt("cook_id"));
                else
                    t.cook = null;
                int turn = rs.getInt("turn_id");
                if (turn != 0)
                    t.turn = Turn.loadPrepTurnById(rs.getInt("turn_id"));
                else
                    t.turn = null;
                result.add(t);
            }
        });

        return result;
    }


    public static void saveAllTasks(ObservableList<Task> taskList, int sheet_id) {
        String taskInsert = "INSERT INTO catering.Tasks (procedure_id, sheet_id, position) VALUES (?, ?, ?);";
        PersistenceManager.executeBatchUpdate(taskInsert, taskList.size(), new BatchUpdateHandler() {
            @Override
            public void handleBatchItem(PreparedStatement ps, int batchCount) throws SQLException {
                ps.setInt(1, taskList.get(batchCount).getProcedure().getId());
                ps.setInt(2, sheet_id);
                ps.setInt(3, batchCount);
            }

            @Override
            public void handleGeneratedIds(ResultSet rs, int count) throws SQLException {
                taskList.get(count).id = rs.getInt(1);
            }
        });

    }

    public static void saveNewTask(int sheet_id, Task task, int position) {
        String taskInsert = "INSERT INTO catering.Tasks (procedure_id, sheet_id, position, added) " +
                "VALUES (" + task.getProcedure().getId() + ", " + sheet_id + ", " + position +", 1);";
        PersistenceManager.executeUpdate(taskInsert);

        task.id = PersistenceManager.getLastId();
    }

    public static void removeTask(int task_id) {
        String rem = "DELETE FROM Tasks WHERE id = " + task_id;
        PersistenceManager.executeUpdate(rem);
    }


    public static void updateAssignment(Task task) {
        String s = "UPDATE Tasks SET time='"+ PersistenceManager.escapeString(task.time)+
                "', portions='"+PersistenceManager.escapeString(task.portions)+
                "', turn_id="+task.turn.getId();

        if (task.cook != null)
            s += ", cook_id="+task.cook.getId();
        s+= " WHERE id="+task.getId();

        PersistenceManager.executeUpdate(s);
    }

    public static void updateTime(Task task) {
        String s = "UPDATE Tasks SET time = '" + PersistenceManager.escapeString(task.time) + "'" +
                " WHERE id = " + task.getId();
        PersistenceManager.executeUpdate(s);
    }

    public static void updatePortions(Task task) {
        String s = "UPDATE Tasks SET portions = '" + PersistenceManager.escapeString(task.portions) + "'" +
                " WHERE id = " + task.getId();
        PersistenceManager.executeUpdate(s);
    }

    public static void updateCompleted(Task task) {
        int value;
        if (task.completed)
            value = 1;
        else
            value = 0;
        String s = "UPDATE Tasks SET completed = '" + value + "'" +
                " WHERE id = " + task.getId();
        PersistenceManager.executeUpdate(s);
    }

    public static void updateAssignmentRemoved(Task task) {
        String s = "UPDATE Tasks SET turn_id = null"+
                " WHERE id = " + task.getId();
        PersistenceManager.executeUpdate(s);
    }


}
