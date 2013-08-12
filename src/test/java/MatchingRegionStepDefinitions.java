import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;
import cucumber.table.DataTable;

import java.util.HashMap;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public class MatchingRegionStepDefinitions {
    class CountRegionRow {
        private int count;
        private String region;
        private String state;
    }

    class RoleRegionRow {
        private String role;
        private String region;
        private String dialog;
    }

    class Holder<T> {
        public Holder(T item, Thread thread) {
            this.item = item;
            this.thread = thread;
        }
        public T item;
        public Thread thread;
    }

    private final HashMap<String, Holder<SBP>> sbps = new HashMap<String, Holder<SBP>>();
    private final HashMap<String, Holder<SalesPerson>> sales = new HashMap<String, Holder<SalesPerson>>();
    private final HashMap<String, User> users = new HashMap<String, User>();

    public MatchingRegionStepDefinitions() {
    }

    @Given("^the following users are logged in$")
    public void the_following_users_are_logged_in(DataTable roles) throws Throwable {
        final List<RoleRegionRow> rows = roles.asList(RoleRegionRow.class);
        for (RoleRegionRow row : rows) {
            final String sbp = row.region;

            // Check for SBP
            if (!sbps.containsKey(sbp)) {
                final SBP newSbp = new SBP(sbp);
                final Thread sbp1Thread = new Thread(newSbp);
                sbps.put(sbp, new Holder<SBP>(newSbp, sbp1Thread));
                sbp1Thread.setDaemon(true);
                sbp1Thread.start();
            }

            // Add User
            if (row.role.startsWith("User")) {
                final User user = new User(row.role);
                users.put(row.role, user);
                sbps.get(row.region).item.login(user);
            } else {
                final SalesPerson salesPerson1 = new SalesPerson(row.role, sbps.get(row.region).item, row.dialog);
                final Thread salesPerson1Thread = new Thread(salesPerson1);
                sales.put(row.role, new Holder<SalesPerson>(salesPerson1, salesPerson1Thread));
                salesPerson1Thread.setDaemon(true);
                salesPerson1Thread.start();
                sbps.get(row.region).item.registerSalesPerson(salesPerson1);
            }
        }

        for (Holder<SBP> sbp : sbps.values()) {
            for (Holder<SBP> sbpAgain : sbps.values()) {
                if (sbp.item != sbpAgain.item) {
                    sbp.item.join(sbpAgain.item);
                }
            }
        }
    }

    @When("^users submit messages as follows$")
    public void users_submit_messages_as_follows(DataTable messages) throws Throwable {
        final List<RoleRegionRow> rows = messages.asList(RoleRegionRow.class);
        int rfqId = 0;
        for (RoleRegionRow row : rows) {
            final SBP sbp = sbps.get(row.region).item;
            sbp.clientIncomingCommunication(new RFQ(users.get(row.role), ++rfqId, sbp));
        }
    }

    @Then("^the FSM looks like:$")
    public void the_FSM_looks_like(DataTable sbpStates) throws Throwable {
        final List<CountRegionRow> rows = sbpStates.asList(CountRegionRow.class);
        for (CountRegionRow row : rows) {
            final Holder<SBP> sbp = sbps.get(row.region);
            assertEquals(row.count, sbp.item.getWorkingRFQCount());
            assertEquals(row.state, sbp.item.getRFQState(row.count));
        }
    }
}
