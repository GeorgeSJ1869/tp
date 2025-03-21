package expense_income.expense.commands;

import expense_income.expense.ExpenseManager;

public class AddCommand extends ExpenseCommand {
    private String description;
    private double amount;

    public AddCommand(String description, double amount) {
        this.description = description;
        this.amount = amount;
    }

    @Override
    public void execute(ExpenseManager manager) {
        manager.addExpense(description, amount);
    }
}
