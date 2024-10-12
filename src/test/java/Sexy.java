import net.yellowstrawberry.objectexpress.param.entity.Entity;
import net.yellowstrawberry.objectexpress.param.entity.Id;

@Entity
public class Sexy {
    @Id
    private Integer id;
    private String abb;

    public String getAbb() {
        return abb;
    }
}
