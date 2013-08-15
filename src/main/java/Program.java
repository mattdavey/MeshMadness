import java.io.IOException;

public class Program
{
    public static void main(String[] args) {
        // Create global deployment
        final SBP sbp1 = new SBP("SBP1");
        final SBP sbp2 = new SBP("SBP2");
        sbp1.join(sbp2);
        sbp2.join(sbp1);

        final Thread sbp1Thread = new Thread(sbp1);
        final Thread sbp2Thread = new Thread(sbp2);
        sbp1Thread.setDaemon(true);
        sbp2Thread.setDaemon(true);
        sbp1Thread.start();
        sbp2Thread.start();

        // Connect one salesperson per region
        final SalesPerson salesPerson1 = new SalesPerson("Sales1", sbp1);
        final SalesPerson salesPerson2 = new SalesPerson("Sales2", sbp2);
//        final Thread salesPerson1Thread = new Thread(salesPerson1);
//        final Thread salesPerson2Thread = new Thread(salesPerson2);
//        salesPerson1Thread.setDaemon(true);
//        salesPerson2Thread.setDaemon(true);
//        salesPerson1Thread.start();
//        salesPerson2Thread.start();
        sbp1.registerSalesPerson(salesPerson1);
        sbp2.registerSalesPerson(salesPerson2);

        // login user to a single region
        final User user1 = new User("User1", sbp1);
        user1.loginToSBP();
        final User user2 = new User("User2", sbp2);
        user2.loginToSBP();

        salesPerson1.dialog(RFQStateManager.RFQState.Putback);
        salesPerson2.dialog(RFQStateManager.RFQState.Quote);
        salesPerson1.dialog(RFQStateManager.RFQState.Quote);

        // Request price
        sbp1.clientIncomingCommunication(new RFQ(user1, 1, sbp1));
//        sbp2.clientIncomingCommunication(new RFQ(user2, 2, sbp2));

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}