
public class User {
    private final String name;
    private final SBP sbp;

    public User(final String name, SBP sbp) {
        this.name = name;
        this.sbp = sbp;
    }

    public void price(final double price) {
        System.out.println(String.format("%d %s %s", System.nanoTime(), name, price));
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
