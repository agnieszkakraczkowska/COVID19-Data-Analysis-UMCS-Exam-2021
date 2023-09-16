import java.time.LocalDate;
import java.util.HashMap;

public class CountryWithoutProvinces  extends Country {
    public CountryWithoutProvinces(String name) {
        super(name);
        statistics = new HashMap<LocalDate, java.util.ArrayList<Integer>>();
    }

    @Override
    public int getConfirmedCases(LocalDate date) {
        int result = 0;
        for(LocalDate key : statistics.keySet()) {
            if(key.equals(date)) {
                result = statistics.get(key).get(0);
            }
        }
        return result;
    }

    @Override
    public int getDeaths(LocalDate date) {
        return statistics.get(date).get(1);
    }
}
