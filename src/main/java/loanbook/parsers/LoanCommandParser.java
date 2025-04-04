package loanbook.parsers;

import loanbook.LoanManager;
import loanbook.commands.DeleteLoanCommand;
import loanbook.commands.PrintMessageCommand;
import loanbook.commands.ListLoansCommand;
import loanbook.commands.LoanCommand;
import loanbook.commands.ShowLoanDetailCommand;
import loanbook.commands.addcommands.AddAdvancedBulletLoanCommand;
import loanbook.commands.addcommands.AddSimpleBulletLoanCommand;
import loanbook.commands.findcommands.FindAssociatedLoanCommand;
import loanbook.commands.findcommands.FindIncomingLoanCommand;
import loanbook.commands.findcommands.FindOutgoingLoanCommand;
import loanbook.commands.setcommands.SetDescriptionCommand;
import loanbook.commands.setcommands.SetInterestCommand;
import loanbook.commands.setcommands.SetPrincipalCommand;
import loanbook.commands.setcommands.SetReturnDateCommand;
import loanbook.commands.setcommands.SetReturnStatusCommand;
import loanbook.commands.setcommands.SetStartDateCommand;
import loanbook.interest.Interest;
import loanbook.loan.SimpleBulletLoan;
import utils.contacts.ContactsList;
import utils.contacts.EmptyNameException;
import utils.datetime.DateParser;
import utils.money.Money;
import utils.money.MoneyParser;
import utils.contacts.Person;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Scanner;

/**
 * This class parses the user input to generate commands. Asks the user for more input if necessary.
 */
public class LoanCommandParser {
    public static LoanCommand parse(LoanManager loanManager, Scanner scanner, Currency defaultCurrency, String input) {
        String[] splitFirst = input.split(" ", 2);
        String firstWord = splitFirst[0].trim();
        try {
            switch (firstWord) {
            case "list":
                return new ListLoansCommand(loanManager);
            case "delete":
                int deleteIndex = Integer.parseInt(splitFirst[1]);
                return new DeleteLoanCommand(loanManager, deleteIndex);
            case "add":
                return handleAddLoanCommand(loanManager, scanner, defaultCurrency);
            case "show":
                int showIndex = Integer.parseInt(splitFirst[1]);
                return new ShowLoanDetailCommand(loanManager, showIndex);
            case "return":
                int returnIndex = Integer.parseInt(splitFirst[1]);
                return new SetReturnStatusCommand(loanManager, returnIndex, true);
            case "unreturn":
                int unReturnIndex = Integer.parseInt(splitFirst[1]);
                return new SetReturnStatusCommand(loanManager, unReturnIndex, false);
            case "edit":
                if (splitFirst.length == 1) {
                    return new PrintMessageCommand(
                            "What part of which loan are you editing? Format: edit X [attribute]");
                }
                String[] split = splitFirst[1].split(" ", 2);
                int index;
                try {
                    index = Integer.parseInt(split[0]);
                } catch (NumberFormatException e) {
                    return new PrintMessageCommand(
                            "What part of which loan are you editing? Format: edit X [attribute]");
                }
                String attribute = split[1];
                return handleSetCommand(loanManager, scanner, index, attribute, defaultCurrency);
            case "find":
                if (splitFirst.length == 1) {
                    return new PrintMessageCommand("What are you looking for? Type \"help find\" for help.");
                }
                return handleFindCommand(loanManager, scanner, splitFirst[1]);
            case "help":
                if (splitFirst.length == 1) {
                    return handleHelpCommand(scanner, null);
                } else {
                    return handleHelpCommand(scanner, splitFirst[1]);
                }
            default:
                return null;
            }
        } catch (IndexOutOfBoundsException | NullPointerException | NumberFormatException e) {
            return null;
        }
    }

    public static LoanCommand parse(LoanManager loanManager, Scanner scanner, String defaultCurrency, String input) {
        return parse(loanManager, scanner, Currency.getInstance(defaultCurrency), input);
    }

    private static LoanCommand handleAddLoanCommand(LoanManager loanManager, Scanner scanner, Currency currency) {
        String mode;
        System.out.print("With or without interest? (y/n)\n> ");
        mode = scanner.nextLine();
        while (!(mode.equalsIgnoreCase("y") || mode.equalsIgnoreCase("n"))) {
            System.out.print("Input y or n only!\n> ");
            mode = scanner.nextLine();
        }
        Person lender = handlePersonInputUI(loanManager.getContactsList(), scanner,
                "Enter the lender's name:");
        Person borrower = handleBorrowerInputUI(loanManager.getContactsList(), scanner,
                "Enter the borrower's name:", lender);

        try {
            if (mode.equals("n")) {
                Money money = MoneyParser.handleMoneyInputUI(scanner, currency,
                        "Key in the amount of money lent:");
                String description = handleDescriptionInputUI(scanner);
                LocalDate returnDate = DateParser.handleLocalDateUI(scanner,
                        "Key in the return date of the loan (yyyy-mm-dd)", true);
                return new AddSimpleBulletLoanCommand(loanManager, description, lender, borrower, money, returnDate);
            } else if (mode.equals("y")) {
                Money money = MoneyParser.handleMoneyInputUI(scanner, currency,
                        "Key in the amount of principal:");
                LocalDate startDate = DateParser.handleLocalDateUI(scanner,
                        "Key in the start date of the loan (yyyy-mm-dd):");
                LocalDate returnDate = DateParser.handleReturnDateUI(scanner,
                        "Key in the return date of the loan (yyyy-mm-dd)", startDate, true);
                Interest interest = InterestParser.handleInterestInputUI(scanner);
                assert interest != null;
                String description = handleDescriptionInputUI(scanner);
                return new AddAdvancedBulletLoanCommand(loanManager, description, lender, borrower, money, startDate,
                        returnDate, interest);
            }
        } catch (NullPointerException | NumberFormatException e) {
            return new PrintMessageCommand("Invalid number input");
        } catch (DateTimeParseException e) {
            return new PrintMessageCommand("Invalid date format");
        }
        return null;
    }

