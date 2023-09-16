import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Country {
    private final String name;
    private static File deathsFile, confirmedCasesFile;
    protected HashMap<LocalDate, ArrayList<Integer>> statistics;

    private static final Logger logger = Logger.getLogger(Country.class.getName());
    private static DateTimeFormatter[] dateFormatters = {
            DateTimeFormatter.ofPattern("M/d/yy"),
            DateTimeFormatter.ofPattern("MM/d/yy"),
            DateTimeFormatter.ofPattern("M/dd/yy"),
            DateTimeFormatter.ofPattern("MM/dd/yy")
    };

    protected Country(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    private HashMap<LocalDate,ArrayList<Integer>> getStatistics() {
        return statistics;
    }

    private record CountryColumns(int firstColumnIndex, int columnCount) {
    }

    private static CountryColumns getCountryColumns(String firstCSVRow, String searchedCountry) throws CountryNotFoundException {
        String[] columns = firstCSVRow.split(";");
        int firstColumnIndex = -1, columnCount = 0;

        for(int i = 0; i < columns.length; i++) {
            if(columns[i].equals(searchedCountry)) {
                if(firstColumnIndex == -1) {
                    firstColumnIndex = i;
                }
                columnCount++;
            }
        }


        if(firstColumnIndex == -1)
            throw new CountryNotFoundException(searchedCountry);

        return new CountryColumns(firstColumnIndex, columnCount);
    }

    public static void setFiles(String deathsFilePath, String confirmedCasesFilePath) throws FileNotFoundException {
        File deaths = new File(deathsFilePath);
        File confirmedCases = new File(confirmedCasesFilePath);

        if(!deaths.exists() || !confirmedCases.exists() || !deaths.canRead() || !confirmedCases.canRead()) {
            if(!deaths.exists() || !deaths.canRead()) {
                throw new FileNotFoundException(deathsFilePath);
            }
            else if(!confirmedCases.exists() || !confirmedCases.canRead()) {
                throw new FileNotFoundException(confirmedCasesFilePath);
            }
        }

        deathsFile = deaths;
        confirmedCasesFile = confirmedCases;
    }

    public void addDailyStatistic(LocalDate date, int diseaseCases, int deaths) {
        ArrayList<Integer> dailyStatistic = new ArrayList<>();
        dailyStatistic.add(diseaseCases);
        dailyStatistic.add(deaths);
        statistics.put(date, dailyStatistic);
    }


    public static Country fromCsv(String countryName) throws FileNotFoundException, CountryNotFoundException {
        Scanner deathsScanner = new Scanner(deathsFile);
        Scanner confirmedCasesScanner = new Scanner(confirmedCasesFile);
        String deathsFirstRow = deathsScanner.nextLine();
        String confirmedDeathsFirstRow = confirmedCasesScanner.nextLine();
        CountryColumns deathsColumns = getCountryColumns(deathsFirstRow, countryName);
        CountryColumns confirmedCasesColumns = getCountryColumns(confirmedDeathsFirstRow, countryName);
        Country country;
        if(deathsColumns.columnCount == 1) {
            country = new CountryWithoutProvinces(countryName);
            deathsScanner.nextLine();
            confirmedCasesScanner.nextLine();
            while (deathsScanner.hasNextLine()) {
                String deathsLine = deathsScanner.nextLine();
                String confirmedCasesLine = confirmedCasesScanner.nextLine();
                String[] deathsColumnsData = deathsLine.split(";");
                LocalDate date = null;
                for (DateTimeFormatter formatter : dateFormatters) {
                    try {
                        date = LocalDate.parse(deathsColumnsData[0], formatter);
                        break;
                    } catch (DateTimeParseException e) {
                        System.out.println("Failed to parse date for input: " + deathsColumnsData[0]);
                    }
                }
                if (date == null) {
                    logger.log(Level.SEVERE,"Failed to parse date for input: " + deathsColumnsData[0]);
                    System.out.println("Failed to parse date for input: " + deathsColumnsData[0]);
                }
                int deaths = Integer.parseInt(deathsColumnsData[deathsColumns.firstColumnIndex]);
                String[] confirmedCasesColumnsData = confirmedCasesLine.split(";");
                int confirmedCases = Integer.parseInt(confirmedCasesColumnsData[confirmedCasesColumns.firstColumnIndex]);
                country.addDailyStatistic(date, confirmedCases, deaths);
            }
        }
        else {
            String deathsLine = deathsScanner.nextLine();
            confirmedCasesScanner.nextLine();
            String[] columns = deathsLine.split(";");
            ArrayList<CountryWithoutProvinces> provinces = new ArrayList<>();
            for(int i = 0; i < deathsColumns.columnCount; i++) {
                provinces.add(new CountryWithoutProvinces(columns[i + deathsColumns.firstColumnIndex]));
            }
            country = new CountryWithProvinces(countryName, provinces);
            while (deathsScanner.hasNextLine()) {
                String currentDeathsLine = deathsScanner.nextLine();
                String confirmedCasesLine = confirmedCasesScanner.nextLine();

                String[] deathsColumnsData = currentDeathsLine.split(";");
                LocalDate date = null;
                for (DateTimeFormatter formatter : dateFormatters) {
                    try {
                        date = LocalDate.parse(deathsColumnsData[0], formatter);
                        break;
                    } catch (DateTimeParseException e) {
                        System.out.println("Failed to parse date for input: " + deathsColumnsData[0]);
                    }
                }
                if (date == null) {
                    logger.log(Level.SEVERE,"Failed to parse date for input: " + deathsColumnsData[0]);
                    System.out.println("Failed to parse date for input: " + deathsColumnsData[0]);
                }
                int[] deaths = new int[deathsColumns.columnCount];
                for (int i = 0; i < deathsColumns.columnCount; i++) {
                    deaths[i] = Integer.parseInt(deathsColumnsData[i + deathsColumns.firstColumnIndex]);
                }
                String[] confirmedCasesColumnsData = confirmedCasesLine.split(";");
                int[] confirmedCases = new int[confirmedCasesColumns.columnCount];
                for (int i = 0; i < confirmedCasesColumns.columnCount; i++) {
                    confirmedCases[i] = Integer.parseInt(confirmedCasesColumnsData[i + confirmedCasesColumns.firstColumnIndex]);
                }
                for(int i = 0; i < confirmedCasesColumns.columnCount; i++) {
                    int cases = confirmedCases[i];
                    int deathsCount = deaths[i];
                    ((CountryWithProvinces) country).provinces.get(i).addDailyStatistic(date, cases, deathsCount);
                }
            }
        }
        deathsScanner.close();
        confirmedCasesScanner.close();
        return country;
        }

        public static ArrayList<Country> fromCsv(ArrayList<Country> countriesNames) {
            ArrayList<Country> countries = new ArrayList<>();
            for (Country country : countriesNames) {
                try {
                    countries.add(fromCsv(country.getName()));
                } catch (FileNotFoundException | CountryNotFoundException e) {
                    System.out.println(e.getMessage());
                }
            }
            return countries;
        }

        public abstract int getConfirmedCases(LocalDate date);
        public abstract int getDeaths(LocalDate date);
        private static int calculateTotalDeathsInRange(Country country, LocalDate startDate, LocalDate endDate) {
            int totalDeaths = 0;
            for(LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
                totalDeaths += country.getDeaths(date);
            }
            return totalDeaths;
        }

        public static void sortByDeaths(ArrayList<Country> countries, LocalDate startDate, LocalDate endDate) {
            Collections.sort(countries, (country1, country2) -> {
                return Integer.compare(calculateTotalDeathsInRange(country2, startDate, endDate), calculateTotalDeathsInRange(country1, startDate, endDate));
            });
        }
        public static void saveToDataFile(String filePath) throws IOException {
            FileWriter fileWriter = new FileWriter(filePath);
            Scanner deathsScanner = new Scanner(deathsFile);
            deathsScanner.nextLine();
            deathsScanner.nextLine();
            while(deathsScanner.hasNextLine()) {
                String deathsLine = deathsScanner.nextLine();
                String[] deathsLineParts = deathsLine.split(";");
                LocalDate date = LocalDate.parse(deathsLineParts[0], DateTimeFormatter.ofPattern("M/d/yy"));
                int deaths = getAllDeathsByDate(date);
                int confirmedCases = getAllConfirmedCasesByDate(date);
                DateTimeFormatter fileDateFormatter = DateTimeFormatter.ofPattern("d.MM.yy");
                fileWriter.write(date.format(fileDateFormatter) + ";" + deaths + ";" + confirmedCases + "\n");
            }
        }

    private static int getAllDeathsByDate(LocalDate date) throws FileNotFoundException {
            Scanner deathsScanner = new Scanner(deathsFile);
            deathsScanner.nextLine();
            deathsScanner.nextLine();
            int deaths = 0;
            while(deathsScanner.hasNextLine()) {
                String deathsLine = deathsScanner.nextLine();
                String[] deathsLineParts = deathsLine.split(";");
                LocalDate currentDate = LocalDate.parse(deathsLineParts[0], DateTimeFormatter.ofPattern("M/d/yy"));
                if(currentDate.equals(date)) {
                    for(int i = 1; i < deathsLineParts.length; i++) {
                        deaths += Integer.parseInt(deathsLineParts[i]);
                    }
                }
            }
            return deaths;
    }

    public static int getAllConfirmedCasesByDate(LocalDate date) throws FileNotFoundException {
            Scanner confirmedCasesScanner = new Scanner(confirmedCasesFile);
            confirmedCasesScanner.nextLine();
            confirmedCasesScanner.nextLine();
            int confirmedCases = 0;
            while(confirmedCasesScanner.hasNextLine()) {
                String confirmedCasesLine = confirmedCasesScanner.nextLine();
                String[] confirmedCasesLineParts = confirmedCasesLine.split(";");
                LocalDate currentDate = LocalDate.parse(confirmedCasesLineParts[0], DateTimeFormatter.ofPattern("M/d/yy"));
                if(currentDate.equals(date)) {
                    for(int i = 1; i < confirmedCasesLineParts.length; i++) {
                        confirmedCases += Integer.parseInt(confirmedCasesLineParts[i]);
                    }
                }
            }
            return confirmedCases;
    }

//    public static Country fromCsv(String countryName) throws CountryNotFoundException {
//        Scanner deathsScanner, confirmedCasesScanner;
//        try {
//            deathsScanner = new Scanner(deathsFile);
//            confirmedCasesScanner = new Scanner(confirmedCasesFile);
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//        String confirmedCasesFirstLine = confirmedCasesScanner.nextLine();
//        CountryColumns confirmedCasesColumns = getCountryColumns(confirmedCasesFirstLine, countryName);
//        deathsScanner.nextLine();
//        deathsScanner.nextLine();
//
//        String confirmedCasesLine, deathsLine;
//        if(confirmedCasesColumns.columnCount == 1) {
//            confirmedCasesScanner.nextLine();
//            CountryWithoutProvinces result = new CountryWithoutProvinces(countryName);
//            while(confirmedCasesScanner.hasNextLine() && deathsScanner.hasNextLine()) {
//                confirmedCasesLine = confirmedCasesScanner.nextLine();
//                deathsLine = deathsScanner.nextLine();
//                String[] confirmedCasesLineParts = confirmedCasesLine.split(";");
//                String[] deathsLineParts = deathsLine.split(";");
//                LocalDate date = LocalDate.parse(confirmedCasesLineParts[0],DateTimeFormatter.ofPattern("M/d/yy"));
//                int confirmedCases = Integer.parseInt(confirmedCasesLineParts[confirmedCasesColumns.firstColumnIndex]);
//                int deaths = Integer.parseInt(deathsLineParts[confirmedCasesColumns.firstColumnIndex]);
//                result.addDailyStatistic(date,confirmedCases,deaths);
//            }
//            confirmedCasesScanner.close();
//            deathsScanner.close();
//            return result;
//        }
//        else {
//            ArrayList<CountryWithoutProvinces> provinces = new ArrayList<>();
//            confirmedCasesLine = confirmedCasesScanner.nextLine();
//            String[] confirmedCasesLineParts = confirmedCasesLine.split(";");
//            for(int i = 0; i < confirmedCasesColumns.firstColumnIndex; i++) {
//                provinces.add(new CountryWithoutProvinces(confirmedCasesLineParts[confirmedCasesColumns.firstColumnIndex + i]));
//            }
//            CountryWithProvinces result = new CountryWithProvinces(countryName,provinces);
//            while(confirmedCasesScanner.hasNextLine() && deathsScanner.hasNextLine()) {
//                confirmedCasesLine = confirmedCasesScanner.nextLine();
//                deathsLine = deathsScanner.nextLine();
//                confirmedCasesLineParts = confirmedCasesLine.split(";");
//                String[] deathsLineParts = deathsLine.split(";");
//                LocalDate date = LocalDate.parse(confirmedCasesLineParts[0],DateTimeFormatter.ofPattern("M/d/yy"));
//                for(int i = 0 ; i < confirmedCasesColumns.columnCount; i++) {
//                    int confirmedCases = Integer.parseInt(confirmedCasesLineParts[confirmedCasesColumns.firstColumnIndex + i]);
//                    int deaths = Integer.parseInt(deathsLineParts[confirmedCasesColumns.firstColumnIndex + i]);
//                    result.provinces.get(i).addDailyStatistic(date,confirmedCases,deaths);
//                }
//            }
//            confirmedCasesScanner.close();
//            deathsScanner.close();
//            return result;
//        }
//    }

}
