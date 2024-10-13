import net.yellowstrawberry.objectexpress.ObjectExpress;

public class Main {

    public static void main(String[] args) {
        ObjectExpress ox = new ObjectExpress("localhost","wow","root","root","");

        Main main = new Main();
        ox.registerTables(main);
//        main.aa();
        main.bb();

        ox.shutdown();
    }

    private WowTable table;

    public Main() {

    }

    public void aa() {
        System.out.println(table);
        System.out.println(table.findAll().get(0).getAbb());
    }

    public void bb() {
        Sexy sexy = new Sexy("wow");
        table.save(sexy);
        System.out.println(sexy.getId());
    }
}
