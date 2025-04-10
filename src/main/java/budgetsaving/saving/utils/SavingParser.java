package budgetsaving.saving.utils;

import budgetsaving.saving.exceptions.SavingException;
import budgetsaving.saving.exceptions.SavingParserException;
import cashflow.ui.command.Command;
import budgetsaving.saving.command.ContributeGoalCommand;
import budgetsaving.saving.command.DeleteContributionCommand;
import budgetsaving.saving.command.CheckSavingCommand;
import budgetsaving.saving.command.DeleteSavingCommand;
import budgetsaving.saving.command.ListSavingsCommand;
import budgetsaving.saving.command.SetSavingCommand;
import cashflow.model.interfaces.SavingManager;
import utils.money.Money;
import java.time.LocalDate;

public class SavingParser {

    private static double isValidAmount(double amount) throws SavingParserException {
        if (Double.isNaN(amount)) {  // Correctly check for NaN
            throw new SavingParserException("You have not entered an amount.");
        }
        if (amount < 0) {
            throw new SavingParserException("The amount you have entered is zero or negative.");
        }
        if (amount < 0.01){
            throw new SavingParserException("Minimum amount is 0.01.");
        }
        return amount;
    }


    private static LocalDate isFutureDate(LocalDate endDate) throws SavingParserException {
        if (endDate != null && endDate.isBefore(LocalDate.now())) {
            throw new SavingParserException("The end date you have entered cannot be before the start date.");
        }
        return endDate;
    }

    // each method will handle its own max index based on their size
    private static int isValidIndex(int index) throws SavingParserException {
        if (index < 0) {
            throw new SavingParserException("The index you have entered is out of range.");
        }
        return index;
    }

    private static String checkValidName(String name) throws SavingParserException {
        if (name == null || name.isEmpty()){
            throw new SavingParserException("Name identifier is missing or name is empty.");
        }
        return name;
    }

    public static Command parseSetGoalCommand(String input, SavingManager savingList)
            throws SavingException {
        SavingAttributes attributes = new SavingAttributes(input);
        String name = checkValidName(attributes.getName());
        double amount = isValidAmount(attributes.getAmount());
        Money setAmount = new Money(savingList.getCurrency(), amount);
        LocalDate deadline = isFutureDate(attributes.getDeadline());
        return new SetSavingCommand(savingList, name, setAmount, deadline);
    }

    public static Command parseContributeGoalCommand(String input, SavingManager savingList)
            throws SavingException {
        SavingAttributes attributes = new SavingAttributes(input);
        int index = isValidIndex(attributes.getIndex());
        double amount = isValidAmount(attributes.getAmount());
        Money setAmount = new Money(savingList.getCurrency(), amount);
        return new ContributeGoalCommand(savingList, index, setAmount);
    }

    public static Command parseListGoalCommand(SavingManager savingList) {
        // Expected format: check-goal with no extra parameters.
        return new ListSavingsCommand(savingList);
    }

    public static Command parseDeleteSavingCommand(String input, SavingManager savingList)
            throws SavingException {
        SavingAttributes attributes = new SavingAttributes(input);
        int index = isValidIndex(attributes.getIndex());

        return new DeleteSavingCommand(index, savingList);
    }

    public static Command parseDeleteContributionCommand(String input, SavingManager savingList)
            throws SavingException {
        SavingAttributes attributes = new SavingAttributes(input);
        int indexS = isValidIndex(attributes.getIndex());
        int indexC = isValidIndex(attributes.getContributionIndex());
        return new DeleteContributionCommand(indexS, indexC, savingList);
    }

    public static Command parseCheckSavingCommand(String input, SavingManager savingList)
            throws SavingException {
        SavingAttributes attributes = new SavingAttributes(input);
        int index = isValidIndex(attributes.getIndex());

        return new CheckSavingCommand(index, savingList);
    }
}
