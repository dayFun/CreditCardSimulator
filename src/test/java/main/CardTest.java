package main;

import static org.junit.Assert.assertEquals;

import java.text.NumberFormat;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


public class CardTest {

    private static final double NEW_ACCOUNT_STARTING_BALANCE = 0;
    private static final double NEW_ACCOUNT_CREDIT_LIMIT = 1000;
    private NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();
    private NumberFormat percentFormatter = NumberFormat.getPercentInstance();

    private Card testCard;

    @Before
    public void setup() {
        testCard = new Card();
    }

    @Test
    public void testWhenNewAccountCreatedThenBalanceIsZero() {
        assertEquals(NEW_ACCOUNT_STARTING_BALANCE, testCard.getBalanceDue(), 0);
    }

    @Test
    public void testWhenNewAccountCreatedThenCreditLimitIs1000() {
        assertEquals(NEW_ACCOUNT_CREDIT_LIMIT, testCard.getCreditLimit(), 0);
    }

    @Test
    public void testCreateCardWithExistingAccountReopensAccount() {
        testCard.setCharge(100.0, "Electric Bill");
        double expectedBalance = testCard.getBalanceDue();
        double expectedCredit = testCard.getAvailCredit();
        int existingAccountNumber = testCard.getAccountNo();
        String expectedMessage = "Account " + existingAccountNumber + " re-opened.";

        Card newCard = new Card(existingAccountNumber);

        assertEquals(expectedMessage, newCard.getActionMsg());
        assertEquals(expectedBalance, newCard.getBalanceDue(), 0);
        assertEquals(expectedCredit, newCard.getAvailCredit(), 0);
    }

    @Test
    public void testValidChargeIncreasesBalanceAndDecreasesCreditLimit() {
        double chargeAmount = 50.00;
        String description = "Porn";
        testCard.setCharge(chargeAmount, description);

        double expectedBalance = chargeAmount - NEW_ACCOUNT_STARTING_BALANCE;
        double expectedCreditLimit = NEW_ACCOUNT_CREDIT_LIMIT - chargeAmount;
        String expectedMessage = createChargeLogMessageTemplate(chargeAmount, description) + " posted.";

        assertEquals(expectedBalance, testCard.getBalanceDue(), 0);
        assertEquals(expectedCreditLimit, testCard.getAvailCredit(), 0);
        assertEquals(expectedMessage, testCard.getActionMsg());
    }

    @Test
    public void testChargeWithNegativeAmountThenTransactionDeclined() {
        double expectedBalance = testCard.getBalanceDue();
        double chargeAmount = -50.00;
        String chargeDescription = "Booze";

        testCard.setCharge(chargeAmount, chargeDescription);

        String expectedErrorMessage = createChargeLogMessageTemplate(chargeAmount, chargeDescription) + " declined - illegal amount.";

        //TODO: Currency formatter removes negative symbol?

        assertEquals(expectedErrorMessage, testCard.getActionMsg());
        assertEquals(expectedBalance, testCard.getBalanceDue(), 0);
    }

    @Test
    public void testWhenChargeOverCreditLimitThenTransactionDeclined() {
        double expectedBalance = testCard.getBalanceDue();
        double chargeAmount = 10000;
        String chargeDescription = "Hooker";

        testCard.setCharge(chargeAmount, chargeDescription);

        String expectedErrorMessage = createChargeLogMessageTemplate(chargeAmount, chargeDescription) + " declined - over limit!";

        assertEquals(expectedErrorMessage, testCard.getActionMsg());
        assertEquals(expectedBalance, testCard.getBalanceDue(), 0);
    }

    @Test
    public void testMakePaymentWithNegativeAmountRejectsPayment() {
        double expectedBalance = testCard.getBalanceDue();
        double paymentAmount = -250.00;

        testCard.setPayment(paymentAmount);

        String expectedErrorMessage = createPaymentLogMessageTemplate(paymentAmount) + " declined - illegal amount.";

        assertEquals(expectedErrorMessage, testCard.getActionMsg());
        assertEquals(expectedBalance, testCard.getBalanceDue(), 0);
    }

    @Test
    public void testMakeValidPaymentDecreasesBalanceAndIncreasesCreditLimit() {
        double chargeAmount = 500.0;
        double paymentAmount = 250.00;
        double expectedBalance = chargeAmount - paymentAmount;
        String expectedErrorMessage = createPaymentLogMessageTemplate(paymentAmount) + " posted.";
        testCard.setCharge(chargeAmount, "Plane Ticket");

        testCard.setPayment(paymentAmount);

        assertEquals(expectedErrorMessage, testCard.getActionMsg());
        assertEquals(expectedBalance, testCard.getBalanceDue(), 0);
    }

    @Test
    public void testDoNotChargeNegativeInterest() {
        double illegalInterestRate = -75.0;
        double expectedBalance = testCard.getBalanceDue();
        double expectedCreditLimit = testCard.getAvailCredit();
        String expectedErrorMessage = "Interest rate of " + percentFormatter.format(illegalInterestRate) + " declined - illegal amount.";

        testCard.setInterestCharge(illegalInterestRate);

        assertEquals(expectedErrorMessage, testCard.getActionMsg());
        assertEquals(expectedBalance, testCard.getBalanceDue(), 0);
        assertEquals(expectedCreditLimit, testCard.getAvailCredit(), 0);
    }

    @Test
    @Ignore
    public void testWhenBalanceIs1000AndInterestRateIs10PercentThenInterestChargeIs100() {
        testCard.setCharge(1000.0, "Rent");
        testCard.setInterestCharge(10.0);

        String incorrectErrorMessage = createChargeLogMessageTemplate(100.0, "Interest charged.") + " declined - illegal amount.";

        assertEquals(incorrectErrorMessage, testCard.getActionMsg());
    }

    private String createChargeLogMessageTemplate(double amount, String description) {
        return "Charge of " + currencyFormatter.format(amount) + " for " + description;
    }

    private String createPaymentLogMessageTemplate(double amount) {
        return "Payment of " + currencyFormatter.format(amount);
    }
}
