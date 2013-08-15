import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;
import cucumber.table.DataTable;
import meshmadness.domain.RFQ;
import meshmadness.domain.RFQStateManager;
import meshmadness.domain.SBP;
import meshmadness.actors.SalesPerson;
import meshmadness.actors.User;
import rx.Subscription;
import rx.util.functions.Action1;
import rx.util.functions.Func1;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;

public class MatchingRegionStepDefinitions {
    private class CountRegionStateRow {
        private int count;
        private String region;
        private String state;
        private String filler;
    }

    private class RoleRegionRow {
        private String role;
        private String region;
    }

    private class UserMessageRow {
        private String role;
        private String message;
    }

    private class Holder<T> {
        public Holder(final T item, final Thread thread) {
            this.item = item;
            this.thread = thread;
        }
        final private T item;
        final private Thread thread;
    }

    private final HashMap<String, Holder<SBP>> sbps = new HashMap<String, Holder<SBP>>();
    private final HashMap<String, Holder<SalesPerson>> sales = new HashMap<String, Holder<SalesPerson>>();
    private final HashMap<String, User> users = new HashMap<String, User>();

    @Given("^the following users are logged in$")
    public void the_following_users_are_logged_in(DataTable roles) throws Throwable {
        final List<RoleRegionRow> rows = roles.asList(RoleRegionRow.class);
        for (final RoleRegionRow row : rows) {
            final String sbp = row.region;

            // Check for SBP
            if (!sbps.containsKey(sbp)) {
                final SBP newSbp = new SBP(sbp);
                final Thread sbp1Thread = new Thread(newSbp);
                sbps.put(sbp, new Holder<>(newSbp, sbp1Thread));
                sbp1Thread.setDaemon(true);
                sbp1Thread.start();
            }

            // Add actors.User
            if (row.role.startsWith("User")) {
                final User user = new User(row.role, sbps.get(sbp).item);
                users.put(row.role, user);
                sbps.get(row.region).item.login(user);
            } else {
                final SalesPerson salesPerson1 = new SalesPerson(row.role, sbps.get(row.region).item);
                sales.put(row.role, new Holder<>(salesPerson1, null));
                sbps.get(row.region).item.registerSalesPerson(salesPerson1);
            }
        }

        for (final Holder<SBP> sbp : sbps.values()) {
            for (final Holder<SBP> sbpAgain : sbps.values()) {
                if (sbp.item != sbpAgain.item) {
                    sbp.item.join(sbpAgain.item);
                }
            }
        }
    }

    @When("^users submit messages as follows$")
    public void users_submit_messages_as_follows(DataTable messages) throws Throwable {
        final List<UserMessageRow> rows = messages.asList(UserMessageRow.class);
        int rfqId = 0;
        for (final UserMessageRow row : rows) {
            final RFQStateManager.RFQState message = RFQStateManager.RFQState.valueOf(row.message);
            switch (message) {
                case StartRFQ:
                    final User user = users.get(row.role);
                    final SBP sbp = user.getSBP();
                    sbp.clientIncomingCommunication(new RFQ(user, ++rfqId, sbp));
                    break;
                case Quote:
                    final SalesPerson salesperson = sales.get(row.role).item;
                    salesperson.dialog(message);
                    break;
                case Putback:
                    break;
            }
        }
    }

    @Then("^the FSM looks like:$")
    public void the_FSM_looks_like(DataTable sbpStates) throws Throwable {
        final List<CountRegionStateRow> rows = sbpStates.asList(CountRegionStateRow.class);

        final CountDownLatch[] latches = new CountDownLatch[rows.size()];
        final Subscription[] subscriptions = new Subscription[rows.size()];
        for (int i=0; i < rows.size(); i++) {
            latches[i] = new CountDownLatch(1);
        }

        int rowCount=0;
        for (final CountRegionStateRow row : rows) {
            final CountDownLatch latch = latches[rowCount++];

            final Holder<SBP> sbp = sbps.get(row.region);
            subscriptions[rowCount-1] = sbp.item.subscribe().filter(new Func1<SBP.RFQSubjectHolder, Boolean>() {
                @Override
                public Boolean call(final SBP.RFQSubjectHolder holder) {
                    boolean retVal = false;
                    if (holder.state == RFQStateManager.RFQState.valueOf(row.state))
                        retVal = true;

                    if (row.filler != null && holder.fillerName != row.filler)
                        retVal = false;


                    return retVal;
                }
            }).subscribe(new Action1<SBP.RFQSubjectHolder>() {
                @Override
                public void call(SBP.RFQSubjectHolder rfqSubjectHolder) {
                    latch.countDown();
                }
            });

            assertEquals(row.count, sbp.item.getWorkingRFQCount());
        }

        // wait for the above to finish or blow up if it's blocked
        for (int i=0; i < rows.size(); i++) {
            latches[i].await(5, TimeUnit.SECONDS);
            assertEquals(0, latches[i].getCount());
        }
    }
}
