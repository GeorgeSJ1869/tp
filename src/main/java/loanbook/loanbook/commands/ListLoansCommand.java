package loanbook.loanbook.commands;

import loanbook.loanbook.LoanList;

public class ListLoansCommand extends LoanCommand {
    protected LoanList loans;

    public ListLoansCommand(LoanList loans) {
        this.loans = loans;
    }

    @Override
    public void execute() {
        System.out.println(loans.simpleFulList());
    }
}
