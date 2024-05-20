package in.precisiontestautomation.utils;

import com.github.javafaker.Faker;

/**
 * <p>Generate_Fake_Demog class.</p>
 *
 * @author PTA-dev
 * @version 1.2
 * @since 2024-05-02
 */
public class Generate_Fake_Demog {

    Faker faker;
    private Generate_Fake_Demog(){
        faker = new Faker();
    }

    /**
     * <p>getInstance.</p>
     *
     * @return a {@link Generate_Fake_Demog} object
     */
    public static Generate_Fake_Demog getInstance(){
        return new Generate_Fake_Demog();
    }

    /**
     * <p>getFirstName.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public String getFirstName(){
       return  faker.name().firstName();
    }

    /**
     * <p>getLastName.</p>
     *
     * @return a {@link java.lang.String} object
     */
    public String getLastName(){
        return  faker.name().lastName();
    }
}
