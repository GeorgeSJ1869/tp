package budget_saving.budget.command;

import cashflow.command.Command;
import cashflow.model.interfaces.BudgetManager;

public class AddToBudgetCommand implements Command {
    private BudgetManager budgetManager;
    private int index;
    private double amount;

    public AddToBudgetCommand(BudgetManager budgetManager, int index, double amount) {
        this.budgetManager = budgetManager;
        this.index = index - 1;
        this.amount = amount;
    }

    @Override
    public void execute() {
        budgetManager.addToBudget(index, amount);
    }
}
