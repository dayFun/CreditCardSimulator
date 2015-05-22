package main;

import static org.junit.Assert.assertEquals;

import java.text.NumberFormat;

import org.junit.Before;
import org.junit.Test;


public class CreditCardTest {

    private static final double NEW_ACCOUNT_STARTING_BALANCE = 0;
    private static final double NEW_ACCOUNT_CREDIT_LIMIT = 1000;
    private NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();
    private NumberFormat percentFormatter = NumberFormat.getPercentInstance();

    private CreditCard testCard;

    @Before
    public void setup() {
        testCard = new CreditCard();
    }

    @Test
    public void testWhenNewAccountCreatedThenBalanceIsZero() {
        assertEquals(NEW_ACCOUNT_STARTING_BALANCE, testCard.getBalance(), 0);
    }

    @Test
    public void testWhenNewAccountCreatedThenCreditLimitIs1000() {
        assertEquals(NEW_ACCOUNT_CREDIT_LIMIT, testCard.getCreditLimit(), 0);
    }

    @Test
    public void testCreateCardWithExistingAccountReopensAccount() {
        testCard.setCharge(100.0, "Electric Bill");
        double expectedBalance = testCard.getBalance();
        double expectedCredit = testCard.getAvailableCredit();
        int existingAccountNumber = testCard.getAccountNumber();
        String expectedMessage = "Account " + existingAccountNumber + " re-opened.";

        CreditCard newCard = new CreditCard(existingAccountNumber);

        assertEquals(expectedMessage, newCard.getActionMsg());
        assertEquals(expectedBalance, newCard.getBalance(), 0);
        assertEquals(expectedCredit, newCard.getAvailableCredit(), 0);
    }

    @Test
    public void testValidChargeIncreasesBalanceAndDecreasesCreditLimit() {
        double chargeAmount = 50.00;
        String description = "Porn";
        testCard.setCharge(chargeAmount, description);

        double expectedBalance = chargeAmount - NEW_ACCOUNT_STARTING_BALANCE;
        double expectedCreditLimit = NEW_ACCOUNT_CREDIT_LIMIT - chargeAmount;
        String expectedMessage = createChargeLogMessageTemplate(chargeAmount, description) + " posted.";

        assertEquals(expectedBalance, testCard.getBalance(), 0);
        assertEquals(expectedCreditLimit, testCard.getAvailableCredit(), 0);
        assertEquals(expectedMessage, testCard.getActionMsg());
    }

    @Test
    public void testChargeWithNegativeAmountThenTransactionDeclined() {
        double expectedBalance = testCard.getBalance();
        double chargeAmount = -50.00;
        String chargeDescription = "Booze";

        testCard.setCharge(chargeAmount, chargeDescription);

        String expectedErrorMessage = createChargeLogMessageTemplate(chargeAmount, chargeDescription) + " declined - illegal amount.";

        // TODO: Currency formatter removes negative symbol?

        assertEquals(expectedErrorMessage, testCard.getActionMsg());
        assertEquals(expectedBalance, testCard.getBalance(), 0);
    }

    @Test
    public void testWhenChargeOverCreditLimitThenTransactionDeclined() {
        double expectedBalance = testCard.getBalance();
        double chargeAmount = 10000;
        String chargeDescription = "Hooker";

        testCard.setCharge(chargeAmount, chargeDescription);

        String expectedErrorMessage = createChargeLogMessageTemplate(chargeAmount, chargeDescription) + " declined - over limit!";

        assertEquals(expectedErrorMessage, testCard.getActionMsg());
        assertEquals(expectedBalance, testCard.getBalance(), 0);
    }

    @Test
    public void testMakePaymentWithNegativeAmountRejectsPayment() {
        double expectedBalance = testCard.getBalance();
        double paymentAmount = -250.00;

        testCard.makePayment(paymentAmount);

        String expectedErrorMessage = createPaymentLogMessageTemplate(paymentAmount) + " declined - illegal amount.";

        assertEquals(expectedErrorMessage, testCard.getActionMsg());
        assertEquals(expectedBalance, testCard.getBalance(), 0);
    }

    @Test
    public void testMakeValidPaymentDecreasesBalanceAndIncreasesCreditLimit() {
        double chargeAmount = 500.0;
        double paymentAmount = 250.00;
        double expectedBalance = chargeAmount - paymentAmount;
        String expectedErrorMessage = createPaymentLogMessageTemplate(paymentAmount) + " posted.";
        testCard.setCharge(chargeAmount, "Plane Ticket");

        testCard.makePayment(paymentAmount);

        assertEquals(expectedErrorMessage, testCard.getActionMsg());
        assertEquals(expectedBalance, testCard.getBalance(), 0);
    }

    @Test
    public void testDoNotChargeNegativeInterest() {
        double illegalInterestRate = -75.0;
        double expectedBalance = testCard.getBalance();
        double expectedCreditLimit = testCard.getAvailableCredit();
        String expectedErrorMessage = "Interest rate of " + percentFormatter.format(illegalInterestRate) + " declined - illegal amount.";

        testCard.setInterestCharge(illegalInterestRate);

        assertEquals(expectedErrorMessage, testCard.getActionMsg());
        assertEquals(expectedBalance, testCard.getBalance(), 0);
        assertEquals(expectedCreditLimit, testCard.getAvailableCredit(), 0);
    }

    @Test
    public void testWhenBalanceIs500AndInterestRateIs10PercentThenInterestChargeIs50() {
        double expectedBalance = 550.0;
        double expectedCreditLimit = 450.0;
        testCard.setCharge(500.0, "Rent");

        testCard.setInterestCharge(0.10);
        String expectedMessage = createChargeLogMessageTemplate(50, "Interest charged") + " posted.";

        assertEquals(expectedBalance, testCard.getBalance(), 0);
        assertEquals(expectedCreditLimit, testCard.getAvailableCredit(), 0);
        assertEquals(expectedMessage, testCard.getActionMsg());
    }

    @Test
    public void testWhenCreditLimitReachedThenStillChargeInterest() {
        testCard.setCharge(1000.0, "Rent");
        testCard.setInterestCharge(0.10);

        String incorrectErrorMessage = createChargeLogMessageTemplate(100.0, "Interest charged.") + " declined - over limit.";

        assertEquals(incorrectErrorMessage, testCard.getActionMsg());
    }

    private String createChargeLogMessageTemplate(double amount, String description) {
        return "Charge of " + currencyFormatter.format(amount) + " for " + description;
    }

    private String createPaymentLogMessageTemplate(double amount) {
        return "Payment of " + currencyFormatter.format(amount);
    }
}
