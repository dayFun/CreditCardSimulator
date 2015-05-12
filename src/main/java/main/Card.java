package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.text.NumberFormat;

/**
 *
 * @author
 */
public class Card {
    private double CLimit, BalDue;
    private int AcctNo;
    private String actmsg, errmsg;
    NumberFormat c = NumberFormat.getCurrencyInstance();
    NumberFormat p = NumberFormat.getPercentInstance();
    
    public Card() {
        //constructor for a 'create new' operation
        this.AcctNo = 0;
        this.CLimit = 0;
        this.BalDue = 0;
        this.actmsg = "";
        this.errmsg = "";
        
        while (this.AcctNo == 0) {
            try {
                this.AcctNo = (int) (Math.random() * 1000000);
                BufferedReader in = new BufferedReader(
                                     new FileReader("CC" + this.AcctNo + ".txt"));
                in.close();
                this.AcctNo = 0;
            } catch (IOException e) {
                //'good' result: account does not yot exist....
                this.CLimit = 1000;
                writestatus();
                if (this.errmsg.isEmpty()) {
                   actmsg = "Account " + this.AcctNo + " opened.";
                   writelog(actmsg);
                }   
                if (!this.errmsg.isEmpty()) {
                    this.CLimit = 0;
                    this.AcctNo = -1;
                }
            } catch (Exception e) {
                errmsg = "Fatal error in constructor: " + e.getMessage();
                this.AcctNo = -1;
            }
        }
    } //end of constructor
    
    public Card(int a) {
        errmsg = "";
        actmsg = "";
        this.CLimit = 0;
        this.BalDue = 0;
        this.AcctNo = a;
        
        try {
            BufferedReader in = new BufferedReader(
                                new FileReader("CC" + this.AcctNo + ".txt"));
            this.CLimit = Double.parseDouble(in.readLine());
            this.BalDue = Double.parseDouble(in.readLine());
            in.close();
            actmsg = "Account " + a + " re-opened.";
        } catch (Exception e) {
            errmsg = "Error re-opening account: " + e.getMessage();
            this.AcctNo = -1;
        }
    }
    
    private void writestatus() {
        try {
            PrintWriter out = new PrintWriter(
                              new FileWriter("CC" + this.AcctNo + ".txt"));
            out.println(this.CLimit);
            out.println(this.BalDue);
            out.close();
        } catch (IOException e) {
            errmsg = "Error writing status file for account: " + this.AcctNo;
        } catch(Exception e) {
            errmsg = "General error in status update: " + e.getMessage();
        }
    } //end of writestatus
    
    private void writelog(String msg) {
        try {
            Calendar cal = Calendar.getInstance();
            DateFormat df = DateFormat.getDateTimeInstance();
            String ts = df.format(cal.getTime());
            PrintWriter out = new PrintWriter(
                              new FileWriter("CCL" + this.AcctNo + ".txt",true));
            out.println(msg + "\t" + ts);
            out.close();
        } catch (IOException e) {
            errmsg = "Error writing log file: " + e.getMessage();
        } catch (Exception e) {
            errmsg = "General error in write log: " + e.getMessage();
        }
    } //end of writelog
    
    public int getAccountNo() {
        return this.AcctNo;
    }
    
    public double getCreditLimit() {
        return this.CLimit;
    }
    
    public double getBalanceDue() {
        return this.BalDue;
    }
    
    public double getAvailCredit() {
        return (this.CLimit - this.BalDue);
    }
    
    public String getErrorMsg() {
        return this.errmsg;
    }
    
    public String getActionMsg() {
        return this.actmsg;
    }
    
    public void setCharge(double amt, String desc) {
        errmsg = "";
        actmsg = "";
        
        if (this.AcctNo <= 0) {
            errmsg = "Charge attempt on non-active account.";
            return;
        }
        
        if (amt <= 0) {
           actmsg = "Charge of " + c.format(amt) + " for " + desc +
                    " declined - illegal amount.";
           writelog(actmsg);
        } else if((this.BalDue + amt) > this.CLimit) {
           actmsg = "Charge of " + c.format(amt) + " for " + desc +
                    " declined - over limit!"; 
           writelog(actmsg);
        } else {
           this.BalDue += amt;
           writestatus();
           if (this.errmsg.isEmpty()) {
               actmsg = "Charge of " + c.format(amt) + " for " + desc +
                        " posted.";
               writelog(actmsg);
           }
        }
    } //end of setcharge
    
    public void setPayment(double amt) {
        errmsg = "";
        actmsg = "";
        
        if (this.AcctNo <= 0) {
            errmsg = "Charge attempt on non-active account.";
            return;
        }
        
        if (amt <= 0) {
            actmsg = "Payment of " + c.format(amt) + " declined - illegal amount.";
            writelog(actmsg); 
        } else {
            this.BalDue -= amt;
            writestatus();
            if (this.errmsg.isEmpty()) {
                actmsg = "Payment of " + c.format(amt) + " posted.";
                writelog(actmsg);
            }
        }
   } //end of payment
    
   public ArrayList<String> getLog() {
       ArrayList<String> h = new ArrayList<String>();
       errmsg = "";
       actmsg = "";
       String t;
       
       if (this.AcctNo <= 0) {
            errmsg = "Charge attempt on non-active account.";
            return h;
        }
       
       try {
           BufferedReader in = new BufferedReader(
                               new FileReader("CCL" + this.AcctNo + ".txt"));
           t = in.readLine();
           
           while (t != null) {
              h.add(t);
              t = in.readLine();
           }
           in.close();
           actmsg = "History returned for account: " + this.AcctNo;
       } catch (Exception e) {
           errmsg = "Error reading log file: " + e.getMessage();
       }
       return h;
   }
   
   public void setInterestCharge(double ir) {
       errmsg = "";
       actmsg = "";
       double intchg;
       
       if (this.AcctNo <= 0) {
           errmsg = "Interest Charge attempt on non-active account.";
           return;
       }
       
       if (ir <= 0) {
           actmsg = "Interest rate of " + p.format(ir) + " declined - illegal amount.";
           writelog(actmsg); 
       } else {
           intchg = this.BalDue * ir/12.00;
           setCharge(intchg,"Interest charged.");
           //The writestatus() and writelog() methods will be performed in the setCharge() method
           
       } //end of interest charge method
   }
}