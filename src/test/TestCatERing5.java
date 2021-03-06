package test;

import businesslogic.CatERing;
import businesslogic.UseCaseLogicException;
import businesslogic.event.EventInfo;
import businesslogic.event.ServiceInfo;
import businesslogic.recipe.Recipe;
import businesslogic.task.Sheet;
import businesslogic.task.Task;
import businesslogic.task.TaskException;
import businesslogic.turn.PreparationTurn;
import businesslogic.user.User;
import javafx.collections.ObservableList;

public class TestCatERing5 {
    public static void main(String[] args) {
        try {
            CatERing.getInstance().getUserManager().fakeLogin("Lidia");
            User cook = User.loadUser("Marinella");

            EventInfo event = CatERing.getInstance().getEventManager().getEventInfo().get(2);
            ServiceInfo service = event.getServices().get(0);
            ObservableList<PreparationTurn> turns = CatERing.getInstance().getTurnManager().getPreparationsTurns();

            Sheet sheet = CatERing.getInstance().getTaskManager().createSheet(event, service);

            ObservableList<Recipe> recipes = CatERing.getInstance().getRecipeManager().getRecipes();
            Task t1 = CatERing.getInstance().getTaskManager().addTask(recipes.get(0));
            Task t2 = CatERing.getInstance().getTaskManager().addTask(recipes.get(1));
            Task t3 = CatERing.getInstance().getTaskManager().addTask(recipes.get(2));

            System.out.println("SHEET BEFORE");
            System.out.println(sheet);

            CatERing.getInstance().getTaskManager().assignTask(turns.get(0),t1,"mezz'ora","");
            CatERing.getInstance().getTaskManager().assignTask(turns.get(1),t2,cook,"","5 porzioni");
            CatERing.getInstance().getTaskManager().assignTask(turns.get(2),t3,"un'ora","10 pezzi");

            System.out.println("\nSHEET AFTER");
            System.out.println(sheet);

        } catch (UseCaseLogicException | TaskException e) {
            System.out.println("Errore di logica nello use case");

        }
    }
}
