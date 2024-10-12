import net.yellowstrawberry.objectexpress.ObjectExpress;

public class Main {

    public static void main(String[] args) {
        ObjectExpress ox = new ObjectExpress("localhost","wow","root","root","");

        Main main = new Main();
        ox.registerTables(main);
        main.aa();
    }

    private WowTable table;

    public Main() {

    }

    public void aa() {
        System.out.println(table);
        System.out.println(table.findAll().get(0).getAbb());
    }
}
