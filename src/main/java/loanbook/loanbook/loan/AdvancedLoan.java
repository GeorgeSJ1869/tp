package loanbook.loanbook.loan;

import loanbook.loanbook.interest.Interest;
import utils.money.Money;
import utils.people.Person;

import java.math.BigDecimal;
import java.time.LocalDate;

import static loanbook.loanbook.interest.InterestType.SIMPLE;

/**
 * Advanced loan type containing interest-related information.
 */
public class AdvancedLoan extends Loan {
    protected Money outstandingBalance;
    protected Interest interest;
    protected int incrementCount;

    public AdvancedLoan(String description, Person lender, Person borrower, Money money,
                        LocalDate startDate, Interest interest) {
        super(description, lender, borrower, money);
        this.interest = interest;
        this.startDate = startDate;
        outstandingBalance = money;
        incrementCount = 0;
    }

    public AdvancedLoan(String description, Person lender, Person borrower, Money money,
                        LocalDate startDate, LocalDate returnDate, Interest interest) {
        super(description, lender, borrower, money, returnDate);
        this.interest = interest;
        this.startDate = startDate;
        outstandingBalance = money;
        incrementCount = 0;
    }

    public Money outstandingBalance() {
        return outstandingBalance;
    }

    public int incrementCount() {
        return incrementCount;
    }

    @Override
    public void setStart(LocalDate date) {
        this.startDate = date;
        calculateBalance();
    }

    public void setInterest(Interest interest) {
        this.interest = interest;
        calculateBalance();
    }

    public void calculateBalance() {
        LocalDate date = startDate;
        Money balance = new Money(principal.getCurrency(), principal.getAmount());
        incrementCount = 0;
        while (date.plus(interest.period()).isBefore(LocalDate.now())) {
            if (interest.type() == SIMPLE) {
                BigDecimal increment = this.principal.getAmount().multiply(BigDecimal.valueOf(interest.rate() / 100));
                balance.increment(increment);
            } else {
                balance.increment(interest.rate());
            }
            incrementCount++;
            date = date.plus(interest.period());
        }
        outstandingBalance = balance;
    }

    /**
     * Displays the basic information of the loan.
     * @return a ready-to-print <code>String</code> containing all necessary information.
     */
    @Override
    public String basicInfo() {
        calculateBalance();
        return "Lender: [" + lender.getName()
                + "]    Borrower: [" + borrower.getName()
                + "]    Amount: " + principal
                + "    Start Date: " + startDate
                + (returnDate == null ? "" : "    Return Date: " + returnDate) + '\n'
                + "    Interest: " + interest + '\n'
                + "    Outstanding Balance: " + outstandingBalance + '\n'
                + (isReturned ? "    Returned" : "    Not Returned");
    }

    /**
     * Shows all details of this loan.
     * @return a ready-to-print <code>String</code> containing all information. Multiple lines.
     */
    @Override
    public String showDetails() {
        return super.showDetails()
                + "Interest: " + interest + '\n'
                + "Outstanding Balance: " + outstandingBalance + '\n'
                + (isReturned ? "Returned" : "Not Returned");
    }
}
