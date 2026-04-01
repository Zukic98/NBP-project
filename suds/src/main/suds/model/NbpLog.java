package ba.unsa.etf.suds.ba.unsa.etf.suds.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NbpLog {
    private Long id;
    private String actionName;
    private String tableName;
    private Timestamp dateTime;
    private String dbUser;
}