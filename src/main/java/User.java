
public class User {
    private final String name;

    public User(final String name) {
        this.name = name;
    }

    public void price(final double price) {
        System.out.println(String.format("%d %s %s", System.nanoTime(), name, price));
        System.exit(0);
    }

    public String getName() {
        return name;
    }
}
