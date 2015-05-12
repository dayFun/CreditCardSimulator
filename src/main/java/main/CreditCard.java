package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class CreditCard {
    //GENERAL To Do: 
    //TODO: Remove sysouts...
    //TODO: Remove unnecessary this's

    private double creditLimit;
    private double balance;
    private int accountNumber;

    private String actmsg; // TODO: Rename?
    private String errmsg; // TODO: Rename? Make two messages into 1?

    //TODO: Move formatters into utility class?
    private NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();
    private NumberFormat percentFormatter = NumberFormat.getPercentInstance();

    public CreditCard() {
        this.actmsg = "";
        this.errmsg = "";

        while (this.accountNumber == 0) {
            try {
                this.accountNumber = (int) (Math.random() * 1000000);
                BufferedReader in = new BufferedReader(new FileReader("CC" + this.accountNumber + ".txt"));
                in.close();
                this.accountNumber = 0;
            } catch (IOException e) {
                // 'good' result: account does not yot exist....
                this.creditLimit = 1000;
                writestatus();
                if (this.errmsg.isEmpty()) {
                    actmsg = "Account " + this.accountNumber + " opened.";
                    System.out.println(actmsg);
                    writelog(actmsg);
                }
                if (!this.errmsg.isEmpty()) {
                    this.creditLimit = 0;
                    this.accountNumber = -1;
                }
            } catch (Exception e) {
                errmsg = "Fatal error in constructor: " + e.getMessage();
                System.out.println(errmsg);
                this.accountNumber = -1;
            }
        }
    }

    public CreditCard(int accountNumber) {
        errmsg = "";
        actmsg = "";
        this.creditLimit = 0;
        this.balance = 0;
        this.accountNumber = accountNumber;

        try {
            BufferedReader in = new BufferedReader(new FileReader("accountLogs\\CC" + this.accountNumber + ".txt"));
            this.creditLimit = Double.parseDouble(in.readLine());
            this.balance = Double.parseDouble(in.readLine());
            in.close();
            actmsg = "Account " + accountNumber + " re-opened.";
            System.out.println(actmsg);
        } catch (Exception e) {
            errmsg = "Error re-opening account: " + e.getMessage();
            System.out.println(errmsg);
            this.accountNumber = -1;
        }
    }

    public int getAccountNumber() {
        return this.accountNumber;
    }

    public double getCreditLimit() {
        return this.creditLimit;
    }

    public double getBalance() {
        return this.balance;
    }

    public double getAvailableCredit() {
        return (this.creditLimit - this.balance);
    }

    public String getErrorMsg() {
        return this.errmsg;
    }

    public String getActionMsg() {
        return this.actmsg;
    }

    public void setCharge(double amount, String description) {
        errmsg = "";
        actmsg = "";

        if (this.accountNumber <= 0) {
            errmsg = "Charge attempt on non-active account.";
            return;
        }

        if (amount <= 0) {
            actmsg = "Charge of " + currencyFormatter.format(amount) + " for " + description + " declined - illegal amount.";
            System.out.println(actmsg);
            writelog(actmsg);
        } else if ((this.balance + amount) > this.creditLimit) {
            actmsg = "Charge of " + currencyFormatter.format(amount) + " for " + description + " declined - over limit!";
            System.out.println(actmsg);
            writelog(actmsg);
        } else {
            this.balance += amount;
            writestatus();
            if (this.errmsg.isEmpty()) {
                actmsg = "Charge of " + currencyFormatter.format(amount) + " for " + description + " posted.";
                writelog(actmsg);
            }
        }
    }

    public void makePayment(double amount) {
        errmsg = "";
        actmsg = "";

        if (this.accountNumber <= 0) {
            errmsg = "Charge attempt on non-active account.";
            return;
        }

        if (amount <= 0) {
            actmsg = "Payment of " + currencyFormatter.format(amount) + " declined - illegal amount.";
            writelog(actmsg);
        } else {
            this.balance -= amount;
            writestatus();
            if (this.errmsg.isEmpty()) {
                actmsg = "Payment of " + currencyFormatter.format(amount) + " posted.";
                writelog(actmsg);
            }
        }
    }

    //BUG: If the Balance is equal to the credit limit, then interest is not charged...
    // If only that was actually the case...
    public void setInterestCharge(double interestRate) {
        errmsg = "";
        actmsg = "";
        double totalInterestCharged;

        if (this.accountNumber <= 0) {
            errmsg = "Interest Charge attempt on non-active account.";
            return;
        }

        if (interestRate <= 0) {
            actmsg = "Interest rate of " + percentFormatter.format(interestRate) + " declined - illegal amount.";
            writelog(actmsg);
        } else {
            totalInterestCharged = this.balance * interestRate / 12.00;
            setCharge(totalInterestCharged, "Interest charged.");
            // The writestatus() and writelog() methods will be performed in the setCharge() method

        }
    }

    public ArrayList<String> getLog() {
        ArrayList<String> h = new ArrayList<String>();
        errmsg = "";
        actmsg = "";
        String t;

        if (this.accountNumber <= 0) {
            errmsg = "Charge attempt on non-active account.";
            return h;
        }

        try {
            BufferedReader in = new BufferedReader(new FileReader("CCL" + this.accountNumber + ".txt"));
            t = in.readLine();

            while (t != null) {
                h.add(t);
                t = in.readLine();
            }
            in.close();
            actmsg = "History returned for account: " + this.accountNumber;
        } catch (Exception e) {
            errmsg = "Error reading log file: " + e.getMessage();
        }
        return h;
    }

    private void writestatus() {
        try {
            File accountStatusLog = new File("accountLogs\\CC" + this.accountNumber + ".txt"); // TODO: Better name for file?
            PrintWriter out = new PrintWriter(new FileWriter(accountStatusLog));
            out.println(this.creditLimit);
            out.println(this.balance);
            out.close();
        } catch (IOException e) {
            errmsg = "Error writing status file for account: " + this.accountNumber;
            System.out.println(errmsg);
        } catch (Exception e) {
            errmsg = "General error in status update: " + e.getMessage();
            System.out.println(errmsg);
        }
    }

    private void writelog(String msg) {
        try {
            File accountLog = new File("accountLogs\\CCL" + this.accountNumber + ".txt"); // TODO: Better name for file?
            Calendar cal = Calendar.getInstance();
            DateFormat df = DateFormat.getDateTimeInstance();
            String ts = df.format(cal.getTime());
            PrintWriter out = new PrintWriter(new FileWriter(accountLog, true));
            out.println(msg + "\t" + ts);
            out.close();
        } catch (IOException e) {
            errmsg = "Error writing log file: " + e.getMessage();
        } catch (Exception e) {
            errmsg = "General error in write log: " + e.getMessage();
        }
    }
}
