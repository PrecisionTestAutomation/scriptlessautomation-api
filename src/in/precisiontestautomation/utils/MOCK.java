package in.precisiontestautomation.utils;

import com.github.javafaker.Faker;
import in.precisiontestautomation.scriptlessautomation.core.configurations.ConfigReader;
import in.precisiontestautomation.apifactory.MailingServices;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * <p>MOCK class.</p>
 *
 * @author PTA-dev
 * @version 1.2
 * @since 2024-05-02
 */
public class MOCK {

    /**
     * <p>getConfigValue.</p>
     *
     * @param fileName a {@link java.lang.String} object
     * @param key a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    public String getConfigValue(String fileName,String key){
        return ConfigReader.getConfigValue(fileName,key);
    }

    /**
     * <p>generateRandomEmailAddress.</p>
     */
    public void generateRandomEmailAddress(){
        ApiKeyInitializers.getMailingServices().set(MailingServices.getInstance().generateRandomEmailAddress());
    }

    //Todo - To remove String return type and add all the variables in global variables
    /**
     * <p>getRandomEmail.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public String getRandomEmail(){
        ApiKeyInitializers.getGlobalVariables().get().put("getRandomEmail", ApiKeyInitializers.getMailingServices().get().getEmailAddress());
        return ApiKeyInitializers.getMailingServices().get().getEmailAddress();
    }


    /**
     * <p>GENERATE_DATE.</p>
     *
     * @param params a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    public static String GENERATE_DATE(String params) {
        String[] dateArr = params.split("\\|");
        String dateGenerate;

            String dateFormat = dateArr[0];
            String year = dateArr[1];
            String month = dateArr[2];
            String strDate = dateArr[3];
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR, Integer.parseInt(year));
            cal.add(Calendar.MONTH, Integer.parseInt(month));
            cal.add(Calendar.DATE, Integer.parseInt(strDate));
            Date date = cal.getTime();
            dateGenerate = new SimpleDateFormat(dateFormat).format(date);
            ApiKeyInitializers.getGlobalVariables().get().put("GENERATE_DATE", dateGenerate);

        return dateGenerate;
    }

    /**
     * <p>main.</p>
     *
     * @param args an array of {@link java.lang.String} objects
     */
    public static void main(String[] args) {
        System.out.println(GENERATE_DATE("yyyy-MM-dd|-18|4|3"));
    }

    /**
     * <p>FIRST_NAME.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public final String FIRST_NAME(){
        String firstName = Faker.instance().name().firstName();
        ApiKeyInitializers.getGlobalVariables().get().put("FIRST_NAME",firstName);
        return firstName;
    }

    /**
     * <p>LAST_NAME.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public final String LAST_NAME(){
        String lastName = Faker.instance().name().lastName();
        ApiKeyInitializers.getGlobalVariables().get().put("LAST_NAME",lastName);
        return lastName;
    }

    /**
     * <p>PHONE_NUMBER.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public final String PHONE_NUMBER(){
        String phoneNumber = Faker.instance(Locale.GERMAN).phoneNumber().cellPhone();
        ApiKeyInitializers.getGlobalVariables().get().put("PHONE_NUMBER",phoneNumber);
        return phoneNumber;
    }

    /**
     * <p>ADDRESS.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public final String ADDRESS(){
        String address = Faker.instance(Locale.GERMAN).address().fullAddress();
        ApiKeyInitializers.getGlobalVariables().get().put("ADDRESS",address);
        return address;
    }
}
