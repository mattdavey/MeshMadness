package meshmadness.actors;

import meshmadness.domain.SBP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class User {
    final Logger logger = LoggerFactory.getLogger(SalesPerson.class);

    private final String name;
    private final SBP sbp;

    public User(final String name, final SBP sbp) {
        this.name = name;
        this.sbp = sbp;
    }

    public void price(final double price) {
        logger.debug(String.format("%d %s %s", System.nanoTime(), name, price));
    }

    public String getName() {
        return name;
    }

    public void loginToSBP() {
        sbp.join(sbp);
    }

    public SBP getSBP() {
        return sbp;
    }
}
