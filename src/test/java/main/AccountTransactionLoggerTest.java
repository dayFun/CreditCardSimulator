package main;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class AccountTransactionLoggerTest {

    @Mock
    private CreditCard creditCard;

    private AccountTransactionLogger logger;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        logger = new AccountTransactionLogger();
    }
}
