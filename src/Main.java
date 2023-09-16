import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            Country.setFiles("data\\deaths.csv","data\\confirmed_cases.csv");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
//        CountryWithoutProvinces belgium;
//        try {
//            belgium = (CountryWithoutProvinces) Country.fromCsv("Belgium");
//        } catch (CountryNotFoundException | FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//        CountryWithoutProvinces albania;
//        try {
//            albania = (CountryWithoutProvinces) Country.fromCsv("Albania");
//        } catch (FileNotFoundException | CountryNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//        CountryWithProvinces australia;
//        try {
//            australia = (CountryWithProvinces) Country.fromCsv("Australia");
//        } catch (FileNotFoundException | CountryNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//        ArrayList<Country> countries = new ArrayList<>();
//        countries.add(belgium);
//        countries.add(albania);
//        countries.add(australia);
//        for (var country : countries) {
//            System.out.println(country.getName());
//        }
//        Country.sortByDeaths(countries,LocalDate.of(2020,2,1),LocalDate.of(2020,12,31));
//        System.out.println("Sorted: ");
//        for (var country : countries) {
//            System.out.println(country.getName());
//        }
        try {
            Country.saveToDataFile("results\\data.csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}