    private static LoanCommand handleSetCommand(LoanManager loanManager, Scanner scanner, int index, String attribute,
                                                Currency defaultCurrency) {
        switch (attribute) {
        case "lender":
        case "borrower":
            return new PrintMessageCommand("You cannot edit the lender or borrower.");
        case "description":
            System.out.print("Key in the new description:\n> ");
            String description = scanner.nextLine();
            if (description.equalsIgnoreCase("N/A")) {
                description = null;
            }
            return new SetDescriptionCommand(loanManager, index, description);
        case "start date":
            LocalDate startDate = DateParser.handleLocalDateUI(scanner, "Key in the new start date:");
            return new SetStartDateCommand(loanManager, index, startDate);
        case "return date":
            LocalDate startDateReference = loanManager.get(index).startDate();
            LocalDate returnDate = DateParser.handleReturnDateUI(scanner, "Key in the new return date:",
                    startDateReference);
            return new SetReturnDateCommand(loanManager, index, returnDate);
        case "principal":
        case "amount":
            String instruction = "Key in the amount" + (attribute.equals("principal") ? "of principal" : "");
            Money money = MoneyParser.handleMoneyInputUI(scanner, defaultCurrency, instruction);
            return new SetPrincipalCommand(loanManager, index, money);
        case "interest":
            if (loanManager.get(index) instanceof SimpleBulletLoan) {
                return new PrintMessageCommand("A simple bullet loan does not apply interest.\n> ");
            } else {
                Interest interest = InterestParser.handleInterestInputUI(scanner);
                return new SetInterestCommand(loanManager, index, interest);
            }
        default:
            return null;
        }
    }

    private static String handleDescriptionInputUI(Scanner scanner) {
        System.out.print("Key in the description (Key in \"N/A\" if not applicable):" + "\n> ");
        String input = scanner.nextLine();
        if (input.equalsIgnoreCase("N/A")) {
            return null;
        }
        return input;
    }

    private static LoanCommand handleFindCommand(LoanManager loanManager, Scanner scanner, String input) {
        if (input.contains("outgoing loan")) {
            String name = input.replace("outgoing loan", "");
            return new FindOutgoingLoanCommand(loanManager, name.trim());
        } else if (input.contains("incoming loan")) {
            String name = input.replace("incoming loan", "");
            return new FindIncomingLoanCommand(loanManager, name.trim());
        } else if (input.trim().startsWith("tag")){
            String tag = input.replace("tag", "").trim();
        } else {
            Person person = loanManager.getContactsList().findName(input.trim());
            if (person != null) {
                return new FindAssociatedLoanCommand(loanManager, person);
            }
        }
        return null;
    }

    private static ArrayList<String> handleAddTags(Scanner scanner) {
        ArrayList<String> output = new ArrayList<>();
        while (true) {
            System.out.print("Key in a tag (Key in \"N/A\" if not applicable");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("N/A")) {
                break;
            }
            output.add(input);
        }
        return output;
    }

    private static LoanCommand handleHelpCommand(Scanner scanner, String input) {
        if (input == null) {
            return new PrintMessageCommand("""
                    Here are some commands that might be useful to you:
                    "add": add a loan.
                    "list": view the list of all loans recorded.
                    "show X": show the details of the Xth loan in the list.
                    "edit X [attribute]": edit the specified attribute of the Xth loan.
                    "delete X": delete the Xth loan.
                    "return X": set the Xth loan as returned.
                    "unreturn X": set the Xth loan as not returned.
                    "find": find loans. Type "help find" for more details.
                    """);
        }
        switch (input.trim()) {
        case "find":
            return new PrintMessageCommand("""
                    You can find loans by entering these commands:
                    "find [name] outgoing loan": shows all loans lent by [name].
                    "find [name] incoming loan": shows all loans borrowed by [name].
                    "find [name]": shows all loans lent or borrowed by [name].
                    "find [tag]": shows all loans with [tag].
                    """);
        default:
            return null;
        }
    }

    private static Person handlePersonInputUI(ContactsList contactsList, Scanner scanner, String instruction) {
        Person person;
        while (true) {
            System.out.print(instruction + "\n> ");
            String input = scanner.nextLine();
            try {
                person = contactsList.findOrAdd(input.trim());
                break;
            } catch (EmptyNameException e) {
                System.out.println("Name cannot be empty");
            }
        }
        return person;
    }

    private static Person handleBorrowerInputUI(ContactsList contactsList, Scanner scanner, String instruction,
                                                Person lender) {
        Person borrower;
        while (true) {
            System.out.print(instruction + "\n> ");
            String input = scanner.nextLine().trim();
            if (input.equals(lender.getName())) {
                System.out.println("The borrower must be different from the lender");
            } else {
                try {
                    borrower = contactsList.findOrAdd(input);
                    break;
                } catch (EmptyNameException e) {
                    System.out.println("Name cannot be empty");
                }
            }
        }
        return borrower;
    }
}
