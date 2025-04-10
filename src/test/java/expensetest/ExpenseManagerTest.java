package expensetest;

import cashflow.model.FinanceData;
import cashflow.model.storage.DummyStorage;
import expenseincome.expense.Expense;
import expenseincome.expense.ExpenseManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExpenseManagerTest {
    private ExpenseManager manager;

    @BeforeEach
    void setUp() throws FileNotFoundException {
        FinanceData data = new FinanceData();
        data.setCurrency("USD");
        DummyStorage dummyStorage = new DummyStorage();
        manager = new ExpenseManager(data, "USD", dummyStorage);
    }

    @Test
    void testAddExpenseWithDate() {
        LocalDate date = LocalDate.of(2025, 3, 20);
        manager.addExpense("Lunch", 10.50, date, "Food");
        assertEquals(1, manager.getExpenseCount());
        Expense e = manager.getExpense(0);
        assertEquals("Lunch", e.getDescription());
        assertEquals(10.50, e.getAmount(), 0.01);
        assertEquals(date, e.getDate());
        assertEquals("Food", e.getCategory());
    }

    @Test
    void testAddExpenseDefaultTodayDate() {
        LocalDate today = LocalDate.now();
        manager.addExpense("Coffee", 4.00, today, "Drink");
        assertEquals(today, manager.getExpense(0).getDate());
        assertEquals("Drink", manager.getExpense(0).getCategory());
    }

    @Test
    void testAddExpenseEmptyDescription() {
        manager.addExpense("", 10.50, LocalDate.now(), "Other");
        assertEquals(0, manager.getExpenseCount());
    }

    @Test
    void testAddExpenseNullDescription() {
        manager.addExpense(null, 10.50, LocalDate.now(), "Other");
        assertEquals(0, manager.getExpenseCount());
    }

    @Test
    void testAddExpenseNegativeAmount() {
        manager.addExpense("Lunch", -5, LocalDate.now(), "Food");
        assertEquals(0, manager.getExpenseCount());
    }

    @Test
    void testAddExpenseEmptyCategory() {
        manager.addExpense("Lunch", 5.00, LocalDate.now(), "");
        assertEquals(0, manager.getExpenseCount());
    }

    @Test
    void testDeleteExpenseValidIndex() {
        manager.addExpense("Lunch", 10.50, LocalDate.now(), "Food");
        assertEquals(1, manager.getExpenseCount());
        manager.deleteExpense(1);
        assertEquals(0, manager.getExpenseCount());
    }

    @Test
    void testDeleteExpenseInvalidIndex() {
        manager.addExpense("Lunch", 10.50, LocalDate.now(), "Food");
        manager.deleteExpense(2);
        assertEquals(1, manager.getExpenseCount());
    }

    @Test
    void testEditExpenseValidEdit() {
        LocalDate oldDate = LocalDate.of(2025, 3, 15);
        LocalDate newDate = LocalDate.of(2025, 3, 20);
        manager.addExpense("Lunch", 10.50, oldDate, "Food");
        manager.editExpense(1, "Dinner", 15.00, newDate, "Dinner");
        Expense edited = manager.getExpense(0);
        assertEquals("Dinner", edited.getDescription());
        assertEquals(15.00, edited.getAmount(), 0.01);
        assertEquals(newDate, edited.getDate());
        assertEquals("Dinner", edited.getCategory());
    }

    @Test
    void testEditExpenseInvalidIndex() {
        manager.editExpense(1, "Dinner", 15.00, LocalDate.now(), "Food");
        assertEquals(0, manager.getExpenseCount());
    }

    @Test
    void testSortExpensesByRecent() {
        manager.addExpense("A", 5, LocalDate.of(2023, 1, 1), "Misc");
        manager.addExpense("B", 10, LocalDate.of(2025, 1, 1), "Misc");
        manager.addExpense("C", 7, LocalDate.of(2024, 1, 1), "Misc");

        manager.sortExpensesByDate(true);

        assertEquals("B", manager.getExpense(0).getDescription());
        assertEquals("C", manager.getExpense(1).getDescription());
        assertEquals("A", manager.getExpense(2).getDescription());
    }

    @Test
    void testSortExpensesByOldest() {
        manager.addExpense("X", 8, LocalDate.of(2025, 3, 10), "Food");
        manager.addExpense("Y", 6, LocalDate.of(2022, 3, 10), "Food");
        manager.addExpense("Z", 9, LocalDate.of(2023, 3, 10), "Food");

        manager.sortExpensesByDate(false);

        assertEquals("Y", manager.getExpense(0).getDescription());
        assertEquals("Z", manager.getExpense(1).getDescription());
        assertEquals("X", manager.getExpense(2).getDescription());
    }
}
