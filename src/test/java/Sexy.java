import net.yellowstrawberry.objectexpress.param.entity.AutoGenerate;
import net.yellowstrawberry.objectexpress.param.entity.Entity;
import net.yellowstrawberry.objectexpress.param.entity.Id;

@Entity
public class Sexy {
    @Id
    @AutoGenerate
    private Integer id;
    private String abb;

    public Sexy() {}

    public Sexy(String abb) {
        this.abb = abb;
    }

    public String getAbb() {
        return abb;
    }

    public Integer getId() {
        return id;
    }
}
