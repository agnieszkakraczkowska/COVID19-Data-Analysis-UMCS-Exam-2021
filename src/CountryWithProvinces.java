import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CountryWithProvinces extends Country {
    protected ArrayList<CountryWithoutProvinces> provinces;

    public CountryWithProvinces(String name, ArrayList<CountryWithoutProvinces> provinces) {
        super(name);
        this.provinces = provinces;
    }

    @Override
    public int getConfirmedCases(LocalDate date) {
        int result = 0;
        for (CountryWithoutProvinces province : provinces) {
            result += province.getConfirmedCases(date);
        }
        return result;
    }

    @Override
    public int getDeaths(LocalDate date) {
        int result = 0;
        for (CountryWithoutProvinces province : provinces) {
            result += province.getDeaths(date);
        }
        return result;
    }
}
