import net.yellowstrawberry.objectexpress.param.entity.AutoGenerate;
import net.yellowstrawberry.objectexpress.param.entity.Entity;
import net.yellowstrawberry.objectexpress.param.entity.Id;

@Entity
public class Sexy {
    @Id
    @AutoGenerate
    private Integer id;
    private String abb;
    private String ccc;

    public Sexy() {}

    public Sexy(String abb, String ccc) {
        this.abb = abb;
        this.ccc = ccc;
    }

    public String getAbb() {
        return abb;
    }

    public String getCcc() {
        return ccc;
    }

    public Integer getId() {
        return id;
    }

    public void setAbb(String abb) {
        this.abb = abb;
    }
}
