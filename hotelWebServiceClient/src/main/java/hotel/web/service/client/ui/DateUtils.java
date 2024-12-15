package hotel.web.service.client.ui;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;

public class DateUtils {

    public static XMLGregorianCalendar convertToXMLGregorianCalendar(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }

        // Convertir ZonedDateTime en GregorianCalendar
        GregorianCalendar calendar = GregorianCalendar.from(zonedDateTime);

        // Convertir en XMLGregorianCalendar
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
        } catch (Exception e) {
            e.printStackTrace();
            return null; // En cas d'erreur, retourner null
        }
    }
}
