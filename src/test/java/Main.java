import net.yellowstrawberry.objectexpress.ObjectExpress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        ObjectExpress ox = new ObjectExpress("localhost","wow","root","root","");

        Main main = new Main();
        ox.registerTables(main);
//        main.aa();
        main.bb();
        main.cc();

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
        Sexy sexy = new Sexy("wow", "asdf");
        table.save(sexy);
        System.out.println(sexy.getId());
    }

    public void cc() {
        Sexy s= table.findById(10).get();
        s.setAbb("afadsfdsafsadfsdafasfdsfas");
        table.save(s);

        System.out.println(table.findById(10).get().getAbb());
    }
}
